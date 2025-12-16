Feature: User Registration

  Scenario: Successful registration with valid data
    Given I am on the registration page
    When I enter valid registration details
    And I click the register button
    Then I should be redirected to the login page
    And I should see a success message

  Scenario: Unsuccessful registration with existing email
    Given I am on the registration page
    When I enter registration details with an existing email
    And I click the register button
    Then I should see an error message indicating email already exists
