package me.honkling.neopbb.profile.offline

import me.honkling.neopbb.world
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtIo
import org.bukkit.OfflinePlayer
import java.util.UUID

class DataAccess(uniqueId: UUID) {
    constructor(player: OfflinePlayer) : this(player.uniqueId)

    private val file = world.worldFolder.resolve("playerdata/$uniqueId.dat")
    var compound = CompoundTag()

    init {
        if (file.exists())
            compound = NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap())
    }

    fun save() {
        NbtIo.writeCompressed(compound, file.toPath())
    }
}