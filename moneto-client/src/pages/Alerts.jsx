import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar'
import { useAuth } from '../context/AuthContext'
import { getCurrencySymbol } from '../utils/currency'
import { getAlerts, createAlert, deleteAlert } from '../services/alertService'
import { getCategories } from '../services/categoryService'

const ALERT_TYPES = [
  { value: 'DAILY_SPENDING', label: 'Daily spending', desc: 'Alert when daily expenses exceed a threshold', needsCategory: false },
  { value: 'CATEGORY_SPENDING', label: 'Category spending', desc: "Alert when a category's monthly total exceeds a threshold", needsCategory: true },
  { value: 'MONTHLY_SPENDING', label: 'Monthly spending', desc: 'Alert when total monthly expenses exceed a threshold', needsCategory: false },
  { value: 'LARGE_TRANSACTION', label: 'Large transaction', desc: 'Alert on any single payment over a threshold', needsCategory: false },
  { value: 'NEGATIVE_BALANCE', label: 'Negative balance', desc: 'Alert when monthly expenses exceed income', needsCategory: false },
]

export default function Alerts() {
  const [alerts, setAlerts] = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [locked, setLocked] = useState(false)
  const [type, setType] = useState('DAILY_SPENDING')
  const [categoryId, setCategoryId] = useState('')
  const [threshold, setThreshold] = useState('')
  const [saving, setSaving] = useState(false)
  const { user } = useAuth()
  const navigate = useNavigate()
  const symbol = getCurrencySymbol(user?.currency)

  const selectedType = ALERT_TYPES.find(t => t.value === type)
  const needsThreshold = type !== 'NEGATIVE_BALANCE'

  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    try {
      const alertsData = await getAlerts()
      setAlerts(alertsData)
      const catData = await getCategories()
      setCategories(catData)
    } catch (err) {
      setLocked(true)
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const handleCreate = async () => {
  if (needsThreshold && (!threshold || parseFloat(threshold) <= 0)) {
    alert('Threshold must be greater than zero')
    return
  }
  setSaving(true)
  
    try {
      await createAlert(
        type,
        selectedType.needsCategory ? categoryId : null,
        needsThreshold ? threshold : 0
      )
      setThreshold('')
      setCategoryId('')
      fetchData()
    } catch (err) {
      alert(err.response?.data || 'Failed to create alert')
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (id) => {
    try {
      await deleteAlert(id)
      fetchData()
    } catch (err) {
      console.error(err)
    }
  }

  const labelFor = (t) => ALERT_TYPES.find(a => a.value === t)?.label || t

  if (loading) return (
    <div className="min-h-screen bg-gray-950 flex items-center justify-center">
      <p className="text-gray-400">Loading...</p>
    </div>
  )

  if (locked) {
    return (
      <div className="min-h-screen bg-gray-950 text-white">
        <Navbar />
        <div className="max-w-2xl mx-auto px-8 py-16 text-center">
          <div className="text-yellow-400 text-5xl mb-4">🔒</div>
          <h1 className="text-2xl font-bold mb-2">Custom Alerts</h1>
          <p className="text-gray-400 mb-6">
            This is a Premium feature. Upgrade to set up personalized spending alerts.
          </p>
          <button onClick={() => navigate('/subscription')}
            className="bg-yellow-500 hover:bg-yellow-400 text-gray-900 font-semibold py-3 px-6 rounded-lg transition">
            Upgrade to Premium
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-950 text-white">
      <Navbar />
      <div className="max-w-3xl mx-auto px-8 py-8">
        <h1 className="text-2xl font-bold mb-6">Custom Alerts</h1>

        {/* Create form */}
        <div className="bg-gray-900 rounded-2xl p-6 mb-8">
          <h2 className="text-lg font-semibold mb-4">New Alert</h2>

          <label className="text-gray-400 text-sm mb-1 block">Type</label>
          <select value={type} onChange={(e) => setType(e.target.value)}
            className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 mb-1 outline-none focus:ring-2 focus:ring-blue-500">
            {ALERT_TYPES.map(t => (
              <option key={t.value} value={t.value}>{t.label}</option>
            ))}
          </select>
          <p className="text-gray-500 text-sm mb-4">{selectedType?.desc}</p>

          {selectedType?.needsCategory && (
            <>
              <label className="text-gray-400 text-sm mb-1 block">Category</label>
              <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)}
                className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 mb-4 outline-none focus:ring-2 focus:ring-blue-500">
                <option value="">Select a category</option>
                {categories.map(c => (
                  <option key={c.id} value={c.id}>{c.name}</option>
                ))}
              </select>
            </>
          )}

          {needsThreshold && (
            <>
              <label className="text-gray-400 text-sm mb-1 block">Threshold ({symbol})</label>
              <input type="number" min="0" step="0.01" value={threshold}
                onChange={(e) => {
                    const val = e.target.value
                    if (val === '' || parseFloat(val) >= 0) setThreshold(val)
                    }}
                    className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 mb-4 outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="0.00" />
            </>
          )}

          <button onClick={handleCreate} disabled={saving || (selectedType?.needsCategory && !categoryId)}
            className="bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2.5 px-5 rounded-lg transition disabled:opacity-50">
            {saving ? 'Creating...' : 'Create Alert'}
          </button>
        </div>

        {/* Existing alerts */}
        <div className="bg-gray-900 rounded-2xl p-6">
          <h2 className="text-lg font-semibold mb-4">Your Alerts</h2>
          {alerts.length === 0 ? (
            <p className="text-gray-500 text-sm">No alerts yet. Create one above.</p>
          ) : (
            <div className="space-y-3">
              {alerts.map(a => (
                <div key={a.id} className="flex justify-between items-center bg-gray-800 rounded-lg px-4 py-3">
                  <div>
                    <p className="font-medium">{labelFor(a.type)}</p>
                    <p className="text-gray-400 text-sm">
                      {a.category ? a.category.name + ' · ' : ''}
                      {a.type !== 'NEGATIVE_BALANCE' ? `${symbol}${a.threshold}` : 'No threshold'}
                    </p>
                  </div>
                  <button onClick={() => handleDelete(a.id)}
                    className="text-red-400 hover:text-red-300 text-sm">Delete</button>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}