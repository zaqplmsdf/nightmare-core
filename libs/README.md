# Local compile dependency

`curios-forge-5.14.1+1.20.1.jar` is kept here only so the project can be
compiled and loaded in a local test instance without depending on a remote
Maven metadata request during `build`.

It is **not** copied into the Nightmare Core jar and is **not** redistributed
as part of the mod. Curios remains a separate, mandatory runtime dependency
for the modpack.

For a fresh checkout on another machine, replace this file with the matching
Curios Forge 5.14.1+1.20.1 jar (or update `curios_version` in
`gradle.properties` and the file name in `build.gradle`).
