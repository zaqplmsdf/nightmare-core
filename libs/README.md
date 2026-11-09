# Local compile dependencies

These jars are kept here only so the project can be compiled and loaded in a
local test instance without depending on remote Maven metadata during `build`.

- `curios-forge-5.14.1+1.20.1.jar` — mandatory Curios API.
- `geckolib-forge-1.20.1-4.8.3.jar` — mandatory GeckoLib model/animation API.
- `jei-1.20.1-forge-15.20.0.130.jar` — optional JEI recipe category.
- `kubejs-forge-2001.6.5-build.16.jar` and `rhino-forge-2001.2.3-build.10.jar`
  — optional KubeJS recipe schema integration. KubeJS also requires Architectury
  at runtime; it is supplied by the modpack.

None of these jars are copied into the Nightmare Core jar or redistributed as
part of the mod. Replace them with matching versions (or update the corresponding
properties in `gradle.properties`) for a fresh checkout.
