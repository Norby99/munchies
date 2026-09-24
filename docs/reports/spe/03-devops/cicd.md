# CI/CD
Continuous Integration (CI) and Continuous Deployment (CD) are essential processes of any agile software development lifecycle. 

In _munchies_ we made an ample use of the available technologies available to us.

Our project, hosted on GitHub, allowed us to utilize [GitHub's Actions](https://github.com/features/action) to execute workflows upon certain events that happened in our repository during the development processes. 

## Workflows

Actions allowed us to automatically test, build artifacts, ensure quality standards and release a ready-to-use product.

These operations were handled by a mix of custom scripts, custom tasks from the build system used and other already-made actions available in [Github's Action Marketplace](https://github.com/marketplace?type=actions)

### CI-CD
The file [.github/workflows/ci-cd.yaml](https://github.com/Norby99/munchies/blob/master/.github/workflows/ci-cd.yaml) is the only workflow that is triggered by automatic repository events, it acts as a dispatcher and central hub for other workflow synchronization.

In GitHub's workflows configuration it is possible to declare dependency associations, meaning a workflow may depend on another completing successfully and as such wait the other's completion; or many workflows may run simoultaneously.

```yaml
publish:
  needs:
    - build
  if: |
    always() &&
    needs.build.result == 'success' &&
    github.event_name == 'push' &&
    github.ref == 'refs/heads/master'
  uses: ./.github/workflows/publish.yaml
```

### Test
The file [.github/workflows/test.yaml](https://github.com/Norby99/munchies/blob/master/.github/workflows/test.yaml) aggregates all our quality assurance checks and tests.

It runs a commit linter (for redundancy since our developer-hosted commit linter can be bypassed), documentation checks, code linters, static code analysis and runs our unit, integration, component and end-to-end tests.

It is, by far, our heaviest and most long-running workflow that is triggered almost everytime, as such we've decided to limit it to only run whenever any change
in the code is detected, skipping execution when only documentation changes are detected.

```yaml
check-and-test:
  needs:
    - changes
    - commitlint
  if: needs.changes.outputs.code == 'true' || github.event_name == 'workflow_dispatch'
  steps:
    - run: ./gradlew spotlessCheck
    - run: ./gradlew test integrationTest componentTest e2eTest -PtestOutput=all
    - run: ./gradlew koverVerify vitestCoverageVerify
```

### Build

The file [.github/workflows/build.yaml](https://github.com/Norby99/munchies/blob/master/.github/workflows/build.yaml) builds the whole project, making sure nothing from the previous workflow is missed or non-functioning. 

```yaml
- name: Build with Gradle
  run: ./gradlew build
```

### Deploy-Docs

The file [.github/workflows/deploy-docs.yaml](https://github.com/Norby99/munchies/blob/master/.github/workflows/deploy-docs.yaml) is tasked with building and publishing all documentation regarding our project.

As mentioned before, we've used custom scripts, tasks and GH actions available from the marketplace; in this workflow:

- Generate OpenAPI docs from OpenAPI specs, created from ```@Annotations```
- Generate Kotlin Docs using Dokka
- Generate TypeScript docs using TypeDocs
- Generate Reports from ```.md``` files using MkDocs
- Upload these documentation to GitHub pages to be statically hosted 

```yaml
- name: Generate Kotlin docs
  run: bash ./gradlew dokkaGenerateHtml
- name: Generate Typescript docs
  run: bash ./gradlew prepareTypeDocs
- name: Build MkDocs Reports
  run: mkdocs build
- name: Deploy to GitHub Pages
  uses: actions/deploy-pages@v5
```

### Publish

The file [.github/workflows/publish.yaml](https://github.com/Norby99/munchies/blob/master/.github/workflows/publish.yaml) is tasked with:

- Computing the expected versioning from the semantic commits being merged
- Preparing and publishing our generated Javascript *-shared modules to [NPM](https://www.npmjs.com/~maggico-munchies)
- Preparing and publishing our *-service images to [DockerHub](https://hub.docker.com/u/maggicomunchies)
- Publishing our *-shared modules to [Maven](https://central.sonatype.com/search?q=norby99)
- Updating the Changelog and creating a new release in GitHub's "Release Page" 

```yaml
- name: Run semantic-release
  run: npx semantic-release

- name: Check version for release
  id: check-version
  run: |
    GIT_VERSION=$(git describe --tags --exact-match HEAD || echo '')
    if [ -n "$GIT_VERSION" ]; then
      echo "version=$(echo $GIT_VERSION | sed 's/^v//')" >> "$GITHUB_OUTPUT"
    fi

- name: Publish to docker hub
  if: steps.check-version.outputs.version != ''
  run: bash ./scripts/publish/docker.sh "${{ steps.check-version.outputs.version }}"
```