Feature: Admin Management

  Background:
    Given I am logged in as an admin

  Scenario: Access admin dashboard
    When I navigate to the admin dashboard
    Then I should see dashboard statistics

  Scenario: Suspend a user
    Given I am on the user management page
    When I click suspend on a user
    Then the user status should change to suspended

  Scenario: Delete an item
    Given I am on the item management page
    When I click delete on an item
    Then the item should be removed from the list
