Feature: Category Variation Management
  As a restaurant manager
  I want to manage variations for a category
  So that customers can customise their order

  Background:
    Given a restaurant exists
    And the restaurant has a menu
    And the menu has a "Pizzas" category

  Scenario: Create a variation to a menu category
    When I create in the "Pizzas" category a "Dough" variation with options:
      | name        | price |
      | Spelt       | 2.00  |
      | Gluten free | 2.50  |
    Then the variation should be created successfully for the category
    And the "Pizzas" category should have a "Dough" variation with options:
      | name        | price |
      | Spelt       | 2.00  |
      | Gluten free | 2.50  |

  Scenario: Update an existing variation of a menu category
    Given the "Pizzas" category has a "Dough" variation with options:
      | name        | price |
      | Spelt       | 2.00  |
      | Gluten free | 2.50  |
    When I update the "Dough" variation for the "Pizzas" category to have options:
      | name        | price |
      | Normal      | 0.00  |
      | Spelt       | 2.50  |
      | Whole wheat | 3.00  |
    Then the variation should be updated successfully for the category
    And the "Pizzas" category should have a "Dough" variation with options:
      | name        | price |
      | Normal      | 0.00  |
      | Spelt       | 2.50  |
      | Whole wheat | 3.00  |

  Scenario: Remove a variation from a menu category
    Given the "Pizzas" category has a "Dough" variation with options:
      | name        | price |
      | Spelt       | 2.00  |
      | Gluten free | 2.50  |
    When I remove the "Dough" variation from the "Pizzas" category
    Then the variation should be removed successfully from the category
    And the "Pizzas" category should have no "Dough" variation
