package com.teamderpy.shouldersurfing;

import com.teamderpy.shouldersurfing.client.KeyHandler;
import com.teamderpy.shouldersurfing.config.Config;
import com.teamderpy.shouldersurfing.plugin.PluginLoader;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig.Type;

public class ShoulderSurfingFabric implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
		ModLoadingContext.registerConfig(ShoulderSurfing.MODID, Type.CLIENT, Config.CLIENT_SPEC, ShoulderSurfing.MODID + ".toml");
		KeyBindingHelper.registerKeyBinding(KeyHandler.KEYBIND_CAMERA_LEFT);
		KeyBindingHelper.registerKeyBinding(KeyHandler.KEYBIND_CAMERA_RIGHT);
		KeyBindingHelper.registerKeyBinding(KeyHandler.KEYBIND_CAMERA_IN);
		KeyBindingHelper.registerKeyBinding(KeyHandler.KEYBIND_CAMERA_OUT);
		KeyBindingHelper.registerKeyBinding(KeyHandler.KEYBIND_CAMERA_UP);
		KeyBindingHelper.registerKeyBinding(KeyHandler.KEYBIND_CAMERA_DOWN);
		KeyBindingHelper.registerKeyBinding(KeyHandler.KEYBIND_SWAP_SHOULDER);
		KeyBindingHelper.registerKeyBinding(KeyHandler.KEYBIND_TOGGLE_SHOULDER_SURFING);
		PluginLoader.getInstance().loadPlugins();
	}
}