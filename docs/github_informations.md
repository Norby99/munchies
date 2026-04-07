# Workflow

The project structure follows the Git-Flow workflow, with master being the primary branch from where all the releases will  be generated. There is also a develop branch used as a trunk where all new features will be merged before

# Rulesets

## Master

(master)

- Require code scanning results:
    - CodeQL.
- Require code quality results:
    - Severity: Errors.

## Important Branches

(master, develop)

- Restrict deletions.
- Require PR before merging:
    - Rebase only.
- Require status check to pass.
    - Require branches to be up to date before merging. (To force users to resolve eventual merge conflicts by themselfs)
- Automatically require Copilot code review.

## Signed commits

(everything except: master, develop)

- Require signed commits.

## General rules

- Block force pushes.