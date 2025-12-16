-- Seed data for testing
-- Note: Admin user is created programmatically at startup (see DataInitializer.java)

-- Test user (password: password123)
INSERT INTO users (id, username, email, password, location, balance, phone, address, profile_photo) 
VALUES (1, 'user1', 'user1@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGf6I.mLq4D1AZWZ7L3xWXqJCqSe', 'Location1', 0.0, NULL, NULL, NULL)
ON CONFLICT (id) DO NOTHING;

-- User roles (test user only - admin role is set by DataInitializer)
INSERT INTO user_roles (id, user_id, role) VALUES (1, 1, 'USER') ON CONFLICT (id) DO NOTHING;

-- User statuses (test user only - admin status is set by DataInitializer)
INSERT INTO user_statuses (id, user_id, status, updated_at) VALUES (1, 1, 'ACTIVE', NOW()) ON CONFLICT (id) DO NOTHING;

-- Reset sequences to avoid conflicts
SELECT setval('users_id_seq', GREATEST((SELECT MAX(id) FROM users), 1), true);
SELECT setval('user_roles_id_seq', GREATEST((SELECT MAX(id) FROM user_roles), 1), true);
SELECT setval('user_statuses_id_seq', GREATEST((SELECT MAX(id) FROM user_statuses), 1), true);

