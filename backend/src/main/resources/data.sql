-- Seed data for testing
INSERT INTO users (id, username, email, password, location, balance) 
VALUES (1, 'user1', 'user1@example.com', 'password123', 'Location1', 0.0)
ON CONFLICT (id) DO NOTHING;
