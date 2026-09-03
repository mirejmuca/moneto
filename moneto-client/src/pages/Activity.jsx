import { useState, useEffect } from 'react'
import Navbar from '../components/Navbar'
import { getMyActivity } from '../services/auditService'

export default function Activity() {
  const [logs, setLogs] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchActivity()
  }, [])

  const fetchActivity = async () => {
    try {
      const data = await getMyActivity(0, 100)
      setLogs(data.content)
    } catch (err) {
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const formatDate = (dateStr) => {
    if (!dateStr) return ''
    return new Date(dateStr).toLocaleString()
  }

  // Përkthe veprimet në tekst miqësor për përdoruesin
  const actionLabel = (action) => {
    const labels = {
      LOGIN: 'Signed in',
      REGISTER: 'Created account',
      CREATE_TRANSACTION: 'Added a transaction',
      DELETE_TRANSACTION: 'Deleted a transaction',
      SUBSCRIBE: 'Changed subscription',
      CANCEL_SUBSCRIPTION: 'Cancelled subscription',
      DELETE_ACCOUNT: 'Requested account deletion',
    }
    return labels[action] || action
  }

  if (loading) return (
    <div className="min-h-screen bg-gray-950 flex items-center justify-center">
      <p className="text-gray-400">Loading...</p>
    </div>
  )

  return (
    <div className="min-h-screen bg-gray-950 text-white">
      <Navbar />
      <div className="max-w-3xl mx-auto px-8 py-8">
        <h1 className="text-2xl font-bold mb-2">My Activity</h1>
        <p className="text-gray-400 text-sm mb-6">A record of recent actions on your account.</p>

        <div className="bg-gray-900 rounded-2xl p-6">
          {logs.length === 0 ? (
            <p className="text-gray-500 text-sm">No activity recorded yet.</p>
          ) : (
            <div className="space-y-3">
              {logs.map(log => (
                <div key={log.id} className="flex justify-between items-center border-b border-gray-800/50 pb-3 last:border-0">
                  <div>
                    <p className="font-medium">{actionLabel(log.action)}</p>
                    {log.details && <p className="text-gray-500 text-sm">{log.details}</p>}
                  </div>
                  <p className="text-gray-500 text-sm whitespace-nowrap">{formatDate(log.createdAt)}</p>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}