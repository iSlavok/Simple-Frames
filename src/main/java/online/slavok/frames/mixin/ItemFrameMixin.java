package online.slavok.frames.mixin;

//? if >=1.22 {
/*import online.slavok.frames.SimpleFramesMod;
import online.slavok.frames.FrameTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrame.class)
public class ItemFrameMixin {
	@Inject(at = @At("HEAD"), method = "hurtServer", cancellable = true)
	private void injectDamage(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		try {
			if (!(source.getEntity() instanceof Player player)) return;
			ItemStack itemStackInHand = player.getMainHandItem();
			ItemFrame frame = ((ItemFrame) (Object) this);
			boolean isInvisibleFrame = FrameTags.has(frame);

			if (itemStackInHand.getItem() == Items.SHEARS && !isInvisibleFrame) {
				if (!player.isCreative() && SimpleFramesMod.CONFIG.doShearsBreak) {
					if (itemStackInHand.getDamageValue() < itemStackInHand.getMaxDamage() - 1) {
						itemStackInHand.setDamageValue(itemStackInHand.getDamageValue() + 1);
					} else {
						itemStackInHand.shrink(1);
					}
				}
				world.playSound(null, frame.blockPosition(), SoundEvents.SNOW_GOLEM_SHEAR, SoundSource.NEUTRAL, 1f, 1.5f);
				world.sendParticles(ParticleTypes.CLOUD, frame.getX(), frame.getY(), frame.getZ(), 3, 0.0, 0.0, 0.0, 0.1);
				FrameTags.add(frame);
				if (!frame.getItem().isEmpty()) {
					frame.setInvisible(true);
				}
				cir.setReturnValue(true);
				cir.cancel();
				return;
			}

			if (itemStackInHand.getItem() == Items.LEATHER && isInvisibleFrame && SimpleFramesMod.CONFIG.fixWithLeather) {
				if (!player.isCreative()) { itemStackInHand.shrink(1); }
				world.playSound(null, frame.blockPosition(), SoundEvents.ITEM_FRAME_PLACE, SoundSource.NEUTRAL, 1f, 1.5f);
				world.sendParticles(ParticleTypes.CRIT, frame.getX(), frame.getY(), frame.getZ(), 10, 0.3, 0.3, 0.3, 0.1);
				frame.setInvisible(false);
				FrameTags.remove(frame);
				cir.setReturnValue(true);
				cir.cancel();
			}
		} catch (Exception e) {
			SimpleFramesMod.LOGGER.error("SimpleFrames error on ItemFrameMixin.hurtServer(): " + e);
		}
	}

	// Sync visibility after any damage (e.g. taking the item out by attacking):
	// an emptied invisible frame must become visible. Covers versions where item
	// removal doesn't route through dropItem.
	@Inject(at = @At("RETURN"), method = "hurtServer")
	private void syncAfterDamage(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		updateState();
	}

	@Inject(at = @At("RETURN"), method = "interact")
	private void injectInteract(Player player, InteractionHand hand, Vec3 hitPos, CallbackInfoReturnable<InteractionResult> cir) {
		updateState();
	}

	@Inject(at = @At("RETURN"), method = "dropItem")
	private void injectDropItem(ServerLevel world, Entity entity, CallbackInfo ci) {
		updateState();
	}

	@Inject(at = @At("TAIL"), method = "getFrameItemStack", cancellable = true)
	private void injectAsItem(CallbackInfoReturnable<ItemStack> cir) {
		try {
			if (!SimpleFramesMod.CONFIG.fixWithLeather) return;
			ItemFrame frame = ((ItemFrame) (Object) this);
			if (FrameTags.has(frame)) {
				ItemStack item = cir.getReturnValue();
				item.set(DataComponents.CUSTOM_NAME, Component.literal("Невидимая рамка"));
				item.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
				CustomData existing = item.get(DataComponents.CUSTOM_DATA);
				CompoundTag nbt = (existing == null) ? new CompoundTag() : existing.copyTag();
				nbt.putBoolean("invisibleframe", true);
				item.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
				cir.setReturnValue(item);
			}
		} catch (Exception e) {
			SimpleFramesMod.LOGGER.error("SimpleFrames error on ItemFrameMixin.getFrameItemStack(): " + e);
		}
	}

	private void updateState() {
		try {
			ItemFrame frame = ((ItemFrame) (Object) this);
			if (FrameTags.has(frame)) {
				frame.setInvisible(!frame.getItem().isEmpty());
			}
		} catch (Exception e) {
			SimpleFramesMod.LOGGER.error("SimpleFrames error on ItemFrameMixin.updateState(): " + e);
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(ItemFrameEntity.class)
public class ItemFrameMixin {
	@Inject(at = @At("HEAD"), method = "damage", cancellable = true)
	//? if >=1.21.2 {
	private void injectDamage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
	//?} else {
	/*private void injectDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {*/
	//?}
		try {
			if (source.getAttacker() == null || !source.getAttacker().isPlayer()) return;

			PlayerEntity player = (PlayerEntity) source.getAttacker();
			ItemStack itemStackInHand = player.getMainHandStack();

			ItemFrameEntity frame = ((ItemFrameEntity) (Object) this);
			//? if >=1.21.2 {
			ServerWorld serverWorld = world;
			//?} else {
			/*if (!(frame.getWorld() instanceof ServerWorld)) return;
			ServerWorld serverWorld = (ServerWorld) frame.getWorld();*/
			//?}
			boolean isInvisibleFrame = FrameTags.has(frame);

			// Shears -> make frame invisible
			if (itemStackInHand.getItem().getTranslationKey().equals("item.minecraft.shears") && !isInvisibleFrame) {
				if (!player.isCreative() && SimpleFramesMod.CONFIG.doShearsBreak) {
					if (itemStackInHand.getDamage() < itemStackInHand.getMaxDamage() - 1) {
						itemStackInHand.setDamage(itemStackInHand.getDamage() + 1);
					} else {
						itemStackInHand.decrement(1);
					}
				}
				serverWorld.playSound(null, frame.getBlockPos(), SoundEvents.ENTITY_SNOW_GOLEM_SHEAR, SoundCategory.NEUTRAL, 1f, 1.5f);
				serverWorld.spawnParticles(ParticleTypes.CLOUD, frame.getX(), frame.getY(), frame.getZ(), 3, 0.0, 0.0, 0.0, 0.1);

				FrameTags.add(frame);
				if (!frame.getHeldItemStack().isEmpty()) {
					frame.setInvisible(true);
				}

				cir.setReturnValue(true);
				cir.cancel();
				return;
			}

			// Leather -> restore frame back to normal
			if (itemStackInHand.getItem().getTranslationKey().equals("item.minecraft.leather") && isInvisibleFrame && SimpleFramesMod.CONFIG.fixWithLeather) {
				if (!player.isCreative()) { itemStackInHand.decrement(1); }
				serverWorld.playSound(null, frame.getBlockPos(), SoundEvents.ENTITY_ITEM_FRAME_PLACE, SoundCategory.NEUTRAL, 1f, 1.5f);
				serverWorld.spawnParticles(ParticleTypes.CRIT, frame.getX(), frame.getY(), frame.getZ(), 10, 0.3, 0.3, 0.3, 0.1);

				frame.setInvisible(false);
				FrameTags.remove(frame);

				cir.setReturnValue(true);
				cir.cancel();
			}
		} catch (Exception e) {
			SimpleFramesMod.LOGGER.error("SimpleFrames error on ItemFrameMixin.damage(): " + e);
		}
	}

	// Sync visibility after any damage (e.g. taking the item out by attacking):
	// an emptied invisible frame must become visible. Covers versions where item
	// removal doesn't route through dropHeldStack.
	@Inject(at = @At("RETURN"), method = "damage")
	//? if >=1.21.2 {
	private void syncAfterDamage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
	//?} else {
	/*private void syncAfterDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {*/
	//?}
		updateState();
	}

	@Inject(at = @At("RETURN"), method = "interact")
	private void injectInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		updateState();
	}

	@Inject(at = @At("RETURN"), method = "dropHeldStack")
	//? if >=1.21.2 {
	private void injectDropItem(ServerWorld world, Entity entity, boolean dropSelf, CallbackInfo ci) {
	//?} else {
	/*private void injectDropItem(Entity entity, boolean dropSelf, CallbackInfo ci) {*/
	//?}
		updateState();
	}

	@Inject(at = @At("TAIL"), method = "getAsItemStack", cancellable = true)
	private void injectAsItem(CallbackInfoReturnable<ItemStack> cir) {
		try {
			if (!SimpleFramesMod.CONFIG.fixWithLeather) return;
			ItemFrameEntity frame = ((ItemFrameEntity) (Object) this);
			if (FrameTags.has(frame)) {
				ItemStack item = cir.getReturnValue();
				//? if >=1.20.5 {
				item.set(DataComponentTypes.ITEM_NAME, Text.of("Невидимая рамка"));
				item.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
				NbtComponent nbtCompound = item.get(DataComponentTypes.CUSTOM_DATA);
				NbtCompound nbt = (nbtCompound == null) ? NbtComponent.DEFAULT.copyNbt() : nbtCompound.copyNbt();
				nbt.putBoolean("invisibleframe", true);
				item.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
				//?} else {
				/*item.setCustomName(Text.of("Невидимая рамка"));
				NbtCompound nbt = item.getOrCreateNbt();
				nbt.putBoolean("invisibleframe", true);*/
				//?}

				cir.setReturnValue(item);
			}
		} catch (Exception e) {
			SimpleFramesMod.LOGGER.error("SimpleFrames error on ItemFrameMixin.getAsItemStack(): " + e);
		}
	}

	private void updateState() {
		try {
			ItemFrameEntity frame = ((ItemFrameEntity) (Object) this);
			if (FrameTags.has(frame)) {
				frame.setInvisible(!frame.getHeldItemStack().isEmpty());
			}
		} catch (Exception e) {
			SimpleFramesMod.LOGGER.error("SimpleFrames error on ItemFrameMixin.updateState(): " + e);
		}
	}
}
//?}
