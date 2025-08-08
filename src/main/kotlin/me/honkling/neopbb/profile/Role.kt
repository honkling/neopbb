package me.honkling.neopbb.profile

import me.honkling.neopbb.*
import me.honkling.neopbb.lib.builder
import me.honkling.neopbb.lib.illegalGoldenApple
import me.honkling.neopbb.lib.mm
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.title.TitlePart
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment.*
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.Team
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class Invite(val player: Player, val role: Role) {
    var taskID: Int? = null

    fun schedule(): Invite {
        taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(instance, {
            taskID = null
            expire()
        }, 30 * 20L)

        return this
    }

    fun cancel() {
        taskID?.let { Bukkit.getScheduler().cancelTask(it) }
    }

    fun expire() {
        cancel()
        player.sendMessage("<p>Your invitation to become a ${role.name.lowercase()} has expired.".mm)
        player.invite = null
    }
}

val keycard = ItemStack(Material.TRIPWIRE_HOOK)
    .builder()
    .displayName("Keycard <red>[CONTRABAND]".mm)
    .build()

val handcuffs = ItemStack(Material.IRON_SHOVEL)
    .builder()
    .displayName("Handcuffs <red>[CONTRABAND]".mm)
    .enchant(KNOCKBACK)
    .build()

enum class Role(
    val isAuthority: Boolean,
    val team: Team,
    prefix: String,
    val prepare: Player.(Boolean) -> Unit = {}
) {
    @OptIn(ExperimentalTime::class)
    Warden(true, wardenTeam, "<white><gray>[<red>WARDEN</red>]</gray>", {
        wardenStart = Clock.System.now().epochSeconds
        noDamageTicks = 20 * 30

        for (player in Bukkit.getOnlinePlayers()) {
            if (player != warden && player.role.isAuthority) {
                player.role = Prisoner
                player.prepare(true, broadcast = true)
            }

            player.invite?.expire()
        }

        val server = Bukkit.getServer()

        world.getEntitiesByClass(Item::class.java).forEach(Entity::remove)
        server.sendTitlePart(TitlePart.TITLE, "<s>$name</s>".mm)
        server.sendTitlePart(TitlePart.SUBTITLE, "is the new warden!".mm)
        server.playSound(Sound.sound {
            it.type(Key.key("minecraft:block.end_portal.spawn"))
        })

        swatUnlocked = false

        val helmet = ItemStack(Material.CHAINMAIL_HELMET)
            .builder()
            .enchant(PROTECTION, 2)
            .build()

        val chestplate = ItemStack(Material.IRON_CHESTPLATE)
            .builder()
            .enchant(PROTECTION)
            .build()

        val leggings = ItemStack(Material.IRON_LEGGINGS)
            .builder()
            .enchant(PROTECTION, 2)
            .build()

        val boots = ItemStack(Material.NETHERITE_BOOTS)
            .builder()
            .enchant(PROTECTION)
            .enchant(PROJECTILE_PROTECTION, 3)
            .build()

        val sword = ItemStack(Material.DIAMOND_SWORD)

        inventory.setItem(EquipmentSlot.HEAD, helmet)
        inventory.setItem(EquipmentSlot.CHEST, chestplate)
        inventory.setItem(EquipmentSlot.LEGS, leggings)
        inventory.setItem(EquipmentSlot.FEET, boots)
        inventory.setItem(EquipmentSlot.OFF_HAND, keycard)
        inventory.addItem(
            sword,
            ItemStack(Material.BOW),
            handcuffs,
            ItemStack(Material.COOKED_BEEF, 64),
            ItemStack(Material.ARROW, 64)
        )

        teleport(currentPrison.wardenSpawn)
    }),
    Guard(true, guardsTeam, "<gray>[<blue>GUARD</blue>]", {
        val helmet = ItemStack(Material.IRON_HELMET)
            .builder()
            .enchant(PROTECTION, 2)
            .build()

        val chestplate = ItemStack(Material.LEATHER_CHESTPLATE)
            .builder()
            .enchant(PROTECTION, 2)
            .enchant(PROJECTILE_PROTECTION)
            .color(126, 135, 245)
            .build()

        val leggings = ItemStack(Material.CHAINMAIL_LEGGINGS)
            .builder()
            .enchant(PROTECTION, 3)
            .build()

        val boots = ItemStack(Material.LEATHER_BOOTS)
            .builder()
            .enchant(PROTECTION, 2)
            .color(126, 135, 245)
            .build()

        val sword = ItemStack(Material.IRON_SWORD)

        inventory.setItem(EquipmentSlot.HEAD, helmet)
        inventory.setItem(EquipmentSlot.CHEST, chestplate)
        inventory.setItem(EquipmentSlot.LEGS, leggings)
        inventory.setItem(EquipmentSlot.FEET, boots)
        inventory.setItem(EquipmentSlot.OFF_HAND, keycard)
        inventory.addItem(
            sword,
            ItemStack(Material.CROSSBOW),
            handcuffs,
            ItemStack(Material.COOKED_BEEF, 32),
            ItemStack(Material.ARROW, 16)
        )
    }),
    Nurse(true, nursesTeam, "<gray>[<light_purple>NURSE</light_purple>]", {
        val helmet = ItemStack(Material.CHAINMAIL_HELMET)
            .builder()
            .enchant(PROTECTION, 2)
            .build()

        val chestplate = ItemStack(Material.LEATHER_CHESTPLATE)
            .builder()
            .enchant(PROTECTION)
            .enchant(PROJECTILE_PROTECTION)
            .color(Color.PURPLE)
            .build()

        val leggings = ItemStack(Material.LEATHER_LEGGINGS)
            .builder()
            .enchant(PROTECTION)
            .color(Color.PURPLE)
            .build()

        val boots = ItemStack(Material.LEATHER_BOOTS)
            .builder()
            .enchant(PROTECTION, 3)
            .color(Color.PURPLE)
            .build()

        val sword = ItemStack(Material.WOODEN_SWORD)
            .builder()
            .enchant(SHARPNESS)
            .build()

        val potion = ItemStack(Material.SPLASH_POTION)
        potion.editMeta(PotionMeta::class.java) {
            it.addCustomEffect(PotionEffectType.INSTANT_HEALTH.createEffect(10, 2), true)
        }

        inventory.setItem(EquipmentSlot.HEAD, helmet)
        inventory.setItem(EquipmentSlot.CHEST, chestplate)
        inventory.setItem(EquipmentSlot.LEGS, leggings)
        inventory.setItem(EquipmentSlot.FEET, boots)
        inventory.setItem(EquipmentSlot.OFF_HAND, keycard)
        inventory.addItem(
            sword,
            ItemStack(Material.CROSSBOW),
            handcuffs,
            ItemStack(Material.COOKED_BEEF, 32),
            potion,
            ItemStack(Material.ARROW, 16)
        )
    }),
    Swat(true, swatsTeam, "<gray>[<dark_gray>SWAT</dark_gray>]", {
        val helmet = ItemStack(Material.IRON_HELMET)
            .builder()
            .enchant(PROTECTION, 2)
            .build()

        val chestplate = ItemStack(Material.NETHERITE_CHESTPLATE)
            .builder()
            .enchant(PROTECTION, 2)
            .build()

        val leggings = ItemStack(Material.LEATHER_LEGGINGS)
            .builder()
            .enchant(PROTECTION)
            .enchant(PROJECTILE_PROTECTION, 2)
            .color(Color.GRAY)
            .build()

        val boots = ItemStack(Material.LEATHER_BOOTS)
            .builder()
            .enchant(PROTECTION, 2)
            .color(Color.GRAY)
            .build()

        val sword = ItemStack(Material.IRON_SWORD)
            .builder()
            .enchant(SHARPNESS)
            .build()

        inventory.setItem(EquipmentSlot.HEAD, helmet)
        inventory.setItem(EquipmentSlot.CHEST, chestplate)
        inventory.setItem(EquipmentSlot.LEGS, leggings)
        inventory.setItem(EquipmentSlot.FEET, boots)
        inventory.setItem(EquipmentSlot.OFF_HAND, keycard)
        inventory.addItem(
            sword,
            ItemStack(Material.BOW),
            handcuffs,
            ItemStack(Material.COOKED_BEEF, 32),
            ItemStack(Material.ARROW, 16)
        )
    }),
    Criminal(false, criminalsTeam, "<gray>[<red>CRIMINAL</red>]", {
        val name = "Armor <red>[CONTRABAND]".mm
        val helmet = ItemStack(Material.CHAINMAIL_HELMET)
            .builder()
            .displayName(name)
            .enchant(PROTECTION, 2)
            .enchant(PROJECTILE_PROTECTION)
            .build()

        val chestplate = ItemStack(Material.LEATHER_CHESTPLATE)
            .builder()
            .displayName(name)
            .enchant(PROTECTION, 3)
            .color(Color.RED)
            .build()

        val leggings = ItemStack(Material.CHAINMAIL_LEGGINGS)
            .builder()
            .displayName(name)
            .enchant(PROTECTION, 2)
            .build()

        val boots = ItemStack(Material.CHAINMAIL_BOOTS)
            .builder()
            .displayName(name)
            .enchant(PROTECTION, 2)
            .build()

        val sword = ItemStack(Material.STONE_SWORD)
            .builder()
            .enchant(SHARPNESS)
            .build()

        inventory.setItem(EquipmentSlot.HEAD, helmet)
        inventory.setItem(EquipmentSlot.CHEST, chestplate)
        inventory.setItem(EquipmentSlot.LEGS, leggings)
        inventory.setItem(EquipmentSlot.FEET, boots)
        inventory.addItem(sword, illegalGoldenApple.asQuantity(4))
    }),
    Prisoner(false, prisonersTeam, "<gray>[<gold>PRISONER</gold>]<dark_gray>", {
        val chestplate = ItemStack(Material.LEATHER_CHESTPLATE)
            .builder()
            .displayName("Prisoner Uniform")
            .color(208, 133, 22)
            .build()

        val leggings = ItemStack(Material.LEATHER_LEGGINGS)
            .builder()
            .displayName("Prisoner Uniform")
            .color(208, 133, 22)
            .build()

        val boots = ItemStack(Material.LEATHER_BOOTS)
            .builder()
            .displayName("Prisoner Uniform")
            .color(40, 20, 2)
            .build()

        inventory.setItem(EquipmentSlot.CHEST, chestplate)
        inventory.setItem(EquipmentSlot.LEGS, leggings)
        inventory.setItem(EquipmentSlot.FEET, boots)
        teleport(currentPrison.prisonerSpawn)
    }),
    Solitary(false, solitaryTeam, "<black><gray>[<black>SOLITARY</black>]</gray>");

    val prefix = prefix.mm
}