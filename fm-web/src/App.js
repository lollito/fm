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
import LiveMatches from './pages/LiveMatches';
import Stadium from './pages/Stadium';
import Infrastructure from './pages/Infrastructure';
import Leagues from './pages/Leagues';
import Transfers from './pages/Transfers';
import History from './pages/History';
import MatchDetail from './pages/MatchDetail';
import Player from './pages/Player';
import Finance from './pages/Finance';
import LiveMatchViewer from './components/LiveMatchViewer';
import ScoutingDashboard from './pages/ScoutingDashboard';
import Watchlist from './pages/Watchlist';
import Loans from './pages/Loans';

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
          <Route path="/team/:id" element={<PrivateRoute><Team /></PrivateRoute>} />
          <Route path="/formation" element={<PrivateRoute><Formation /></PrivateRoute>} />
          <Route path="/ranking" element={<PrivateRoute><Ranking /></PrivateRoute>} />
          <Route path="/schedule" element={<PrivateRoute><Schedule /></PrivateRoute>} />
          <Route path="/live-matches" element={<PrivateRoute><LiveMatches /></PrivateRoute>} />
          <Route path="/stadium" element={<PrivateRoute><Stadium /></PrivateRoute>} />
          <Route path="/infrastructure" element={<PrivateRoute><Infrastructure /></PrivateRoute>} />
          <Route path="/leagues" element={<PrivateRoute><Leagues /></PrivateRoute>} />
          <Route path="/transfers" element={<PrivateRoute><Transfers /></PrivateRoute>} />
          <Route path="/history" element={<PrivateRoute><History /></PrivateRoute>} />
          <Route path="/finance" element={<PrivateRoute><Finance /></PrivateRoute>} />
          <Route path="/scouting" element={<PrivateRoute><ScoutingDashboard /></PrivateRoute>} />
          <Route path="/watchlist" element={<PrivateRoute><Watchlist /></PrivateRoute>} />
          <Route path="/loans" element={<PrivateRoute><Loans /></PrivateRoute>} />
          <Route path="/match/:id" element={<PrivateRoute><MatchDetail /></PrivateRoute>} />
          <Route path="/match/live/:matchId" element={<PrivateRoute><LiveMatchViewer /></PrivateRoute>} />
          <Route path="/player/:id" element={<PrivateRoute><Player /></PrivateRoute>} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
