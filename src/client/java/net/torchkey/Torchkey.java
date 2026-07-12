package net.torchkey;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class Torchkey implements ClientModInitializer {
	private static final String MOD_ID = "torchkey";

	private static KeyMapping keyPlaceTorch;
	TorchkeyConfig torchkeyConfig = new TorchkeyConfig();

	@Override
	public void onInitializeClient() {

		// Initialize the keybinding
		KeyMapping.Category keybindCaregory = KeyMapping.Category.register(
				Identifier.fromNamespaceAndPath(Torchkey.MOD_ID, "torchkey"));
		keyPlaceTorch = new KeyMapping("key.torchkey.placetorch", InputConstants.Type.MOUSE,
				GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
				keybindCaregory);

		// Register the keybinding
		KeyMappingHelper.registerKeyMapping(keyPlaceTorch);

		// Give the keybinding functionality by listening for the client tick event
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (keyPlaceTorch.consumeClick()) {
				placeTorch(client);
			}
		});
	}

	// TODO: Find a more reliable/extensible way of detecting item type
	private void placeTorch(Minecraft client) {
		LocalPlayer player = client.player;
		if (player == null)
			return;

		Inventory inv = player.getInventory();

		// Find target
		HitResult target = client.hitResult;
		if (target == null)
			return;
		if (target.getType() != HitResult.Type.BLOCK) {
			return;
		}
		BlockHitResult targetBlock = (BlockHitResult) target;

		MultiPlayerGameMode gameMode = client.gameMode;
		if (gameMode == null)
			return; // could never place anyway

		// Check main hand for torch
		int selectedSlot = inv.getSelectedSlot();
		if (isTorchInSlot(inv, selectedSlot)) {

			// Place the torch
			gameMode.useItemOn(player, InteractionHand.MAIN_HAND, targetBlock);

			return;
		}

		// Check off hand for torch
		if (isTorchInSlot(inv, Inventory.SLOT_OFFHAND)) {

			// Place the torch
			gameMode.useItemOn(player, InteractionHand.OFF_HAND, targetBlock);

			return;
		}

		// Check hotbar for torch
		for (int slotI = 0; slotI < 9; slotI++) {

			// Check if item in this slot is a torch
			if (isTorchInSlot(inv, slotI)) {

				// Select the torch
				inv.setSelectedSlot(slotI);

				// Place the torch
				gameMode.useItemOn(player, InteractionHand.MAIN_HAND, targetBlock);

				// De-select the torch
				inv.setSelectedSlot(selectedSlot);

				return;
			}
		}
	}

	private boolean isTorchInSlot(Inventory inv, int slot) {
		return torchkeyConfig.getOrDefault("validTorches", new ArrayList<String>())
				.contains(inv.getItem(slot).getItem().toString());
	}
}
