import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { api } from '../api/client'
import type { MenuItemInfo } from '../api/types'
import { useCart } from '../cart'

export default function Menu() {
  const { restaurantId } = useParams()
  const navigate = useNavigate()
  const { addItem, lines, restaurantId: cartRestaurantId } = useCart()
  const [items, setItems] = useState<MenuItemInfo[]>([])
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api
      .get<MenuItemInfo[]>(`/menu-service/api/v1/menus/restaurant/${restaurantId}`)
      .then(setItems)
      .catch((err: unknown) => setError(err instanceof Error ? err.message : 'Falha ao carregar cardápio'))
      .finally(() => setLoading(false))
  }, [restaurantId])

  if (loading) return <p>Carregando cardápio...</p>
  if (error) return <p className="error">{error}</p>

  const switchingRestaurant = cartRestaurantId !== null && cartRestaurantId !== Number(restaurantId)

  return (
    <div>
      <h1>Cardápio</h1>
      {switchingRestaurant && (
        <p className="warning">Seu carrinho tem itens de outro restaurante -- adicionar aqui vai substituí-lo.</p>
      )}
      {items.length === 0 && <p>Nenhum item cadastrado ainda.</p>}
      <ul className="menu-list">
        {items.map((item) => (
          <li key={item.id}>
            <span>
              {item.name} - R$ {item.price.toFixed(2)}
            </span>
            <button type="button" onClick={() => addItem(item)} disabled={!item.available}>
              Adicionar
            </button>
          </li>
        ))}
      </ul>
      {lines.length > 0 && (
        <button type="button" onClick={() => navigate('/checkout')}>
          Ir para o carrinho ({lines.length})
        </button>
      )}
    </div>
  )
}
