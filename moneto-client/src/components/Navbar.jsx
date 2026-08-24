import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Bell } from 'lucide-react'
import { useState, useEffect } from 'react'
import { getUnreadNotifications } from '../services/notificationService'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [unreadCount, setUnreadCount] = useState(0)

  useEffect(() => {
    const fetchUnread = async () => {
      try {
        const data = await getUnreadNotifications()
        setUnreadCount(data.length)
      } catch (err) {
        console.error(err)
      }
    }
    fetchUnread()
  }, [])

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
          <Link to="/categories" className="text-gray-400 hover:text-white text-sm transition">Categories</Link>
          <Link to="/recurring" className="text-gray-400 hover:text-white text-sm transition">Recurring</Link>
          <Link to="/settings" className="text-gray-400 hover:text-white text-sm transition">Settings</Link>
          <Link to="/subscription" className="text-gray-400 hover:text-white text-sm transition">Premium</Link>
          <Link to="/forecast" className="text-gray-400 hover:text-white text-sm transition">Forecast</Link>
          <Link to="/insights" className="text-gray-400 hover:text-white text-sm transition">Insights</Link>
        </div>
      </div>
      <div className="flex items-center gap-5">
        <Link to="/notifications" className="relative text-gray-400 hover:text-white transition">
          <Bell size={20} />
          {unreadCount > 0 && (
            <span className="absolute -top-2 -right-2 bg-red-500 text-white text-xs font-bold rounded-full w-5 h-5 flex items-center justify-center">
              {unreadCount > 9 ? '9+' : unreadCount}
            </span>
          )}
        </Link>
        <span className="text-gray-400 text-sm">Hello, {user?.name}</span>
        <button onClick={handleLogout} className="text-sm text-red-400 hover:text-red-300">Logout</button>
      </div>
    </nav>
  )
}