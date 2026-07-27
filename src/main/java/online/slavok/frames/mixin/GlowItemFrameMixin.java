package online.slavok.frames.mixin;

//? if >=1.22 {
/*import online.slavok.frames.SimpleFramesMod;
import online.slavok.frames.FrameTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlowItemFrame.class)
public class GlowItemFrameMixin {
    @Inject(at = @At("TAIL"), method = "getFrameItemStack", cancellable = true)
    private void injectAsItem(CallbackInfoReturnable<ItemStack> cir) {
        try {
            if (!SimpleFramesMod.CONFIG.fixWithLeather) return;
            ItemFrame frame = ((ItemFrame) (Object) this);
            if (FrameTags.has(frame)) {
                ItemStack item = cir.getReturnValue();
                item.set(DataComponents.ITEM_NAME, Component.literal(SimpleFramesMod.CONFIG.invisibleGlowFrameName));
                item.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
                CustomData existing = item.get(DataComponents.CUSTOM_DATA);
                CompoundTag nbt = (existing == null) ? new CompoundTag() : existing.copyTag();
                nbt.putBoolean("invisibleframe", true);
                item.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
                cir.setReturnValue(item);
            }
        } catch (Exception e) {
            SimpleFramesMod.LOGGER.error("SimpleFrames error on GlowItemFrameMixin: " + e);
        }
    }
}
*///?} else {
import online.slavok.frames.SimpleFramesMod;
import online.slavok.frames.FrameTags;
//? if >=1.20.5 {
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
//?}
import net.minecraft.entity.decoration.GlowItemFrameEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
//? if <1.20.5 {
/*import net.minecraft.nbt.NbtList;*/
//?}
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlowItemFrameEntity.class)
public class GlowItemFrameMixin {
    @Inject(at = @At("TAIL"), method = "getAsItemStack", cancellable = true)
    private void injectAsItem(CallbackInfoReturnable<ItemStack> cir) {
        try {
            if (!SimpleFramesMod.CONFIG.fixWithLeather) return;
            ItemFrameEntity frame = ((ItemFrameEntity) (Object) this);
            if (FrameTags.has(frame)) {
                ItemStack item = cir.getReturnValue();
                //? if >=1.20.5 {
                item.set(DataComponentTypes.ITEM_NAME, Text.of(SimpleFramesMod.CONFIG.invisibleGlowFrameName));
                item.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
                NbtComponent nbtCompound = item.get(DataComponentTypes.CUSTOM_DATA);
                NbtCompound nbt = (nbtCompound == null) ? NbtComponent.DEFAULT.copyNbt() : nbtCompound.copyNbt();
                nbt.putBoolean("invisibleframe", true);
                item.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
                //?} else {
                /*item.setCustomName(online.slavok.frames.CompatTextKt.literalText(SimpleFramesMod.CONFIG.invisibleGlowFrameName).setStyle(net.minecraft.text.Style.EMPTY.withItalic(false).withFormatting(net.minecraft.util.Formatting.WHITE)));
                NbtCompound nbt = item.getOrCreateNbt();
                nbt.putBoolean("invisibleframe", true);
                // Glint without a visible enchant (pre-1.20.5 has no glint component).
                NbtList frameGlint = new NbtList();
                frameGlint.add(new NbtCompound());
                nbt.put("Enchantments", frameGlint);*/
                //?}

                cir.setReturnValue(item);
            }
        } catch (Exception e) {
            SimpleFramesMod.LOGGER.error("SimpleFrames error on GlowItemFrameMixin: " + e);
        }
    }
}
//?}
