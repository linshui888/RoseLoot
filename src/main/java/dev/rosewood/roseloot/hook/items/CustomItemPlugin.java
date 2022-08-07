package dev.rosewood.roseloot.hook.items;

import dev.rosewood.roseloot.loot.context.LootContext;
import java.util.List;
import java.util.function.BiPredicate;
import org.bukkit.inventory.ItemStack;

public enum CustomItemPlugin {

    /**
     * @deprecated Use {@code ECO} instead.
     */
    @Deprecated ECOITEMS(new EcoItemProvider()),
    ECO(new EcoItemProvider()),
    MMOITEMS(new MMOItemProvider()),
    MYTHICMOBS(new MythicMobsItemProvider()),
    ITEMBRIDGE(new ItemBridgeItemProvider()),
    EXECUTABLEITEMS(new ExecutableItemProvider()),
    EXECUTABLEBLOCKS(new ExecutableBlockProvider()),
    ITEMSADDER(new ItemsAdderItemProvider()),
    ITEMSXL(new ItemsXLItemProvider()),
    ORAXEN(new OraxenItemProvider()),
    KNOKKOCUSTOMITEMS(new KnokkoCustomItemProvider()),
    ITEMEDIT(new ItemEditItemProvider()),
    UBERITEMS(new UberItemProvider()),
    SLIMEFUN(new SlimefunItemProvider()),
    CUSTOMCRAFTING(new CustomCraftingItemProvider());

    private final ItemProvider itemProvider;

    CustomItemPlugin(ItemProvider itemProvider) {
        this.itemProvider = itemProvider;
    }

    public boolean isEnabled() {
        return this.itemProvider.isEnabled();
    }

    public ItemStack resolveItem(LootContext context, String id) {
        return this.itemProvider.getItem(context, id);
    }

    public String resolveItemId(ItemStack itemStack) {
        return this.itemProvider.getItemId(itemStack);
    }

    public boolean supportsIdLookup() {
        return this.itemProvider.supportsIdLookup();
    }

    public String getConditionSuffix() {
        return this.itemProvider.getConditionSuffix();
    }

    public BiPredicate<LootContext, List<String>> getLootConditionPredicate() {
        return (context, values) -> context.getItemUsed()
                .map(this::resolveItemId)
                .filter(x -> values.stream().anyMatch(x::equalsIgnoreCase))
                .isPresent();
    }

    public static CustomItemPlugin fromString(String name) {
        for (CustomItemPlugin value : values())
            if (value.name().equalsIgnoreCase(name))
                return value;
        return null;
    }

}
