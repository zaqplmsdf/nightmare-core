# Local compile dependencies

`curios-forge-5.14.1+1.20.1.jar` and `geckolib-forge-1.20.1-4.8.3.jar` are kept
here only so the project can be compiled and loaded in a local test instance
without depending on remote Maven metadata during `build`.

They are **not** copied into the Nightmare Core jar and are **not redistributed**
as part of the mod. Curios and GeckoLib remain separate, mandatory runtime
dependencies for the modpack.

For a fresh checkout on another machine, replace these files with matching
versions (or update `curios_version` / `geckolib_version` in
`gradle.properties` and the file names in `build.gradle`).
