package online.slavok.frames;

import net.minecraft.entity.Entity;

/**
 * Marker tag used to identify frames the mod has made invisible.
 *
 * The entity "tag" API was renamed piecemeal across versions
 * (getScoreboardTags/addScoreboardTag/removeScoreboardTag -> getCommandTags/
 * addCommandTag/removeCommandTag; get/add flipped at 1.19, remove at 1.20; 26+
 * uses the Mojang getTags/addTag/removeTag), so it is branched once here instead
 * of at every call site.
 */
public final class FrameTags {
    public static final String INVISIBLE = "invisibleframe";

    private FrameTags() {}

    public static boolean has(Entity entity) {
        //? if >=1.19 {
        return entity.getCommandTags().contains(INVISIBLE);
        //?} else {
        /*return entity.getScoreboardTags().contains(INVISIBLE);*/
        //?}
    }

    public static void add(Entity entity) {
        //? if >=1.19 {
        entity.addCommandTag(INVISIBLE);
        //?} else {
        /*entity.addScoreboardTag(INVISIBLE);*/
        //?}
    }

    public static void remove(Entity entity) {
        //? if >=1.20 {
        entity.removeCommandTag(INVISIBLE);
        //?} else {
        /*entity.removeScoreboardTag(INVISIBLE);*/
        //?}
    }
}
