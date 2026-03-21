package cat.emir.echostats

import org.bukkit.Material
import org.bukkit.Tag

class TagUtils {
    companion object {
        fun isTool(type: Material) = Tag.ITEMS_ENCHANTABLE_MINING.isTagged(type)
        fun isWeapon(type: Material) = Tag.ITEMS_ENCHANTABLE_WEAPON.isTagged(type)
        fun isArmor(type: Material) = Tag.ITEMS_ENCHANTABLE_ARMOR.isTagged(type)
    }
}