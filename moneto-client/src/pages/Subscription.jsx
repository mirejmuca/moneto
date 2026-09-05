import { useState, useEffect } from 'react'
import Navbar from '../components/Navbar'
import { getSubscriptionStatus, subscribe, cancelSubscription } from '../services/subscriptionService'

const PLANS = [
  {
    tier: 'FREE',
    name: 'Free',
    price: '$0',
    features: ['Transaction tracking', 'Budgets & categories', 'Saving goals', 'Basic analytics', 'Multi-currency support'],
    highlight: false
  },
  {
    tier: 'PLUS',
    name: 'Plus',
    price: '$2.99',
    features: ['Everything in Free', 'Expense forecasting', 'PDF export', 'Advanced insights'],
    highlight: false
  },
  {
    tier: 'PREMIUM',
    name: 'Premium',
    price: '$4.99',
    features: ['Everything in Plus', 'AI Financial Assistant', 'Custom alerts'],
    highlight: true
  }
]

export default function Subscription() {
  const [status, setStatus] = useState(null)
  const [loading, setLoading] = useState(true)
  const [selectedTier, setSelectedTier] = useState(null)
  const [processing, setProcessing] = useState(false)
  const [card, setCard] = useState({ number: '', name: '', expiry: '', cvc: '' })

  useEffect(() => {
    fetchStatus()
  }, [])

  const fetchStatus = async () => {
    try {
      const data = await getSubscriptionStatus()
      setStatus(data)
    } catch (err) {
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const handlePayment = async (e) => {
    e.preventDefault()
    setProcessing(true)
    try {
      await new Promise(resolve => setTimeout(resolve, 1500))
      // Nxirr 4 shifrat e fundit të kartës (vetëm shifrat, pastaj 4 të fundit)
      const digits = card.number.replace(/\D/g, '')
      const last4 = digits.slice(-4)
      await subscribe(selectedTier.tier, last4)
      setSelectedTier(null)
      setCard({ number: '', name: '', expiry: '', cvc: '' })
      fetchStatus()
    } catch (err) {
      console.error(err)
    } finally {
      setProcessing(false)
    }
  }

  const handleCancel = async () => {
    try {
      await cancelSubscription()
      fetchStatus()
    } catch (err) {
      console.error(err)
    }
  }

  if (loading) return (
    <div className="min-h-screen bg-gray-950 flex items-center justify-center">
      <p className="text-gray-400">Loading...</p>
    </div>
  )

  const currentTier = status?.tier || 'FREE'

  return (
    <div className="min-h-screen bg-gray-950 text-white">
      <Navbar />
      <div className="max-w-5xl mx-auto px-8 py-8">
        <h1 className="text-2xl font-bold mb-6">Subscription</h1>

        {/* Current Status */}
        <div className="bg-gray-900 rounded-2xl p-6 mb-8">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-400 text-sm">Current Plan</p>
              <p className={`text-2xl font-bold ${currentTier !== 'FREE' ? 'text-yellow-400' : 'text-gray-300'}`}>
                {currentTier.charAt(0) + currentTier.slice(1).toLowerCase()}
              </p>
              {status?.isActive && status?.expiresAt && (
                <p className="text-gray-500 text-sm mt-1">Renews on {status.expiresAt}</p>
              )}
            </div>
            {status?.isActive && (
              <button onClick={handleCancel}
                className="text-gray-500 hover:text-red-400 text-sm transition">
                Cancel subscription
              </button>
            )}
          </div>
        </div>

        {/* Plans */}
        <div className="grid grid-cols-3 gap-6">
          {PLANS.map(plan => (
            <div key={plan.tier}
              className={`bg-gray-900 rounded-2xl p-6 relative ${plan.highlight ? 'border-2 border-yellow-500' : ''}`}>
              {plan.highlight && (
                <span className="absolute -top-3 left-6 bg-yellow-500 text-gray-900 text-xs font-bold px-3 py-1 rounded-full">
                  RECOMMENDED
                </span>
              )}
              <h2 className={`text-lg font-semibold mb-2 ${plan.highlight ? 'text-yellow-400' : ''}`}>{plan.name}</h2>
              <p className="text-3xl font-bold mb-4">{plan.price}
                {plan.tier !== 'FREE' && <span className="text-gray-500 text-base font-normal">/month</span>}
              </p>
              <ul className="space-y-2 text-sm text-gray-400 mb-6">
                {plan.features.map((f, i) => (
                  <li key={i} className={plan.tier !== 'FREE' ? 'text-white' : ''}>✓ {f}</li>
                ))}
              </ul>
              {plan.tier === 'FREE' ? (
                <div className="text-center text-gray-600 text-sm py-3">
                  {currentTier === 'FREE' ? 'Current plan' : ''}
                </div>
              ) : currentTier === plan.tier ? (
                <button onClick={() => setSelectedTier(plan)}
                  className="w-full bg-gray-800 hover:bg-gray-700 text-white font-semibold py-3 rounded-lg transition">
                  Extend by 1 month
                </button>
              ) : (
                <button onClick={() => setSelectedTier(plan)}
                  className={`w-full font-semibold py-3 rounded-lg transition ${plan.highlight ? 'bg-yellow-500 hover:bg-yellow-400 text-gray-900' : 'bg-blue-600 hover:bg-blue-700 text-white'}`}>
                  Choose {plan.name}
                </button>
              )}
            </div>
          ))}
        </div>

        {/* Payment Modal */}
        {selectedTier && (
          <div className="fixed inset-0 bg-black bg-opacity-60 flex items-center justify-center p-4 z-50">
            <div className="bg-gray-900 rounded-2xl p-6 w-full max-w-md">
              <h2 className="text-lg font-semibold mb-1">Payment Details</h2>
              <p className="text-gray-400 text-sm mb-4">{selectedTier.name} — {selectedTier.price}/month</p>
              <form onSubmit={handlePayment} className="space-y-4">
                <div>
                  <label className="text-gray-400 text-sm mb-1 block">Card Number</label>
                  <input type="text" value={card.number}
                    onChange={(e) => setCard({ ...card, number: e.target.value })}
                    className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-yellow-500"
                    placeholder="4242 4242 4242 4242" required />
                </div>
                <div>
                  <label className="text-gray-400 text-sm mb-1 block">Cardholder Name</label>
                  <input type="text" value={card.name}
                    onChange={(e) => setCard({ ...card, name: e.target.value })}
                    className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-yellow-500"
                    placeholder="Name on card" required />
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="text-gray-400 text-sm mb-1 block">Expiry</label>
                    <input type="text" value={card.expiry}
                        onChange={(e) => {
                         let val = e.target.value.replace(/\D/g, '').slice(0, 4)
                         if (val.length >= 3) val = val.slice(0, 2) + '/' + val.slice(2)
                            setCard({ ...card, expiry: val })
                        }}
                    className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-yellow-500"
                    placeholder="MM/YY" maxLength="5" required />
                  </div>
                  <div>
                    <label className="text-gray-400 text-sm mb-1 block">CVC</label>
                    <input type="text" value={card.cvc}
                        onChange={(e) => setCard({ ...card, cvc: e.target.value.replace(/\D/g, '').slice(0, 3) })}
                        className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-yellow-500"
                        placeholder="123" maxLength="3" required />
                  </div>
                </div>
                <p className="text-gray-600 text-xs">This is a simulated payment for demonstration purposes.</p>
                <div className="flex gap-3">
                  <button type="submit" disabled={processing}
                    className="flex-1 bg-yellow-500 hover:bg-yellow-400 text-gray-900 font-semibold py-3 rounded-lg transition">
                    {processing ? 'Processing...' : `Pay ${selectedTier.price}`}
                  </button>
                  <button type="button" onClick={() => setSelectedTier(null)}
                    className="bg-gray-800 hover:bg-gray-700 text-white font-semibold py-3 px-6 rounded-lg transition">
                    Cancel
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}