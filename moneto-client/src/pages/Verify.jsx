import { useState, useEffect } from 'react'
import { useSearchParams, Link } from 'react-router-dom'
import axios from 'axios'

export default function Verify() {
  const [searchParams] = useSearchParams()
  const [status, setStatus] = useState('verifying')

  useEffect(() => {
    const token = searchParams.get('token')
    if (!token) {
      setStatus('error')
      return
    }

    const verifyEmail = async () => {
      try {
        await axios.get(`http://localhost:8080/api/auth/verify?token=${token}`)
        setStatus('success')
      } catch (err) {
        setStatus('error')
      }
    }
    verifyEmail()
  }, [searchParams])

  return (
    <div className="min-h-screen bg-gray-950 flex items-center justify-center">
      <div className="bg-gray-900 p-8 rounded-2xl w-full max-w-md shadow-lg text-center">
        {status === 'verifying' && (
          <>
            <h1 className="text-2xl font-bold text-white mb-2">Verifying...</h1>
            <p className="text-gray-400">Please wait while we verify your email.</p>
          </>
        )}
        {status === 'success' && (
          <>
            <div className="text-green-400 text-5xl mb-4">✓</div>
            <h1 className="text-2xl font-bold text-white mb-2">Email Verified!</h1>
            <p className="text-gray-400 mb-6">Your account has been verified successfully.</p>
            <Link to="/login" className="inline-block bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 px-6 rounded-lg transition">
              Go to Login
            </Link>
          </>
        )}
        {status === 'error' && (
          <>
            <div className="text-red-400 text-5xl mb-4">✕</div>
            <h1 className="text-2xl font-bold text-white mb-2">Verification Failed</h1>
            <p className="text-gray-400 mb-6">The verification link is invalid or has expired.</p>
            <Link to="/register" className="inline-block bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 px-6 rounded-lg transition">
              Back to Register
            </Link>
          </>
        )}
      </div>
    </div>
  )
}