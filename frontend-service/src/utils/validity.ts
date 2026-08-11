// Client-side evaluator for the `Validity` rule engine. The server stores
// these rules but never evaluates them — every "available now" indicator in
// the UI depends on this file being correct.
//
// A menu's validity is a list of rules, not a single rule with an AND
// combinator — all rules in the list must hold, matching the infrastructure
// layer's wire format exactly.

import type { Validity } from '@/types'

const WEEKDAY_LABELS = ['', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']

function minutesOfDay(time: string): number {
  const [h, m] = time.split(':')
  return Number(h) * 60 + Number(m ?? 0)
}

function isoWeekday(date: Date): number {
  const day = date.getDay()
  return day === 0 ? 7 : day
}

function isRuleValid(rule: Validity, now: Date): boolean {
  switch (rule.type) {
    case 'always':
      return true

    case 'weekly':
      return rule.days.includes(isoWeekday(now))

    case 'hours': {
      const t = now.getHours() * 60 + now.getMinutes()
      const start = minutesOfDay(rule.start)
      const end = minutesOfDay(rule.end)
      return start <= end ? t >= start && t <= end : t >= start || t <= end
    }

    case 'yearly': {
      const month = now.getMonth() + 1
      const day = now.getDate()
      const from = rule.startMonth * 100 + rule.startDay
      const to = rule.endMonth * 100 + rule.endDay
      const current = month * 100 + day
      return from <= to ? current >= from && current <= to : current >= from || current <= to
    }

    case 'period':
      return now >= new Date(rule.start) && now <= new Date(rule.end)

    case 'from':
      return now >= new Date(rule.start)

    case 'until':
      return now <= new Date(rule.end)

    default:
      return true
  }
}

function describeRule(rule: Validity): string {
  switch (rule.type) {
    case 'always':
      return 'Always'

    case 'weekly':
      return rule.days.map((d) => WEEKDAY_LABELS[d]).join(', ')

    case 'hours':
      return `${rule.start}–${rule.end}`

    case 'yearly':
      return `${rule.startDay}/${rule.startMonth} – ${rule.endDay}/${rule.endMonth}`

    case 'period':
      return `${rule.start} – ${rule.end}`

    case 'from':
      return `From ${rule.start}`

    case 'until':
      return `Until ${rule.end}`

    default:
      return 'Always'
  }
}

export function isValid(rules: Validity[] | null | undefined, now: Date = new Date()): boolean {
  if (!rules || rules.length === 0) return true
  return rules.every((rule) => isRuleValid(rule, now))
}

export function describe(rules: Validity[] | null | undefined): string {
  if (!rules || rules.length === 0) return 'Always'
  return rules.map(describeRule).join(' and ')
}
