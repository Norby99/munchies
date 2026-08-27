// Menu / category / item CRUD. Routes are singular (`/restaurant/...`),
// unlike the restaurant family above — a real inconsistency in the API, kept
// as-is rather than "fixed" client-side (see ui_specification.md §7.1).
//
// There is no GET for a single category or item — both only come back as
// part of the full menu fetch (getMenu).

import { http, unwrap } from '@/api/client'
import { decodeValidity, encodeValidity } from '@/utils/validity'
import type { Category, Menu, MenuSummary, MenuItem, Validity, Variation } from '@/types'
import type { MenuDto } from 'munchies-wire-types/kotlin/restaurant-wire-types'

const base = (restaurantId: string) => `/restaurant/${restaurantId}/menus`

// MenuDto.validity is ValidityDto[] on the wire; every response carrying a full menu goes through
// this before reaching a caller, so callers only ever see the UI-friendly Validity[] shape.
function toMenu(dto: MenuDto): Menu {
  return { ...dto, validity: decodeValidity(dto.validity) }
}

export async function listMenus(restaurantId: string): Promise<MenuSummary[]> {
  return unwrap(http.get(base(restaurantId)))
}

export async function getMenu(restaurantId: string, menuId: string): Promise<Menu> {
  return toMenu(await unwrap<MenuDto>(http.get(`${base(restaurantId)}/${menuId}`)))
}

export async function createMenu(restaurantId: string, name: string): Promise<Menu> {
  return toMenu(await unwrap<MenuDto>(http.post(base(restaurantId), { name })))
}

export async function updateMenu(
  restaurantId: string,
  menuId: string,
  name: string,
  validity: Validity[],
): Promise<Menu> {
  const body = { name, validity: encodeValidity(validity) }
  return toMenu(await unwrap<MenuDto>(http.put(`${base(restaurantId)}/${menuId}`, body)))
}

export async function deleteMenu(restaurantId: string, menuId: string): Promise<string> {
  return unwrap(http.delete(`${base(restaurantId)}/${menuId}`))
}

export async function createCategory(
  restaurantId: string,
  menuId: string,
  name: string,
  variations: Variation[] = [],
): Promise<Category> {
  return unwrap(http.post(`${base(restaurantId)}/${menuId}/categories`, { name, variations }))
}

export async function updateCategory(
  restaurantId: string,
  menuId: string,
  categoryId: string,
  name: string,
  variations: Variation[] = [],
): Promise<Category> {
  return unwrap(
    http.put(`${base(restaurantId)}/${menuId}/categories/${categoryId}`, { name, variations }),
  )
}

export async function deleteCategory(
  restaurantId: string,
  menuId: string,
  categoryId: string,
): Promise<string> {
  return unwrap(http.delete(`${base(restaurantId)}/${menuId}/categories/${categoryId}`))
}

export interface MenuItemInput {
  name: string
  description: string
  price: string
  variations: Variation[]
}

export async function createMenuItem(
  restaurantId: string,
  menuId: string,
  categoryId: string,
  input: MenuItemInput,
): Promise<string> {
  return unwrap(
    http.post(`${base(restaurantId)}/${menuId}/categories/${categoryId}/items`, input),
  )
}

// PUT replaces the whole item — the caller must submit every field,
// including untouched variations.
export async function updateMenuItem(
  restaurantId: string,
  menuId: string,
  categoryId: string,
  itemId: string,
  input: MenuItemInput,
): Promise<MenuItem> {
  return unwrap(
    http.put(`${base(restaurantId)}/${menuId}/categories/${categoryId}/items/${itemId}`, input),
  )
}

export async function removeMenuItem(
  restaurantId: string,
  menuId: string,
  categoryId: string,
  itemId: string,
): Promise<string> {
  return unwrap(
    http.delete(`${base(restaurantId)}/${menuId}/categories/${categoryId}/items/${itemId}`),
  )
}
