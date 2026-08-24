import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar'
import { getForecast } from '../services/forecastService'
import { useAuth } from '../context/AuthContext'
import { getCurrencySymbol } from '../utils/currency'
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, ReferenceLine } from 'recharts'

export default function Forecast() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [locked, setLocked] = useState(false)
  const { user } = useAuth()
  const navigate = useNavigate()
  const currencySymbol = getCurrencySymbol(user?.currency)

  useEffect(() => {
    fetchForecast()
  }, [])

  const fetchForecast = async () => {
  try {
    const result = await getForecast()
    setData(result)
  } catch (err) {
    setLocked(true)
    console.error(err)
  } finally {
    setLoading(false)
  }
}

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
          <h1 className="text-2xl font-bold mb-2">Expense Forecasting</h1>
          <p className="text-gray-400 mb-6">
            This is a Plus feature. Upgrade to unlock expense predictions based on your spending history.
          </p>
          <button onClick={() => navigate('/subscription')}
            className="bg-yellow-500 hover:bg-yellow-400 text-gray-900 font-semibold py-3 px-6 rounded-lg transition">
            Upgrade to Plus
          </button>
        </div>
      </div>
    )
  }

  // Ndërto të dhënat për grafikun
    // Ndërto të dhënat për grafikun
  const chartData = data.history.map((value, i) => ({
    month: data.labels[i],
    amount: Math.round(value)
  }))
  // Shto pikat e parashikuara
  data.forecasts.forEach((value, i) => {
    chartData.push({
      month: data.forecastLabels[i],
      forecast: Math.round(value)
    })
  })

  return (
    <div className="min-h-screen bg-gray-950 text-white">
      <Navbar />
      <div className="max-w-4xl mx-auto px-8 py-8">
        <h1 className="text-2xl font-bold mb-6">Expense Forecast</h1>

        {/* Forecast Cards */}
        <div className="grid grid-cols-3 gap-4 mb-6">
          {data.forecasts.map((value, i) => (
            <div key={i} className="bg-gray-900 rounded-2xl p-6">
              <p className="text-gray-400 text-sm mb-1">
                {data.forecastLabels[i].charAt(0) + data.forecastLabels[i].slice(1).toLowerCase()}
              </p>
              <p className="text-2xl font-bold text-yellow-400">
                {currencySymbol}{Math.round(value).toLocaleString()}
              </p>
            </div>
          ))}
        </div>

        {/* Chart */}
        <div className="bg-gray-900 rounded-2xl p-6">
          <h2 className="text-lg font-semibold mb-4">Spending Trend & Prediction</h2>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={chartData}>
              <XAxis dataKey="month" stroke="#6b7280" tick={{ fontSize: 12 }} />
              <YAxis stroke="#6b7280" tick={{ fontSize: 12 }} />
              <Tooltip />
              <Line type="monotone" dataKey="amount" stroke="#3b82f6" strokeWidth={2} name="Actual" connectNulls />
              <Line type="monotone" dataKey="forecast" stroke="#eab308" strokeWidth={2} strokeDasharray="5 5" name="Forecast" connectNulls />
            </LineChart>
          </ResponsiveContainer>
          <p className="text-gray-600 text-xs mt-3">
            Forecast accuracy decreases further into the future. Predictions are based on your recent spending trend.
          </p>
        </div>
      </div>
    </div>
  )
}