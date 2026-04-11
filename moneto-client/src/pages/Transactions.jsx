import { useState, useEffect } from 'react'
import { getTransactions, createTransaction, deleteTransaction } from '../services/transactionService'
import { getCategories } from '../services/categoryService'
import Navbar from '../components/Navbar'
import { getCurrencySymbol } from '../utils/currency'

export default function Transactions() {
  const [transactions, setTransactions] = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({
    categoryId: '',
    amount: '',
    type: 'EXPENSE',
    description: '',
    date: new Date().toISOString().split('T')[0],
    isRecurring: false,
    currency: 'USD'
  })

  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    try {
      const [t, c] = await Promise.all([getTransactions(), getCategories()])
      setTransactions(t)
      setCategories(c)
    } catch (err) {
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const handleChange = (e) => {
    const value = e.target.type === 'checkbox' ? e.target.checked : e.target.value
    setForm({ ...form, [e.target.name]: value })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      await createTransaction(form)
      setShowForm(false)
      setForm({
        categoryId: '',
        amount: '',
        type: 'EXPENSE',
        description: '',
        date: new Date().toISOString().split('T')[0],
        isRecurring: false
      })
      fetchData()
    } catch (err) {
      console.error(err)
    }
  }

  const handleDelete = async (id) => {
    try {
      await deleteTransaction(id)
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
          <h1 className="text-2xl font-bold">Transactions</h1>
          <button
            onClick={() => setShowForm(!showForm)}
            className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-semibold transition">
            {showForm ? 'Cancel' : '+ Add Transaction'}
          </button>
        </div>

        {/* Add Transaction Form */}
        {showForm && (
          <div className="bg-gray-900 rounded-2xl p-6 mb-6">
            <h2 className="text-lg font-semibold mb-4">New Transaction</h2>
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
                <label className="text-gray-400 text-sm mb-1 block">Currency</label>
                <select name="currency" value={form.currency} onChange={handleChange}
                  className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500">
                  <option value="USD">USD</option>
                  <option value="EUR">EUR</option>
                  <option value="GBP">GBP</option>
                  <option value="ALL">ALL</option>
                  <option value="CHF">CHF</option>
                  <option value="CAD">CAD</option>
                  <option value="AUD">AUD</option>
                  <option value="JPY">JPY</option>
                </select>
              </div>
              <div>
                <label className="text-gray-400 text-sm mb-1 block">Date</label>
                <input type="date" name="date" value={form.date} onChange={handleChange}
                  className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"
                  required />
              </div>
              <div className="col-span-2">
                <label className="text-gray-400 text-sm mb-1 block">Description</label>
                <input type="text" name="description" value={form.description} onChange={handleChange}
                  className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Optional description" />
              </div>
              <div className="col-span-2 flex items-center gap-2">
                <input type="checkbox" name="isRecurring" checked={form.isRecurring} onChange={handleChange}
                  className="w-4 h-4" />
                <label className="text-gray-400 text-sm">Mark as recurring</label>
              </div>
              <div className="col-span-2">
                <button type="submit"
                  className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 rounded-lg transition">
                  Add Transaction
                </button>
              </div>
            </form>
          </div>
        )}

        {/* Transactions List */}
        <div className="bg-gray-900 rounded-2xl overflow-hidden">
          {transactions.length === 0 ? (
            <p className="text-gray-500 text-sm p-6">No transactions yet.</p>
          ) : (
            <table className="w-full">
              <thead className="border-b border-gray-800">
                <tr>
                  <th className="text-left text-gray-400 text-sm px-6 py-4">Date</th>
                  <th className="text-left text-gray-400 text-sm px-6 py-4">Description</th>
                  <th className="text-left text-gray-400 text-sm px-6 py-4">Category</th>
                  <th className="text-left text-gray-400 text-sm px-6 py-4">Type</th>
                  <th className="text-right text-gray-400 text-sm px-6 py-4">Amount</th>
                  <th className="text-right text-gray-400 text-sm px-6 py-4"></th>
                </tr>
              </thead>
              <tbody>
                {transactions.map(t => (
                  <tr key={t.id} className="border-b border-gray-800 hover:bg-gray-800 transition">
                    <td className="px-6 py-4 text-sm text-gray-300">{t.date}</td>
                    <td className="px-6 py-4 text-sm text-gray-300">{t.description || '—'}</td>
                    <td className="px-6 py-4 text-sm text-gray-300">{t.category?.name}</td>
                    <td className="px-6 py-4">
                      <span className={`text-xs font-semibold px-2 py-1 rounded-full ${t.type === 'INCOME' ? 'bg-green-900 text-green-400' : 'bg-red-900 text-red-400'}`}>
                        {t.type}
                      </span>
                    </td>
                    <td className={`px-6 py-4 text-sm font-semibold text-right ${t.type === 'INCOME' ? 'text-green-400' : 'text-red-400'}`}>
                      {t.type === 'INCOME' ? '+' : '-'}{getCurrencySymbol(t.currency)}{t.amount}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <button onClick={() => handleDelete(t.id)}
                        className="text-gray-500 hover:text-red-400 text-sm transition">
                        Delete
                      </button>
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