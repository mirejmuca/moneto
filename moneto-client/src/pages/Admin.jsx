import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { getAllUsers, getStats, deleteUser, changeRole } from '../services/adminService'

export default function Admin() {
  const [users, setUsers] = useState([])
  const [stats, setStats] = useState(null)
  const [loading, setLoading] = useState(true)
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const isAdmin = user?.role === 'ADMIN'

  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    try {
      const [usersData, statsData] = await Promise.all([getAllUsers(), getStats()])
      setUsers(usersData)
      setStats(statsData)
    } catch (err) {
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const handleDelete = async (userId) => {
    if (!window.confirm('Are you sure you want to delete this user?')) return
    try {
      await deleteUser(userId)
      fetchData()
    } catch (err) {
      alert(err.response?.data || 'Failed to delete user')
    }
  }

  const handleRoleChange = async (userId, newRole) => {
    try {
      await changeRole(userId, newRole)
      fetchData()
    } catch (err) {
      alert(err.response?.data || 'Failed to change role')
    }
  }

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  if (loading) return (
    <div className="min-h-screen bg-gray-950 flex items-center justify-center">
      <p className="text-gray-400">Loading...</p>
    </div>
  )

  return (
    <div className="min-h-screen bg-gray-950 text-white">
      {/* Header */}
      <div className="border-b border-gray-800 px-8 py-4 flex justify-between items-center">
        <div>
          <h1 className="text-xl font-bold">Moneto Admin</h1>
          <p className="text-gray-500 text-sm">{user?.role} · {user?.email}</p>
        </div>
        <button onClick={handleLogout}
          className="text-gray-400 hover:text-white text-sm transition">Logout</button>
      </div>

      <div className="max-w-6xl mx-auto px-8 py-8">
        {/* Stats */}
        <div className="grid grid-cols-5 gap-4 mb-8">
          <div className="bg-gray-900 rounded-2xl p-5">
            <p className="text-gray-400 text-sm">Total Users</p>
            <p className="text-2xl font-bold">{stats.totalUsers}</p>
          </div>
          <div className="bg-gray-900 rounded-2xl p-5">
            <p className="text-gray-400 text-sm">Free</p>
            <p className="text-2xl font-bold text-gray-300">{stats.freeUsers}</p>
          </div>
          <div className="bg-gray-900 rounded-2xl p-5">
            <p className="text-gray-400 text-sm">Plus</p>
            <p className="text-2xl font-bold text-blue-400">{stats.plusUsers}</p>
          </div>
          <div className="bg-gray-900 rounded-2xl p-5">
            <p className="text-gray-400 text-sm">Premium</p>
            <p className="text-2xl font-bold text-yellow-400">{stats.premiumUsers}</p>
          </div>
          <div className="bg-gray-900 rounded-2xl p-5">
            <p className="text-gray-400 text-sm">Verified</p>
            <p className="text-2xl font-bold text-green-400">{stats.verifiedUsers}</p>
          </div>
        </div>

        {/* Users table */}
        <div className="bg-gray-900 rounded-2xl p-6">
          <h2 className="text-lg font-semibold mb-4">Users</h2>
          <table className="w-full text-sm">
            <thead>
              <tr className="text-gray-500 text-left border-b border-gray-800">
                <th className="pb-3">Name</th>
                <th className="pb-3">Email</th>
                <th className="pb-3">Tier</th>
                <th className="pb-3">Role</th>
                <th className="pb-3">Verified</th>
                <th className="pb-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map(u => (
                <tr key={u.id} className="border-b border-gray-800/50">
                  <td className="py-3">{u.name}</td>
                  <td className="py-3 text-gray-400">{u.email}</td>
                  <td className="py-3">
                    <span className={u.tier === 'PREMIUM' ? 'text-yellow-400' : u.tier === 'PLUS' ? 'text-blue-400' : 'text-gray-400'}>
                      {u.tier}
                    </span>
                  </td>
                  <td className="py-3">
                    <span className={u.role === 'ADMIN' ? 'text-red-400' : u.role === 'MODERATOR' ? 'text-purple-400' : 'text-gray-400'}>
                      {u.role}
                    </span>
                  </td>
                  <td className="py-3">{u.verified ? '✓' : '—'}</td>
                  <td className="py-3 text-right space-x-2">
                    {/* Ndryshim roli — vetëm admin, dhe jo mbi admin */}
                    {isAdmin && u.role !== 'ADMIN' && (
                      u.role === 'MODERATOR' ? (
                        <button onClick={() => handleRoleChange(u.id, 'USER')}
                          className="text-purple-400 hover:text-purple-300 text-xs">Demote</button>
                      ) : (
                        <button onClick={() => handleRoleChange(u.id, 'MODERATOR')}
                          className="text-purple-400 hover:text-purple-300 text-xs">Make Mod</button>
                      )
                    )}
                    {/* Fshirje — admin fshin këdo (jo admin); moderator fshin vetëm USER */}
                    {u.role !== 'ADMIN' && (isAdmin || u.role === 'USER') && (
                      <button onClick={() => handleDelete(u.id)}
                        className="text-red-400 hover:text-red-300 text-xs">Delete</button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}