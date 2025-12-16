Feature: Item Management Edge Cases
  As a platform user or admin
  I want the item management system to handle edge cases properly
  So that listings are managed reliably

  # Item Creation Edge Cases
  Scenario: Reject item creation with negative price
    Given I am logged in as a user
    When I try to create an item with a price of -10
    Then the item should not be created
    And I should see a validation error for price

  Scenario: Reject item creation with zero price
    Given I am logged in as a user
    When I try to create an item with a price of 0
    Then the item should not be created
    And I should see a validation error for price

  Scenario: Reject item creation with empty name
    Given I am logged in as a user
    When I try to create an item with an empty name
    Then the item should not be created
    And I should see a validation error for name

  Scenario: Reject item creation without category
    Given I am logged in as a user
    When I try to create an item without selecting a category
    Then the item should not be created
    And I should see a validation error for category

  # Item Update Edge Cases
  Scenario: Cannot update item with active bookings to unavailable
    Given I have an item with active bookings
    When I try to mark the item as unavailable
    Then the update should be rejected
    And I should see a message about active bookings

  Scenario: Price change does not affect confirmed bookings
    Given I have an item with a confirmed booking at €20/day
    When I change the item price to €30/day
    Then the existing booking should still be at €20/day
    And new bookings should use the €30/day price

  Scenario: Only owner can update their item
    Given I am logged in as a user
    And another user owns an item
    When I try to update that item
    Then the update should be rejected with 403 Forbidden

  # Item Deletion/Deactivation Edge Cases
  Scenario: Cannot delete item with active bookings
    Given I have an item with active bookings
    When I try to delete the item
    Then the deletion should be rejected
    And I should see a message about active bookings

  Scenario: Admin can deactivate item with reason
    Given I am logged in as an admin
    And a user has listed an item
    When I deactivate the item with reason "Policy violation"
    Then the item should be deactivated
    And the reason should be stored
    And the deactivation should appear in audit log

  Scenario: Owner cannot reactivate admin-deactivated item
    Given my item was deactivated by an admin
    When I try to reactivate my item
    Then the action should be rejected
    And I should see a message to contact support

  # Item Visibility Edge Cases
  Scenario: Deactivated items not shown in search
    Given an item has been deactivated
    When a user searches for items
    Then the deactivated item should not appear in results

  Scenario: Unavailable items shown with unavailable badge
    Given an item is marked as unavailable
    When a user views the item listings
    Then the item should show with an "Unavailable" badge

  Scenario: Suspended user items not shown in search
    Given a user's account has been suspended
    When another user searches for items
    Then the suspended user's items should not appear in results

  # Item Image Edge Cases
  Scenario: Reject oversized image upload
    Given I am logged in as a user
    When I try to upload an image larger than 10MB
    Then the upload should be rejected
    And I should see a file size error

  Scenario: Reject non-image file upload
    Given I am logged in as a user
    When I try to upload a PDF file as an item image
    Then the upload should be rejected
    And I should see a file type error

  # Review Edge Cases
  Scenario: Cannot review item without completed booking
    Given I have never booked an item
    When I try to leave a review for that item
    Then the review should be rejected

  Scenario: Cannot review same booking twice
    Given I have already reviewed a completed booking
    When I try to leave another review for the same booking
    Then the second review should be rejected

  Scenario: Owner can respond to reviews
    Given my item has received a review
    When I reply to that review
    Then my response should be saved
    And visible on the item page

  # Category Edge Cases
  Scenario: Item with deleted category remains accessible
    Given an item belongs to a category
    And the category is later deleted by admin
    Then the item should still be accessible
    And should show "Uncategorized" as category
