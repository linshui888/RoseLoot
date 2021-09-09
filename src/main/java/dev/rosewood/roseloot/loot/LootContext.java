package dev.rosewood.roseloot.loot;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LootContext {

    private final Entity looter;
    private final LivingEntity lootedEntity;
    private final Block lootedBlock;
    private final FishHook fishHook;
    private final ItemStack inputItem;
    private final NamespacedKey vanillaLootTableKey, advancementKey;
    private final ExplosionType explosionType;

    private LootContext(Entity looter, LivingEntity lootedEntity, Block lootedBlock, FishHook fishHook, ItemStack inputItem, NamespacedKey vanillaLootTableKey, NamespacedKey advancementKey, ExplosionType explosionType) {
        this.looter = looter;
        this.lootedEntity = lootedEntity;
        this.lootedBlock = lootedBlock;
        this.fishHook = fishHook;
        this.inputItem = inputItem;
        this.vanillaLootTableKey = vanillaLootTableKey;
        this.advancementKey = advancementKey;
        this.explosionType = explosionType;
    }

    public LootContext(@Nullable Entity looter, @NotNull LivingEntity lootedEntity) {
        this(looter, lootedEntity, null, null, null, null, null, null);
    }

    public LootContext(@Nullable Entity looter, @NotNull Block lootedBlock) {
        this(looter, null, lootedBlock, null, null, null, null, null);
    }

    public LootContext(@NotNull Entity looter, @NotNull FishHook fishHook) {
        this(looter, null, null, fishHook, null, null, null, null);
    }

    public LootContext(@NotNull LivingEntity lootedEntity, @NotNull ItemStack inputItem) {
        this(null, lootedEntity, null, null, inputItem, null, null, null);
    }

    public LootContext(@Nullable Entity looter, @NotNull Block lootedBlock, @NotNull NamespacedKey vanillaLootTableKey) {
        this(looter, null, lootedBlock, null, null, vanillaLootTableKey, null, null);
    }

    public LootContext(@Nullable Entity looter, @NotNull Block lootedBlock, @NotNull ExplosionType explosionType) {
        this(looter, null, lootedBlock, null, null, null, null, explosionType);
    }

    public LootContext(@Nullable Entity looter, @NotNull LivingEntity lootedEntity, @NotNull ItemStack inputItem) {
        this(looter, lootedEntity, null, null, inputItem, null, null, null);
    }

    public LootContext(@NotNull Player player, @NotNull NamespacedKey advancementKey) {
        this(player, null, null, null, null, null, advancementKey, null);
    }

    /**
     * @return the entity that triggered the loot generation
     */
    @Nullable
    public Entity getLooter() {
        return this.looter;
    }

    /**
     * @return the Player that ultimately caused the loot generation, may not be the same as {@link LootContext#getLooter()}
     */
    @Nullable
    public Player getLootingPlayer() {
        if (this.lootedEntity == null)
            return null;
        return this.lootedEntity.getKiller();
    }

    /**
     * @return the looted entity
     */
    @Nullable
    public LivingEntity getLootedEntity() {
        return this.lootedEntity;
    }

    /**
     * @return the looted block
     */
    @Nullable
    public Block getLootedBlock() {
        return this.lootedBlock;
    }

    /**
     * @return the fish hook
     */
    @Nullable
    public FishHook getFishHook() {
        return this.fishHook;
    }

    /**
     * Gets the item primarily used for the loot generation.
     * For piglin bartering, it will be the bartered item.
     * For entity item drops, it will be the dropped item.
     *
     * @return the item primarily used for the loot generation
     */
    @Nullable
    public ItemStack getInputItem() {
        return this.inputItem;
    }

    /**
     * @return the NamespacedKey of the vanilla loot table
     */
    @Nullable
    public NamespacedKey getVanillaLootTableKey() {
        return this.vanillaLootTableKey;
    }

    /**
     * @return the NamespacedKey of the advancement
     */
    @Nullable
    public NamespacedKey getAdvancementKey() {
        return this.advancementKey;
    }

    /**
     * @return the type of explosion or null if there was none
     */
    @Nullable
    public ExplosionType getExplosionType() {
        if (this.explosionType != null)
            return this.explosionType;

        if (this.looter instanceof Creeper && ((Creeper) this.looter).isPowered())
            return ExplosionType.CHARGED_ENTITY;

        return null;
    }

    /**
     * @return the Location for this context
     */
    @NotNull
    public Location getLocation() {
        if (this.lootedEntity != null) return this.lootedEntity.getLocation();
        if (this.lootedBlock != null) return this.lootedBlock.getLocation();
        if (this.fishHook != null) return this.fishHook.getLocation();
        if (this.looter != null) return this.looter.getLocation();
        throw new IllegalStateException("LootContext does not have a Location");
    }

    /**
     * @return the luck level for this context, used for bonus rolls
     */
    public double getLuckLevel() {
        double luck = 0;
        if (this.looter != null && this.looter instanceof LivingEntity) {
            AttributeInstance attribute = ((LivingEntity) this.looter).getAttribute(Attribute.GENERIC_LUCK);
            if (attribute != null)
                luck += attribute.getValue();
        }

        if (this.fishHook != null) {
            ItemStack item = this.getItemUsed();
            if (item != null && item.getType() == Material.FISHING_ROD && item.getItemMeta() != null)
                luck += item.getItemMeta().getEnchantLevel(Enchantment.LUCK);
        }
        
        return luck;
    }

    /**
     * @return the ItemStack used by the looter
     */
    @Nullable
    public ItemStack getItemUsed() {
        if (this.looter == null || !(this.looter instanceof LivingEntity))
            return null;

        EntityEquipment equipment = ((LivingEntity) this.looter).getEquipment();
        if (equipment == null)
            return null;

        if (equipment.getItemInMainHand().getType() != Material.AIR) {
            return equipment.getItemInMainHand();
        } else if (equipment.getItemInOffHand().getType() != Material.AIR) {
            return equipment.getItemInOffHand();
        } else {
            return null;
        }
    }

}
