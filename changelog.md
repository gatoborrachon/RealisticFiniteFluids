v1.6.2
-Added: Config options to let you modify the behavior of the flow of water on creative players and to control whether fluids should tick randomly.
-Fixed: Texture size and orientation of flowing fluids.
-Code cleanup and refactor: FiniteFluidLogic (remove unused functions and optimized the search of Finite Fluid Indexes using Blocks/FluidRegistry names).

v1.6.1
-Fixed: Fatal issue where other mods register Finite Fluid blocks as theirs (a mistake on my side).
-Note: You still need to fix manually all the (possible) crashes you get, related to my mod.

v1.6.0
-Added: Universal Compatibility Coremod config option (false by default)

v1.5.0
-Added: Pressure System (water equalizes on containers when they are connected).
-Fixed: Water from rivers and ocean freezes when it is on a cold biome.
-Refactor: IFluidBlock functions.

v1.4.0
-Code refactor for BlockFiniteFluid class and BakedModel Classes (Now i use 1 single BakedModel class, making it easier start to implement a system for any FluidRegistry fluid).
-Added: Config option to replace vanilla water/lava from already existing worlds. Autosets to false every launch.
-Added: New method to control rain falling (true by default, configurable).
-Fixed: Now water blends its color to the current biome.

v1.3.0
-Added: Compat for IC2's Fluid Cell.
-Added: Compat for Traveler's Backpack Hose.
-Added: Fluids push you according to their movement vector.
-Fixed: water of LEVEL below 15 to display effects (fog, FOV change, water overlay) only when its LEVEL is above the eye of the player.
-Fixed: water light opacity on world.
-Fixed: water superficial face to display when you are below water too.
-Removed solitary water and lava blocks on worldgen.

v1.2.0
+MINOR update:
-Added: compat for most of ForgeFluid tanks for the finite fluid buckets.
-Added: compat for ClimaticBiomes (using my water instead of vanilla water).
-Added: debug option (see the water level over the block).

v1.1.0
+MINOR update: Flowing water now handles the interaction with still lava when water is above it, making the interaction happen faster due to the faster tickrate of water.

v1.0.1
+hotfix: Change water and lava tick rates.

v1.0.0
+Initial release.