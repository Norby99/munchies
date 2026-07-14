Feature: Get User
  As a user
  I want to manage my own account

  Background:
    Given a registered user

  Scenario: Obtain user information
    When I query the system with the user id
    Then the system should respond with the user information