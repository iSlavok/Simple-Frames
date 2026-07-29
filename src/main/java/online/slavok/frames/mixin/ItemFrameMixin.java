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
import net.minecraft.world.item.AxeItem;
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
			ItemFrame self = ((ItemFrame) (Object) this);
			if (SimpleFramesMod.CONFIG.enableWax && SimpleFramesMod.CONFIG.waxFullLock && FrameTags.has(self, FrameTags.WAXED)) {
				// Under full lock, a deliberate left-click with an axe removes the wax
				// (durability), instead of being denied — the only way left-click can act.
				if (SimpleFramesMod.CONFIG.axeButton.allowsLeft() && source.getEntity() instanceof Player axePlayer
						&& axePlayer.getMainHandItem().getItem() instanceof AxeItem) {
					ItemStack axe = axePlayer.getMainHandItem();
					if (!axePlayer.isCreative() && SimpleFramesMod.CONFIG.doAxeBreak && consumesDurability(world, axe)) {
						if (axe.getDamageValue() < axe.getMaxDamage() - 1) {
							axe.setDamageValue(axe.getDamageValue() + 1);
						} else {
							axe.shrink(1);
						}
					}
					FrameTags.remove(self, FrameTags.WAXED);
					world.playSound(null, self.blockPosition(), SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1f, 1.5f);
					world.sendParticles(ParticleTypes.WAX_OFF, self.getX(), self.getY(), self.getZ(), 7, 0.2, 0.2, 0.2, 0.1);
					cir.setReturnValue(false);
					cir.cancel();
					return;
				}
				if (source.getEntity() instanceof Player && self.level() instanceof ServerLevel sl) {
					sl.playSound(null, self.blockPosition(), SoundEvents.SHIELD_BLOCK.value(), SoundSource.NEUTRAL, 0.8f, 1.5f);
				}
				cir.setReturnValue(false);
				cir.cancel();
				return;
			}

			if (!(source.getEntity() instanceof Player player)) return;
			ItemStack itemStackInHand = player.getMainHandItem();
			ItemFrame frame = ((ItemFrame) (Object) this);
			boolean isInvisibleFrame = FrameTags.has(frame, FrameTags.INVISIBLE);

			if (itemStackInHand.getItem() == Items.SHEARS && !isInvisibleFrame && SimpleFramesMod.CONFIG.shearsButton.allowsLeft()) {
				if (!player.isCreative() && SimpleFramesMod.CONFIG.doShearsBreak && consumesDurability(world, itemStackInHand)) {
					if (itemStackInHand.getDamageValue() < itemStackInHand.getMaxDamage() - 1) {
						itemStackInHand.setDamageValue(itemStackInHand.getDamageValue() + 1);
					} else {
						itemStackInHand.shrink(1);
					}
				}
				world.playSound(null, frame.blockPosition(), SoundEvents.SNOW_GOLEM_SHEAR, SoundSource.NEUTRAL, 1f, 1.5f);
				world.sendParticles(ParticleTypes.CLOUD, frame.getX(), frame.getY(), frame.getZ(), 3, 0.0, 0.0, 0.0, 0.1);
				FrameTags.add(frame, FrameTags.INVISIBLE);
				if (!frame.getItem().isEmpty()) {
					frame.setInvisible(true);
				}
				cir.setReturnValue(true);
				cir.cancel();
				return;
			}

			if (itemStackInHand.getItem() == Items.LEATHER && isInvisibleFrame && SimpleFramesMod.CONFIG.fixWithLeather
					&& SimpleFramesMod.CONFIG.leatherButton.allowsLeft()) {
				if (!player.isCreative() && SimpleFramesMod.CONFIG.doLeatherConsume) { itemStackInHand.shrink(1); }
				world.playSound(null, frame.blockPosition(), SoundEvents.ITEM_FRAME_PLACE, SoundSource.NEUTRAL, 1f, 1.5f);
				world.sendParticles(ParticleTypes.CRIT, frame.getX(), frame.getY(), frame.getZ(), 10, 0.3, 0.3, 0.3, 0.1);
				frame.setInvisible(false);
				FrameTags.remove(frame, FrameTags.INVISIBLE);
				cir.setReturnValue(true);
				cir.cancel();
				return;
			}

			// Left-click honeycomb -> wax (mode allows left). Un-waxed frame holding an item.
			if (SimpleFramesMod.CONFIG.enableWax && SimpleFramesMod.CONFIG.honeycombButton.allowsLeft()
					&& itemStackInHand.getItem() == Items.HONEYCOMB
					&& !FrameTags.has(frame, FrameTags.WAXED) && !frame.getItem().isEmpty()) {
				if (!player.isCreative() && SimpleFramesMod.CONFIG.doHoneycombConsume) { itemStackInHand.shrink(1); }
				FrameTags.add(frame, FrameTags.WAXED);
				world.playSound(null, frame.blockPosition(), SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1f, 1.5f);
				world.sendParticles(ParticleTypes.WAX_ON, frame.getX(), frame.getY(), frame.getZ(), 7, 0.2, 0.2, 0.2, 0.1);
				cir.setReturnValue(true);
				cir.cancel();
				return;
			}

			if (SimpleFramesMod.CONFIG.enableWax && FrameTags.has(frame, FrameTags.WAXED) && !frame.getItem().isEmpty()) {
				FrameTags.remove(frame, FrameTags.WAXED);
				world.playSound(null, frame.blockPosition(), SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1f, 1.5f);
				world.sendParticles(ParticleTypes.WAX_OFF, frame.getX(), frame.getY(), frame.getZ(), 7, 0.2, 0.2, 0.2, 0.1);
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

	@Inject(at = @At("HEAD"), method = "interact", cancellable = true)
	private void injectWax(Player player, InteractionHand hand, Vec3 hitPos, CallbackInfoReturnable<InteractionResult> cir) {
		try {
			ItemFrame frame = ((ItemFrame) (Object) this);
			if (!(frame.level() instanceof ServerLevel serverLevel)) return;
			ItemStack held = player.getItemInHand(hand);
			boolean invisibleFrame = FrameTags.has(frame, FrameTags.INVISIBLE);

			// Tool interactions on right-click (invisibility is orthogonal to wax; handle
			// before the wax block and independent of enableWax). Plain right-click does the
			// mod action when it applies (shears -> a visible frame, leather -> an invisible
			// one) and cancels so the tool isn't placed; with no action to do (or when
			// sneaking) it falls through to vanilla so the tool can be placed / the item rotated.
			if (held.getItem() == Items.SHEARS && !invisibleFrame && SimpleFramesMod.CONFIG.shearsButton.allowsRight() && !player.isShiftKeyDown()) {
				if (!player.isCreative() && SimpleFramesMod.CONFIG.doShearsBreak && consumesDurability(serverLevel, held)) {
					if (held.getDamageValue() < held.getMaxDamage() - 1) {
						held.setDamageValue(held.getDamageValue() + 1);
					} else {
						held.shrink(1);
					}
				}
				FrameTags.add(frame, FrameTags.INVISIBLE);
				if (!frame.getItem().isEmpty()) {
					frame.setInvisible(true);
				}
				serverLevel.playSound(null, frame.blockPosition(), SoundEvents.SNOW_GOLEM_SHEAR, SoundSource.NEUTRAL, 1f, 1.5f);
				serverLevel.sendParticles(ParticleTypes.CLOUD, frame.getX(), frame.getY(), frame.getZ(), 3, 0.0, 0.0, 0.0, 0.1);
				cir.setReturnValue(InteractionResult.SUCCESS);
				cir.cancel();
				return;
			}
			if (held.getItem() == Items.LEATHER && invisibleFrame && SimpleFramesMod.CONFIG.fixWithLeather
					&& SimpleFramesMod.CONFIG.leatherButton.allowsRight() && !player.isShiftKeyDown()) {
				if (!player.isCreative() && SimpleFramesMod.CONFIG.doLeatherConsume) held.shrink(1);
				frame.setInvisible(false);
				FrameTags.remove(frame, FrameTags.INVISIBLE);
				serverLevel.playSound(null, frame.blockPosition(), SoundEvents.ITEM_FRAME_PLACE, SoundSource.NEUTRAL, 1f, 1.5f);
				serverLevel.sendParticles(ParticleTypes.CRIT, frame.getX(), frame.getY(), frame.getZ(), 10, 0.3, 0.3, 0.3, 0.1);
				cir.setReturnValue(InteractionResult.SUCCESS);
				cir.cancel();
				return;
			}

			if (!SimpleFramesMod.CONFIG.enableWax) return;
			boolean waxed = FrameTags.has(frame, FrameTags.WAXED);

			if (waxed && held.getItem() instanceof AxeItem && SimpleFramesMod.CONFIG.axeButton.allowsRight()) {
				if (!player.isCreative() && SimpleFramesMod.CONFIG.doAxeBreak && consumesDurability(serverLevel, held)) {
					if (held.getDamageValue() < held.getMaxDamage() - 1) {
						held.setDamageValue(held.getDamageValue() + 1);
					} else {
						held.shrink(1);
					}
				}
				FrameTags.remove(frame, FrameTags.WAXED);
				serverLevel.playSound(null, frame.blockPosition(), SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1f, 1.5f);
				serverLevel.sendParticles(ParticleTypes.WAX_OFF, frame.getX(), frame.getY(), frame.getZ(), 7, 0.2, 0.2, 0.2, 0.1);
				cir.setReturnValue(InteractionResult.SUCCESS);
				cir.cancel();
				return;
			}

			if (!waxed && held.getItem() == Items.HONEYCOMB && !frame.getItem().isEmpty()
					&& SimpleFramesMod.CONFIG.honeycombButton.allowsRight()) {
				if (!player.isCreative() && SimpleFramesMod.CONFIG.doHoneycombConsume) held.shrink(1);
				FrameTags.add(frame, FrameTags.WAXED);
				serverLevel.playSound(null, frame.blockPosition(), SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1f, 1.5f);
				serverLevel.sendParticles(ParticleTypes.WAX_ON, frame.getX(), frame.getY(), frame.getZ(), 7, 0.2, 0.2, 0.2, 0.1);
				cir.setReturnValue(InteractionResult.SUCCESS);
				cir.cancel();
				return;
			}

			if (waxed) {
				if (hand == InteractionHand.MAIN_HAND) {
					serverLevel.playSound(null, frame.blockPosition(), SoundEvents.SHIELD_BLOCK.value(), SoundSource.NEUTRAL, 0.8f, 1.5f);
				}
				cir.setReturnValue(InteractionResult.SUCCESS);
				cir.cancel();
			}
		} catch (Exception e) {
			SimpleFramesMod.LOGGER.error("SimpleFrames error on ItemFrameMixin.interact wax: " + e);
		}
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
			if (FrameTags.has(frame, FrameTags.INVISIBLE)) {
				ItemStack item = cir.getReturnValue();
				item.set(DataComponents.ITEM_NAME, Component.literal(SimpleFramesMod.CONFIG.invisibleFrameName));
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
			if (FrameTags.has(frame, FrameTags.INVISIBLE)) {
				frame.setInvisible(!frame.getItem().isEmpty());
			}
		} catch (Exception e) {
			SimpleFramesMod.LOGGER.error("SimpleFrames error on ItemFrameMixin.updateState(): " + e);
		}
	}

	// Vanilla Unbreaking for a non-armor tool: a durability point is applied only with
	// probability 1/(level+1) (skipped when random.nextInt(level+1) != 0). Level 0 ->
	// always applied, preserving the old unconditional behaviour.
	private static boolean consumesDurability(ServerLevel level, ItemStack stack) {
		int unbreaking = unbreakingLevel(level, stack);
		return unbreaking <= 0 || level.getRandom().nextInt(unbreaking + 1) == 0;
	}

	// Cross-version Unbreaking level lookup. Returns 0 on any failure, which is the safe
	// fallback (always consume durability = the previous behaviour).
	private static int unbreakingLevel(ServerLevel level, ItemStack stack) {
		try {
			net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> holder =
					level.registryAccess()
							.lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
							.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING);
			return net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
		} catch (Exception e) {
			SimpleFramesMod.LOGGER.error("SimpleFrames error resolving Unbreaking level: " + e);
			return 0;
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
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
//? if <1.20.5 {
/*import net.minecraft.nbt.NbtList;*/
//?}
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
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
			ItemFrameEntity self = ((ItemFrameEntity) (Object) this);
			if (SimpleFramesMod.CONFIG.enableWax && SimpleFramesMod.CONFIG.waxFullLock && FrameTags.has(self, FrameTags.WAXED)) {
				//? if >=1.21.10 {
				/*ServerWorld lockWorld = self.getEntityWorld() instanceof ServerWorld ? (ServerWorld) self.getEntityWorld() : null;*/
				//?} else {
				ServerWorld lockWorld = self.getWorld() instanceof ServerWorld ? (ServerWorld) self.getWorld() : null;
				//?}
				// Under full lock, a deliberate left-click with an axe removes the wax
				// (durability), instead of being denied — the only way left-click can act.
				if (lockWorld != null && SimpleFramesMod.CONFIG.axeButton.allowsLeft()
						&& source.getAttacker() instanceof PlayerEntity axePlayer
						&& axePlayer.getMainHandStack().getItem() instanceof AxeItem) {
					ItemStack axe = axePlayer.getMainHandStack();
					if (!axePlayer.isCreative() && SimpleFramesMod.CONFIG.doAxeBreak && consumesDurability(lockWorld, axe)) {
						if (axe.getDamage() < axe.getMaxDamage() - 1) {
							axe.setDamage(axe.getDamage() + 1);
						} else {
							axe.decrement(1);
						}
					}
					FrameTags.remove(self, FrameTags.WAXED);
					lockWorld.playSound(null, self.getBlockPos(), SoundEvents.ITEM_AXE_WAX_OFF, SoundCategory.BLOCKS, 1f, 1.5f);
					lockWorld.spawnParticles(ParticleTypes.WAX_OFF, self.getX(), self.getY(), self.getZ(), 7, 0.2, 0.2, 0.2, 0.1);
					cir.setReturnValue(false);
					cir.cancel();
					return;
				}
				if (lockWorld != null && source.getAttacker() instanceof PlayerEntity) {
					lockWorld.playSound(null, self.getBlockPos(), shieldBlockSound(), SoundCategory.NEUTRAL, 0.8f, 1.5f);
				}
				cir.setReturnValue(false);
				cir.cancel();
				return;
			}

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
			boolean isInvisibleFrame = FrameTags.has(frame, FrameTags.INVISIBLE);

			// Shears -> make frame invisible
			if (itemStackInHand.getItem().getTranslationKey().equals("item.minecraft.shears") && !isInvisibleFrame
					&& SimpleFramesMod.CONFIG.shearsButton.allowsLeft()) {
				if (!player.isCreative() && SimpleFramesMod.CONFIG.doShearsBreak && consumesDurability(serverWorld, itemStackInHand)) {
					if (itemStackInHand.getDamage() < itemStackInHand.getMaxDamage() - 1) {
						itemStackInHand.setDamage(itemStackInHand.getDamage() + 1);
					} else {
						itemStackInHand.decrement(1);
					}
				}
				serverWorld.playSound(null, frame.getBlockPos(), SoundEvents.ENTITY_SNOW_GOLEM_SHEAR, SoundCategory.NEUTRAL, 1f, 1.5f);
				serverWorld.spawnParticles(ParticleTypes.CLOUD, frame.getX(), frame.getY(), frame.getZ(), 3, 0.0, 0.0, 0.0, 0.1);

				FrameTags.add(frame, FrameTags.INVISIBLE);
				if (!frame.getHeldItemStack().isEmpty()) {
					frame.setInvisible(true);
				}

				cir.setReturnValue(true);
				cir.cancel();
				return;
			}

			// Leather -> restore frame back to normal
			if (itemStackInHand.getItem().getTranslationKey().equals("item.minecraft.leather") && isInvisibleFrame && SimpleFramesMod.CONFIG.fixWithLeather
					&& SimpleFramesMod.CONFIG.leatherButton.allowsLeft()) {
				if (!player.isCreative() && SimpleFramesMod.CONFIG.doLeatherConsume) { itemStackInHand.decrement(1); }
				serverWorld.playSound(null, frame.getBlockPos(), SoundEvents.ENTITY_ITEM_FRAME_PLACE, SoundCategory.NEUTRAL, 1f, 1.5f);
				serverWorld.spawnParticles(ParticleTypes.CRIT, frame.getX(), frame.getY(), frame.getZ(), 10, 0.3, 0.3, 0.3, 0.1);

				frame.setInvisible(false);
				FrameTags.remove(frame, FrameTags.INVISIBLE);

				cir.setReturnValue(true);
				cir.cancel();
				return;
			}

			// Left-click honeycomb -> wax (mode allows left). Un-waxed frame holding an item.
			if (SimpleFramesMod.CONFIG.enableWax && SimpleFramesMod.CONFIG.honeycombButton.allowsLeft()
					&& itemStackInHand.getItem().getTranslationKey().equals("item.minecraft.honeycomb")
					&& !FrameTags.has(frame, FrameTags.WAXED) && !frame.getHeldItemStack().isEmpty()) {
				if (!player.isCreative() && SimpleFramesMod.CONFIG.doHoneycombConsume) { itemStackInHand.decrement(1); }
				FrameTags.add(frame, FrameTags.WAXED);
				serverWorld.playSound(null, frame.getBlockPos(), SoundEvents.ITEM_HONEYCOMB_WAX_ON, SoundCategory.BLOCKS, 1f, 1.5f);
				serverWorld.spawnParticles(ParticleTypes.WAX_ON, frame.getX(), frame.getY(), frame.getZ(), 7, 0.2, 0.2, 0.2, 0.1);
				cir.setReturnValue(true);
				cir.cancel();
				return;
			}

			// var2: a normal player attack knocks the item out of a waxed frame -> remove
			// the wax so the now-empty frame is reusable (waxing needs an item).
			if (SimpleFramesMod.CONFIG.enableWax && FrameTags.has(frame, FrameTags.WAXED) && !frame.getHeldItemStack().isEmpty()) {
				FrameTags.remove(frame, FrameTags.WAXED);
				serverWorld.playSound(null, frame.getBlockPos(), SoundEvents.ITEM_AXE_WAX_OFF, SoundCategory.BLOCKS, 1f, 1.5f);
				serverWorld.spawnParticles(ParticleTypes.WAX_OFF, frame.getX(), frame.getY(), frame.getZ(), 7, 0.2, 0.2, 0.2, 0.1);
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

	@Inject(at = @At("HEAD"), method = "interact", cancellable = true)
	private void injectWax(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		try {
			ItemFrameEntity frame = ((ItemFrameEntity) (Object) this);
			//? if >=1.21.10 {
			/*if (!(frame.getEntityWorld() instanceof ServerWorld serverWorld)) return;*/
			//?} else {
			if (!(frame.getWorld() instanceof ServerWorld serverWorld)) return;
			//?}
			ItemStack held = player.getStackInHand(hand);
			boolean invisibleFrame = FrameTags.has(frame, FrameTags.INVISIBLE);

			// Tool interactions on right-click (invisibility is orthogonal to wax; handle
			// before the wax block and independent of enableWax). Plain right-click does the
			// mod action when it applies (shears -> a visible frame, leather -> an invisible
			// one) and cancels so the tool isn't placed; with no action to do (or when
			// sneaking) it falls through to vanilla so the tool can be placed / the item rotated.
			if (held.getItem().getTranslationKey().equals("item.minecraft.shears") && !invisibleFrame
					&& SimpleFramesMod.CONFIG.shearsButton.allowsRight() && !player.isSneaking()) {
				if (!player.isCreative() && SimpleFramesMod.CONFIG.doShearsBreak && consumesDurability(serverWorld, held)) {
					if (held.getDamage() < held.getMaxDamage() - 1) {
						held.setDamage(held.getDamage() + 1);
					} else {
						held.decrement(1);
					}
				}
				FrameTags.add(frame, FrameTags.INVISIBLE);
				if (!frame.getHeldItemStack().isEmpty()) {
					frame.setInvisible(true);
				}
				serverWorld.playSound(null, frame.getBlockPos(), SoundEvents.ENTITY_SNOW_GOLEM_SHEAR, SoundCategory.NEUTRAL, 1f, 1.5f);
				serverWorld.spawnParticles(ParticleTypes.CLOUD, frame.getX(), frame.getY(), frame.getZ(), 3, 0.0, 0.0, 0.0, 0.1);
				cir.setReturnValue(ActionResult.SUCCESS);
				cir.cancel();
				return;
			}
			if (held.getItem().getTranslationKey().equals("item.minecraft.leather") && invisibleFrame
					&& SimpleFramesMod.CONFIG.fixWithLeather && SimpleFramesMod.CONFIG.leatherButton.allowsRight() && !player.isSneaking()) {
				if (!player.isCreative() && SimpleFramesMod.CONFIG.doLeatherConsume) held.decrement(1);
				frame.setInvisible(false);
				FrameTags.remove(frame, FrameTags.INVISIBLE);
				serverWorld.playSound(null, frame.getBlockPos(), SoundEvents.ENTITY_ITEM_FRAME_PLACE, SoundCategory.NEUTRAL, 1f, 1.5f);
				serverWorld.spawnParticles(ParticleTypes.CRIT, frame.getX(), frame.getY(), frame.getZ(), 10, 0.3, 0.3, 0.3, 0.1);
				cir.setReturnValue(ActionResult.SUCCESS);
				cir.cancel();
				return;
			}

			if (!SimpleFramesMod.CONFIG.enableWax) return;
			boolean waxed = FrameTags.has(frame, FrameTags.WAXED);

			// Axe on a waxed frame -> remove wax.
			if (waxed && held.getItem() instanceof AxeItem && SimpleFramesMod.CONFIG.axeButton.allowsRight()) {
				if (!player.isCreative() && SimpleFramesMod.CONFIG.doAxeBreak && consumesDurability(serverWorld, held)) {
					if (held.getDamage() < held.getMaxDamage() - 1) {
						held.setDamage(held.getDamage() + 1);
					} else {
						held.decrement(1);
					}
				}
				FrameTags.remove(frame, FrameTags.WAXED);
				serverWorld.playSound(null, frame.getBlockPos(), SoundEvents.ITEM_AXE_WAX_OFF, SoundCategory.BLOCKS, 1f, 1.5f);
				serverWorld.spawnParticles(ParticleTypes.WAX_OFF, frame.getX(), frame.getY(), frame.getZ(), 7, 0.2, 0.2, 0.2, 0.1);
				cir.setReturnValue(ActionResult.SUCCESS);
				cir.cancel();
				return;
			}

			// Honeycomb on an un-waxed frame that holds an item -> wax it.
			if (!waxed && held.getItem().getTranslationKey().equals("item.minecraft.honeycomb") && !frame.getHeldItemStack().isEmpty()
					&& SimpleFramesMod.CONFIG.honeycombButton.allowsRight()) {
				if (!player.isCreative() && SimpleFramesMod.CONFIG.doHoneycombConsume) held.decrement(1);
				FrameTags.add(frame, FrameTags.WAXED);
				serverWorld.playSound(null, frame.getBlockPos(), SoundEvents.ITEM_HONEYCOMB_WAX_ON, SoundCategory.BLOCKS, 1f, 1.5f);
				serverWorld.spawnParticles(ParticleTypes.WAX_ON, frame.getX(), frame.getY(), frame.getZ(), 7, 0.2, 0.2, 0.2, 0.1);
				cir.setReturnValue(ActionResult.SUCCESS);
				cir.cancel();
				return;
			}

			// Waxed frame -> block rotation / item change; denied feedback (main hand only).
			if (waxed) {
				if (hand == Hand.MAIN_HAND) {
					serverWorld.playSound(null, frame.getBlockPos(), shieldBlockSound(), SoundCategory.NEUTRAL, 0.8f, 1.5f);
				}
				cir.setReturnValue(ActionResult.SUCCESS);
				cir.cancel();
			}
		} catch (Exception e) {
			SimpleFramesMod.LOGGER.error("SimpleFrames error on ItemFrameMixin.interact wax: " + e);
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
			if (FrameTags.has(frame, FrameTags.INVISIBLE)) {
				ItemStack item = cir.getReturnValue();
				//? if >=1.20.5 {
				item.set(DataComponentTypes.ITEM_NAME, Text.of(SimpleFramesMod.CONFIG.invisibleFrameName));
				item.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
				NbtComponent nbtCompound = item.get(DataComponentTypes.CUSTOM_DATA);
				NbtCompound nbt = (nbtCompound == null) ? NbtComponent.DEFAULT.copyNbt() : nbtCompound.copyNbt();
				nbt.putBoolean("invisibleframe", true);
				item.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
				//?} else {
				/*item.setCustomName(online.slavok.frames.CompatTextKt.literalText(SimpleFramesMod.CONFIG.invisibleFrameName).setStyle(net.minecraft.text.Style.EMPTY.withItalic(false).withFormatting(net.minecraft.util.Formatting.WHITE)));
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
			SimpleFramesMod.LOGGER.error("SimpleFrames error on ItemFrameMixin.getAsItemStack(): " + e);
		}
	}

	private void updateState() {
		try {
			ItemFrameEntity frame = ((ItemFrameEntity) (Object) this);
			if (FrameTags.has(frame, FrameTags.INVISIBLE)) {
				frame.setInvisible(!frame.getHeldItemStack().isEmpty());
			}
		} catch (Exception e) {
			SimpleFramesMod.LOGGER.error("SimpleFrames error on ItemFrameMixin.updateState(): " + e);
		}
	}

	// SoundEvents.ITEM_SHIELD_BLOCK became a RegistryEntry.Reference<SoundEvent> (instead of a
	// plain SoundEvent) starting 1.21.5, so playSound(..., SoundEvent, ...) needs .value() from
	// that version on.
	private static SoundEvent shieldBlockSound() {
		//? if >=1.21.5 {
		return SoundEvents.ITEM_SHIELD_BLOCK.value();
		//?} else {
		/*return SoundEvents.ITEM_SHIELD_BLOCK;*/
		//?}
	}

	// Vanilla Unbreaking for a non-armor tool: a durability point is applied only with
	// probability 1/(level+1) (skipped when random.nextInt(level+1) != 0). Level 0 ->
	// always applied, preserving the old unconditional behaviour.
	private static boolean consumesDurability(ServerWorld world, ItemStack stack) {
		int level = unbreakingLevel(world, stack);
		return level <= 0 || world.getRandom().nextInt(level + 1) == 0;
	}

	// Cross-version Unbreaking level lookup. Returns 0 on any failure, which is the safe
	// fallback (always consume durability = the previous behaviour). The enchantment
	// registry rework landed in 1.21: from then on UNBREAKING is a RegistryKey and the
	// level is looked up through a RegistryEntry; before that it's a plain Enchantment.
	// 1.21.2+ resolves UNBREAKING via manager.getOrThrow(...).getOrThrow(...); 1.21-1.21.1
	// the manager exposes getOptionalWrapper (renamed to getOrThrow in 1.21.2) and only
	// RegistryWrapper.Impl turns a RegistryKey into a RegistryEntry; <1.21 UNBREAKING is a
	// plain Enchantment object.
	private static int unbreakingLevel(ServerWorld world, ItemStack stack) {
		try {
			//? if >=1.21.2 {
			net.minecraft.registry.entry.RegistryEntry<net.minecraft.enchantment.Enchantment> entry =
					world.getRegistryManager()
							.getOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT)
							.getOrThrow(net.minecraft.enchantment.Enchantments.UNBREAKING);
			return net.minecraft.enchantment.EnchantmentHelper.getLevel(entry, stack);
			//?} elif >=1.21 {
			/*net.minecraft.registry.entry.RegistryEntry<net.minecraft.enchantment.Enchantment> entry =
					world.getRegistryManager()
							.getOptionalWrapper(net.minecraft.registry.RegistryKeys.ENCHANTMENT).orElseThrow()
							.getOrThrow(net.minecraft.enchantment.Enchantments.UNBREAKING);
			return net.minecraft.enchantment.EnchantmentHelper.getLevel(entry, stack);*/
			//?} else {
			/*return net.minecraft.enchantment.EnchantmentHelper.getLevel(
					net.minecraft.enchantment.Enchantments.UNBREAKING, stack);*/
			//?}
		} catch (Exception e) {
			SimpleFramesMod.LOGGER.error("SimpleFrames error resolving Unbreaking level: " + e);
			return 0;
		}
	}
}
//?}
