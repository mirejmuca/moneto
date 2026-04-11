import { useState, useEffect } from 'react'
import Navbar from '../components/Navbar'
import { getBudgets, getBudgetStatus, createBudget, deleteBudget } from '../services/budgetService'
import { getCategories } from '../services/categoryService'
import { useAuth } from '../context/AuthContext'
import { getCurrencySymbol } from '../utils/currency'

export default function Budgets() {
  const [budgetStatus, setBudgetStatus] = useState({})
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const { user } = useAuth()
  const currencySymbol = getCurrencySymbol(user?.currency)
  const [form, setForm] = useState({
    categoryId: '',
    amount: '',
    month: new Date().getMonth() + 1,
    year: new Date().getFullYear()
  })

  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    try {
      const [status, cats] = await Promise.all([
        getBudgetStatus(),
        getCategories()
      ])
      setBudgetStatus(status)
      setCategories(cats.filter(c => c.type === 'EXPENSE'))
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
      await createBudget(form)
      setShowForm(false)
      setForm({
        categoryId: '',
        amount: '',
        month: new Date().getMonth() + 1,
        year: new Date().getFullYear()
      })
      fetchData()
    } catch (err) {
      console.error(err)
    }
  }

  const handleDelete = async (id) => {
    try {
      await deleteBudget(id)
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
      <div className="max-w-4xl mx-auto px-8 py-8">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-2xl font-bold">Budgets</h1>
          <button
            onClick={() => setShowForm(!showForm)}
            className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-semibold transition">
            {showForm ? 'Cancel' : '+ Add Budget'}
          </button>
        </div>

        {/* Add Budget Form */}
        {showForm && (
          <div className="bg-gray-900 rounded-2xl p-6 mb-6">
            <h2 className="text-lg font-semibold mb-4">New Budget</h2>
            <form onSubmit={handleSubmit} className="grid grid-cols-2 gap-4">
              <div>
                <label className="text-gray-400 text-sm mb-1 block">Category</label>
                <select name="categoryId" value={form.categoryId} onChange={handleChange}
                  className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"
                  required>
                  <option value="">Select category</option>
                  {categories.map(c => (
                    <option key={c.id} value={c.id}>{c.name}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="text-gray-400 text-sm mb-1 block">Amount</label>
                <input type="number" name="amount" value={form.amount} onChange={handleChange}
                  className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="0.00" min="0.01" step="0.01"  required />
              </div>
              <div>
                <label className="text-gray-400 text-sm mb-1 block">Month</label>
                <select name="month" value={form.month} onChange={handleChange}
                  className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500">
                  {Array.from({ length: 12 }, (_, i) => (
                    <option key={i + 1} value={i + 1}>
                      {new Date(0, i).toLocaleString('default', { month: 'long' })}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="text-gray-400 text-sm mb-1 block">Year</label>
                <input type="number" name="year" value={form.year} onChange={handleChange}
                  className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div className="col-span-2">
                <button type="submit"
                  className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 rounded-lg transition">
                  Add Budget
                </button>
              </div>
            </form>
          </div>
        )}

        {/* Budget Status List */}
        <div className="space-y-4">
          {Object.keys(budgetStatus).length === 0 ? (
            <div className="bg-gray-900 rounded-2xl p-6">
              <p className="text-gray-500 text-sm">No budgets set for this month.</p>
            </div>
          ) : (
            Object.entries(budgetStatus).map(([category, data]) => (
              <div key={category} className="bg-gray-900 rounded-2xl p-6">
                <div className="flex justify-between items-center mb-3">
                  <h3 className="font-semibold">{category}</h3>
                  <div className="flex items-center gap-4">
                    <span className={`text-sm font-semibold ${data.exceeded ? 'text-red-400' : 'text-gray-400'}`}>
                      {currencySymbol}{data.spent} / {currencySymbol}{data.budgetAmount}
                    </span>
                    <button onClick={() => handleDelete(data.budgetId)}
                      className="text-gray-500 hover:text-red-400 text-sm transition">
                      Delete
                    </button>
                  </div>
                </div>
                <div className="w-full bg-gray-800 rounded-full h-2">
                  <div
                    className={`h-2 rounded-full transition-all ${data.exceeded ? 'bg-red-500' : data.percentage > 75 ? 'bg-yellow-500' : 'bg-blue-500'}`}
                    style={{ width: `${Math.min(data.percentage, 100)}%` }}>
                  </div>
                </div>
                <div className="flex justify-between mt-2">
                  <span className="text-gray-500 text-xs">{data.percentage.toFixed(1)}% used</span>
                  {data.exceeded && <span className="text-red-400 text-xs font-semibold">Budget exceeded!</span>}
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  )
}