-- Seed data for testing
INSERT INTO users (id, username, email, password, location, balance, phone, address, profile_photo) 
VALUES (1, 'user1', 'user1@example.com', 'password123', 'Location1', 0.0, NULL, NULL, NULL)
ON CONFLICT (id) DO NOTHING;
