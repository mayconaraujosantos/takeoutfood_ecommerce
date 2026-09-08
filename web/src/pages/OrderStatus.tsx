import { useCallback, useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { api } from '../api/client'
import type { OrderInfo, OrderStatus as OrderStatusValue } from '../api/types'

const STATUS_LABELS: Record<OrderStatusValue, string> = {
  CART: 'Carrinho',
  PENDING_PAYMENT: 'Aguardando pagamento',
  CONFIRMED: 'Confirmado',
  PAYMENT_FAILED: 'Pagamento falhou',
  PREPARING: 'Em preparo',
  OUT_FOR_DELIVERY: 'A caminho',
  DELIVERED: 'Entregue',
  CANCELLED: 'Cancelado',
}

export default function OrderStatus() {
  const { orderId } = useParams()
  const [order, setOrder] = useState<OrderInfo | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  const load = useCallback(() => {
    setLoading(true)
    api
      .get<OrderInfo>(`/order-service/api/v1/orders/${orderId}`)
      .then(setOrder)
      .catch((err: unknown) => setError(err instanceof Error ? err.message : 'Falha ao carregar pedido'))
      .finally(() => setLoading(false))
  }, [orderId])

  useEffect(() => {
    load()
  }, [load])

  if (loading && !order) return <p>Carregando pedido...</p>
  if (error) return <p className="error">{error}</p>
  if (!order) return null

  return (
    <div>
      <h1>Pedido #{order.id}</h1>
      <p className="status">{STATUS_LABELS[order.status]}</p>
      <ul>
        {order.items.map((item) => (
          <li key={item.id}>
            {item.quantity}x {item.itemName}
          </li>
        ))}
      </ul>
      <p>
        <strong>Total: R$ {order.totalAmount.toFixed(2)}</strong>
      </p>
      <button type="button" onClick={load} disabled={loading}>
        {loading ? 'Atualizando...' : 'Atualizar status'}
      </button>
    </div>
  )
}
