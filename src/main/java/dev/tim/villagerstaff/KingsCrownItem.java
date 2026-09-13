package dev.tim.villagerstaff;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;

public final class KingsCrownItem extends Item {
    public KingsCrownItem(Properties properties) {
        super(properties.humanoidArmor(ArmorMaterials.NETHERITE, ArmorType.HELMET)
            // Render the crown's open 3D item model on the head instead of a closed armor helmet.
            .equippable(EquipmentSlot.HEAD)
            .component(DataComponents.UNBREAKABLE, Unit.INSTANCE)
            .attributes(ArmorMaterials.NETHERITE.createAttributes(ArmorType.HELMET)
                .withModifierAdded(Attributes.MAX_HEALTH,
                    new AttributeModifier(Identifier.fromNamespaceAndPath(VillagerStaffMod.ID, "crown_health"),
                        10, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HEAD))
            .rarity(Rarity.EPIC).fireResistant());
    }

    public static void ensureProtection(ItemStack stack, HolderLookup.Provider registries) {
        var protection = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.PROTECTION);
        if (stack.getEnchantments().getLevel(protection) < 5) stack.enchant(protection, 5);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, EquipmentSlot slot) {
        // Covers /give, creative inventory and future crafting; no repeated healing or modifier stacking.
        ensureProtection(stack, level.registryAccess());
    }
}
