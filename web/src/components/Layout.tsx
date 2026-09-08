import { Link, Outlet, useNavigate } from 'react-router-dom'
import { clearSession, getCurrentUser, isAuthenticated } from '../auth'
import { useCart } from '../cart'

export default function Layout() {
  const navigate = useNavigate()
  const authed = isAuthenticated()
  const user = getCurrentUser()
  const { lines } = useCart()

  function handleLogout() {
    clearSession()
    navigate('/login')
  }

  return (
    <div className="app-shell">
      <nav>
        <Link to="/" className="brand">
          iFood Clone
        </Link>
        <div className="nav-links">
          {lines.length > 0 && <Link to="/checkout">Carrinho ({lines.length})</Link>}
          {authed ? (
            <>
              <span>{user?.firstName}</span>
              <button type="button" onClick={handleLogout}>
                Sair
              </button>
            </>
          ) : (
            <>
              <Link to="/login">Entrar</Link>
              <Link to="/register">Cadastrar</Link>
            </>
          )}
        </div>
      </nav>
      <main>
        <Outlet />
      </main>
    </div>
  )
}
