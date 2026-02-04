import React, { useContext } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, AuthContext } from './context/AuthContext';
import Dashboard from './pages/Dashboard';
import Login from './pages/Login';
import UserManagement from './pages/UserManagement';
import DebugTools from './pages/DebugTools';
import AdminDashboard from './pages/AdminDashboard';
import ClubManagement from './pages/ClubManagement';

const PrivateRoute = ({ children }) => {
  const { user } = useContext(AuthContext);
  // Ideally check for ADMIN role here
  return user ? children : <Navigate to="/login" />;
};

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/" element={<PrivateRoute><AdminDashboard /></PrivateRoute>} />
          <Route path="/dashboard" element={<PrivateRoute><Dashboard /></PrivateRoute>} />
          <Route path="/clubs" element={<PrivateRoute><ClubManagement /></PrivateRoute>} />
          <Route path="/users" element={<PrivateRoute><UserManagement /></PrivateRoute>} />
          <Route path="/debug" element={<PrivateRoute><DebugTools /></PrivateRoute>} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
