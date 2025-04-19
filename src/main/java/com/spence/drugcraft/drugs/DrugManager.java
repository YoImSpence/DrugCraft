package com.spence.drugcraft.drugs;

import com.spence.drugcraft.DrugCraft;
import org.bukkit.Material;
import org.bukkit.NamespacedKey; // Added import
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class DrugManager {
    private final DrugCraft plugin;
    private final List<Drug> drugs;

    public DrugManager(DrugCraft plugin) {
        this.plugin = plugin;
        this.drugs = new ArrayList<>();
        registerDrugs();
        registerRecipes();
    }

    private void registerDrugs() {
        ConfigurationSection drugConfig = plugin.getConfig().getConfigurationSection("drugs");
        if (drugConfig == null) {
            plugin.getLogger().warning("No drugs defined in config.yml!");
            return;
        }

        drugs.add(new Cannabis(getSellPrice(drugConfig, "Cannabis"), getAddictionStrength(drugConfig, "Cannabis")));
        drugs.add(new Blazepowder(getSellPrice(drugConfig, "Blazepowder"), getAddictionStrength(drugConfig, "Blazepowder")));
        drugs.add(new MysticShroom(getSellPrice(drugConfig, "MysticShroom"), getAddictionStrength(drugConfig, "MysticShroom")));
        drugs.add(new PoppyNectar(getSellPrice(drugConfig, "PoppyNectar"), getAddictionStrength(drugConfig, "PoppyNectar")));
        drugs.add(new LunarEssence(getSellPrice(drugConfig, "LunarEssence"), getAddictionStrength(drugConfig, "LunarEssence")));
        drugs.add(new GlowvineExtract(getSellPrice(drugConfig, "GlowvineExtract"), getAddictionStrength(drugConfig, "GlowvineExtract")));

        plugin.getLogger().info("Registered " + drugs.size() + " drugs.");
    }

    private double getSellPrice(ConfigurationSection config, String drugName) {
        return config.getDouble(drugName + ".sellPrice", 10.0);
    }

    private int getAddictionStrength(ConfigurationSection config, String drugName) {
        return config.getInt(drugName + ".addictionStrength", 5);
    }

    private void registerRecipes() {
        // Cannabis Recipe
        ItemStack cannabis = drugs.get(0).getItem();
        NamespacedKey cannabisKey = new NamespacedKey(plugin, "cannabis");
        ShapedRecipe cannabisRecipe = new ShapedRecipe(cannabisKey, cannabis);
        cannabisRecipe.shape(" K ", "KWK", " K ");
        cannabisRecipe.setIngredient('K', Material.DRIED_KELP);
        cannabisRecipe.setIngredient('W', Material.WHEAT);
        plugin.getServer().addRecipe(cannabisRecipe);

        // Blazepowder Recipe
        ItemStack blazepowder = drugs.get(1).getItem();
        NamespacedKey blazepowderKey = new NamespacedKey(plugin, "blazepowder");
        ShapedRecipe blazepowderRecipe = new ShapedRecipe(blazepowderKey, blazepowder);
        blazepowderRecipe.shape(" B ", "BSB", " B ");
        blazepowderRecipe.setIngredient('B', Material.BLAZE_POWDER);
        blazepowderRecipe.setIngredient('S', Material.SUGAR);
        plugin.getServer().addRecipe(blazepowderRecipe);

        // Mystic Shroom Recipe
        ItemStack mysticShroom = drugs.get(2).getItem();
        NamespacedKey mysticShroomKey = new NamespacedKey(plugin, "mystic_shroom");
        ShapedRecipe mysticShroomRecipe = new ShapedRecipe(mysticShroomKey, mysticShroom);
        mysticShroomRecipe.shape(" M ", "MSM", " M ");
        mysticShroomRecipe.setIngredient('M', Material.RED_MUSHROOM);
        mysticShroomRecipe.setIngredient('S', Material.SUGAR);
        plugin.getServer().addRecipe(mysticShroomRecipe);

        // Poppy Nectar Recipe
        ItemStack poppyNectar = drugs.get(3).getItem();
        NamespacedKey poppyNectarKey = new NamespacedKey(plugin, "poppy_nectar");
        ShapedRecipe poppyNectarRecipe = new ShapedRecipe(poppyNectarKey, poppyNectar);
        poppyNectarRecipe.shape(" P ", "BSB", " S ");
        poppyNectarRecipe.setIngredient('P', Material.POPPY);
        poppyNectarRecipe.setIngredient('B', Material.GLASS_BOTTLE);
        poppyNectarRecipe.setIngredient('S', Material.SUGAR);
        plugin.getServer().addRecipe(poppyNectarRecipe);

        // Lunar Essence Recipe
        ItemStack lunarEssence = drugs.get(4).getItem();
        NamespacedKey lunarEssenceKey = new NamespacedKey(plugin, "lunar_essence");
        ShapedRecipe lunarEssenceRecipe = new ShapedRecipe(lunarEssenceKey, lunarEssence);
        lunarEssenceRecipe.shape(" G ", "BSB", " S ");
        lunarEssenceRecipe.setIngredient('G', Material.GLISTERING_MELON_SLICE);
        lunarEssenceRecipe.setIngredient('B', Material.GLASS_BOTTLE);
        lunarEssenceRecipe.setIngredient('S', Material.SUGAR);
        plugin.getServer().addRecipe(lunarEssenceRecipe);

        // Glowvine Extract Recipe
        ItemStack glowvineExtract = drugs.get(5).getItem();
        NamespacedKey glowvineExtractKey = new NamespacedKey(plugin, "glowvine_extract");
        ShapedRecipe glowvineExtractRecipe = new ShapedRecipe(glowvineExtractKey, glowvineExtract);
        glowvineExtractRecipe.shape(" G ", "GSG", " G ");
        glowvineExtractRecipe.setIngredient('G', Material.GLOW_BERRIES);
        glowvineExtractRecipe.setIngredient('S', Material.SUGAR);
        plugin.getServer().addRecipe(glowvineExtractRecipe);
    }

    public Drug getDrugByName(String name) {
        for (Drug drug : drugs) {
            if (drug.getName().equalsIgnoreCase(name)) {
                return drug;
            }
        }
        return null;
    }

    public Drug getDrugByItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return null;
        }
        String displayName = meta.getDisplayName();
        for (Drug drug : drugs) {
            ItemStack drugItem = drug.getItem();
            ItemMeta drugMeta = drugItem.getItemMeta();
            if (drugMeta != null && drugMeta.hasDisplayName() &&
                    drugMeta.getDisplayName().equals(displayName) &&
                    item.getType() == drugItem.getType()) {
                return drug;
            }
        }
        return null;
    }

    public List<Drug> getDrugs() {
        return new ArrayList<>(drugs);
    }
}