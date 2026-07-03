Feature: Item Management
  As a restaurant manager
  I want to manage items within a category
  So that customers can see individual dishes

  Background:
    Given a restaurant exists
    And the restaurant has a menu
    And the menu has a "Pizzas" category

  Scenario: Create an item into a category
    When I create in the "Pizzas" category a "Margherita" item with:
      | description      | price |
      | The real classic | 5.80  |
    Then the item should be created successfully
    And the "Pizzas" category should have a "Margherita" item with:
      | description      | price |
      | The real classic | 5.80  |

  Scenario: Update an existing menu item
    Given the "Pizzas" category has a "Margherita" item with:
      | description      | price |
      | The real classic | 5.80  |
    When I update in the "Pizzas" category the "Margherita" item to have:
      | description   | price |
      | The very best | 6.00  |
    Then the item should be updated successfully
    And the "Pizzas" category should have a "Margherita" item with:
      | description   | price |
      | The very best | 6.00  |

  Scenario: Remove a menu item
    Given the "Pizzas" category has a "Margherita" item with:
      | description      | price |
      | The real classic | 5.80  |
    When I remove from the "Pizzas" category the "Margherita" item
    Then the item should be removed successfully
    And the "Pizzas" category should have no "Margherita" item
