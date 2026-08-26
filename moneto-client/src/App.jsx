import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './context/AuthContext'
import Login from './pages/Login'
import Register from './pages/Register'
import Dashboard from './pages/Dashboard'
import Transactions from './pages/Transactions'
import Budgets from './pages/Budgets'
import Goals from './pages/Goals'
import Settings from './pages/Settings'
import Categories from './pages/Categories'
import Recurring from './pages/Recurring'
import Notifications from './pages/Notifications'
import Verify from './pages/Verify'
import Subscription from './pages/Subscription'
import Forecast from './pages/Forecast'
import Insights from './pages/Insights'
import Admin from './pages/Admin'
import Alerts from './pages/Alerts'

const ProtectedRoute = ({ children }) => {
  const { token } = useAuth()
  return token ? children : <Navigate to="/login" />
}

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/" element={
        <ProtectedRoute>
          <Dashboard />
        </ProtectedRoute>
      } />
      <Route path="/transactions" element={
        <ProtectedRoute>
          <Transactions />
         </ProtectedRoute>
      } />
      <Route path="/budgets" element={
        <ProtectedRoute>
          <Budgets />
         </ProtectedRoute>
      } />
      <Route path="/goals" element={
        <ProtectedRoute>
          <Goals />
        </ProtectedRoute>
      } />
      <Route path="/settings" element={
        <ProtectedRoute>
          <Settings />
        </ProtectedRoute>
      } />
      <Route path="/categories" element={
        <ProtectedRoute>
          <Categories />
        </ProtectedRoute>
      } />
      <Route path="/recurring" element={
        <ProtectedRoute>
          <Recurring />
        </ProtectedRoute>
      } />
      <Route path="/notifications" element={
        <ProtectedRoute>
         <Notifications />
      </ProtectedRoute>
      } />
      <Route path="/verify" element={<Verify />} />
      <Route path="/subscription" element={
        <ProtectedRoute>
          <Subscription />
        </ProtectedRoute>
      } />
      <Route path="/forecast" element={
        <ProtectedRoute>
          <Forecast />
        </ProtectedRoute>
      } />
      <Route path="/insights" element={
        <ProtectedRoute>
          <Insights />
        </ProtectedRoute>
      } />
      <Route path="/admin" element={
        <ProtectedRoute>
          <Admin />
        </ProtectedRoute>
      } />
      <Route path="/alerts" element={
        <ProtectedRoute>
         <Alerts />
      </ProtectedRoute>
} />
    </Routes>
  )
}

export default App