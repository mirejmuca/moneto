import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar'
import { getForecast } from '../services/forecastService'
import { useAuth } from '../context/AuthContext'
import { getCurrencySymbol } from '../utils/currency'
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, Legend } from 'recharts'
import PaywallLock from '../components/PaywallLock'

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

  const format = (str) => str ? str.charAt(0) + str.slice(1).toLowerCase() : ''

  if (loading) return (
    <div className="min-h-screen bg-gray-950 flex items-center justify-center">
      <p className="text-gray-400">Loading...</p>
    </div>
  )

if (locked) return (
<PaywallLock
  title="Expense Forecasting"
  description="This is a Plus feature. Upgrade to unlock expense predictions based on your spending history."
  tier="Plus"
/>
)

  // Ndërto të dhënat për grafikun — historiku (muajt e plotë).
  // Muajt me 0 (pa të dhëna, para se përdoruesi të kishte aktivitet) shfaqen si
  // boshllëk në grafik, jo si pikë te zero, që vija të nisë aty ku fillojnë të dhënat.
  const chartData = data.history.map((value, i) => ({
    month: data.labels[i],
    amount: value > 0 ? Math.round(value) : null
  }))

  // Pika lidhëse: vlera e fundit e historikut përsëritet si fillim i parashikimit
  // (që vija e parashikimit të nisë aty ku mbaron ajo reale)
  if (data.history.length > 0) {
    const lastValue = data.history[data.history.length - 1]
    if (lastValue > 0) {
      chartData[chartData.length - 1].forecast = Math.round(lastValue)
    }
  }

  // Shto pikat e parashikuara
  data.forecasts.forEach((value, i) => {
    const point = {
      month: data.forecastLabels[i],
      forecast: Math.round(value)
    }
    // Te muaji aktual (i pari i parashikimit), shto edhe shpenzimet aktuale deri tani
    if (i === 0 && data.currentActual != null) {
      point.actualSoFar = Math.round(data.currentActual)
    }
    chartData.push(point)
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
              <p className="text-gray-400 text-sm mb-1">{format(data.forecastLabels[i])}</p>
              <p className="text-2xl font-bold text-yellow-400">
                {currencySymbol}{Math.round(value).toLocaleString()}
              </p>
            </div>
          ))}
        </div>

        {/* This month so far vs forecast */}
        {data.currentActual != null && data.forecasts.length > 0 && (
          <div className="bg-gray-900 rounded-2xl p-5 mb-6 flex items-center justify-between">
            <div>
              <p className="text-gray-400 text-sm">{format(data.currentMonthLabel)} so far</p>
              <p className="text-xl font-bold text-green-400">
                {currencySymbol}{Math.round(data.currentActual).toLocaleString()}
              </p>
            </div>
            <div className="text-gray-600">of</div>
            <div className="text-right">
              <p className="text-gray-400 text-sm">Predicted for {format(data.currentMonthLabel)}</p>
              <p className="text-xl font-bold text-yellow-400">
                {currencySymbol}{Math.round(data.forecasts[0]).toLocaleString()}
              </p>
            </div>
          </div>
        )}

        {/* Chart */}
        <div className="bg-gray-900 rounded-2xl p-6">
          <h2 className="text-lg font-semibold mb-4">Spending Trend & Prediction</h2>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={chartData}>
              <XAxis dataKey="month" stroke="#6b7280" tick={{ fontSize: 12 }} />
              <YAxis stroke="#6b7280" tick={{ fontSize: 12 }} />
              <Tooltip />
              <Legend />
              <Line type="monotone" dataKey="amount" stroke="#3b82f6" strokeWidth={2} name="Actual" connectNulls />
              <Line type="monotone" dataKey="forecast" stroke="#eab308" strokeWidth={2} strokeDasharray="5 5" name="Forecast" connectNulls />
              <Line type="monotone" dataKey="actualSoFar" stroke="#10b981" strokeWidth={0} name="This month so far"
                dot={{ r: 6, fill: '#10b981' }} connectNulls />
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