import { useState, useEffect } from 'react'
import Navbar from '../components/Navbar'
import { getCategories, createCategory, deleteCategory } from '../services/categoryService'

export default function Categories() {
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({
    name: '',
    color: '#3b82f6',
    icon: '',
    type: 'EXPENSE'
  })

  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    try {
      const data = await getCategories()
      setCategories(data)
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
      await createCategory(form)
      setShowForm(false)
      setForm({ name: '', color: '#3b82f6', icon: '', type: 'EXPENSE' })
      fetchData()
    } catch (err) {
      console.error(err)
    }
  }

  const handleDelete = async (id) => {
    try {
      await deleteCategory(id)
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

  const expenses = categories.filter(c => c.type === 'EXPENSE')
  const income = categories.filter(c => c.type === 'INCOME')

  return (
    <div className="min-h-screen bg-gray-950 text-white">
      <Navbar />
      <div className="max-w-4xl mx-auto px-8 py-8">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-2xl font-bold">Categories</h1>
          <button
            onClick={() => setShowForm(!showForm)}
            className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-semibold transition">
            {showForm ? 'Cancel' : '+ New Category'}
          </button>
        </div>

        {/* Create Category Form */}
        {showForm && (
          <div className="bg-gray-900 rounded-2xl p-6 mb-6">
            <h2 className="text-lg font-semibold mb-4">New Category</h2>
            <form onSubmit={handleSubmit} className="grid grid-cols-2 gap-4">
              <div>
                <label className="text-gray-400 text-sm mb-1 block">Name</label>
                <input type="text" name="name" value={form.name} onChange={handleChange}
                  className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="e.g. Groceries" required />
              </div>
              <div>
                <label className="text-gray-400 text-sm mb-1 block">Type</label>
                <select name="type" value={form.type} onChange={handleChange}
                  className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500">
                  <option value="EXPENSE">Expense</option>
                  <option value="INCOME">Income</option>
                </select>
              </div>
              <div>
                <label className="text-gray-400 text-sm mb-1 block">Color</label>
                <div className="flex items-center gap-3">
                  <input type="color" name="color" value={form.color} onChange={handleChange}
                    className="w-12 h-12 rounded-lg cursor-pointer bg-gray-800 border-0" />
                  <span className="text-gray-400 text-sm">{form.color}</span>
                </div>
              </div>
              <div>
                <label className="text-gray-400 text-sm mb-1 block">Icon (optional)</label>
                <input type="text" name="icon" value={form.icon} onChange={handleChange}
                  className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="e.g. food" />
              </div>
              <div className="col-span-2">
                <button type="submit"
                  className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 rounded-lg transition">
                  Create Category
                </button>
              </div>
            </form>
          </div>
        )}

        {/* Expense Categories */}
        <div className="mb-6">
          <h2 className="text-lg font-semibold mb-3 text-red-400">Expense Categories</h2>
          <div className="grid grid-cols-2 gap-3">
            {expenses.length === 0 ? (
              <p className="text-gray-500 text-sm col-span-2">No expense categories yet.</p>
            ) : (
              expenses.map(c => (
                <div key={c.id} className="bg-gray-900 rounded-xl p-4 flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className="w-4 h-4 rounded-full" style={{ backgroundColor: c.color }}></div>
                    <span className="text-sm font-semibold">{c.name}</span>
                  </div>
                  <button onClick={() => handleDelete(c.id)}
                    className="text-gray-500 hover:text-red-400 text-sm transition">
                    Delete
                  </button>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Income Categories */}
        <div>
          <h2 className="text-lg font-semibold mb-3 text-green-400">Income Categories</h2>
          <div className="grid grid-cols-2 gap-3">
            {income.length === 0 ? (
              <p className="text-gray-500 text-sm col-span-2">No income categories yet.</p>
            ) : (
              income.map(c => (
                <div key={c.id} className="bg-gray-900 rounded-xl p-4 flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className="w-4 h-4 rounded-full" style={{ backgroundColor: c.color }}></div>
                    <span className="text-sm font-semibold">{c.name}</span>
                  </div>
                  <button onClick={() => handleDelete(c.id)}
                    className="text-gray-500 hover:text-red-400 text-sm transition">
                    Delete
                  </button>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  )
}