import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { getMonthlySummary, getSpendingByCategory, getMonthlyTrend, getTopCategory } from '../services/analyticsService'
import { PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Legend } from 'recharts'
import Navbar from '../components/Navbar'
import { getCurrencySymbol } from '../utils/currency'
import api from '../services/api'


const COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#14b8a6', '#f97316']

export default function Dashboard() {
  const { user, logout } = useAuth()
  const [summary, setSummary] = useState(null)
  const [categoryData, setCategoryData] = useState([])
  const [trendData, setTrendData] = useState([])
  const [topCategory, setTopCategory] = useState(null)
  const [loading, setLoading] = useState(true)
  const [currencySymbol, setCurrencySymbol] = useState('$')

  useEffect(() => {
    const fetchData = async () => {
  try {
    const [s, c, t, top] = await Promise.all([
      getMonthlySummary(),
      getSpendingByCategory(),
      getMonthlyTrend(),
      getTopCategory()
    ])
    const profile = await api.get('/user/profile')
    setCurrencySymbol(getCurrencySymbol(profile.data.currency))
    setSummary(s)
    setCategoryData(Object.entries(c).map(([name, value]) => ({ name, value })))
    setTrendData(t)
    setTopCategory(top)
  } catch (err) {
    console.error(err)
  } finally {
    setLoading(false)
  }
}
    fetchData()
  }, [])

  
  if (loading) return (
    <div className="min-h-screen bg-gray-950 flex items-center justify-center">
      <p className="text-gray-400">Loading...</p>
    </div>
  )

  return (
    <div className="min-h-screen bg-gray-950 text-white">
      <Navbar />
      <div className="max-w-7xl mx-auto px-8 py-8">
        {/* Summary Cards */}
        <div className="grid grid-cols-4 gap-4 mb-8">
          <div className="bg-gray-900 rounded-2xl p-6">
            <p className="text-gray-400 text-sm mb-1">Total Income</p>
            <p className="text-2xl font-bold text-green-400">{currencySymbol}{summary?.totalIncome ?? 0}</p>
          </div>
          <div className="bg-gray-900 rounded-2xl p-6">
            <p className="text-gray-400 text-sm mb-1">Total Expenses</p>
            <p className="text-2xl font-bold text-red-400">{currencySymbol}{summary?.totalExpenses ?? 0}</p>
          </div>
          <div className="bg-gray-900 rounded-2xl p-6">
            <p className="text-gray-400 text-sm mb-1">Net Balance</p>
            <p className="text-2xl font-bold text-blue-400">{currencySymbol}{summary?.netBalance ?? 0}</p>
          </div>
          <div className="bg-gray-900 rounded-2xl p-6">
            <p className="text-gray-400 text-sm mb-1">Total Saved</p>
            <p className="text-2xl font-bold text-purple-400">{currencySymbol}{summary?.totalSaved ?? 0}</p>
          </div>
        </div>

        {/* Top Category */}
        {topCategory?.category && (
          <div className="bg-gray-900 rounded-2xl p-4 mb-8 flex items-center gap-3">
            <span className="text-yellow-400 text-lg">🏆</span>
            <p className="text-gray-300 text-sm">
              Top spend this month: <span className="text-white font-semibold">{topCategory.category}</span>
              {' '}— <span className="text-red-400">${topCategory.amount}</span>
            </p>
          </div>
        )}

        {/* Charts */}
        <div className="grid grid-cols-2 gap-6">
          {/* Pie Chart */}
          <div className="bg-gray-900 rounded-2xl p-6">
            <h2 className="text-lg font-semibold mb-4">Spending by Category</h2>
            {categoryData.length > 0 ? (
              <ResponsiveContainer width="100%" height={300}>
                <PieChart>
                  <Pie data={categoryData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={100} label>
                    {categoryData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                  </Pie>
                  <Tooltip />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <p className="text-gray-500 text-sm">No expenses this month</p>
            )}
          </div>

          {/* Bar Chart */}
          <div className="bg-gray-900 rounded-2xl p-6">
            <h2 className="text-lg font-semibold mb-4">Income vs Expenses</h2>
            {trendData.length > 0 ? (
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={trendData}>
                  <XAxis dataKey="month" stroke="#6b7280" tick={{ fontSize: 12 }} />
                  <YAxis stroke="#6b7280" tick={{ fontSize: 12 }} />
                  <Tooltip />
                  <Legend />
                  <Bar dataKey="income" fill="#10b981" radius={[4, 4, 0, 0]} />
                  <Bar dataKey="expenses" fill="#ef4444" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <p className="text-gray-500 text-sm">No data yet</p>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}