package com.teamderpy.shouldersurfing.forge;

import com.teamderpy.shouldersurfing.client.KeyHandler;
import com.teamderpy.shouldersurfing.client.ShoulderInstance;
import com.teamderpy.shouldersurfing.client.ShoulderRenderer;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ViewportEvent.ComputeCameraAngles;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEventHandler
{
	@SubscribeEvent
	public static void clientTickEvent(ClientTickEvent event)
	{
		if(Phase.START.equals(event.phase))
		{
			ShoulderInstance.getInstance().tick();
		}
	}
	
	@SubscribeEvent
	public static void preRenderPlayerEvent(RenderPlayerEvent.Pre event)
	{
		if(event.isCancelable() && event.getEntity().equals(Minecraft.getInstance().player) && Minecraft.getInstance().screen == null && ShoulderRenderer.getInstance().skipRenderPlayer())
		{
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public static void preRenderGuiOverlayEvent(RenderGuiOverlayEvent.Pre event)
	{
		if(VanillaGuiOverlay.CROSSHAIR.id().equals(event.getOverlay().id()))
		{
			ShoulderRenderer.getInstance().offsetCrosshair(event.getPoseStack(), event.getWindow(), event.getPartialTick());
		}
		//Using BOSS_EVENT_PROGRESS to pop matrix because when CROSSHAIR is cancelled it will not fire RenderGuiOverlayEvent.Pre and cause a stack overflow
		else if(VanillaGuiOverlay.BOSS_EVENT_PROGRESS.id().equals(event.getOverlay().id()))
		{
			ShoulderRenderer.getInstance().clearCrosshairOffset(event.getPoseStack());
		}
	}
	
	@SubscribeEvent
	@SuppressWarnings("resource")
	public static void computeCameraAnglesEvent(ComputeCameraAngles event)
	{
		ShoulderRenderer.getInstance().offsetCamera(event.getCamera(), Minecraft.getInstance().level, event.getPartialTick());
	}
	
	@SubscribeEvent
	public static void renderLevelStageEvent(RenderLevelStageEvent event)
	{
		if(RenderLevelStageEvent.Stage.AFTER_SKY.equals(event.getStage()))
		{
			ShoulderRenderer.getInstance().updateDynamcRaytrace(event.getCamera(), event.getPoseStack().last().pose(), event.getProjectionMatrix(), event.getPartialTick());
		}
	}
	
	@SubscribeEvent
	public static void keyInputEvent(InputEvent.Key event)
	{
		KeyHandler.onKeyInput();
	}
}