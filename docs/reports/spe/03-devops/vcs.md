# VCS & Repository
Our projects uses **Git** as its Version Control System; furthermore we host our project on GitHub to hold a distributed single trutful repository.

## Semantic Versioning

This project uses Semantic Version during its development and release processes.
Any versioned release is available on GitHub, [this](https://github.com/Norby99/munchies/releases/latest) will always be the latest marked release.

## Conventional Commits

This project uses Conventional Commits, where each commit follows a strict naming convention to keep track trough commit messages the changes that the commit stores.

Conventional commits are enforced twice:

1. At the developer level through [```org.danilopianini.gradle-pre-commit-git-hooks```](https://github.com/DanySK/gradle-pre-commit-git-hooks), a Gradle plugin, which runs a regex to check whether a commit contains a message valid by these [rules](https://www.conventionalcommits.org/en/v1.0.0/).  
2. At the workflow level through [```./github/workflows/test.yaml```](https://github.com/Norby99/munchies/blob/master/.github/workflows/test.yaml)'s "commitlint" step which uses npm's [commitlint](https://www.npmjs.com/package/@commitlint/cli) configured for conventional commits   

## Semantic Release

By enforcing conventional commits throughout the project's history, a clear progression can be determined and subsequently an understandable version may be computed for releases.

We used a [plugin](https://semantic-release.org/) tasked with computing such version during our [./github/workflows/publish.yaml](https://github.com/Norby99/munchies/blob/master/.github/workflows/publish.yaml) workflow, it also creates a new release in GitHub's "Releases" menu. 


Although each subproject experiences different development changes and by conventional commits standards' it should have different version numbers from other subprojects; we decided to keep a single version number for the whole project such that the whole project is being brought forward.
## Repository Management

The repository is structured as follows:

- a ```master``` branch where we store stable releases of our code and where release workflows trigger from.
- a ```develop``` branch where team member converge their changes and enforce quality standards.
- a branch where each developer pushes changed based on tasks to then merge into develop after the required checks go through.

aGGIUNGERE REGOLE DEI BRANCH


## Pull Requests

WORKFLOW DELLE PULL REQUEST E REBASE
