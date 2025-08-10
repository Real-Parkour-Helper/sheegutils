package org.realparkourhelper.sheegutils.client

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.collections.forEach
import kotlin.time.Duration

enum class RoomName(val displayName: String) {
    AroundPillars("around pillars"),
    Blocks("blocks"),
    CastleWall("castle wall"),
    Early31("early 3+1"),
    FenceSqueeze("fence squeeze"),
    Fences("fences"),
    Fortress("fortress"),
    FourTowers("four towers"),
    RngSkip("rng skip"),
    Ice("ice"),
    LadderSlide("ladder slide"),
    LadderTower("ladder tower"),
    Overhead4b("overhead 4b"),
    QuartzClimb("quartz climb"),
    QuartzTemple("quartz temple"),
    Sandpit("sandpit"),
    Scatter("scatter"),
    SlimeScatter("slime scatter"),
    SlimeSkip("slime skip"),
    Tightrope("tightrope"),
    TowerTightrope("tower tightrope"),
    TriplePlatform("triple platform"),
    TripleTrapdoors("triple trapdoors"),
    Underbridge("underbridge")
}

data class BlockData(
    val x: Int,
    val y: Int,
    val z: Int,
    val blockName: String  // Note: Kotlin uses camelCase convention
)

private const val LIGHT_WEIGHTED_PRESSURE_PLATE = "Light Weighted Pressure Plate"

data class BoostRoomsResponse(
    @SerializedName("name") val name: String,
    @SerializedName("pacelock") val pacelock: Double,
    @SerializedName("index") val index: Int
)

data class PkdutilsBody(
    @SerializedName("boost_time") val boostTime: String,
    @SerializedName("boostless_time") val boostlessTime: String,
    @SerializedName("boost_rooms") val boostRooms: List<BoostRoomsResponse>?
)

data class PkdutilsResponse(
    @SerializedName("best") val best: PkdutilsBody,
    @SerializedName("personal") val personal: PkdutilsBody,
    @SerializedName("error") val error: String?
)
class RoomID {
    private var currentRoom: RoomName? = null
    private var currentRoomNumber = 0
    private var startPosition = Vec3i(18, 72, 13)
    private var currentCheckpoint = 0
    private val rooms = mutableListOf<RoomName>()
    private val checkpointRegex = Regex("""CHECKPOINT! You reached checkpoint (\d+) in [\d:.]+!""")
    private val calcURL = "https://wired-cod-kindly.ngrok-free.app/api/pkdutils/calc"

    private val checkpointCount = mapOf(
        RoomName.AroundPillars to 2,
        RoomName.Blocks to 2,
        RoomName.CastleWall to 2,
        RoomName.Early31 to 2,
        RoomName.FenceSqueeze to 2,
        RoomName.Fences to 2,
        RoomName.Fortress to 2,
        RoomName.FourTowers to 3,
        RoomName.RngSkip to 2,
        RoomName.Ice to 3,
        RoomName.LadderSlide to 2,
        RoomName.LadderTower to 3,
        RoomName.Overhead4b to 3,
        RoomName.QuartzClimb to 2,
        RoomName.QuartzTemple to 2,
        RoomName.Sandpit to 3,
        RoomName.Scatter to 2,
        RoomName.SlimeScatter to 3,
        RoomName.SlimeSkip to 2,
        RoomName.Tightrope to 2,
        RoomName.TowerTightrope to 2,
        RoomName.TriplePlatform to 2,
        RoomName.TripleTrapdoors to 2,
        RoomName.Underbridge to 2,
    )

    val uniqueBlocks = mapOf(
        RoomName.AroundPillars to BlockData(0, 9, 12, LIGHT_WEIGHTED_PRESSURE_PLATE),
        RoomName.Blocks to BlockData(-6, 11, 25, LIGHT_WEIGHTED_PRESSURE_PLATE),
        RoomName.CastleWall to BlockData(13, 11, 26, LIGHT_WEIGHTED_PRESSURE_PLATE),
        RoomName.Early31 to BlockData(5, 14, 31, LIGHT_WEIGHTED_PRESSURE_PLATE),
        RoomName.FenceSqueeze to BlockData(0, 5, 26, LIGHT_WEIGHTED_PRESSURE_PLATE),
        RoomName.Fences to BlockData(1, 8, 26, LIGHT_WEIGHTED_PRESSURE_PLATE),
        RoomName.Fortress to BlockData(0, 10, 19, LIGHT_WEIGHTED_PRESSURE_PLATE),
        RoomName.FourTowers to BlockData(-3, 12, 11, LIGHT_WEIGHTED_PRESSURE_PLATE),
        RoomName.RngSkip to BlockData(0, 5, 14, LIGHT_WEIGHTED_PRESSURE_PLATE),
        RoomName.Ice to BlockData(-4, 6, 19, LIGHT_WEIGHTED_PRESSURE_PLATE),
        RoomName.LadderSlide to BlockData(9, 3, 25, LIGHT_WEIGHTED_PRESSURE_PLATE),
        RoomName.LadderTower to BlockData(0, 18, 10, LIGHT_WEIGHTED_PRESSURE_PLATE),
        RoomName.Overhead4b to BlockData(5, 8, 11, LIGHT_WEIGHTED_PRESSURE_PLATE),
        RoomName.QuartzClimb to BlockData(0, 16, 27, LIGHT_WEIGHTED_PRESSURE_PLATE),
        RoomName.QuartzTemple to BlockData(0, 8, 11, LIGHT_WEIGHTED_PRESSURE_PLATE),
        RoomName.Sandpit to BlockData(-13, 12, 15, LIGHT_WEIGHTED_PRESSURE_PLATE),
        RoomName.Scatter to BlockData(-7, 10, 22, LIGHT_WEIGHTED_PRESSURE_PLATE),
        RoomName.SlimeScatter to BlockData(-1, 3, 22, LIGHT_WEIGHTED_PRESSURE_PLATE),
        RoomName.SlimeSkip to BlockData(0, 4, 19, LIGHT_WEIGHTED_PRESSURE_PLATE),
        RoomName.Tightrope to BlockData(9, 14, 33, LIGHT_WEIGHTED_PRESSURE_PLATE),
        RoomName.TowerTightrope to BlockData(13, 7, 17, LIGHT_WEIGHTED_PRESSURE_PLATE),
        RoomName.TriplePlatform to BlockData(9, 14, 16, LIGHT_WEIGHTED_PRESSURE_PLATE),
        RoomName.TripleTrapdoors to BlockData(14, 9, 15, LIGHT_WEIGHTED_PRESSURE_PLATE),
        RoomName.Underbridge to BlockData(-8, 8, 28, LIGHT_WEIGHTED_PRESSURE_PLATE)
    )
    private fun verifyBlock(from: Vec3i, block: BlockData): Boolean {
        val blockPos = Vec3i(block.x, block.y, block.z).add(from)
        val blockAtPos = MinecraftClient.getInstance().world?.getBlockState(BlockPos(blockPos)) ?: return false

        println("${blockAtPos.block.name.string}")

        return blockAtPos.block.name.string == block.blockName
    }

    private fun detectRoom(): RoomName? {
        val zAddend = 57 * this.currentRoomNumber
        val startPos = Vec3i(0, 0, zAddend).add(this.startPosition)

        for ((room, block) in uniqueBlocks) {
            if (verifyBlock(startPos, block)) {
                return room
            }
        }


        return null
    }

    private fun reset() {
        this.currentCheckpoint = 0
        this.currentRoomNumber = 0
        this.currentRoom = null
        this.rooms.clear()
    }

    private fun calcSeed(rooms: List<RoomName>): Result<PkdutilsResponse> {
        return try {
            val roomsCalc = rooms.take(8).map { it.displayName }
            val gson = Gson()

            val requestMap = mapOf(
                "rooms" to roomsCalc,
                "splits" to null
            )
            val requestBody = gson.toJson(requestMap)

            val client = HttpClient.newBuilder().build()

            val request = HttpRequest.newBuilder()
                .uri(URI.create(calcURL))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() !in 200..299) {
                return Result.failure(RuntimeException("HTTP error: ${response.statusCode()}"))
            }

            val parsedResponse = gson.fromJson(response.body(), PkdutilsResponse::class.java)
            Result.success(parsedResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    fun listen(message: Text, overlay: Boolean): Boolean {
        val sb = StringBuilder()
        message.siblings.forEach { text -> sb.append(text.string) }
        val rawText = sb.toString()

        if (rawText.startsWith(" ") && (rawText.trim().startsWith("Opponents:") || rawText.trim().startsWith("Opponent:"))) {
            reset()

            val detectedRoom = this.detectRoom()

            if (detectedRoom == null) {
                println("failed to identify first room")
                MinecraftClient.getInstance().inGameHud.chatHud.addMessage(Text.literal("failed to identify first room"))
                return true
            }

            this.currentRoom = detectedRoom
            this.rooms.add(detectedRoom)

            println("You are in room $detectedRoom")

            return true
        }

        if (rawText.startsWith("COMPLETED! You completed the parkour")) {
            val result = this.calcSeed(rooms).getOrNull()
            if (result == null) {
                val message = Text.literal("Failed to calculate seed :(").formatted(Formatting.RED)
                MinecraftClient.getInstance().inGameHud.chatHud.addMessage(message)
                return true
            }

            val instance = MinecraftClient.getInstance()
            val boostMessage = Text.literal("Perfect boost time: ").formatted(Formatting.BLUE)
                                .append(Text.literal(result.best.boostTime).formatted(Formatting.GREEN))
            instance.inGameHud.chatHud.addMessage(boostMessage)

            val boostlessMessage = Text.literal("Perfect boost time: ").formatted(Formatting.BLUE)
                .append(Text.literal(result.best.boostlessTime).formatted(Formatting.GREEN))
            instance.inGameHud.chatHud.addMessage(boostlessMessage)

            val boostRoomsMessage = Text.literal("You should boost in the following rooms:").formatted(Formatting.BLUE)
            instance.inGameHud.chatHud.addMessage(boostRoomsMessage)

            result.best.boostRooms?.forEach { (name, pacelock, index) ->
                var pacelockMsg = Text.literal("")
                if (pacelock > 0) {
                    val pacelockStr = String.format("%.1f", pacelock)
                    pacelockMsg = Text.literal(" (").formatted(Formatting.BLUE)
                        .append(Text.literal("${pacelockStr}s").formatted(Formatting.RED))
                        .append(Text.literal(")").formatted(Formatting.BLUE))
                }

                val message = Text.literal("Room ${index + 1}: ").formatted(Formatting.BLUE)
                    .append(Text.literal(name).formatted(Formatting.GREEN)).append(pacelockMsg)
                instance.inGameHud.chatHud.addMessage(message)
            }

            return true
        }

        val match = checkpointRegex.find(rawText) ?: return true
        println(match)

        this.currentCheckpoint = match.groupValues[1].toInt()
        var nextRoomAt = 0
        this.rooms.forEach { room -> nextRoomAt += checkpointCount.getValue(room)}

        if (this.currentCheckpoint == nextRoomAt) {
            if (this.rooms.size >= 8) {
                return true
            }

            this.currentRoomNumber++
            val detectedRoom = this.detectRoom() ?: return true
            this.currentRoom = detectedRoom
            this.rooms.add(detectedRoom)

            println("You are in room $detectedRoom")
        }

        return true
    }
}