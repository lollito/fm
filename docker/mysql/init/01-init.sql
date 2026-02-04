-- Football Manager Database Initialization

-- Create additional databases if needed
CREATE DATABASE IF NOT EXISTS footballmanager_test;

-- Grant permissions
GRANT ALL PRIVILEGES ON footballmanager.* TO 'fmuser'@'%';
GRANT ALL PRIVILEGES ON footballmanager_test.* TO 'fmuser'@'%';

-- Create indexes for better performance
USE footballmanager;

-- Player indexes
CREATE INDEX IF NOT EXISTS idx_player_team ON player(team_id);
CREATE INDEX IF NOT EXISTS idx_player_role ON player(role);
CREATE INDEX IF NOT EXISTS idx_player_age ON player(birth);

-- Match indexes
CREATE INDEX IF NOT EXISTS idx_match_date ON match(match_date);
CREATE INDEX IF NOT EXISTS idx_match_status ON match(status);
CREATE INDEX IF NOT EXISTS idx_match_teams ON match(home_team_id, away_team_id);

-- User indexes
CREATE INDEX IF NOT EXISTS idx_user_email ON user(email);
CREATE INDEX IF NOT EXISTS idx_user_club ON user(club_id);

-- Performance optimizations
SET GLOBAL innodb_buffer_pool_size = 268435456; -- 256MB
SET GLOBAL query_cache_size = 67108864; -- 64MB
SET GLOBAL query_cache_type = 1;

FLUSH PRIVILEGES;
