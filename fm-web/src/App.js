import React, { useContext } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, AuthContext } from './context/AuthContext';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import Team from './pages/Team';
import Formation from './pages/Formation';
import Ranking from './pages/Ranking';
import Schedule from './pages/Schedule';
import UpcomingMatches from './pages/UpcomingMatches';
import Stadium from './pages/Stadium';
import Leagues from './pages/Leagues';
import Transfers from './pages/Transfers';
import History from './pages/History';
import MatchDetail from './pages/MatchDetail';

const PrivateRoute = ({ children }) => {
  const { user, loading } = useContext(AuthContext);
  if (loading) return <div>Loading...</div>;
  return user ? children : <Navigate to="/login" />;
};

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/" element={<PrivateRoute><Home /></PrivateRoute>} />
          <Route path="/team" element={<PrivateRoute><Team /></PrivateRoute>} />
          <Route path="/formation" element={<PrivateRoute><Formation /></PrivateRoute>} />
          <Route path="/ranking" element={<PrivateRoute><Ranking /></PrivateRoute>} />
          <Route path="/schedule" element={<PrivateRoute><Schedule /></PrivateRoute>} />
          <Route path="/upcoming-matches" element={<PrivateRoute><UpcomingMatches /></PrivateRoute>} />
          <Route path="/stadium" element={<PrivateRoute><Stadium /></PrivateRoute>} />
          <Route path="/leagues" element={<PrivateRoute><Leagues /></PrivateRoute>} />
          <Route path="/transfers" element={<PrivateRoute><Transfers /></PrivateRoute>} />
          <Route path="/history" element={<PrivateRoute><History /></PrivateRoute>} />
          <Route path="/match/:id" element={<PrivateRoute><MatchDetail /></PrivateRoute>} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
