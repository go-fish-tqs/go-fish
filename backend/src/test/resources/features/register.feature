Feature: User Registration

  Scenario: Successful registration with valid data
    When I register with name "Test User" email "newuser@gofish.pt" password "password123" and location "Lisbon"
    Then the registration should be successful
    And the user should have role "USER"
    And the user should have status "ACTIVE"
    And the user should have balance 0.0

  Scenario: Failed registration with existing email
    Given a user exists with email "existing@gofish.pt" and password "password123"
    When I register with name "Another User" email "existing@gofish.pt" password "password123" and location "Porto"
    Then the registration should fail with a duplicate email error
