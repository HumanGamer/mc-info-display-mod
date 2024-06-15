package com.humangamer.hginfohud.mixin.client;

import com.humangamer.hginfohud.InfoHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.LayeredDrawer;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

	@Shadow @Final private LayeredDrawer layeredDrawer;
	public InfoHud infoHud = null;

	@Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/client/MinecraftClient;)V")
	private void init(MinecraftClient client, CallbackInfo ci) {
		this.infoHud = new InfoHud(client);
		LayeredDrawer subDrawer = new LayeredDrawer().addLayer((context, tickCounter) -> {
			if (this.infoHud.shouldShowInfoHud()) {
				this.infoHud.render(context);
			}
		});
		this.layeredDrawer.addSubDrawer(subDrawer, () -> !client.options.hudHidden);
	}

	@Inject(at = @At("TAIL"), method = "clear")
	private void clear(CallbackInfo ci) {
		this.infoHud.clear();
	}
}