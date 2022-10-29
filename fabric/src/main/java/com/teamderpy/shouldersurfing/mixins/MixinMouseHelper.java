package com.teamderpy.shouldersurfing.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.teamderpy.shouldersurfing.client.KeyHandler;

import net.minecraft.client.MouseHelper;

@Mixin(MouseHelper.class)
public class MixinMouseHelper
{
	@Inject
	(
		method = "onPress",
		at = @At
		(
			value = "FIELD",
			target = "Lnet/minecraft/client/Minecraft;overlay",
			shift = Shift.BEFORE,
			ordinal = 0
		)
	)
	private void onPress(long window, int button, int action, int modifiers, CallbackInfo info)
	{
		KeyHandler.onInput();
	}
}
