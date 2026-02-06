import React from 'react';
import { Link } from 'react-router-dom';

const TeamLink = ({ club, style, className }) => {
  if (!club) return null;

  const handleClick = (e) => {
    e.stopPropagation();
  };

  return (
    <Link
      to={`/team/${club.teamId}`}
      onClick={handleClick}
      className={className}
      style={{ textDecoration: 'none', color: 'inherit', ...style }}
    >
      {club.name}
      {club.isHuman && (
        <i
          className="fas fa-user-circle text-primary"
          style={{ marginLeft: '6px' }}
          title="Human Manager"
        />
      )}
    </Link>
  );
};

export default TeamLink;
