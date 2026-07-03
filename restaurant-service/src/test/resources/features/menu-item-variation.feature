Feature: Item Variation Management
  As a restaurant manager
  I want to manage variations for a specific item
  So that customers can customise individual dishes

  Background:
    Given a restaurant exists
    And the restaurant has a menu
    And the menu has a "Pizzas" category
    And the "Pizzas" category has an "Crudo" item with description "Margherita with Crudo" and price 8.50

  Scenario: Create a variation to a specific menu item
    When I create in the "Crudo" item a "Crudo preparation" variation with options:
      | name      | price |
      | Post-bake | 0.00  |
      | Baked in  | 0.00  |
    Then the variation should be created successfully to the item
    And the "Crudo" item should have a "Crudo preparation" variation with options:
      | name      | price |
      | Post-bake | 0.00  |
      | Baked in  | 0.00  |

  Scenario: Update an existing variation of a menu item
    Given the "Crudo" item has a "Crudo preparation" variation with options:
      | name      | price |
      | Post-bake | 0.00  |
    When I update the "Crudo preparation" variation for the "Crudo" item to have options:
      | name      | price |
      | Post-bake | 0.00  |
      | Baked in  | 0.00  |
    Then the variation should be updated successfully for the item
    And the "Crudo" item should have a "Crudo preparation" variation with options:
      | name      | price |
      | Post-bake | 0.00  |
      | Baked in  | 0.00  |

  Scenario: Remove a variation from a menu item
    Given the "Crudo" item has a "Crudo preparation" variation with options:
      | name      | price |
      | Post-bake | 0.00  |
      | Baked in  | 0.00  |
    When I remove the "Crudo preparation" variation from the "Crudo" item
    Then the variation should be removed successfully
    And the "Crudo" item should have no "Crudo preparation" variation
