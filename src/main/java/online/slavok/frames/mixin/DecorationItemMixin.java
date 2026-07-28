package online.slavok.frames.mixin;

//? if >=1.22 {
/*import online.slavok.frames.SimpleFramesMod;
import online.slavok.frames.FrameTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HangingEntityItem.class)
public class DecorationItemMixin {
    @Shadow
    protected boolean mayPlace(Player player, Direction side, ItemStack stack, BlockPos pos) {
        return true;
    }

    @Final
    @Shadow
    private EntityType<? extends HangingEntity> type;

    @Inject(at = @At("HEAD"), method = "useOn", cancellable = true)
    private void inject(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        try {
            BlockPos blockPos = context.getClickedPos();
            Direction direction = context.getClickedFace();
            BlockPos blockPos2 = blockPos.relative(direction);
            Player playerEntity = context.getPlayer();
            ItemStack itemStack = context.getItemInHand();

            if (playerEntity == null) return;
            if (!this.mayPlace(playerEntity, direction, itemStack, blockPos2)) return;

            // 26+ dropped the EntityType static constants; identify the frame by registry path.
            String typePath = BuiltInRegistries.ENTITY_TYPE.getKey(this.type).getPath();
            if (!typePath.equals("item_frame") && !typePath.equals("glow_item_frame")) return;

            CustomData data = itemStack.get(DataComponents.CUSTOM_DATA);
            if (data == null) return;
            CompoundTag nbt = data.copyTag();

            if (nbt.contains("invisibleframe")) {
                Level world = context.getLevel();
                ItemFrame frameEntity;
                if (typePath.equals("item_frame")) {
                    frameEntity = new ItemFrame(world, blockPos2, direction);
                } else {
                    frameEntity = new GlowItemFrame(world, blockPos2, direction);
                }
                FrameTags.add(frameEntity, FrameTags.INVISIBLE);

                if (frameEntity.survives()) {
                    if (world instanceof ServerLevel serverLevel) {
                        frameEntity.playPlacementSound();
                        serverLevel.addFreshEntity(frameEntity);
                    }

                    itemStack.shrink(1);
                    cir.setReturnValue(InteractionResult.SUCCESS);
                } else {
                    cir.setReturnValue(InteractionResult.CONSUME);
                }
            }
        } catch (Exception e) {
            SimpleFramesMod.LOGGER.error("SimpleFrames error on DecorationItemMixin: " + e);
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
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.GlowItemFrameEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DecorationItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DecorationItem.class)
public class DecorationItemMixin {
    @Shadow
    protected boolean canPlaceOn(PlayerEntity player, Direction side, ItemStack stack, BlockPos pos) {
        return true;
    }

    @Final
    @Shadow
    private EntityType<? extends AbstractDecorationEntity> entityType;


    @Inject(at = @At("HEAD"), method = "useOnBlock", cancellable = true)
    private void inject(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        try {
            BlockPos blockPos = context.getBlockPos();
            Direction direction = context.getSide();
            BlockPos blockPos2 = blockPos.offset(direction);
            PlayerEntity playerEntity = context.getPlayer();
            ItemStack itemStack = context.getStack();

            if (playerEntity == null) return;
            if (!this.canPlaceOn(playerEntity, direction, itemStack, blockPos2)) return;

            if (this.entityType != EntityType.ITEM_FRAME && this.entityType != EntityType.GLOW_ITEM_FRAME) return;

            //? if >=1.20.5 {
            NbtComponent nbtComponent = itemStack.get(DataComponentTypes.CUSTOM_DATA);
            if (nbtComponent == null) return;
            NbtCompound nbt = nbtComponent.copyNbt();
            //?} else {
            /*NbtCompound nbt = itemStack.getNbt();
            if (nbt == null) return;*/
            //?}

            if (nbt.contains("invisibleframe")) {
                World world = context.getWorld();
                AbstractDecorationEntity frameEntity;
                if (this.entityType == EntityType.ITEM_FRAME) {
                    frameEntity = new ItemFrameEntity(world, blockPos2, direction);
                } else {
                    frameEntity = new GlowItemFrameEntity(world, blockPos2, direction);
                }
                FrameTags.add(frameEntity, FrameTags.INVISIBLE);

                if (frameEntity.canStayAttached()) {
                    if (world instanceof ServerWorld) {
                        frameEntity.onPlace();
                        world.spawnEntity(frameEntity);
                    }

                    itemStack.decrement(1);
                    cir.setReturnValue(ActionResult.SUCCESS);
                } else {
                    cir.setReturnValue(ActionResult.CONSUME);
                }
            }
        } catch (Exception e) {
            SimpleFramesMod.LOGGER.error("SimpleFrames error on DecorationItemMixin: " + e);
        }
    }
}
//?}
