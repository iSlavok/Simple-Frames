package online.slavok.frames.gametest;

import online.slavok.frames.SimpleFramesMod;
//? if >=1.21.2 {
import net.fabricmc.fabric.api.gametest.v1.GameTest;
//?} else {
/*import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;*/
//?}
//? if >=1.22 {
/*import net.minecraft.gametest.framework.GameTestHelper;*/
//?} else {
import net.minecraft.test.TestContext;
//?}
import net.minecraft.entity.decoration.GlowItemFrameEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * In-game apply/smoke test: boots a real headless server (so onInitialize runs and
 * the mixin config loads) and constructs the two entity types this mod mixes into.
 * With required mixins, a mis-targeted mixin crashes on class-load, so constructing
 * ItemFrameEntity + GlowItemFrameEntity and reaching the finish call proves both
 * mixins applied on this Minecraft version. Also round-trips the config toggle to
 * confirm the mod initialised. The shear/leather interaction needs a real player
 * attacker, which a headless gametest can't provide, so this stays apply-level.
 *
 * Only the GameTest harness (interface / annotation / context type / finish call)
 * is branched per version; the body touches only this mod's own API.
 */
//? if >=1.21.2 {
public class FramesGameTest {
//?} else {
/*public class FramesGameTest implements FabricGameTest {*/
//?}

    //? if >=1.21.2 {
    @GameTest
    //?} elif >=1.19 {
    /*@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)*/
    //?} else {
    /*@GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE)*/
    //?}
    //? if >=1.22 {
    /*public void framesApply(GameTestHelper context) {*/
    //?} else {
    public void framesApply(TestContext context) {
    //?}
        ServerWorld world = context.getWorld();
        BlockPos pos = new BlockPos(0, 0, 0);

        // Constructing these forces the mixin target classes to load; a required
        // mixin with a wrong target/signature would crash here.
        ItemFrameEntity frame = new ItemFrameEntity(world, pos, Direction.NORTH);
        GlowItemFrameEntity glow = new GlowItemFrameEntity(world, pos, Direction.NORTH);
        if (frame == null || glow == null) throw new RuntimeException("frame construction failed");

        // FrameTags round-trips independently for each tag on a real entity.
        online.slavok.frames.FrameTags.add(frame, online.slavok.frames.FrameTags.WAXED);
        if (!online.slavok.frames.FrameTags.has(frame, online.slavok.frames.FrameTags.WAXED)) {
            throw new RuntimeException("WAXED tag not added");
        }
        if (online.slavok.frames.FrameTags.has(frame, online.slavok.frames.FrameTags.INVISIBLE)) {
            throw new RuntimeException("WAXED must not imply INVISIBLE");
        }
        online.slavok.frames.FrameTags.remove(frame, online.slavok.frames.FrameTags.WAXED);
        if (online.slavok.frames.FrameTags.has(frame, online.slavok.frames.FrameTags.WAXED)) {
            throw new RuntimeException("WAXED tag not removed");
        }

        // Mod is initialised: config is live and toggles round-trip.
        boolean original = SimpleFramesMod.CONFIG.doShearsBreak;
        SimpleFramesMod.CONFIG.doShearsBreak = !original;
        if (SimpleFramesMod.CONFIG.doShearsBreak == original) {
            throw new RuntimeException("config toggle did not take effect");
        }
        SimpleFramesMod.CONFIG.doShearsBreak = original;

        boolean origWax = SimpleFramesMod.CONFIG.enableWax;
        SimpleFramesMod.CONFIG.enableWax = !origWax;
        if (SimpleFramesMod.CONFIG.enableWax == origWax) {
            throw new RuntimeException("enableWax toggle did not take effect");
        }
        SimpleFramesMod.CONFIG.enableWax = origWax;

        // Regression: evaluating the command permission predicate must not throw.
        // fabric-permissions-api's int check referenced a stale hasPermissionLevel
        // intermediary that crashed on player join (NoSuchMethodError).
        online.slavok.frames.commands.api.Permission.INSTANCE
                .require("simpleframes.command", 3)
                .test(world.getServer().getCommandSource());

        //? if >=1.22 {
        /*context.succeed();*/
        //?} else {
        context.complete();
        //?}
    }
}
