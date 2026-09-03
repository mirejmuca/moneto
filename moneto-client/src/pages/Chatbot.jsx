import { useState, useRef, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar'
import { askChatbot } from '../services/chatbotService'

export default function Chatbot() {
  const [messages, setMessages] = useState([
    { role: 'bot', text: "Hi! I'm your financial assistant. Ask me anything about your spending, budgets, or goals." }
  ])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const [locked, setLocked] = useState(false)
  const navigate = useNavigate()
  const bottomRef = useRef(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, loading])

  const handleSend = async () => {
    const question = input.trim()
    if (!question || loading) return

    setMessages(prev => [...prev, { role: 'user', text: question }])
    setInput('')
    setLoading(true)

    try {
      const answer = await askChatbot(question)
      setMessages(prev => [...prev, { role: 'bot', text: answer }])
    } catch (err) {
      if (err.response?.status === 403) {
        setLocked(true)
      } else {
        setMessages(prev => [...prev, { role: 'bot', text: "Sorry, something went wrong. Please try again." }])
      }
    } finally {
      setLoading(false)
    }
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  if (locked) {
    return (
      <div className="min-h-screen bg-gray-950 text-white">
        <Navbar />
        <div className="max-w-2xl mx-auto px-8 py-16 text-center">
          <div className="text-yellow-400 text-5xl mb-4">🔒</div>
          <h1 className="text-2xl font-bold mb-2">AI Financial Assistant</h1>
          <p className="text-gray-400 mb-6">
            This is a Premium feature. Upgrade to chat with your personal financial assistant.
          </p>
          <button onClick={() => navigate('/subscription')}
            className="bg-yellow-500 hover:bg-yellow-400 text-gray-900 font-semibold py-3 px-6 rounded-lg transition">
            Upgrade to Premium
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-950 text-white flex flex-col">
      <Navbar />
      <div className="max-w-3xl w-full mx-auto px-8 py-6 flex-1 flex flex-col">
        <h1 className="text-2xl font-bold mb-4">AI Financial Assistant</h1>

        {/* Messages */}
        <div className="flex-1 bg-gray-900 rounded-2xl p-6 overflow-y-auto mb-4 space-y-4" style={{ maxHeight: '60vh' }}>
          {messages.map((m, i) => (
            <div key={i} className={`flex ${m.role === 'user' ? 'justify-end' : 'justify-start'}`}>
              <div className={`max-w-[75%] rounded-2xl px-4 py-3 ${
                m.role === 'user' ? 'bg-blue-600 text-white' : 'bg-gray-800 text-gray-100'
              }`}>
                <p className="text-sm whitespace-pre-wrap">{m.text}</p>
              </div>
            </div>
          ))}
          {loading && (
            <div className="flex justify-start">
              <div className="bg-gray-800 text-gray-400 rounded-2xl px-4 py-3 text-sm">
                Thinking...
              </div>
            </div>
          )}
          <div ref={bottomRef} />
        </div>

        {/* Input */}
        <div className="flex gap-3">
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Ask about your finances..."
            className="flex-1 bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500"
          />
          <button onClick={handleSend} disabled={loading || !input.trim()}
            className="bg-blue-600 hover:bg-blue-700 text-white font-semibold px-6 rounded-lg transition disabled:opacity-50">
            Send
          </button>
        </div>
      </div>
    </div>
  )
}