import { createContext, useContext, useMemo, useState, type ReactNode } from 'react'
import type { MenuItemInfo } from './api/types'

export interface CartLine {
  menuItem: MenuItemInfo
  quantity: number
}

interface CartState {
  restaurantId: number | null
  lines: CartLine[]
  addItem: (item: MenuItemInfo) => void
  removeItem: (menuItemId: number) => void
  clear: () => void
  total: number
}

const CartContext = createContext<CartState | null>(null)

export function CartProvider({ children }: { children: ReactNode }) {
  const [restaurantId, setRestaurantId] = useState<number | null>(null)
  const [lines, setLines] = useState<CartLine[]>([])

  function addItem(item: MenuItemInfo) {
    setLines((prev) => {
      // Starting a cart from a different restaurant replaces it -- like the real thing,
      // this app only carries one restaurant's order at a time.
      if (restaurantId !== null && restaurantId !== item.restaurantId) {
        return [{ menuItem: item, quantity: 1 }]
      }
      const existing = prev.find((line) => line.menuItem.id === item.id)
      if (existing) {
        return prev.map((line) =>
          line.menuItem.id === item.id ? { ...line, quantity: line.quantity + 1 } : line,
        )
      }
      return [...prev, { menuItem: item, quantity: 1 }]
    })
    setRestaurantId(item.restaurantId)
  }

  function removeItem(menuItemId: number) {
    setLines((prev) => prev.filter((line) => line.menuItem.id !== menuItemId))
  }

  function clear() {
    setLines([])
    setRestaurantId(null)
  }

  const total = useMemo(
    () => lines.reduce((sum, line) => sum + line.menuItem.price * line.quantity, 0),
    [lines],
  )

  return (
    <CartContext.Provider value={{ restaurantId, lines, addItem, removeItem, clear, total }}>
      {children}
    </CartContext.Provider>
  )
}

export function useCart(): CartState {
  const ctx = useContext(CartContext)
  if (!ctx) throw new Error('useCart must be used within CartProvider')
  return ctx
}
