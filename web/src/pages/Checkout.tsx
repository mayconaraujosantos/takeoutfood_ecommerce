import { useState, type ChangeEvent, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api/client'
import type { OrderInfo, PaymentMethod } from '../api/types'
import { isAuthenticated } from '../auth'
import { useCart } from '../cart'

export default function Checkout() {
  const navigate = useNavigate()
  const { restaurantId, lines, total, clear } = useCart()
  const [deliveryAddress, setDeliveryAddress] = useState('')
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>('PIX')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  if (lines.length === 0) {
    return (
      <p>
        Seu carrinho está vazio. <a href="/">Ver restaurantes</a>
      </p>
    )
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (!isAuthenticated()) {
      navigate('/login')
      return
    }
    setError(null)
    setLoading(true)
    try {
      // order-service's cart is stateful server-side (POST creates it, then each item is
      // added one call at a time against that same cart id) -- this has to run sequentially,
      // not in parallel.
      const cart = await api.post<OrderInfo>('/order-service/api/v1/orders', { restaurantId })
      for (const line of lines) {
        await api.post<OrderInfo>(`/order-service/api/v1/orders/${cart.id}/items`, {
          menuItemId: line.menuItem.id,
          quantity: line.quantity,
        })
      }
      const confirmed = await api.post<OrderInfo>(`/order-service/api/v1/orders/${cart.id}/checkout`, {
        deliveryAddress,
        paymentMethod,
      })
      clear()
      navigate(`/orders/${confirmed.id}`)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Falha ao finalizar pedido')
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="checkout-form">
      <h1>Seu pedido</h1>
      <ul>
        {lines.map((line) => (
          <li key={line.menuItem.id}>
            {line.quantity}x {line.menuItem.name} - R$ {(line.menuItem.price * line.quantity).toFixed(2)}
          </li>
        ))}
      </ul>
      <p>
        <strong>Total: R$ {total.toFixed(2)}</strong>
      </p>
      {error && <p className="error">{error}</p>}
      <label>
        Endereço de entrega
        <input
          value={deliveryAddress}
          onChange={(event: ChangeEvent<HTMLInputElement>) => setDeliveryAddress(event.target.value)}
          required
        />
      </label>
      <label>
        Forma de pagamento
        <select
          value={paymentMethod}
          onChange={(event: ChangeEvent<HTMLSelectElement>) => setPaymentMethod(event.target.value as PaymentMethod)}
        >
          <option value="PIX">PIX</option>
          <option value="CREDIT_CARD">Cartão de crédito</option>
        </select>
      </label>
      <button type="submit" disabled={loading}>
        {loading ? 'Enviando...' : 'Confirmar pedido'}
      </button>
    </form>
  )
}
