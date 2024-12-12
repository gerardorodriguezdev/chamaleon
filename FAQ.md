# Github workflow

## How to contribute?

- Create a branch from master
- Add the code changes there
- Send a PR to merge with master
- If all good the maintainers will merge the PR

## How to release?

- Create a branch from master
- Update the release version on the [release version](/gradle/libs.versions.toml) in this file using semantic versioning
- Send a PR to merge with release
- If all good the maintainers will merge the PR
- When this happens a deployment will be done
- After the deployment is finished it will upload the artifacts and create the github release

## How to update the jvm?

- Update the jvm version on the [jvm version](/gradle/libs.versions.toml) in this file
- Update the jvm version on the [java-version](.github/actions/setup-action/action.yml) in this file