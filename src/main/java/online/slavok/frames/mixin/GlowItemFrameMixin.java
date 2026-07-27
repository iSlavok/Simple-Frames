package online.slavok.frames.mixin;

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
                item.set(DataComponentTypes.ITEM_NAME, Text.of("Невидимая светящаяся рамка"));
                item.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
                NbtComponent nbtCompound = item.get(DataComponentTypes.CUSTOM_DATA);
                NbtCompound nbt = (nbtCompound == null) ? NbtComponent.DEFAULT.copyNbt() : nbtCompound.copyNbt();
                nbt.putBoolean("invisibleframe", true);
                item.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
                //?} else {
                /*item.setCustomName(Text.of("Невидимая светящаяся рамка"));
                NbtCompound nbt = item.getOrCreateNbt();
                nbt.putBoolean("invisibleframe", true);*/
                //?}

                cir.setReturnValue(item);
            }
        } catch (Exception e) {
            SimpleFramesMod.LOGGER.error("SimpleFrames error on GlowItemFrameMixin: " + e);
        }
    }
}
