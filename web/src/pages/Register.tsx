import { useState, type ChangeEvent, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api } from '../api/client'

interface RegisterForm {
  email: string
  password: string
  firstName: string
  lastName: string
  phone: string
}

const EMPTY_FORM: RegisterForm = { email: '', password: '', firstName: '', lastName: '', phone: '' }

export default function Register() {
  const navigate = useNavigate()
  const [form, setForm] = useState<RegisterForm>(EMPTY_FORM)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  function update(field: keyof RegisterForm) {
    return (event: ChangeEvent<HTMLInputElement>) => setForm((f) => ({ ...f, [field]: event.target.value }))
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setLoading(true)
    try {
      // register() doesn't return a token (see auth-service's AuthController) -- it only
      // creates the account, so the user still has to log in right after.
      await api.post('/auth-service/api/v1/auth/register', { ...form, role: 'CUSTOMER' })
      navigate('/login')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Falha no cadastro')
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="auth-form">
      <h1>Cadastrar</h1>
      {error && <p className="error">{error}</p>}
      <label>
        Nome
        <input value={form.firstName} onChange={update('firstName')} required />
      </label>
      <label>
        Sobrenome
        <input value={form.lastName} onChange={update('lastName')} required />
      </label>
      <label>
        Email
        <input type="email" value={form.email} onChange={update('email')} required />
      </label>
      <label>
        Telefone
        <input value={form.phone} onChange={update('phone')} />
      </label>
      <label>
        Senha
        <input type="password" minLength={8} value={form.password} onChange={update('password')} required />
      </label>
      <button type="submit" disabled={loading}>
        {loading ? 'Enviando...' : 'Cadastrar'}
      </button>
      <p>
        Já tem conta? <Link to="/login">Entrar</Link>
      </p>
    </form>
  )
}
