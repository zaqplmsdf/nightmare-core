# Local compile dependencies

`curios-forge-5.14.1+1.20.1.jar`, `geckolib-forge-1.20.1-4.8.3.jar`, and
`jei-1.20.1-forge-15.20.0.130.jar` are kept here only so the project can be
compiled and loaded in a local test instance without depending on remote Maven
metadata during `build`.

They are **not** copied into the Nightmare Core jar and are **not redistributed**
as part of the mod. Curios and GeckoLib are mandatory runtime dependencies; JEI
is optional and only enables the Nightmare Forge recipe category.

For a fresh checkout on another machine, replace these files with matching
versions (or update `curios_version`, `geckolib_version`, or `jei_version` in
`gradle.properties` and the file names in `build.gradle`).
