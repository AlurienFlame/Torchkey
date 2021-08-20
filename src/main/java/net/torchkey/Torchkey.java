package net.torchkey;

import java.util.Arrays;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class Torchkey implements ClientModInitializer {

    private static KeyBinding keyPlaceTorch;
	// TODO: Add support for Druidcraft firey torches
    private static String[] validTorches = { "torch", "redstone_torch", "stone_torch" };

    @Override
    public void onInitializeClient() {

        // Define the keybinding object
        keyPlaceTorch = new KeyBinding("key.torchkey.placetorch", InputUtil.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
                "category.torchkey.Torchkey");

        // Register the keybinding
        KeyBindingHelper.registerKeyBinding(keyPlaceTorch);

        // Give the keybinding functionality by listening for the client tick event
        ClientTickCallback.EVENT.register(client -> {
            if (keyPlaceTorch.wasPressed()) {
                placeTorch(client);
            }
        });
    }

    // TODO: Find a more reliable/extensible way of detecting item type
    private void placeTorch(MinecraftClient client) {
        PlayerInventory inv = client.player.getInventory();

        // Find target
        HitResult target = client.crosshairTarget;
        if (target.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockHitResult targetBlock = (BlockHitResult) target;

        // Check main hand for torch
        if (Arrays.asList(validTorches).contains(inv.getMainHandStack().getItem().toString())) {

            // Place the torch
            client.interactionManager.interactBlock(client.player, client.world, Hand.MAIN_HAND, targetBlock);

            return;
        }

        // Check off hand for torch
        if (Arrays.asList(validTorches).contains(inv.offHand.get(0).getItem().toString())) {

            // Place the torch
            client.interactionManager.interactBlock(client.player, client.world, Hand.OFF_HAND, targetBlock);

            return;
        }

        // Check hotbar for torch
        int oldSlot = inv.selectedSlot;
        for (int slot = 0; slot < 9; slot++) {

            // Check if item in this slot is a torch
            if (Arrays.asList(validTorches).contains(inv.getStack(slot).getItem().toString())) {

                // Select the torch
                inv.selectedSlot = slot;

                // Place the torch
                client.interactionManager.interactBlock(client.player, client.world, Hand.MAIN_HAND, targetBlock);

                // De-select the torch
                inv.selectedSlot = oldSlot;

                return;
            }
        }
    }

}
