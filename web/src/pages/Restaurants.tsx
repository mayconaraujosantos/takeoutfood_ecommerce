import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/client'
import type { RestaurantInfo } from '../api/types'

export default function Restaurants() {
  const [restaurants, setRestaurants] = useState<RestaurantInfo[]>([])
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api
      .get<RestaurantInfo[]>('/restaurant-service/api/v1/restaurants')
      .then(setRestaurants)
      .catch((err: unknown) => setError(err instanceof Error ? err.message : 'Falha ao carregar restaurantes'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <p>Carregando restaurantes...</p>
  if (error) return <p className="error">{error}</p>

  return (
    <div>
      <h1>Restaurantes</h1>
      {restaurants.length === 0 && <p>Nenhum restaurante cadastrado ainda.</p>}
      <ul className="restaurant-list">
        {restaurants.map((restaurant) => (
          <li key={restaurant.id}>
            <Link to={`/restaurants/${restaurant.id}`}>
              <strong>{restaurant.name}</strong> - {restaurant.cuisineType}
            </Link>
          </li>
        ))}
      </ul>
    </div>
  )
}
