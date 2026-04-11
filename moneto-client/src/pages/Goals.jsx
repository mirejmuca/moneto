import { useState, useEffect } from 'react'
import Navbar from '../components/Navbar'
import { getGoals, createGoal, addToGoal, cancelGoal } from '../services/goalService'
import { useAuth } from '../context/AuthContext'
import { getCurrencySymbol } from '../utils/currency'


export default function Goals() {
  const [goals, setGoals] = useState([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [addingTo, setAddingTo] = useState(null)
  const [addAmount, setAddAmount] = useState('')
const { user } = useAuth()
const currencySymbol = getCurrencySymbol(user?.currency)
  const [form, setForm] = useState({
    name: '',
    targetAmount: '',
    deadline: ''
  })

  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    try {
      const data = await getGoals()
      setGoals(data)
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
      await createGoal(form)
      setShowForm(false)
      setForm({ name: '', targetAmount: '', deadline: '' })
      fetchData()
    } catch (err) {
      console.error(err)
    }
  }

  const handleAddAmount = async (id) => {
    try {
      await addToGoal(id, addAmount)
      setAddingTo(null)
      setAddAmount('')
      fetchData()
    } catch (err) {
      console.error(err)
    }
  }

  const handleCancel = async (id) => {
    try {
      await cancelGoal(id)
      fetchData()
    } catch (err) {
      console.error(err)
    }
  }

  const getPercentage = (current, target) => {
    return Math.min((current / target) * 100, 100).toFixed(1)
  }

  const getStatusColor = (status) => {
    switch (status) {
      case 'COMPLETED': return 'text-green-400'
      case 'CANCELLED': return 'text-red-400'
      default: return 'text-blue-400'
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
          <h1 className="text-2xl font-bold">Saving Goals</h1>
          <button
            onClick={() => setShowForm(!showForm)}
            className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-semibold transition">
            {showForm ? 'Cancel' : '+ New Goal'}
          </button>
        </div>

        {/* Create Goal Form */}
        {showForm && (
          <div className="bg-gray-900 rounded-2xl p-6 mb-6">
            <h2 className="text-lg font-semibold mb-4">New Saving Goal</h2>
            <form onSubmit={handleSubmit} className="grid grid-cols-2 gap-4">
              <div className="col-span-2">
                <label className="text-gray-400 text-sm mb-1 block">Goal Name</label>
                <input type="text" name="name" value={form.name} onChange={handleChange}
                  className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="e.g. New Laptop" required />
              </div>
              <div>
                <label className="text-gray-400 text-sm mb-1 block">Target Amount</label>
                <input type="number" name="targetAmount" value={form.targetAmount} onChange={handleChange}
                  className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="0.00" min="0.01" step="0.01" required />
              </div>
              <div>
                <label className="text-gray-400 text-sm mb-1 block">Deadline</label>
                <input type="date" name="deadline" value={form.deadline} onChange={handleChange}
                  className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"
                  min={new Date().toISOString().split('T')[0]} required />
              </div>
              <div className="col-span-2">
                <button type="submit"
                  className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 rounded-lg transition">
                  Create Goal
                </button>
              </div>
            </form>
          </div>
        )}

        {/* Goals List */}
        <div className="space-y-4">
          {goals.length === 0 ? (
            <div className="bg-gray-900 rounded-2xl p-6">
              <p className="text-gray-500 text-sm">No saving goals yet.</p>
            </div>
          ) : (
            goals.map(goal => (
              <div key={goal.id} className="bg-gray-900 rounded-2xl p-6">
                <div className="flex justify-between items-start mb-3">
                  <div>
                    <h3 className="font-semibold text-lg">{goal.name}</h3>
                    <p className="text-gray-500 text-sm">Deadline: {goal.deadline}</p>
                  </div>
                  <div className="flex items-center gap-3">
                    <span className={`text-xs font-semibold ${getStatusColor(goal.status)}`}>
                      {goal.status}
                    </span>
                    {goal.status === 'IN_PROGRESS' && (
                      <>
                        <button onClick={() => setAddingTo(goal.id)}
                          className="text-blue-400 hover:text-blue-300 text-sm transition">
                          + Add
                        </button>
                        <button onClick={() => handleCancel(goal.id)}
                          className="text-gray-500 hover:text-red-400 text-sm transition">
                          Cancel
                        </button>
                      </>
                    )}
                  </div>
                </div>

                {/* Progress Bar */}
                <div className="w-full bg-gray-800 rounded-full h-3 mb-2">
                  <div
                    className={`h-3 rounded-full transition-all ${goal.status === 'COMPLETED' ? 'bg-green-500' : 'bg-blue-500'}`}
                    style={{ width: `${getPercentage(goal.currentAmount, goal.targetAmount)}%` }}>
                  </div>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-400 text-sm">{currencySymbol}{goal.currentAmount} saved</span>
                  <span className="text-gray-400 text-sm">{currencySymbol}{goal.targetAmount} goal — 
                    {getPercentage(goal.currentAmount, goal.targetAmount)}%</span>
                </div>

                {/* Add Amount Input */}
                {addingTo === goal.id && (
                  <div className="mt-4 flex gap-3">
                    <input type="number" value={addAmount} onChange={(e) => setAddAmount(e.target.value)}
                      className="flex-1 bg-gray-800 text-white rounded-lg px-4 py-2 outline-none focus:ring-2 focus:ring-blue-500"
                      placeholder="Amount to add" />
                    <button onClick={() => handleAddAmount(goal.id)}
                      className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-semibold transition">
                      Add
                    </button>
                    <button onClick={() => setAddingTo(null)}
                      className="text-gray-400 hover:text-white text-sm transition">
                      Cancel
                    </button>
                  </div>
                )}
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  )
}