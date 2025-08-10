package org.realparkourhelper.sheegutils.client

import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class PreviewChatListener {
    companion object {
        private val roomLineRegex = Regex("""> Room (\d+): (\w+)""")
        private val roomMap = mapOf(
            "3d" to "Around Pillars",
            "3b" to "Blocks",
            "2b" to "Castle Wall",
            "1a" to "Early 3+1",
            "4c" to "Fence Squeeze",
            "1d" to "Fences",
            "3a" to "Fortress",
            "4a" to "Four Towers",
            "5a" to "Ice",
            "2c" to "Ladder Slide",
            "4b" to "Ladder Tower",
            "3e" to "Overhead 4b",
            "2e" to "Quartz Climb",
            "5d" to "Quartz Temple",
            "5b" to "Rng Skip",
            "4d" to "Sandpit",
            "2a" to "Scatter",
            "5c" to "Slime Scatter",
            "4e" to "Slime Skip",
            "1b" to "Tightrope",
            "1c" to "Tower Tightrope",
            "1e" to "Triple Platform",
            "3c" to "Triple Trapdoors",
            "2d" to "Underbridge"
        )

        fun listenGameChat(message: Text, overlay: Boolean): Boolean {
            val sb = StringBuilder()
            message.siblings.forEach { text -> sb.append(text.string) }
            val rawText = sb.toString()
            println(rawText)

            if (rawText.startsWith("Preview of the first 3 rooms:")) {
                return true
            }

            val match = roomLineRegex.find(rawText)
            if (match != null) {
                val roomNum = match.groupValues[2]
                val code = match.groupValues[2]
                val readable = roomMap[code]

                if (readable != null) {
                    val newMessage = Text.literal("> ").formatted(Formatting.DARK_GRAY).append(Text.literal("Room $roomNum: ").formatted(Formatting.GRAY)).append(Text.literal(readable).formatted(Formatting.AQUA))
                    MinecraftClient.getInstance().inGameHud.chatHud.addMessage(newMessage)

                    return false
                }
            }

            return true
        }
    }
}