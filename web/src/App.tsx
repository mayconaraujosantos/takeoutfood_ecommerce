import { BrowserRouter, Route, Routes } from 'react-router-dom'
import './App.css'
import Layout from './components/Layout'
import { CartProvider } from './cart'
import Checkout from './pages/Checkout'
import Login from './pages/Login'
import Menu from './pages/Menu'
import OrderStatus from './pages/OrderStatus'
import Register from './pages/Register'
import Restaurants from './pages/Restaurants'

export default function App() {
  return (
    <CartProvider>
      <BrowserRouter>
        <Routes>
          <Route element={<Layout />}>
            <Route index element={<Restaurants />} />
            <Route path="restaurants/:restaurantId" element={<Menu />} />
            <Route path="checkout" element={<Checkout />} />
            <Route path="orders/:orderId" element={<OrderStatus />} />
            <Route path="login" element={<Login />} />
            <Route path="register" element={<Register />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </CartProvider>
  )
}
