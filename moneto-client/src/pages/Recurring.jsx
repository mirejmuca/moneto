import { useState, useEffect } from 'react'
import Navbar from '../components/Navbar'
import { getRecurring, createRecurring, cancelRecurring } from '../services/recurringService'
import { getCategories } from '../services/categoryService'
import { useAuth } from '../context/AuthContext'
import { getCurrencySymbol } from '../utils/currency'

export default function Recurring() {
  const [recurring, setRecurring] = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const { user } = useAuth()
  const currencySymbol = getCurrencySymbol(user?.currency)
  const [form, setForm] = useState({
    categoryId: '',
    amount: '',
    type: 'EXPENSE',
    description: '',
    frequency: 'MONTHLY',
    startDate: new Date().toISOString().split('T')[0]
  })

  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    try {
      const [r, c] = await Promise.all([getRecurring(), getCategories()])
      setRecurring(r)
      setCategories(c)
    } catch (err) {
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      await createRecurring(form)
      setShowForm(false)
      setForm({
        categoryId: '',
        amount: '',
        type: 'EXPENSE',
        description: '',
        frequency: 'MONTHLY',
        startDate: new Date().toISOString().split('T')[0]
      })
      fetchData()
    } catch (err) {
      console.error(err)
    }
  }

  const handleCancel = async (id) => {
    try {
      await cancelRecurring(id)
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

  return (
    <div className="min-h-screen bg-gray-950 text-white">
      <Navbar />
      <div className="max-w-5xl mx-auto px-8 py-8">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-2xl font-bold">Recurring Transactions</h1>
          <button
            onClick={() => setShowForm(!showForm)}
            className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-semibold transition">
            {showForm ? 'Cancel' : '+ New Recurring'}
          </button>
        </div>

        {/* Create Recurring Form */}
        {showForm && (
          <div className="bg-gray-900 rounded-2xl p-6 mb-6">
            <h2 className="text-lg font-semibold mb-4">New Recurring Transaction</h2>
            <form onSubmit={handleSubmit} className="grid grid-cols-2 gap-4">
              <div>
                <label className="text-gray-400 text-sm mb-1 block">Type</label>
                <select name="type" value={form.type} onChange={handleChange}
                  className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500">
                  <option value="EXPENSE">Expense</option>
                  <option value="INCOME">Income</option>
                </select>
              </div>
              <div>
                <label className="text-gray-400 text-sm mb-1 block">Category</label>
                <select name="categoryId" value={form.categoryId} onChange={handleChange}
                  className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"
                  required>
                  <option value="">Select category</option>
                  {categories
                    .filter(c => c.type === form.type)
                    .map(c => (
                      <option key={c.id} value={c.id}>{c.name}</option>
                    ))}
                </select>
              </div>
              <div>
                <label className="text-gray-400 text-sm mb-1 block">Amount</label>
                <input type="number" name="amount" value={form.amount} onChange={handleChange}
                  className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="0.00" min="0.01" step="0.01" required />
              </div>
              <div>
                <label className="text-gray-400 text-sm mb-1 block">Frequency</label>
                <select name="frequency" value={form.frequency} onChange={handleChange}
                  className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500">
                  <option value="WEEKLY">Weekly</option>
                  <option value="MONTHLY">Monthly</option>
                  <option value="YEARLY">Yearly</option>
                </select>
              </div>
              <div>
                <label className="text-gray-400 text-sm mb-1 block">Start Date</label>
                <input type="date" name="startDate" value={form.startDate} onChange={handleChange}
                  className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"
                  min={new Date().toISOString().split('T')[0]}
                  required />
              </div>
              <div>
                <label className="text-gray-400 text-sm mb-1 block">Description</label>
                <input type="text" name="description" value={form.description} onChange={handleChange}
                  className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="e.g. Netflix subscription" />
              </div>
              <div className="col-span-2">
                <button type="submit"
                  className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 rounded-lg transition">
                  Create Recurring
                </button>
              </div>
            </form>
          </div>
        )}

        {/* Recurring List */}
        <div className="bg-gray-900 rounded-2xl overflow-hidden">
          {recurring.length === 0 ? (
            <p className="text-gray-500 text-sm p-6">No recurring transactions yet.</p>
          ) : (
            <table className="w-full">
              <thead className="border-b border-gray-800">
                <tr>
                  <th className="text-left text-gray-400 text-sm px-6 py-4">Description</th>
                  <th className="text-left text-gray-400 text-sm px-6 py-4">Category</th>
                  <th className="text-left text-gray-400 text-sm px-6 py-4">Frequency</th>
                  <th className="text-left text-gray-400 text-sm px-6 py-4">Next Due</th>
                  <th className="text-left text-gray-400 text-sm px-6 py-4">Status</th>
                  <th className="text-right text-gray-400 text-sm px-6 py-4">Amount</th>
                  <th className="text-right text-gray-400 text-sm px-6 py-4"></th>
                </tr>
              </thead>
              <tbody>
                {recurring.map(r => (
                  <tr key={r.id} className="border-b border-gray-800 hover:bg-gray-800 transition">
                    <td className="px-6 py-4 text-sm text-gray-300">{r.description || '—'}</td>
                    <td className="px-6 py-4 text-sm text-gray-300">{r.category?.name}</td>
                    <td className="px-6 py-4 text-sm text-gray-300">{r.frequency}</td>
                    <td className="px-6 py-4 text-sm text-gray-300">{r.nextDueDate}</td>
                    <td className="px-6 py-4">
                      <span className={`text-xs font-semibold px-2 py-1 rounded-full ${r.isActive ? 'bg-green-900 text-green-400' : 'bg-gray-800 text-gray-500'}`}>
                        {r.isActive ? 'Active' : 'Cancelled'}
                      </span>
                    </td>
                    <td className={`px-6 py-4 text-sm font-semibold text-right ${r.type === 'INCOME' ? 'text-green-400' : 'text-red-400'}`}>
                      {r.type === 'INCOME' ? '+' : '-'}{currencySymbol}{r.amount}
                    </td>
                    <td className="px-6 py-4 text-right">
                      {r.isActive && (
                        <button onClick={() => handleCancel(r.id)}
                          className="text-gray-500 hover:text-red-400 text-sm transition">
                          Cancel
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  )
}