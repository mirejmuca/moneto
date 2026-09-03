import { useState } from 'react'
import Navbar from '../components/Navbar'
import { importTransactions } from '../services/importService'

export default function Import() {
  const [file, setFile] = useState(null)
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)

  const handleFileChange = (e) => {
    setFile(e.target.files[0])
    setResult(null)
  }

  const handleImport = async () => {
    if (!file) return
    setLoading(true)
    setResult(null)
    try {
      const data = await importTransactions(file)
      setResult(data)
    } catch (err) {
      setResult({ error: err.response?.data?.message || 'Import failed. Please check your file.' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-950 text-white">
      <Navbar />
      <div className="max-w-2xl mx-auto px-8 py-8">
        <h1 className="text-2xl font-bold mb-2">Import Transactions</h1>
        <p className="text-gray-400 text-sm mb-6">
          Upload a CSV file to import transactions in bulk.
        </p>

        {/* Format guide */}
        <div className="bg-gray-900 rounded-2xl p-6 mb-6">
          <h2 className="text-sm font-semibold mb-3 text-gray-300">Expected CSV format</h2>
          <div className="bg-gray-800 rounded-lg p-4 font-mono text-xs text-gray-400 overflow-x-auto">
            <p>date,type,category,amount,currency,description</p>
            <p>2026-08-01,EXPENSE,Food,25.50,USD,Lunch</p>
            <p>2026-08-02,INCOME,Salary,1000,USD,Monthly salary</p>
          </div>
          <p className="text-gray-500 text-xs mt-3">
            The first row must be the header. Date format: YYYY-MM-DD. Type: INCOME or EXPENSE.
            Missing categories will be created automatically.
          </p>
        </div>

        {/* Upload */}
        <div className="bg-gray-900 rounded-2xl p-6">
          <input
            type="file"
            accept=".csv"
            onChange={handleFileChange}
            className="block w-full text-sm text-gray-400 mb-4
              file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0
              file:bg-blue-600 file:text-white file:font-semibold
              hover:file:bg-blue-700 file:cursor-pointer"
          />
          <button onClick={handleImport} disabled={!file || loading}
            className="bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2.5 px-5 rounded-lg transition disabled:opacity-50">
            {loading ? 'Importing...' : 'Import'}
          </button>
        </div>

        {/* Result */}
        {result && (
          <div className="bg-gray-900 rounded-2xl p-6 mt-6">
            {result.error ? (
              <p className="text-red-400">{result.error}</p>
            ) : (
              <>
                <p className="text-green-400 font-semibold mb-2">
                  ✓ {result.imported} transaction{result.imported !== 1 ? 's' : ''} imported successfully
                </p>
                {result.errors && result.errors.length > 0 && (
                  <div className="mt-4">
                    <p className="text-yellow-400 text-sm mb-2">
                      {result.errors.length} row{result.errors.length !== 1 ? 's' : ''} skipped:
                    </p>
                    <div className="bg-gray-800 rounded-lg p-4 max-h-48 overflow-y-auto space-y-1">
                      {result.errors.map((err, i) => (
                        <p key={i} className="text-gray-400 text-xs font-mono">{err}</p>
                      ))}
                    </div>
                  </div>
                )}
              </>
            )}
          </div>
        )}
      </div>
    </div>
  )
}