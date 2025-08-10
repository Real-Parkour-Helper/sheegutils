package org.realparkourhelper.sheegutils.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class SheegutilsClient : ClientModInitializer {

    override fun onInitializeClient() {
        ClientReceiveMessageEvents.ALLOW_GAME.register(PreviewChatListener::listenGameChat)
    }
}
