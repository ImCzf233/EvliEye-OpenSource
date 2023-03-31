/*

 *



 */
package gg.evlieye.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;
import gg.evlieye.EvlieyeClient;
import gg.evlieye.event.EventManager;
import gg.evlieye.events.AirStrafingSpeedListener.AirStrafingSpeedEvent;
import gg.evlieye.events.IsPlayerInLavaListener.IsPlayerInLavaEvent;
import gg.evlieye.events.IsPlayerInWaterListener.IsPlayerInWaterEvent;
import gg.evlieye.events.KnockbackListener.KnockbackEvent;
import gg.evlieye.events.PlayerMoveListener.PlayerMoveEvent;
import gg.evlieye.events.PostMotionListener.PostMotionEvent;
import gg.evlieye.events.PreMotionListener.PreMotionEvent;
import gg.evlieye.events.UpdateListener.UpdateEvent;
import gg.evlieye.hack.HackList;
import gg.evlieye.mixinterface.IClientPlayerEntity;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin extends AbstractClientPlayerEntity
	implements IClientPlayerEntity
{
	@Shadow
	private float lastYaw;
	@Shadow
	private float lastPitch;
	@Shadow
	private ClientPlayNetworkHandler networkHandler;
	@Shadow
	@Final
	protected MinecraftClient client;
	
	private Screen tempCurrentScreen;
	
	public ClientPlayerEntityMixin(EvlieyeClient wurst, ClientWorld world,
		GameProfile profile)
	{
		super(world, profile);
	}
	
	@Inject(at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V",
		ordinal = 0), method = "tick()V")
	private void onTick(CallbackInfo ci)
	{
		EventManager.fire(UpdateEvent.INSTANCE);
	}
	
	@Redirect(at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z",
		ordinal = 0), method = "tickMovement()V")
	private boolean wurstIsUsingItem(ClientPlayerEntity player)
	{
		if(EvlieyeClient.INSTANCE.getHax().noSlowdownHack.isEnabled())
			return false;
		
		return player.isUsingItem();
	}
	
	@Inject(at = {@At("HEAD")}, method = {"sendMovementPackets()V"})
	private void onSendMovementPacketsHEAD(CallbackInfo ci)
	{
		EventManager.fire(PreMotionEvent.INSTANCE);
	}
	
	@Inject(at = {@At("TAIL")}, method = {"sendMovementPackets()V"})
	private void onSendMovementPacketsTAIL(CallbackInfo ci)
	{
		EventManager.fire(PostMotionEvent.INSTANCE);
	}
	
	@Inject(at = {@At("HEAD")},
		method = {
			"move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"})
	private void onMove(MovementType type, Vec3d offset, CallbackInfo ci)
	{
		PlayerMoveEvent event = new PlayerMoveEvent(this);
		EventManager.fire(event);
	}
	
	@Inject(at = {@At("HEAD")},
		method = {"isAutoJumpEnabled()Z"},
		cancellable = true)
	private void onIsAutoJumpEnabled(CallbackInfoReturnable<Boolean> cir)
	{
		if(!EvlieyeClient.INSTANCE.getHax().stepHack.isAutoJumpAllowed())
			cir.setReturnValue(false);
	}
	
	@Inject(at = @At(value = "FIELD",
		target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;",
		opcode = Opcodes.GETFIELD,
		ordinal = 0), method = {"updateNausea()V"})
	private void beforeUpdateNausea(CallbackInfo ci)
	{
		if(!EvlieyeClient.INSTANCE.getHax().portalGuiHack.isEnabled())
			return;
		
		tempCurrentScreen = client.currentScreen;
		client.currentScreen = null;
	}
	
	@Inject(at = @At(value = "FIELD",
		target = "Lnet/minecraft/client/network/ClientPlayerEntity;nextNauseaStrength:F",
		opcode = Opcodes.GETFIELD,
		ordinal = 1), method = {"updateNausea()V"})
	private void afterUpdateNausea(CallbackInfo ci)
	{
		if(tempCurrentScreen == null)
			return;
		
		client.currentScreen = tempCurrentScreen;
		tempCurrentScreen = null;
	}
	
	/**
	 * Getter method for what used to be airStrafingSpeed.
	 * Overridden to allow for the speed to be modified by hacks.
	 */
	@Override
	protected float getOffGroundSpeed()
	{
		AirStrafingSpeedEvent event =
			new AirStrafingSpeedEvent(super.getOffGroundSpeed());
		EventManager.fire(event);
		return event.getSpeed();
	}
	
	@Override
	public void setVelocityClient(double x, double y, double z)
	{
		KnockbackEvent event = new KnockbackEvent(x, y, z);
		EventManager.fire(event);
		super.setVelocityClient(event.getX(), event.getY(), event.getZ());
	}
	
	@Override
	public boolean isTouchingWater()
	{
		boolean inWater = super.isTouchingWater();
		IsPlayerInWaterEvent event = new IsPlayerInWaterEvent(inWater);
		EventManager.fire(event);
		
		return event.isInWater();
	}
	
	@Override
	public boolean isInLava()
	{
		boolean inLava = super.isInLava();
		IsPlayerInLavaEvent event = new IsPlayerInLavaEvent(inLava);
		EventManager.fire(event);
		
		return event.isInLava();
	}
	
	@Override
	public boolean isSpectator()
	{
		return super.isSpectator()
			|| EvlieyeClient.INSTANCE.getHax().freecamHack.isEnabled();
	}
	
	@Override
	public boolean isTouchingWaterBypass()
	{
		return super.isTouchingWater();
	}
	
	@Override
	protected float getJumpVelocity()
	{
		return super.getJumpVelocity()
			+ EvlieyeClient.INSTANCE.getHax().highJumpHack
				.getAdditionalJumpMotion();
	}
	
	@Override
	protected boolean clipAtLedge()
	{
		return super.clipAtLedge()
			|| EvlieyeClient.INSTANCE.getHax().safeWalkHack.isEnabled();
	}
	
	@Override
	protected Vec3d adjustMovementForSneaking(Vec3d movement, MovementType type)
	{
		Vec3d result = super.adjustMovementForSneaking(movement, type);
		
		if(movement != null)
			EvlieyeClient.INSTANCE.getHax().safeWalkHack
				.onClipAtLedge(!movement.equals(result));
		
		return result;
	}
	
	@Override
	public boolean hasStatusEffect(StatusEffect effect)
	{
		HackList hax = EvlieyeClient.INSTANCE.getHax();
		
		if(effect == StatusEffects.NIGHT_VISION
			&& hax.fullbrightHack.isNightVisionActive())
			return true;
		
		if(effect == StatusEffects.LEVITATION
			&& hax.noLevitationHack.isEnabled())
			return false;
		
		return super.hasStatusEffect(effect);
	}
	
	@Override
	public void setNoClip(boolean noClip)
	{
		this.noClip = noClip;
	}
	
	@Override
	public float getLastYaw()
	{
		return lastYaw;
	}
	
	@Override
	public float getLastPitch()
	{
		return lastPitch;
	}
	
	@Override
	public void setMovementMultiplier(Vec3d movementMultiplier)
	{
		this.movementMultiplier = movementMultiplier;
	}
}
