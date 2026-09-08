Feature: Update user profile
  Scenario: A registered user attempts to update its profile
  Given a user which is registered
  When the user updates its profile
  Then only the correct changes are allowed
  And the user has its profile updated