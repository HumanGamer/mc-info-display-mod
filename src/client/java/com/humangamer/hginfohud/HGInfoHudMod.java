package com.humangamer.hginfohud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class HGInfoHudMod implements ClientModInitializer {

	private static KeyBinding keyBinding;
	public static InfoHud infoHud;

	@Override
	public void onInitializeClient() {

		keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.hginfohud.toggle_info_hud",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_F6,
			"category.hginfohud.hginfohud"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (keyBinding.wasPressed()) {
				// Toggle the info hud
				if (infoHud != null)
					infoHud.toggleInfoHud();
			}
		});
	}
}