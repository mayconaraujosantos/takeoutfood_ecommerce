export interface ApiResponse<T> {
  success: boolean
  message?: string
  data?: T
  error?: string
  timestamp?: string
}

export type UserRole = 'CUSTOMER' | 'RESTAURANT_OWNER' | 'DELIVERY_DRIVER' | 'ADMIN'

export interface UserInfo {
  id: number
  email: string
  firstName: string
  lastName: string
  fullName: string
  phone: string
  role: UserRole
  active: boolean
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: UserInfo
}

export interface RestaurantInfo {
  id: number
  name: string
  description: string
  cuisineType: string
  address: string
  phone: string
  ownerId: number
  active: boolean
  createdAt: string
}

export interface MenuItemInfo {
  id: number
  restaurantId: number
  name: string
  description: string
  price: number
  category: string
  available: boolean
}

export type PaymentMethod = 'CREDIT_CARD' | 'PIX'

export type OrderStatus =
  | 'CART'
  | 'PENDING_PAYMENT'
  | 'CONFIRMED'
  | 'PAYMENT_FAILED'
  | 'PREPARING'
  | 'OUT_FOR_DELIVERY'
  | 'DELIVERED'
  | 'CANCELLED'

export interface OrderItemInfo {
  id: number
  menuItemId: number
  itemName: string
  quantity: number
  unitPrice: number
  notes?: string
}

export interface OrderInfo {
  id: number
  userId: number
  restaurantId: number
  status: OrderStatus
  deliveryAddress?: string
  paymentMethod?: PaymentMethod
  totalAmount: number
  items: OrderItemInfo[]
  createdAt: string
}
