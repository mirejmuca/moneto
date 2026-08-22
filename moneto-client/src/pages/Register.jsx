import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { register as registerService, verify as verifyService } from '../services/authService'

export default function Register() {
  const [form, setForm] = useState({
    name: '',
    email: '',
    password: '',
    currency: 'USD'
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [step, setStep] = useState('register') // 'register' or 'verify'
  const [code, setCode] = useState('')
  const navigate = useNavigate()

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      await registerService(form)
      setStep('verify')
    } catch (err) {
      setError('Registration failed. Email may already be in use.')
    } finally {
      setLoading(false)
    }
  }

  const handleVerify = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      await verifyService(form.email, code)
      navigate('/login')
    } catch (err) {
      setError('Invalid verification code. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  if (step === 'verify') {
    return (
      <div className="min-h-screen bg-gray-950 flex items-center justify-center">
        <div className="bg-gray-900 p-8 rounded-2xl w-full max-w-md shadow-lg">
          <div className="text-blue-400 text-5xl mb-4 text-center">✉</div>
          <h1 className="text-2xl font-bold text-white mb-2 text-center">Verify your email</h1>
          <p className="text-gray-400 mb-6 text-center">
            We've sent a 6-digit code to <span className="text-white">{form.email}</span>
          </p>

          {error && <p className="text-red-400 text-sm mb-4 text-center">{error}</p>}

          <form onSubmit={handleVerify} className="space-y-4">
            <div>
              <input
                type="text"
                value={code}
                onChange={(e) => setCode(e.target.value)}
                className="w-full bg-gray-800 text-white text-center text-2xl tracking-widest rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="000000"
                maxLength="6"
                required
              />
            </div>
            <button
              type="submit"
              disabled={loading}
              className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 rounded-lg transition">
              {loading ? 'Verifying...' : 'Verify'}
            </button>
          </form>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-950 flex items-center justify-center">
      <div className="bg-gray-900 p-8 rounded-2xl w-full max-w-md shadow-lg">
        <h1 className="text-2xl font-bold text-white mb-2">Create account</h1>
        <p className="text-gray-400 mb-6">Start managing your finances with Moneto</p>

        {error && <p className="text-red-400 text-sm mb-4">{error}</p>}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="text-gray-400 text-sm mb-1 block">Name</label>
            <input
              type="text"
              name="name"
              value={form.name}
              onChange={handleChange}
              className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Your name"
              required
            />
          </div>
          <div>
            <label className="text-gray-400 text-sm mb-1 block">Email</label>
            <input
              type="email"
              name="email"
              value={form.email}
              onChange={handleChange}
              className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="you@example.com"
              required
            />
          </div>
          <div>
            <label className="text-gray-400 text-sm mb-1 block">Password</label>
            <input
              type="password"
              name="password"
              value={form.password}
              onChange={handleChange}
              className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="••••••••"
              required
            />
          </div>
          <div>
            <label className="text-gray-400 text-sm mb-1 block">Currency</label>
            <select
              name="currency"
              value={form.currency}
              onChange={handleChange}
              className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500">
              <option value="USD">USD - US Dollar</option>
              <option value="EUR">EUR - Euro</option>
              <option value="GBP">GBP - British Pound</option>
              <option value="ALL">ALL - Albanian Lek</option>
              <option value="CHF">CHF - Swiss Franc</option>
              <option value="CAD">CAD - Canadian Dollar</option>
              <option value="AUD">AUD - Australian Dollar</option>
              <option value="JPY">JPY - Japanese Yen</option>
            </select>
          </div>
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 rounded-lg transition">
            {loading ? 'Creating account...' : 'Create account'}
          </button>
        </form>

        <p className="text-gray-400 text-sm mt-6 text-center">
          Already have an account?{' '}
          <Link to="/login" className="text-blue-400 hover:underline">Sign in</Link>
        </p>
      </div>
    </div>
  )
}