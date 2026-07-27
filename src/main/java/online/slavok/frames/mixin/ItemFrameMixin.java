package online.slavok.frames.mixin;

import online.slavok.frames.SimpleFramesMod;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
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
			/*ServerWorld serverWorld = (ServerWorld) frame.getWorld();*/
			//?}
			boolean isInvisibleFrame = frame.getCommandTags().contains("invisibleframe");

			// Shears -> make frame invisible
			if (itemStackInHand.getItem().getTranslationKey().equals("item.minecraft.shears") && !isInvisibleFrame) {
				if (!player.isCreative() && SimpleFramesMod.CONFIG.doShearsBreak) {
					if (itemStackInHand.getDamage() < 237) {
						itemStackInHand.damage(1, player, EquipmentSlot.MAINHAND);
					} else {
						itemStackInHand.decrement(1);
					}
				}
				serverWorld.playSound(null, frame.getBlockPos(), SoundEvents.ENTITY_SNOW_GOLEM_SHEAR, SoundCategory.NEUTRAL, 1f, 1.5f);
				serverWorld.spawnParticles(ParticleTypes.CLOUD, frame.getX(), frame.getY(), frame.getZ(), 3, 0.0, 0.0, 0.0, 0.1);

				frame.addCommandTag("invisibleframe");
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
				frame.removeCommandTag("invisibleframe");

				cir.setReturnValue(true);
				cir.cancel();
			}
		} catch (Exception e) {
			SimpleFramesMod.LOGGER.error("SimpleFrames error on ItemFrameMixin.damage(): " + e);
		}
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
			if (frame.getCommandTags().contains("invisibleframe")) {
				ItemStack item = cir.getReturnValue();
				item.set(DataComponentTypes.ITEM_NAME, Text.of("Невидимая рамка"));
				item.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

				NbtComponent nbtCompound = item.get(DataComponentTypes.CUSTOM_DATA);
				NbtCompound nbt = (nbtCompound == null) ? NbtComponent.DEFAULT.copyNbt() : nbtCompound.copyNbt();
				nbt.putBoolean("invisibleframe", true);
				item.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

				cir.setReturnValue(item);
			}
		} catch (Exception e) {
			SimpleFramesMod.LOGGER.error("SimpleFrames error on ItemFrameMixin.getAsItemStack(): " + e);
		}
	}

	private void updateState() {
		try {
			ItemFrameEntity frame = ((ItemFrameEntity) (Object) this);
			if (frame.getCommandTags().contains("invisibleframe")) {
				frame.setInvisible(!frame.getHeldItemStack().isEmpty());
			}
		} catch (Exception e) {
			SimpleFramesMod.LOGGER.error("SimpleFrames error on ItemFrameMixin.updateState(): " + e);
		}
	}
}
