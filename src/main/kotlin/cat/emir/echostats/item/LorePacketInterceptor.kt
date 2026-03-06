package cat.emir.echostats.item

import cat.emir.echostats.EchoStats
import cat.emir.echostats.item.ItemStats.Companion.pdcKey
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.text.Component
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket
import net.minecraft.network.protocol.game.ClientboundBundlePacket
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket
import net.minecraft.network.protocol.game.ClientboundGameEventPacket
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket
import net.minecraft.network.protocol.game.ClientboundSetCursorItemPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket
import net.minecraft.network.protocol.game.ClientboundSetTimePacket
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.item.component.TooltipDisplay
import net.minecraft.world.item.enchantment.ItemEnchantments
import net.minecraft.world.level.ItemLike
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import java.util.SequencedSet

class LorePacketInterceptor(val player: Player) : ChannelDuplexHandler() {

//    override fun channelRead(ctx: ChannelHandlerContext?, packet: Any?) {
//        if (packet !is ServerboundClientTickEndPacket &&
//            packet !is ServerboundMovePlayerPacket.Pos &&
//            packet !is ServerboundMovePlayerPacket.Rot &&
//            packet !is ServerboundMovePlayerPacket.PosRot &&
//            packet !is ServerboundKeepAlivePacket &&
//            packet !is ServerboundPlayerInputPacket)
//            player.sendRichMessage("serverbound packet: ${packet?.javaClass?.simpleName}")

//        if (packet is ServerboundSetCreativeModeSlotPacket) {
//            player.sendRichMessage(packet.itemStack.get(DataComponents.LORE).toString())

//            if (ItemStats.hasStats(packet.itemStack.bukkitStack)) {
//                packet.itemStack.apply {
//                    set(DataComponents.LORE, ItemLore(listOf()))
//                    set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay(false, linkedSetOf()))
//                }
//            }
//            player.sendRichMessage(packet.itemStack.get(DataComponents.LORE).toString())
//        }

//        super.channelRead(ctx, packet)
//    }

    override fun write(ctx: ChannelHandlerContext, packet: Any?, promise: ChannelPromise) {
//        if (packet !is ClientboundSystemChatPacket &&
//            packet !is ClientboundSetTimePacket &&
//            packet !is ClientboundKeepAlivePacket &&
//            packet !is ClientboundLevelChunkWithLightPacket &&
//            packet !is ClientboundForgetLevelChunkPacket &&
//            packet !is ClientboundRotateHeadPacket &&
//            packet !is ClientboundMoveEntityPacket.Rot &&
//            packet !is ClientboundMoveEntityPacket.Pos &&
//            packet !is ClientboundMoveEntityPacket.PosRot &&
//            packet !is ClientboundSetEntityMotionPacket &&
//            packet !is ClientboundSetEntityDataPacket &&
//            packet !is ClientboundEntityPositionSyncPacket &&
//            packet !is ClientboundEntityEventPacket &&
//            packet !is ClientboundBundlePacket &&
//            packet !is ClientboundUpdateAttributesPacket &&
//            packet !is ClientboundSoundPacket &&
//            packet !is ClientboundRemoveEntitiesPacket)
//            player.sendRichMessage("clientbound packet: ${packet?.javaClass?.simpleName}")

        when (packet) {
            is ClientboundGameEventPacket -> {
                if (packet.event == ClientboundGameEventPacket.CHANGE_GAME_MODE) {
                    toggleCreative(packet.param == 1f)
                }
            }

            is ClientboundContainerSetSlotPacket -> {
                packet.item.apply { modifyNmsItem(this) }
            }

            is ClientboundSetCursorItemPacket -> {
                return
//                msg.contents.apply { modifyNmsItem(this) }
            }

            is ClientboundContainerSetContentPacket -> {
                packet.items.forEach { modifyNmsItem(it) }
            }
        }

        super.write(ctx, packet, promise)
    }

    private fun toggleCreative(state: Boolean) {
        player.inventory
            .filterNotNull()
            .filter { ItemStats.hasStats(it) }
            .forEach {
                it.editPersistentDataContainer { pdc ->
                    val statsPDC = pdc.getOrDefault(pdcKey("stats"), PersistentDataType.TAG_CONTAINER, pdc.adapterContext.newPersistentDataContainer())

                    if (state) {
                        statsPDC.set(pdcKey("paused"), PersistentDataType.BOOLEAN, true)
                    } else {
                        statsPDC.remove(pdcKey("paused"))
                    }

                    pdc.set(pdcKey("stats"), PersistentDataType.TAG_CONTAINER, statsPDC)
                }
            }
    }

    private fun modifyNmsItem(nmsItem: ItemStack) {
        if (nmsItem.isEmpty) return

        val bukkitItem = nmsItem.bukkitStack
        if (!ItemStats.hasStats(bukkitItem)) return

        val statsPDC = bukkitItem.persistentDataContainer.getOrDefault(pdcKey("stats"),
            PersistentDataType.TAG_CONTAINER, bukkitItem.persistentDataContainer.adapterContext.newPersistentDataContainer())
        val paused = statsPDC.getOrDefault(pdcKey("paused"), PersistentDataType.BOOLEAN, false)
        if (paused) return

        val showMore = statsPDC.getOrDefault(pdcKey("show_more"), PersistentDataType.BOOLEAN, false)

        val lore = ItemStats(bukkitItem)
            .buildLore(player)
            .map(PaperAdventure::asVanilla)

        nmsItem.set(DataComponents.LORE, ItemLore(lore))

        if (showMore) {
            nmsItem.set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay(
                false,
                linkedSetOf(
                    DataComponents.ENCHANTMENTS,
                    DataComponents.ATTRIBUTE_MODIFIERS,
                    DataComponents.DAMAGE
                ))
            )
        }
    }
}