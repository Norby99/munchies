Feature: Restaurant Creation and Configuration
  As a manager user
  I want to create and configure restaurant information
  So that I can set up my restaurant in the system

  Background:
    Given I am a manager user

  Scenario: Create a new restaurant
    When I create a restaurant with:
      | name         | address         | phone     | email                  |
      | Pizza Palace | 456 Oak Avenue  | 123456789 | info@pizza-palace.com   |
    Then the restaurant should be created successfully
    And I should have a restaurant with:
      | name         | address         | phone     | email                  |
      | Pizza Palace | 456 Oak Avenue  | 123456789 | info@pizza-palace.com   |

  Scenario: Retrieve created restaurant details
    Given I created a restaurant with:
      | name         | address         | phone     | email                  |
      | Pizza Palace | 456 Oak Avenue  | 123456789 | info@pizza-palace.com   |
    When I retrieve the restaurant details
    Then the restaurant should be retrieved successfully
    And the restaurant details should match:
      | name         | address         | phone     | email                  |
      | Pizza Palace | 456 Oak Avenue  | 123456789 | info@pizza-palace.com   |

  Scenario: Update restaurant information
    Given I created a restaurant with:
      | name         | address          | phone     | email                  |
      | Pizza Palace | 321 Main Street  | 123456789 | info@pizza-palace.com   |
    When I update the restaurant details to:
      | name           | address          | phone     | email                    |
      | Pizza Paradise | 789 Elm Street   | 987654321 | info@pizza-paradise.com   |
    Then the restaurant should be updated successfully
    And I should have a restaurant with:
      | name           | address          | phone     | email                    |
      | Pizza Paradise | 789 Elm Street   | 987654321 | info@pizza-paradise.com   |

  Scenario: Delete restaurant
    Given I created a restaurant with:
      | name         | address         | phone     | email                  |
      | Pizza Palace | 456 Oak Avenue  | 123456789 | info@pizza-palace.com   |
    When I delete the restaurant
    Then the restaurant should be deleted successfully
    And I should have no restaurant with:
        | name         | address         | phone     | email                  |
        | Pizza Palace | 456 Oak Avenue  | 123456789 | info@pizza-palace.com  |
