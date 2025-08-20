package me.honkling.neopbb.profile.offline

import me.honkling.neopbb.world
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.world.inventory.PlayerEnderChestContainer
import org.bukkit.OfflinePlayer
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.entity.CraftHumanEntity

class OfflineEnderChest(owner: OfflinePlayer) : PlayerEnderChestContainer(null) {
    private val registryAccess = (world as CraftWorld).handle.registryAccess()
    private val dataAccess = DataAccess(owner)

    init {
        val data = dataAccess.compound.getList("EnderItems", CompoundTag.TAG_COMPOUND.toInt())
        fromTag(data, registryAccess)
    }

    override fun onClose(player: CraftHumanEntity) {
        super.onClose(player)

        val slots = mutableListOf<Tag>()

        for ((index, item) in items.withIndex()) {
            if (item.isEmpty)
                continue

            val tag = item.save(registryAccess) as CompoundTag
            tag.putInt("Slot", index)
            slots += tag
        }

        dataAccess.compound.put("EnderItems", ListTag(slots, CompoundTag.TAG_COMPOUND))
        dataAccess.save()
    }
}