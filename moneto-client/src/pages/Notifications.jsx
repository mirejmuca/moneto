import { useState, useEffect } from 'react'
import Navbar from '../components/Navbar'
import { getNotifications, markAsRead, markAllAsRead, deleteNotification } from '../services/notificationService'

export default function Notifications() {
  const [notifications, setNotifications] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    try {
      const data = await getNotifications()
      setNotifications(data)
    } catch (err) {
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const handleMarkAsRead = async (id) => {
    try {
      await markAsRead(id)
      fetchData()
    } catch (err) {
      console.error(err)
    }
  }

  const handleMarkAllAsRead = async () => {
    try {
      await markAllAsRead()
      fetchData()
    } catch (err) {
      console.error(err)
    }
  }

  const handleDelete = async (id) => {
    try {
      await deleteNotification(id)
      fetchData()
    } catch (err) {
      console.error(err)
    }
  }

  if (loading) return (
    <div className="min-h-screen bg-gray-950 flex items-center justify-center">
      <p className="text-gray-400">Loading...</p>
    </div>
  )

  const unreadCount = notifications.filter(n => !n.isRead).length

  return (
    <div className="min-h-screen bg-gray-950 text-white">
      <Navbar />
      <div className="max-w-3xl mx-auto px-8 py-8">
        <div className="flex justify-between items-center mb-6">
          <div>
            <h1 className="text-2xl font-bold">Notifications</h1>
            {unreadCount > 0 && (
              <p className="text-gray-400 text-sm mt-1">{unreadCount} unread</p>
            )}
          </div>
          {unreadCount > 0 && (
            <button
              onClick={handleMarkAllAsRead}
              className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-semibold transition">
              Mark all as read
            </button>
          )}
        </div>

        <div className="space-y-3">
          {notifications.length === 0 ? (
            <div className="bg-gray-900 rounded-2xl p-6">
              <p className="text-gray-500 text-sm">No notifications yet.</p>
            </div>
          ) : (
            notifications.map(n => (
              <div key={n.id}
                className={`rounded-2xl p-4 flex items-start justify-between gap-4 transition ${n.isRead ? 'bg-gray-900' : 'bg-gray-800 border-l-4 border-blue-500'}`}>
                <div className="flex-1">
                  <p className={`text-sm ${n.isRead ? 'text-gray-400' : 'text-white'}`}>{n.message}</p>
                  <p className="text-gray-600 text-xs mt-1">{new Date(n.createdAt).toLocaleString()}</p>
                </div>
                <div className="flex items-center gap-3">
                  {!n.isRead && (
                    <button onClick={() => handleMarkAsRead(n.id)}
                      className="text-blue-400 hover:text-blue-300 text-xs transition whitespace-nowrap">
                      Mark read
                    </button>
                  )}
                  <button onClick={() => handleDelete(n.id)}
                    className="text-gray-500 hover:text-red-400 text-xs transition">
                    Delete
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  )
}