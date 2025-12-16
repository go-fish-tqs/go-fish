Feature: User Management Edge Cases
  As an admin or platform user
  I want the user management system to handle edge cases properly
  So that the platform maintains security and data integrity

  # User Suspension Edge Cases
  Scenario: Suspend user with active bookings
    Given I am logged in as an admin
    And a user has active bookings
    When I suspend that user
    Then the user should be suspended
    And the active bookings should remain valid
    And the user's items should become unavailable

  Scenario: Suspended user cannot list new items
    Given I am a suspended user
    When I try to create a new item listing
    Then the action should be rejected
    And I should see a suspension notice

  Scenario: Suspended user cannot make bookings
    Given I am a suspended user
    When I try to make a booking
    Then the action should be rejected
    And I should see a suspension notice

  Scenario: Suspended user can still view their active bookings
    Given I am a suspended user with active bookings
    When I navigate to my bookings page
    Then I should be able to view my existing bookings

  Scenario: Reactivate suspended user
    Given I am logged in as an admin
    And a user is currently suspended
    When I reactivate the user
    Then the user status should change to active
    And the user's items should become available again

  # User Deletion Edge Cases
  Scenario: Delete user with active bookings as renter
    Given I am logged in as an admin
    And a user has active bookings as a renter
    When I try to delete that user
    Then I should see a warning about active bookings
    And I should be asked to confirm the deletion

  Scenario: Delete user with active bookings as owner
    Given I am logged in as an admin
    And a user has active bookings on their items
    When I try to delete that user
    Then I should see a warning about impacted bookings
    And the bookings should be handled appropriately

  Scenario: Deleted user items become unavailable
    Given I am logged in as an admin
    And a user has listed items
    When I delete that user
    Then all their items should be deactivated
    And the items should no longer appear in search results

  # Admin Self-Management Edge Cases
  Scenario: Admin cannot suspend themselves
    Given I am logged in as an admin
    When I try to suspend my own account
    Then the action should be rejected
    And I should see an error message

  Scenario: Admin cannot delete themselves
    Given I am logged in as an admin
    When I try to delete my own account
    Then the action should be rejected
    And I should see an error message

  Scenario: Admin cannot demote themselves from admin role
    Given I am logged in as the only admin
    When I try to remove my admin role
    Then the action should be rejected
    And I should see an error about needing at least one admin

  # User Status Transitions
  Scenario: Cannot suspend already suspended user
    Given I am logged in as an admin
    And a user is already suspended
    When I try to suspend that user again
    Then I should see an appropriate message
    And the user should remain suspended

  Scenario: Cannot reactivate active user
    Given I am logged in as an admin
    And a user is currently active
    When I try to reactivate that user
    Then I should see an appropriate message
    And the user should remain active

  # Audit Log Edge Cases
  Scenario: All suspension actions are logged
    Given I am logged in as an admin
    When I suspend a user with a reason
    Then the suspension should appear in the audit log
    And the reason should be recorded

  Scenario: All reactivation actions are logged
    Given I am logged in as an admin
    When I reactivate a suspended user
    Then the reactivation should appear in the audit log
    And the admin who performed it should be recorded

  Scenario: All deletion actions are logged
    Given I am logged in as an admin
    When I delete a user with a reason
    Then the deletion should appear in the audit log
    And the reason and target details should be recorded

  # Role-Based Access Edge Cases
  Scenario: Non-admin user cannot access admin pages
    Given I am logged in as a regular user
    When I try to access the admin dashboard
    Then I should be redirected to the home page
    And I should not see admin features

  Scenario: Non-admin user cannot suspend users
    Given I am logged in as a regular user
    When I try to call the suspend user API directly
    Then the request should fail with 403 Forbidden

  Scenario: Non-admin user cannot view audit logs
    Given I am logged in as a regular user
    When I try to access the audit log
    Then I should be denied access

  # Concurrent User Actions Edge Cases
  Scenario: Handle concurrent suspension attempts
    Given I am logged in as an admin
    And another admin is also logged in
    When we both try to suspend the same user simultaneously
    Then only one suspension should be recorded
    And no duplicate audit logs should be created

  # User Data Integrity
  Scenario: User email uniqueness is enforced
    Given a user exists with email "test@example.com"
    When another user tries to register with "test@example.com"
    Then registration should fail
    And an appropriate error message should be shown

  Scenario: User cannot change email to existing email
    Given a user exists with email "existing@example.com"
    And I am logged in as a different user
    When I try to change my email to "existing@example.com"
    Then the change should be rejected
    And I should see an error about email already in use
