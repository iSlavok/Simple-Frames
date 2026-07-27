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
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
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
	private void injectDamage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		try {
			if (source.getAttacker() == null || !source.getAttacker().isPlayer()) return;

			PlayerEntity player = (PlayerEntity) source.getAttacker();
			ItemStack itemStackInHand = player.getInventory().getStack(player.getInventory().selectedSlot);

			ItemFrameEntity frame = ((ItemFrameEntity) (Object) this);
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
				frame.getWorld().playSound(
						null,
						frame.getBlockPos(),
						SoundEvents.ENTITY_SNOW_GOLEM_SHEAR,
						SoundCategory.NEUTRAL,
						1f,
						1.5f
				);

				SimpleFramesMod.sendPackets((ServerPlayerEntity) player, new ParticleS2CPacket(
						ParticleTypes.CLOUD,
						false,
						false,
						frame.getX(),
						frame.getY(),
						frame.getZ(),
						0f,
						0f,
						0f,
						0.1f,
						3
				));

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
				frame.getWorld().playSound(
						null,
						frame.getBlockPos(),
						SoundEvents.ENTITY_ITEM_FRAME_PLACE,
						SoundCategory.NEUTRAL,
						1f,
						1.5f
				);

				frame.setInvisible(false);
				frame.removeCommandTag("invisibleframe");

				SimpleFramesMod.sendPackets((ServerPlayerEntity) player, new ParticleS2CPacket(
						ParticleTypes.CRIT,
						false,
						false,
						frame.getX(),
						frame.getY(),
						frame.getZ(),
						0.3f,
						0.3f,
						0.3f,
						0.1f,
						10
				));

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
	private void injectDropItem(ServerWorld world, Entity entity, boolean dropSelf, CallbackInfo ci) {
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
