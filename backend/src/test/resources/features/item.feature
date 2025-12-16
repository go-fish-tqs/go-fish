Feature: Item Management

  Background:
    Given I am logged in as a user

  Scenario: List a new item
    Given I am on the list item page
    When I enter valid item details
    And I click the submit button
    Then I should see the item in my items list

  Scenario: View item details
    Given there are items listed
    When I click on an item
    Then I should see the item details page
