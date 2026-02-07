import React, { useContext } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, AuthContext } from './context/AuthContext';
import { ToastProvider } from './context/ToastContext';
import ToastContainer from './components/ToastContainer';
import Dashboard from './pages/Dashboard';
import Login from './pages/Login';
import UserManagement from './pages/UserManagement';
import DebugTools from './pages/DebugTools';
import AdminDashboard from './pages/AdminDashboard';
import ClubManagement from './pages/ClubManagement';
import ServerManagement from './pages/ServerManagement';
import LiveMatchMonitoring from './pages/LiveMatchMonitoring';

const PrivateRoute = ({ children }) => {
  const { user } = useContext(AuthContext);

  if (!user) {
    return <Navigate to="/login" />;
  }

  if (!user.roles || !user.roles.includes('ROLE_ADMIN')) {
    return <Navigate to="/login" />;
  }

  return children;
};

function App() {
  return (
    <AuthProvider>
      <ToastProvider>
        <Router>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/" element={<PrivateRoute><AdminDashboard /></PrivateRoute>} />
            <Route path="/dashboard" element={<PrivateRoute><Dashboard /></PrivateRoute>} />
            <Route path="/clubs" element={<PrivateRoute><ClubManagement /></PrivateRoute>} />
            <Route path="/users" element={<PrivateRoute><UserManagement /></PrivateRoute>} />
            <Route path="/servers" element={<PrivateRoute><ServerManagement /></PrivateRoute>} />
            <Route path="/live-matches" element={<PrivateRoute><LiveMatchMonitoring /></PrivateRoute>} />
            <Route path="/debug" element={<PrivateRoute><DebugTools /></PrivateRoute>} />
          </Routes>
        </Router>
        <ToastContainer />
      </ToastProvider>
    </AuthProvider>
  );
}

export default App;
