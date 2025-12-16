Feature: Admin Operations

  Scenario: Suspend a user
    Given an admin user "admin@gofish.pt" exists
    And a regular user "tosuspend@gofish.pt" exists
    When the admin suspends user "tosuspend@gofish.pt"
    Then user "tosuspend@gofish.pt" should have status "SUSPENDED"

  Scenario: Unsuspend a user
    Given an admin user "admin2@gofish.pt" exists
    And a suspended user "toreactivate@gofish.pt" exists
    When the admin unsuspends user "toreactivate@gofish.pt"
    Then user "toreactivate@gofish.pt" should have status "ACTIVE"

  Scenario: Get all users as admin
    Given an admin user "admin3@gofish.pt" exists
    And a regular user "listuser@gofish.pt" exists
    When the admin requests all users
    Then the user list should contain user "listuser@gofish.pt"
