package me.honkling.neopbb.tutorial

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import com.github.retrooper.packetevents.util.Vector3f
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import me.honkling.neopbb.profile.key.createKey
import me.tofaa.entitylib.meta.display.BlockDisplayMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.block.Block
import org.bukkit.entity.Player

private var Player.highlightedBlocks by createKey<MutableList<Highlight>>(mutableListOf(), persistent = false)

data class Highlight(val block: Block) : WrapperEntity(EntityTypes.BLOCK_DISPLAY) {
    val entityMeta: BlockDisplayMeta = getEntityMeta(BlockDisplayMeta::class.java)

    init {
        entityMeta.blockId = SpigotConversionUtil.fromBukkitBlockData(block.blockData).globalId
        entityMeta.translation = Vector3f(0f, 1f, 0f)
        entityMeta.scale = Vector3f(0.99f, 0.99f, 0.99f)
        entityMeta.isGlowing = true
    }
}

fun Player.highlightBlocks(vararg blocks: Block) {
    val highlightedBlocks = highlightedBlocks
    highlightedBlocks += blocks.map {
        val entity = Highlight(it)
        entity.addViewer(uniqueId)
        entity.spawn(SpigotConversionUtil.fromBukkitLocation(it.location))
        entity
    }
    this.highlightedBlocks = highlightedBlocks
}

fun Player.clearHighlights() {
    for (entity in highlightedBlocks)
        entity.remove()

    highlightedBlocks = mutableListOf()
}