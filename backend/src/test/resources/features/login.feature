Feature: User Authentication

  Scenario: Successful login with valid credentials
    Given a user exists with email "test@gofish.pt" and password "password123"
    When I attempt to login with email "test@gofish.pt" and password "password123"
    Then the login should be successful
    And I should receive a valid JWT token
    And the response should contain user details

  Scenario: Failed login with invalid password
    Given a user exists with email "test@gofish.pt" and password "password123"
    When I attempt to login with email "test@gofish.pt" and password "wrongpassword"
    Then the login should fail with an invalid credentials error

  Scenario: Failed login with non-existent email
    When I attempt to login with email "nonexistent@gofish.pt" and password "anypassword"
    Then the login should fail with an invalid credentials error
