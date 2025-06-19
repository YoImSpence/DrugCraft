package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.businesses.BusinessManager;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.handlers.ActiveGUI;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class PlayerGUI {
    private final DrugCraft plugin;
    private final DataManager dataManager;
    private final BusinessManager businessManager;

    public PlayerGUI(DrugCraft plugin, DataManager dataManager, BusinessManager businessManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.businessManager = businessManager;
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("gui.player.main-title")));
        ActiveGUI activeGUI = new ActiveGUI("PLAYER", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, "gui.player.border");
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        ItemStack levels = createItem(Material.EXPERIENCE_BOTTLE, "gui.player.levels");
        ItemStack worldTp = createItem(Material.ENDER_PEARL, "gui.player.world-tp");
        ItemStack games = createItem(Material.CARTOGRAPHY_TABLE, "gui.player.games");
        ItemStack settings = createItem(Material.COMPARATOR, "gui.player.settings");
        ItemStack business = createItem(Material.GOLD_INGOT, "gui.player.business");

        inv.setItem(20, levels);
        inv.setItem(22, worldTp);
        inv.setItem(24, games);
        inv.setItem(29, settings);
        inv.setItem(31, business);

        player.openInventory(inv);
    }

    public void openLevelsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("gui.player.levels-title")));
        ActiveGUI activeGUI = new ActiveGUI("PLAYER", inv, "levels");
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, "gui.player.border");
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        int level = dataManager.getPlayerLevel(player.getUniqueId());
        long currentXP = dataManager.getPlayerDrugXP(player.getUniqueId(), "overall");
        long xpRequired = dataManager.getXPRequiredForLevel(level + 1);

        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(MessageUtils.getMessage("gui.player.current-level", "level", String.valueOf(level)));
            infoMeta.setLore(Arrays.asList(
                    MessageUtils.getMessage("gui.player.xp-progress", "current_xp", String.valueOf(currentXP), "required_xp", String.valueOf(xpRequired))
            ));
            info.setItemMeta(infoMeta);
        }

        ItemStack craftingSkill = createSkillItem(Material.CRAFTING_TABLE, "crafting", player);
        ItemStack farmingSkill = createSkillItem(Material.WHEAT, "farming", player);
        ItemStack back = createItem(Material.BARRIER, "gui.player.back");

        inv.setItem(20, info);
        inv.setItem(22, craftingSkill);
        inv.setItem(24, farmingSkill);
        inv.setItem(49, back);

        player.openInventory(inv);
    }

    public void openWorldTpMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("gui.player.world-tp-title")));
        ActiveGUI activeGUI = new ActiveGUI("PLAYER", inv, "world-tp");
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, "gui.player.border");
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        ItemStack greenfield = createItem(Material.GRASS_BLOCK, "gui.player.world-greenfield");
        ItemStack nether = createItem(Material.NETHERRACK, "gui.player.world-nether");
        ItemStack back = createItem(Material.BARRIER, "gui.player.back");

        inv.setItem(20, greenfield);
        inv.setItem(22, nether);
        inv.setItem(49, back);

        player.openInventory(inv);
    }

    public void openSettingsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("gui.player.settings-title")));
        ActiveGUI activeGUI = new ActiveGUI("PLAYER", inv, "settings");
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, "gui.player.border");
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        ItemStack msgToggle = createItem(Material.PAPER, "gui.player.msg-toggle");
        ItemStack tpToggle = createItem(Material.ENDER_PEARL, "gui.player.tp-toggle");
        ItemStack back = createItem(Material.BARRIER, "gui.player.back");

        inv.setItem(20, msgToggle);
        inv.setItem(22, tpToggle);
        inv.setItem(49, back);

        player.openInventory(inv);
    }

    public void openBusinessMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("gui.player.business-title")));
        ActiveGUI activeGUI = new ActiveGUI("PLAYER", inv, "business");
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, "gui.player.border");
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        ItemStack back = createItem(Material.BARRIER, "gui.player.back");
        inv.setItem(49, back);

        player.openInventory(inv);
    }

    private ItemStack createItem(Material material, String messageKey) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtils.getMessage(messageKey));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createSkillItem(Material material, String skill, Player player) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String hexColor = skill.equals("crafting") ? "#FF5555" : "#55FF55";
            meta.setDisplayName(MessageUtils.getMessage("gui.player.skill", "skill", "<" + hexColor + ">" + skill + "</" + hexColor + ">"));
            long currentXP = dataManager.getPlayerDrugXP(player.getUniqueId(), skill);
            int skillLevel = (int) (currentXP / 1000);
            meta.setLore(Arrays.asList(
                    MessageUtils.getMessage("gui.player.skill-level", "skill", skill, "level", String.valueOf(skillLevel))
            ));
            item.setItemMeta(meta);
        }
        return item;
    }
}