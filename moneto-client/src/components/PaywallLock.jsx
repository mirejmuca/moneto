import { useNavigate } from 'react-router-dom'
import Navbar from './Navbar'

export default function PaywallLock({ title, description, tier = 'Plus' }) {
  const navigate = useNavigate()

  return (
    <div className="min-h-screen bg-gray-950 text-white">
      <Navbar />
      <div className="max-w-2xl mx-auto px-8 py-16 text-center">
        <div className="text-yellow-400 text-5xl mb-4">🔒</div>
        <h1 className="text-2xl font-bold mb-2">{title}</h1>
        <p className="text-gray-400 mb-6">{description}</p>
        <button onClick={() => navigate('/subscription')}
          className="bg-yellow-500 hover:bg-yellow-400 text-gray-900 font-semibold py-3 px-6 rounded-lg transition">
          Upgrade to {tier}
        </button>
      </div>
    </div>
  )
}