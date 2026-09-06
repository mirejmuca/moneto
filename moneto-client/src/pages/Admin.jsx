import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { getAllUsers, getStats, deleteUser, changeRole, changeSubscription } from '../services/adminService'
import { getAuditLogs, getUserAudit } from '../services/auditService'

export default function Admin() {
  const [tab, setTab] = useState('users')
  const [users, setUsers] = useState([])
  const [stats, setStats] = useState(null)
  const [logs, setLogs] = useState([])
  const [loading, setLoading] = useState(true)
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [auditModal, setAuditModal] = useState(null)
  const [userLogs, setUserLogs] = useState([])
  const [modalLoading, setModalLoading] = useState(false)

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

  const fetchLogs = async () => {
    try {
      const data = await getAuditLogs(0, 100)
      setLogs(data.content)
    } catch (err) {
      console.error(err)
    }
  }

  useEffect(() => {
    if (tab === 'audit') fetchLogs()
  }, [tab])

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

  const handleSubscriptionChange = async (userId, newTier) => {
    try {
      await changeSubscription(userId, newTier)
      fetchData()
    } catch (err) {
      alert(err.response?.data?.error || 'Failed to change subscription')
    }
  }

  const openAudit = async (email) => {
    setAuditModal(email)
    setModalLoading(true)
    setUserLogs([])
    try {
      const data = await getUserAudit(email)
      setUserLogs(data.content)
    } catch (err) {
      console.error(err)
    } finally {
      setModalLoading(false)
    }
  }

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const formatDate = (dateStr) => {
    if (!dateStr) return ''
    const d = new Date(dateStr)
    return d.toLocaleString()
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
        {/* Tabs */}
        <div className="flex gap-2 mb-6 border-b border-gray-800">
          <button onClick={() => setTab('users')}
            className={`px-4 py-2 text-sm font-medium transition border-b-2 ${
              tab === 'users' ? 'border-blue-500 text-white' : 'border-transparent text-gray-400 hover:text-white'
            }`}>
            Users
          </button>
          <button onClick={() => setTab('audit')}
            className={`px-4 py-2 text-sm font-medium transition border-b-2 ${
              tab === 'audit' ? 'border-blue-500 text-white' : 'border-transparent text-gray-400 hover:text-white'
            }`}>
            Audit Log
          </button>
        </div>

        {tab === 'users' && (
          <>
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
                        {isAdmin && u.role === 'USER' ? (
                          <select value={u.tier}
                            onChange={(e) => handleSubscriptionChange(u.id, e.target.value)}
                            className={`bg-gray-800 text-sm rounded px-2 py-1 outline-none focus:ring-1 focus:ring-blue-500 ${u.tier === 'PREMIUM' ? 'text-yellow-400' : u.tier === 'PLUS' ? 'text-blue-400' : 'text-gray-400'}`}>
                            <option value="FREE">FREE</option>
                            <option value="PLUS">PLUS</option>
                            <option value="PREMIUM">PREMIUM</option>
                          </select>
                        ) : (
                          <span className={u.tier === 'PREMIUM' ? 'text-yellow-400' : u.tier === 'PLUS' ? 'text-blue-400' : 'text-gray-400'}>
                            {u.tier}
                          </span>
                        )}
                      </td>
                      <td className="py-3">
                        <span className={u.role === 'ADMIN' ? 'text-red-400' : u.role === 'MODERATOR' ? 'text-purple-400' : 'text-gray-400'}>
                          {u.role}
                        </span>
                      </td>
                      <td className="py-3">{u.verified ? '✓' : '—'}</td>
                      <td className="py-3 text-right space-x-2">
                        <button onClick={() => openAudit(u.email)}
                          className="text-blue-400 hover:text-blue-300 text-xs">Activity</button>
                        {isAdmin && u.role !== 'ADMIN' && (
                          u.role === 'MODERATOR' ? (
                            <button onClick={() => handleRoleChange(u.id, 'USER')}
                              className="text-purple-400 hover:text-purple-300 text-xs">Demote</button>
                          ) : (
                            <button onClick={() => handleRoleChange(u.id, 'MODERATOR')}
                              className="text-purple-400 hover:text-purple-300 text-xs">Make Mod</button>
                          )
                        )}
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
          </>
        )}

        {tab === 'audit' && (
          <div className="bg-gray-900 rounded-2xl p-6">
            <h2 className="text-lg font-semibold mb-4">Audit Log</h2>
            {logs.length === 0 ? (
              <p className="text-gray-500 text-sm">No activity recorded yet.</p>
            ) : (
              <table className="w-full text-sm">
                <thead>
                  <tr className="text-gray-500 text-left border-b border-gray-800">
                    <th className="pb-3">Time</th>
                    <th className="pb-3">User</th>
                    <th className="pb-3">Action</th>
                    <th className="pb-3">Details</th>
                  </tr>
                </thead>
                <tbody>
                  {logs.map(log => (
                    <tr key={log.id} className="border-b border-gray-800/50">
                      <td className="py-3 text-gray-400 whitespace-nowrap">{formatDate(log.createdAt)}</td>
                      <td className="py-3 text-gray-300">{log.userEmail}</td>
                      <td className="py-3">
                        <span className="text-blue-400 font-mono text-xs">{log.action}</span>
                      </td>
                      <td className="py-3 text-gray-400">{log.details || '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}
      </div>

      {/* Audit modal */}
      {auditModal && (
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50 px-4"
          onClick={() => setAuditModal(null)}>
          <div className="bg-gray-900 rounded-2xl p-6 max-w-2xl w-full max-h-[80vh] overflow-hidden flex flex-col"
            onClick={(e) => e.stopPropagation()}>
            <div className="flex justify-between items-center mb-4">
              <div>
                <h2 className="text-lg font-semibold">User Activity</h2>
                <p className="text-gray-500 text-sm">{auditModal}</p>
              </div>
              <button onClick={() => setAuditModal(null)}
                className="text-gray-400 hover:text-white text-xl leading-none">×</button>
            </div>

            <div className="overflow-y-auto flex-1">
              {modalLoading ? (
                <p className="text-gray-400 text-sm">Loading...</p>
              ) : userLogs.length === 0 ? (
                <p className="text-gray-500 text-sm">No activity recorded for this user.</p>
              ) : (
                <table className="w-full text-sm">
                  <thead>
                    <tr className="text-gray-500 text-left border-b border-gray-800">
                      <th className="pb-2">Time</th>
                      <th className="pb-2">Action</th>
                      <th className="pb-2">Details</th>
                    </tr>
                  </thead>
                  <tbody>
                    {userLogs.map(log => (
                      <tr key={log.id} className="border-b border-gray-800/50">
                        <td className="py-2 text-gray-400 whitespace-nowrap">{formatDate(log.createdAt)}</td>
                        <td className="py-2">
                          <span className="text-blue-400 font-mono text-xs">{log.action}</span>
                        </td>
                        <td className="py-2 text-gray-400">{log.details || '—'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}