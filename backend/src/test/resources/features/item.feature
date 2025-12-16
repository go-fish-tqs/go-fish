Feature: Item Management

  Scenario: Create a new item
    Given a user "owner@gofish.pt" exists
    When "owner@gofish.pt" creates an item with name "Fishing Rod" description "Great rod" and price 50.0
    Then the item should be created successfully
    And the item owner should be "owner@gofish.pt"

  Scenario: Update item as owner
    Given a user "itemowner@gofish.pt" exists
    And an item "Test Item" exists owned by "itemowner@gofish.pt"
    When "itemowner@gofish.pt" updates the item with name "Updated Fishing Rod"
    Then the item name should be "Updated Fishing Rod"

  Scenario: Fail to update item as non-owner
    Given a user "realowner@gofish.pt" exists
    And a user "intruder@gofish.pt" exists
    And an item "Protected Item" exists owned by "realowner@gofish.pt"
    When "intruder@gofish.pt" attempts to update the item
    Then the update should fail with a forbidden error

  Scenario: Find items by owner
    Given a user "searcher@gofish.pt" exists
    And an item "Searchable Item" exists owned by "searcher@gofish.pt"
    When I search for items owned by "searcher@gofish.pt"
    Then I should receive items owned by "searcher@gofish.pt"
