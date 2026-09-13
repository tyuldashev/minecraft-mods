package dev.tim.villagerstaff;

import net.minecraft.world.item.*;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;

@Mod(VillagerStaffMod.ID)
public final class VillagerStaffMod {
    public static final String ID = "villagerstaff";
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);
    public static final DeferredRegister<net.minecraft.world.effect.MobEffect> EFFECTS =
        DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, ID);
    public static final RegistryObject<net.minecraft.world.effect.MobEffect> KINGS_TARGET =
        EFFECTS.register("kings_target", () -> new net.minecraft.world.effect.MobEffect(
            net.minecraft.world.effect.MobEffectCategory.HARMFUL, 0x45BBFF) {});
    public static final RegistryObject<Item> SWORD = ITEMS.register("kings_sword", () ->
        new KingsSwordItem(new Item.Properties().setId(ITEMS.key("kings_sword"))));
    public static final RegistryObject<Item> STAFF = ITEMS.register("villager_staff", () ->
        new VillagerStaffItem(new Item.Properties().setId(ITEMS.key("villager_staff"))
            .stacksTo(1).rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<Item> CROWN = ITEMS.register("kings_crown", () ->
        new KingsCrownItem(new Item.Properties().setId(ITEMS.key("kings_crown"))));

    public VillagerStaffMod(FMLJavaModLoadingContext context) {
        ITEMS.register(context.getModBusGroup());
        EFFECTS.register(context.getModBusGroup());
        net.minecraftforge.event.entity.living.LivingDamageEvent.BUS.addListener(event -> {
            if (event.getEntity().hasEffect(KINGS_TARGET.getHolder().orElseThrow()))
                event.setAmount(event.getAmount() * 1.2f);
        });
        BuildCreativeModeTabContentsEvent.BUS.addListener(event -> {
            if (event.getTabKey() == CreativeModeTabs.COMBAT) {
                event.accept(STAFF.get());
                event.accept(SWORD.get());
                var crown = CROWN.get().getDefaultInstance();
                KingsCrownItem.ensureProtection(crown, event.getParameters().holders());
                event.accept(crown);
            }
        });
    }
}
