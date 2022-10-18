package com.teamderpy.shouldersurfing.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.teamderpy.shouldersurfing.client.ShoulderRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;

@Mixin(GameRenderer.class)
public class MixinGameRenderer2
{
	@Shadow
	private ActiveRenderInfo mainCamera;
	
	@Inject
	(
		method = "renderLevel",
		at = @At
		(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/ActiveRenderInfo;setup(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/entity/Entity;ZZF)V",
			shift = Shift.AFTER
		)
	)
	@SuppressWarnings("resource")
	private void onCameraSetup(float partialTick, long nanos, MatrixStack poseStack, CallbackInfo ci)
	{
		ShoulderRenderer.getInstance().offsetCamera(this.mainCamera, Minecraft.getInstance().level, partialTick);
	}
}