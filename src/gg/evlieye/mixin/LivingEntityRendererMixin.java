/*

 *



 */
package gg.evlieye.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import gg.evlieye.EvlieyeClient;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin
{
	/**
	 * Makes invisible entities render as ghosts if TrueSight is enabled.
	 */
	@Redirect(at = @At(value = "INVOKE",
		target = "Lnet/minecraft/entity/LivingEntity;isInvisibleTo(Lnet/minecraft/entity/player/PlayerEntity;)Z",
		ordinal = 0),
		method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	private boolean isInvisibleToEvlieye(LivingEntity e, PlayerEntity player)
	{
		if(EvlieyeClient.INSTANCE.getHax().trueSightHack.isEnabled())
			return false;
		
		return e.isInvisibleTo(player);
	}
	
	/**
	 * Disables the distance limit in hasLabel() if configured in NameTags.
	 */
	@Redirect(at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;getSquaredDistanceToCamera(Lnet/minecraft/entity/Entity;)D",
		ordinal = 0), method = "hasLabel(Lnet/minecraft/entity/LivingEntity;)Z")
	private double adjustDistance(EntityRenderDispatcher render, Entity entity)
	{
		// pretend the distance is 1 so the check always passes
		if(EvlieyeClient.INSTANCE.getHax().nameTagsHack.isUnlimitedRange())
			return 1;
		
		return render.getSquaredDistanceToCamera(entity);
	}
	
	/**
	 * Forces the nametag to be rendered if configured in NameTags.
	 */
	@Inject(at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/MinecraftClient;getInstance()Lnet/minecraft/client/MinecraftClient;",
		ordinal = 0),
		method = "hasLabel(Lnet/minecraft/entity/LivingEntity;)Z",
		cancellable = true)
	private void shouldForceLabel(LivingEntity e,
		CallbackInfoReturnable<Boolean> cir)
	{
		// return true immediately after the distance check
		if(EvlieyeClient.INSTANCE.getHax().nameTagsHack.shouldForceNametags())
			cir.setReturnValue(true);
	}
}
