v2.1.4
-Added: Command to kill invasive ocean blocks (when a mod places vanilla fluid blocks or you used my config option to make some modded fluids to be placed as ocean blocks, and one of these blocks have free way to replicate itself over an open area, the command /rffkillswitch will help you to stop this mess).
-Added: Config option to decide whether fluids can go downwards in the four cardinal directions or only laterally (to let fluids "leak" below edges or not, false by default, i like to not have leaks). Fixes #7.
-Fixed: Missing item model texture in JEI/Creative search (FluidBlocks Item Render).
-Fixed: Removed some reflextions from mixins.
-Fixed: Crash with Nothirium and Thaumic Isorropia.
-Fixed: Improved liquid spreading (Fluids will disperse more completely over large areas). 
-Fixed: Improved performance (removed ancient slow code to check pressure nodes for faster alternatives, removed unnecessary get/set IBlockState calls).
-Fixed: Crash with Fluidlogged API (fields with same names, and crash with the last FluidloggedAPI Mixin). Fixes #10.

v2.1.3
-Added: Glass bottles consume water too (almost 1/3 of block).
-Added: Config options to change aesthetic features of fluids (using both dynamic'n'static textures OR only using static texture, and if fluids should render as full blocks or not [vanilla-like]).
-Fixed: Not using the patched renderer when FluidLoggedAPI is not installed (not using the SRG name in the injected mixin. Why nobody told me about this render issue??).

v.2.1.2
-Added: Config option to decide which modded fluids should spawn as infinite fluids (can break easely).
-Fixed: Dupe bug on TileEntities that use FluidUtil#TryPlaceFluid() to place fluids in world.

v.2.1.1
-Fixed: Crash with Aqua Acrobatics.
-Fixed: Tweaked the compat for getFlowVector()/getFlow() to make still fluids to always give power (it is hard to make the right slope for some waterwheels).

v2.1.0
-Added: Fluidlogged API Compat.
-Added: ForgeEventFactory.fireFluidPlaceBlockEvent() integration.
-Added: Compat for waterwheels that use getFlowVector() or getFlow().
-Fixed: Render issue with blocks that are not fluids (i still don't fix the issue with FluidBlocks Item Render).

v2.0.0
-Removed: BlockFiniteFluid fluid blocks.
-Removed: Universal Compat Coremod.
-Added: Mixins to BlockFluidClassic, BlockDynamicLiquid, and BlockStaticLiquid.
-Added: Compat for the vanilla'n'forge renderers (BlockFluidRenderer, ModelFluid).
-Added: New fluid movement for gasses.

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

v1.7.1
-Added: New fluid barrier method: Vanilla (the best and default to use).
-Added: DataFixer to 1) automatically turn vanilla fluids into realistic finite fluids (replaceVanillaFluids is no longer required), 2) convert old oceanic fluids into still fluids and 3) convert the old 0-15 LEVEL of already existing fluids into the new 0-7 LEVEL interval.
-Note: I STILL RECOMMEND TO DO A BACKUP.

v1.7.0
-Added: Config option to let you decide whether mig masses of fluids (water/lava) should be infinite. (shouldFluidsBeInfinite).
-Refactor (BIG ONE): Removed Ocean Blocks (all the logic is carried by Flowing and Still fluids, for compat purposes).
-Refactor: Refactored all the references (i could find) about LEVELs (0,1,15,16, and others like 5,9,10,14, etc) to let me get these medium values (between 0-15/1-16) from a global Reference Value.
*This will allow me to modify the max and minimum values from 1 point.
-Refactor: Changed both literal and conceptual getVolume and setVolume for unification purposes (aparently nothing is broken).
-Refactor: Unified both world.setBlockState() and state.getValue(LEVEL) inside BlockFiniteFluid for global control over blocks placed and Property LEVEL reading.
*This is intended to implement compat with FluidLogged API.

v1.6.3
-Fixed: Superficial layer of water from rivers and ocean becomes ice on a cold biome when chunk is first generated.
-Refactor: Names of some classes (BlockNewWater and BlockNewLava).

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