# Notes

## Workflow

- Create branch from master
- Do all the development in the branch
- Update change notes in [plugin.xml](/intellij-plugin/plugin/src/main/resources/META-INF/plugin.xml)
- Push the branch
- Create a pr from branch to master
- When all checks are ok merge to master

## How to release?

- After the last commit for the release has been merged, add a git tag for it
- Wait for the deployment to succeed
- Bump [release version](/gradle/libs.versions.toml) in this file using semantic versioning

## How to update the jvm?

- Update the jvm version on the [java version](/gradle/libs.versions.toml) in this file
- Update the jvm version on the [java-version](.github/actions/setup-action/action.yml) in this file