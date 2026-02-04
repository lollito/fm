import React from 'react';
import { useParams } from 'react-router-dom';
import Layout from '../components/Layout';
import PlayerHistory from '../components/PlayerHistory';

const Player = () => {
    const { id } = useParams();

    return (
        <Layout>
            <PlayerHistory playerId={id} />
        </Layout>
    );
};

export default Player;
