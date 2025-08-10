package org.realparkourhelper.sheegutils.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents

class SheegutilsClient : ClientModInitializer {

    override fun onInitializeClient() {
        val roomID = RoomID()

        ClientReceiveMessageEvents.ALLOW_GAME.register(roomID::listen)
        ClientReceiveMessageEvents.ALLOW_GAME.register(PreviewChatListener::listenGameChat)
    }
}
