import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar'
import { getInsights } from '../services/insightsService'
import { useAuth } from '../context/AuthContext'
import { getCurrencySymbol } from '../utils/currency'

export default function Insights() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [locked, setLocked] = useState(false)
  const { user } = useAuth()
  const navigate = useNavigate()
  const symbol = getCurrencySymbol(user?.currency)

  useEffect(() => {
    fetchInsights()
  }, [])

  const fetchInsights = async () => {
    try {
      const result = await getInsights()
      setData(result)
    } catch (err) {
      setLocked(true)
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const format = (str) => str ? str.charAt(0) + str.slice(1).toLowerCase() : ''

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
          <h1 className="text-2xl font-bold mb-2">Advanced Insights</h1>
          <p className="text-gray-400 mb-6">
            This is a Plus feature. Upgrade to unlock detailed insights about your spending patterns.
          </p>
          <button onClick={() => navigate('/subscription')}
            className="bg-yellow-500 hover:bg-yellow-400 text-gray-900 font-semibold py-3 px-6 rounded-lg transition">
            Upgrade to Plus
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-950 text-white">
      <Navbar />
      <div className="max-w-4xl mx-auto px-8 py-8">
        <h1 className="text-2xl font-bold mb-6">Advanced Insights</h1>

        <div className="grid grid-cols-2 gap-4">
          {/* Month over month */}
          <div className="bg-gray-900 rounded-2xl p-6">
            <p className="text-gray-400 text-sm mb-2">Month-over-Month</p>
            {data.monthOverMonthChange !== null ? (
              <>
                <p className={`text-3xl font-bold ${data.monthOverMonthChange > 0 ? 'text-red-400' : 'text-green-400'}`}>
                  {data.monthOverMonthChange > 0 ? '+' : ''}{data.monthOverMonthChange}%
                </p>
                <p className="text-gray-500 text-sm mt-1">
                  {data.monthOverMonthChange > 0 ? 'more' : 'less'} than last month
                </p>
              </>
            ) : (
              <p className="text-gray-500 text-sm">Not enough data yet</p>
            )}
          </div>

          {/* Savings rate */}
          <div className="bg-gray-900 rounded-2xl p-6">
            <p className="text-gray-400 text-sm mb-2">Savings Rate</p>
            {data.savingsRate !== null ? (
              <>
                <p className={`text-3xl font-bold ${data.savingsRate >= 0 ? 'text-green-400' : 'text-red-400'}`}>
                  {data.savingsRate}%
                </p>
                <p className="text-gray-500 text-sm mt-1">of your income this month</p>
              </>
            ) : (
              <p className="text-gray-500 text-sm">No income recorded this month</p>
            )}
          </div>

          {/* Daily average */}
          <div className="bg-gray-900 rounded-2xl p-6">
            <p className="text-gray-400 text-sm mb-2">Daily Average</p>
            <p className="text-3xl font-bold">{symbol}{data.dailyAverage.toLocaleString()}</p>
            <p className="text-gray-500 text-sm mt-1">spent per day this month</p>
          </div>

          {/* Most expensive day */}
          <div className="bg-gray-900 rounded-2xl p-6">
            <p className="text-gray-400 text-sm mb-2">Priciest Day</p>
            {data.mostExpensiveDay ? (
              <p className="text-3xl font-bold text-yellow-400">{format(data.mostExpensiveDay)}</p>
            ) : (
              <p className="text-gray-500 text-sm">Not enough data yet</p>
            )}
            <p className="text-gray-500 text-sm mt-1">your highest spending day</p>
          </div>

          {/* Top growing category */}
          <div className="bg-gray-900 rounded-2xl p-6 col-span-2">
            <p className="text-gray-400 text-sm mb-2">Fastest Growing Category</p>
            {data.topGrowingCategory ? (
              <p className="text-2xl font-bold text-red-400">{data.topGrowingCategory}</p>
            ) : (
              <p className="text-gray-500 text-sm">No significant increase this month</p>
            )}
            <p className="text-gray-500 text-sm mt-1">where your spending grew most vs last month</p>
          </div>
        </div>
      </div>
    </div>
  )
}