import React from 'react';
import WatchlistManager from '../components/WatchlistManager';
import Layout from '../components/Layout';

const Watchlist = () => {
  const user = JSON.parse(localStorage.getItem('user'));
  const clubId = user ? user.clubId : null;

  return (
    <Layout>
      {clubId ? <WatchlistManager clubId={clubId} /> : <div>Please log in to view watchlist.</div>}
    </Layout>
  );
};

export default Watchlist;
