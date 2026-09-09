import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar'
import { getInsights } from '../services/insightsService'
import { useAuth } from '../context/AuthContext'
import { getCurrencySymbol } from '../utils/currency'
import { downloadReport } from '../services/reportService'
import PaywallLock from '../components/PaywallLock'

export default function Insights() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [locked, setLocked] = useState(false)
  const [downloading, setDownloading] = useState(false)
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

  

  const handleDownload = async () => {
  setDownloading(true)
  try {
    await downloadReport()
  } catch (err) {
    console.error(err)
  } finally {
    setDownloading(false)
  }
}

if (locked) return (
<PaywallLock
  title="Advanced Insights"
  description="This is a Plus feature. Upgrade to unlock detailed insights about your spending patterns."
  tier="Plus"
/>
)

  return (
    <div className="min-h-screen bg-gray-950 text-white">
      <Navbar />
      <div className="max-w-4xl mx-auto px-8 py-8">
        <div className="flex justify-between items-center mb-6">
            <h1 className="text-2xl font-bold">Advanced Insights</h1>
            <button onClick={handleDownload} disabled={downloading}
                className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-semibold transition">
                {downloading ? 'Generating...' : '↓ Download PDF Report'}
            </button>
</div>

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
                <p className="text-gray-500 text-sm mt-1">of your income this month so far</p>
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