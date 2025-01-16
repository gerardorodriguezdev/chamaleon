# Notes

## Workflow

- Create branch from master
- Do all the development in the branch
- Push the branch
- Create a pr from branch to master
- When all checks are ok merge to master

## How to release?

- Create a pr from master to release
- Add release flag
- When all checks are ok merge to master
- In case any release patch needed to be done, merge into master
- In master, bump [release version](/gradle/libs.versions.toml) in this file using semantic versioning

## How to update the jvm?

- Update the jvm version on the [java version](/gradle/libs.versions.toml) in this file
- Update the jvm version on the [java-version](.github/actions/setup-action/action.yml) in this file