package dev.rosewood.roseloot.loot;

import dev.rosewood.roseloot.loot.condition.LootCondition;
import dev.rosewood.roseloot.loot.item.LootItem;
import dev.rosewood.roseloot.util.LootUtils;
import dev.rosewood.roseloot.util.RandomCollection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LootPool implements LootItemGenerator {

    private final List<LootCondition> conditions;
    private final int minRolls, maxRolls;
    private final int bonusRolls;
    private final List<LootEntry> entries;

    public LootPool(List<LootCondition> conditions, int minRolls, int maxRolls, int bonusRolls, List<LootEntry> entries) {
        this.conditions = conditions;
        this.minRolls = minRolls;
        this.maxRolls = maxRolls;
        this.bonusRolls = bonusRolls;
        this.entries = entries;
    }

    @Override
    public List<LootItem<?>> generate(LootContext context) {
        if (!this.check(context))
            return Collections.emptyList();

        List<LootItem<?>> lootItems = new ArrayList<>();
        List<LootEntry> unweightedEntries = new ArrayList<>();
        RandomCollection<LootEntry> randomEntries = new RandomCollection<>();
        for (LootEntry entry : this.entries) {
            if (!entry.check(context))
                continue;

            if (entry.isWeighted()) {
                // If weighted, add to the random entries
                randomEntries.add(entry.getWeight(context), entry);
            } else {
                // Otherwise generate it right away
                unweightedEntries.add(entry);
            }
        }

        int numRolls = LootUtils.randomInRange(this.minRolls, this.maxRolls) + (int) Math.round(this.bonusRolls * context.getLuckLevel());
        for (int i = 0; i < numRolls; i++) {
            if (!randomEntries.isEmpty())
                lootItems.addAll(randomEntries.next().generate(context));
            lootItems.addAll(unweightedEntries.stream().flatMap(x -> x.generate(context).stream()).collect(Collectors.toList()));
        }

        return lootItems;
    }

    @Override
    public boolean check(LootContext context) {
        return this.conditions.stream().allMatch(x -> x.check(context));
    }

}
