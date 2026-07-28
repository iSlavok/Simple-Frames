package online.slavok.frames;

//? if >=1.22 {
/*import net.minecraft.world.entity.Entity;*/
//?} else {
import net.minecraft.entity.Entity;
//?}

/**
 * Marker tags used to identify frames in either mod state: made invisible
 * (shears) or waxed/rotation-locked (honeycomb).
 *
 * The entity "tag" API was renamed piecemeal across versions
 * (getScoreboardTags/addScoreboardTag/removeScoreboardTag -> getCommandTags/
 * addCommandTag/removeCommandTag; get/add flipped at 1.19, remove at 1.20; 26+
 * uses the Mojang entityTags/addTag/removeTag), so it is branched once here
 * instead of at every call site.
 */
public final class FrameTags {
    public static final String INVISIBLE = "invisibleframe";
    public static final String WAXED = "waxedframe";

    private FrameTags() {}

    public static boolean has(Entity entity, String tag) {
        //? if >=1.22 {
        /*return entity.entityTags().contains(tag);*/
        //?} elif >=1.19 {
        return entity.getCommandTags().contains(tag);
        //?} else {
        /*return entity.getScoreboardTags().contains(tag);*/
        //?}
    }

    public static void add(Entity entity, String tag) {
        //? if >=1.22 {
        /*entity.addTag(tag);*/
        //?} elif >=1.19 {
        entity.addCommandTag(tag);
        //?} else {
        /*entity.addScoreboardTag(tag);*/
        //?}
    }

    public static void remove(Entity entity, String tag) {
        //? if >=1.22 {
        /*entity.removeTag(tag);*/
        //?} elif >=1.20 {
        entity.removeCommandTag(tag);
        //?} else {
        /*entity.removeScoreboardTag(tag);*/
        //?}
    }
}
