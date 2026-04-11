import { useState, useEffect } from 'react'
import Navbar from '../components/Navbar'
import { getProfile, updateProfile, changePassword, updateSettings } from '../services/userService'

export default function Settings() {
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [profileForm, setProfileForm] = useState({ name: '', email: '' })
  const [passwordForm, setPasswordForm] = useState({ oldPassword: '', newPassword: '', confirmPassword: '' })
  const [settingsForm, setSettingsForm] = useState({ currency: '', monthStart: 1, budgetAlerts: true, recurringReminders: true })
  const [profileMsg, setProfileMsg] = useState('')
  const [passwordMsg, setPasswordMsg] = useState('')
  const [settingsMsg, setSettingsMsg] = useState('')

  useEffect(() => {
    fetchProfile()
  }, [])

  const fetchProfile = async () => {
    try {
      const data = await getProfile()
      setProfile(data)
      setProfileForm({ name: data.name, email: data.email })
      setSettingsForm({
        currency: data.currency,
        monthStart: data.monthStart,
        budgetAlerts: data.budgetAlerts,
        recurringReminders: data.recurringReminders
      })
    } catch (err) {
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const handleProfileSubmit = async (e) => {
    e.preventDefault()
    try {
      await updateProfile(profileForm)
      setProfileMsg('Profile updated successfully')
      setTimeout(() => setProfileMsg(''), 3000)
    } catch (err) {
      setProfileMsg('Failed to update profile')
    }
  }

  const handlePasswordSubmit = async (e) => {
    e.preventDefault()
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setPasswordMsg('Passwords do not match')
      return
    }
    try {
      await changePassword({ oldPassword: passwordForm.oldPassword, newPassword: passwordForm.newPassword })
      setPasswordMsg('Password changed successfully')
      setPasswordForm({ oldPassword: '', newPassword: '', confirmPassword: '' })
      setTimeout(() => setPasswordMsg(''), 3000)
    } catch (err) {
      setPasswordMsg('Incorrect current password')
    }
  }

  const handleSettingsSubmit = async (e) => {
    e.preventDefault()
    try {
      await updateSettings(settingsForm)
      setSettingsMsg('Settings saved successfully')
      setTimeout(() => setSettingsMsg(''), 3000)
    } catch (err) {
      setSettingsMsg('Failed to save settings')
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
      <div className="max-w-3xl mx-auto px-8 py-8 space-y-6">
        <h1 className="text-2xl font-bold">Settings</h1>

        {/* Profile */}
        <div className="bg-gray-900 rounded-2xl p-6">
          <h2 className="text-lg font-semibold mb-4">Profile</h2>
          <form onSubmit={handleProfileSubmit} className="space-y-4">
            <div>
              <label className="text-gray-400 text-sm mb-1 block">Name</label>
              <input type="text" value={profileForm.name}
                onChange={(e) => setProfileForm({ ...profileForm, name: e.target.value })}
                className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500" />
            </div>
            <div>
              <label className="text-gray-400 text-sm mb-1 block">Email</label>
              <input type="email" value={profileForm.email}
                onChange={(e) => setProfileForm({ ...profileForm, email: e.target.value })}
                className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500" />
            </div>
            {profileMsg && <p className="text-sm text-green-400">{profileMsg}</p>}
            <button type="submit"
              className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded-lg text-sm font-semibold transition">
              Save Profile
            </button>
          </form>
        </div>

        {/* Password */}
        <div className="bg-gray-900 rounded-2xl p-6">
          <h2 className="text-lg font-semibold mb-4">Change Password</h2>
          <form onSubmit={handlePasswordSubmit} className="space-y-4">
            <div>
              <label className="text-gray-400 text-sm mb-1 block">Current Password</label>
              <input type="password" value={passwordForm.oldPassword}
                onChange={(e) => setPasswordForm({ ...passwordForm, oldPassword: e.target.value })}
                className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500" />
            </div>
            <div>
              <label className="text-gray-400 text-sm mb-1 block">New Password</label>
              <input type="password" value={passwordForm.newPassword}
                onChange={(e) => setPasswordForm({ ...passwordForm, newPassword: e.target.value })}
                className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500" />
            </div>
            <div>
              <label className="text-gray-400 text-sm mb-1 block">Confirm New Password</label>
              <input type="password" value={passwordForm.confirmPassword}
                onChange={(e) => setPasswordForm({ ...passwordForm, confirmPassword: e.target.value })}
                className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500" />
            </div>
            {passwordMsg && <p className={`text-sm ${passwordMsg.includes('success') ? 'text-green-400' : 'text-red-400'}`}>{passwordMsg}</p>}
            <button type="submit"
              className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded-lg text-sm font-semibold transition">
              Change Password
            </button>
          </form>
        </div>

        {/* Preferences */}
        <div className="bg-gray-900 rounded-2xl p-6">
          <h2 className="text-lg font-semibold mb-4">Preferences</h2>
          <form onSubmit={handleSettingsSubmit} className="space-y-4">
            <div>
              <label className="text-gray-400 text-sm mb-1 block">Currency</label>
              <select value={settingsForm.currency}
                onChange={(e) => setSettingsForm({ ...settingsForm, currency: e.target.value })}
                className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500">
                <option value="USD">USD - US Dollar</option>
                <option value="EUR">EUR - Euro</option>
                <option value="GBP">GBP - British Pound</option>
                <option value="ALL">ALL - Albanian Lek</option>
                <option value="CHF">CHF - Swiss Franc</option>
                <option value="CAD">CAD - Canadian Dollar</option>
                <option value="AUD">AUD - Australian Dollar</option>
                <option value="JPY">JPY - Japanese Yen</option>
              </select>
            </div>
            <div>
              <label className="text-gray-400 text-sm mb-1 block">Month Start Day</label>
              <input type="number" value={settingsForm.monthStart} min="1" max="28"
                onChange={(e) => setSettingsForm({ ...settingsForm, monthStart: e.target.value })}
                className="w-full bg-gray-800 text-white rounded-lg px-4 py-3 outline-none focus:ring-2 focus:ring-blue-500" />
            </div>
            <div className="flex items-center justify-between">
              <label className="text-gray-300 text-sm">Budget Alerts</label>
              <input type="checkbox" checked={settingsForm.budgetAlerts}
                onChange={(e) => setSettingsForm({ ...settingsForm, budgetAlerts: e.target.checked })}
                className="w-4 h-4" />
            </div>
            <div className="flex items-center justify-between">
              <label className="text-gray-300 text-sm">Recurring Reminders</label>
              <input type="checkbox" checked={settingsForm.recurringReminders}
                onChange={(e) => setSettingsForm({ ...settingsForm, recurringReminders: e.target.checked })}
                className="w-4 h-4" />
            </div>
            {settingsMsg && <p className="text-sm text-green-400">{settingsMsg}</p>}
            <button type="submit"
              className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded-lg text-sm font-semibold transition">
              Save Settings
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}