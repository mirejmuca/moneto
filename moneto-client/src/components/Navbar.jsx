import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <nav className="bg-gray-900 px-8 py-4 flex justify-between items-center border-b border-gray-800">
      <div className="flex items-center gap-8">
        <h1 className="text-xl font-bold text-blue-400">Moneto</h1>
        <div className="flex gap-6">
          <Link to="/" className="text-gray-400 hover:text-white text-sm transition">Dashboard</Link>
          <Link to="/transactions" className="text-gray-400 hover:text-white text-sm transition">Transactions</Link>
          <Link to="/budgets" className="text-gray-400 hover:text-white text-sm transition">Budgets</Link>
          <Link to="/goals" className="text-gray-400 hover:text-white text-sm transition">Goals</Link>
          <Link to="/settings" className="text-gray-400 hover:text-white text-sm transition">Settings</Link>
        </div>
      </div>
      <div className="flex items-center gap-4">
        <span className="text-gray-400 text-sm">Hello, {user?.name}</span>
        <button onClick={handleLogout} className="text-sm text-red-400 hover:text-red-300">Logout</button>
      </div>
    </nav>
  )
}