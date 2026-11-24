// Transport of the live-notification channel: one Socket.io (WebSocket) connection to the gateway,
// on the same origin as the REST API (nginx and the Vite dev server proxy /notifications). The
// gateway authenticates it with the session cookie, the browser sends it on the upgrade request,
// and proxies it to notification-service. It is read-only: the server pushes, the client listens;
// anything the user does in response goes through the REST API.
//
// Path and event name mirror LiveNotificationsConfig in notification-shared (the frontend is not
// built against the -shared packages).

import { io, type Socket } from 'socket.io-client'

import type { LiveNotification } from '@/types'

const LIVE_PATH = '/notifications'
const LIVE_EVENT = 'notification'

export type LiveStatus = 'connecting' | 'live' | 'offline'

export interface LiveHandlers {
  onNotification: (notification: LiveNotification) => void
  onStatus: (status: LiveStatus) => void
  /** Called on every successful connection after the first: notifications may have been missed. */
  onReconnect: () => void
}

export interface LiveConnection {
  close: () => void
}

export function openLiveConnection(handlers: LiveHandlers): LiveConnection {
  const socket: Socket = io({
    path: LIVE_PATH,
    // WebSocket only: the gateway proxies the upgrade, there is no long-polling fallback.
    transports: ['websocket'],
    withCredentials: true,
    // Socket.io's built-in reconnection, with exponential backoff and jitter: 1 s doubling up to
    // 30 s, so a gateway restart or a dropped network is recovered without hammering the server.
    reconnection: true,
    reconnectionDelay: 1_000,
    reconnectionDelayMax: 30_000,
    randomizationFactor: 0.5,
  })

  let connectedBefore = false
  handlers.onStatus('connecting')

  socket.on('connect', () => {
    handlers.onStatus('live')
    if (connectedBefore) handlers.onReconnect()
    connectedBefore = true
  })
  socket.on('disconnect', () => handlers.onStatus('offline'))
  socket.on('connect_error', () => handlers.onStatus('offline'))
  socket.on(LIVE_EVENT, (notification: LiveNotification) => handlers.onNotification(notification))

  return {
    close: () => {
      socket.removeAllListeners()
      socket.disconnect()
    },
  }
}
