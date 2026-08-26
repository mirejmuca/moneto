import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Bell, ChevronDown } from 'lucide-react'
import { useState, useEffect, useRef } from 'react'
import { getUnreadNotifications } from '../services/notificationService'

function Dropdown({ label, children }) {
  const [open, setOpen] = useState(false)
  const ref = useRef(null)

  useEffect(() => {
    const handleClick = (e) => {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false)
    }
    document.addEventListener('mousedown', handleClick)
    return () => document.removeEventListener('mousedown', handleClick)
  }, [])

  return (
    <div className="relative" ref={ref}>
      <button onClick={() => setOpen(!open)}
        className="flex items-center gap-1 text-gray-400 hover:text-white text-sm transition">
        {label} <ChevronDown size={14} />
      </button>
      {open && (
        <div className="absolute top-full mt-2 left-0 bg-gray-800 rounded-lg shadow-lg py-2 min-w-[160px] z-50"
          onClick={() => setOpen(false)}>
          {children}
        </div>
      )}
    </div>
  )
}

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

  const dropdownLink = "block px-4 py-2 text-gray-300 hover:bg-gray-700 hover:text-white text-sm transition"

  return (
    <nav className="bg-gray-900 px-8 py-4 flex justify-between items-center border-b border-gray-800">
      <div className="flex items-center gap-8">
        <h1 className="text-xl font-bold text-blue-400">Moneto</h1>
        <div className="flex items-center gap-6">
          <Link to="/" className="text-gray-400 hover:text-white text-sm transition">Dashboard</Link>
          <Link to="/transactions" className="text-gray-400 hover:text-white text-sm transition">Transactions</Link>
          <Link to="/budgets" className="text-gray-400 hover:text-white text-sm transition">Budgets</Link>
          <Link to="/goals" className="text-gray-400 hover:text-white text-sm transition">Goals</Link>

          <Dropdown label="Analytics">
            <Link to="/forecast" className={dropdownLink}>Forecast</Link>
            <Link to="/insights" className={dropdownLink}>Insights</Link>
            <Link to="/alerts" className={dropdownLink}>Alerts</Link>
          </Dropdown>

          <Dropdown label="More">
            <Link to="/categories" className={dropdownLink}>Categories</Link>
            <Link to="/recurring" className={dropdownLink}>Recurring</Link>
          </Dropdown>
        </div>
      </div>
      <div className="flex items-center gap-5">
        <Link to="/subscription" className="text-yellow-400 hover:text-yellow-300 text-sm font-medium transition">Premium</Link>
        <Link to="/notifications" className="relative text-gray-400 hover:text-white transition">
          <Bell size={20} />
          {unreadCount > 0 && (
            <span className="absolute -top-2 -right-2 bg-red-500 text-white text-xs font-bold rounded-full w-5 h-5 flex items-center justify-center">
              {unreadCount > 9 ? '9+' : unreadCount}
            </span>
          )}
        </Link>
        <Dropdown label={`Hello, ${user?.name || ''}`}>
          <Link to="/settings" className={dropdownLink}>Settings</Link>
          <button onClick={handleLogout} className={`${dropdownLink} w-full text-left text-red-400 hover:text-red-300`}>Logout</button>
        </Dropdown>
      </div>
    </nav>
  )
}