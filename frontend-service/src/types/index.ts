// Domain types mirroring the backend DTOs described in ui_specification.md.
// Money is always a decimal string, never a float. Timestamps are epoch-ms strings.

export type Role = 'CUSTOMER' | 'MANAGER'

export interface User {
  id: string
  username: string
  email: string
  role: Role
}

export interface Restaurant {
  id: string
  name: string
  address: string
  phone: string
  email: string
}

export interface VariationOption {
  name: string
  additionalPrice: string
}

export interface Variation {
  name: string
  options: VariationOption[]
}

export interface MenuItem {
  id: string
  name: string
  description: string
  price: string
  variations: Variation[]
}

export interface Category {
  id: string
  name: string
  items: MenuItem[]
  variations: Variation[]
}

// Validity — the availability rule engine. The server stores it but never
// evaluates it; utils/validity.ts is the only place "available now" is computed.
export interface AlwaysValidity {
  type: 'always'
}
export interface PeriodValidity {
  type: 'period'
  start: string
  end: string
}
export interface YearlyValidity {
  type: 'yearly'
  startMonth: number
  startDay: number
  endMonth: number
  endDay: number
}
export interface WeeklyValidity {
  type: 'weekly'
  days: number[] // ISO 1 (Mon) .. 7 (Sun)
}
export interface HoursValidity {
  type: 'hours'
  start: string // HH:MM[:SS]
  end: string
}
export interface FromValidity {
  type: 'from'
  start: string
}
export interface UntilValidity {
  type: 'until'
  end: string
}

// No AND variant here: the wire format has no per-rule AND combinator at all — "AND" is expressed
// by list cardinality (a Menu's validity is Validity[], every entry must hold), matching
// restaurant-shared's ValidityDomainMapper.kt exactly. See utils/validity.ts.
export type Validity =
  | AlwaysValidity
  | PeriodValidity
  | YearlyValidity
  | WeeklyValidity
  | HoursValidity
  | FromValidity
  | UntilValidity

export type ValidityType = Validity['type']

export interface Menu {
  id: string
  name: string
  categories: Category[]
  validity: Validity[]
}

export interface MenuSummary {
  id: string
  name: string
}

export type OrderType = 'DELIVERY' | 'TAKEAWAY' | 'DINE_IN'

export type OrderStatus = 'PENDING' | 'PREPARING' | 'READY' | 'ON_THE_WAY' | 'COMPLETED' | 'CANCELLED'

export interface OrderLineItem {
  menuItemId: string
  quantity: number
}

export interface Order {
  orderId: string
  restaurantId: string
  customerId: string
  status: OrderStatus
  items: OrderLineItem[]
  orderType: OrderType

  estimatedDeliveryTime?: string
  deliveryAddress?: string
  bellName?: string
  customerPhone?: string

  pickupTime?: string
  customerName?: string

  tableNumber?: number
  numberOfGuests?: number
}

// API envelope — success and error share the same shape, branch on `code`.
export interface ApiEnvelope<T> {
  result: T
  code: number
}

export type LoadStatus = 'ready' | 'loading' | 'empty' | 'error'
