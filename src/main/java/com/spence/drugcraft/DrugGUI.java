package com.spence.drugcraft;

import com.spence.drugcraft.drugs.Drug;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DrugGUI implements Listener {
    private final DrugCraft plugin;

    public enum MenuType {
        MAIN, BUY, SELL, ADMIN_GIVE
    }

    public DrugGUI(DrugCraft plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "DrugCraft Menu");

        ItemStack buyItem = new ItemStack(Material.EMERALD);
        ItemMeta buyMeta = buyItem.getItemMeta();
        if (buyMeta != null) {
            buyMeta.setDisplayName("§aBuy Drugs");
            buyMeta.setLore(Arrays.asList("§7Click to open the buy menu"));
            buyItem.setItemMeta(buyMeta);
        }
        gui.setItem(12, buyItem);

        ItemStack sellItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta sellMeta = sellItem.getItemMeta();
        if (sellMeta != null) {
            sellMeta.setDisplayName("§eSell Drugs");
            sellMeta.setLore(Arrays.asList("§7Click to open the sell menu"));
            sellItem.setItemMeta(sellMeta);
        }
        gui.setItem(13, sellItem);

        if (player.hasPermission("drugcraft.admin")) {
            ItemStack adminItem = new ItemStack(Material.DIAMOND);
            ItemMeta adminMeta = adminItem.getItemMeta();
            if (adminMeta != null) {
                adminMeta.setDisplayName("§bAdmin Give Drugs");
                adminMeta.setLore(Arrays.asList("§7Click to open the admin give menu"));
                adminItem.setItemMeta(adminMeta);
            }
            gui.setItem(14, adminItem);
        }

        setFillers(gui);
        player.openInventory(gui);
    }

    public void openBuyMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "DrugCraft Buy Menu");

        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§7Back to Main Menu");
            backItem.setItemMeta(backMeta);
        }
        gui.setItem(22, backItem);

        List<Drug> drugs = plugin.getDrugManager().getDrugs();
        for (int i = 1; i < drugs.size() && i < 7; i++) {
            Drug drug = drugs.get(i);
            ItemStack drugItem = drug.getItem().clone();
            ItemMeta meta = drugItem.getItemMeta();
            if (meta != null) {
                List<String> lore = new ArrayList<>();
                double buyPrice = drug.getSellPrice() * 1.5;
                lore.add("§7Buy Price: §a$" + String.format("%.2f", buyPrice));
                lore.add("§7Addiction Strength: §c" + drug.getAddictionStrength());
                lore.add("§7Effects:");
                for (PotionEffect effect : drug.getEffects()) {
                    String effectName = effect.getType().getName().replace("_", " ");
                    int duration = effect.getDuration() / 20;
                    int amplifier = effect.getAmplifier() + 1;
                    lore.add("§7- " + effectName + " " + amplifier + " (" + duration + "s)");
                }
                lore.add("§eLeft-Click: Buy 1");
                lore.add("§eRight-Click: Buy custom amount");
                meta.setLore(lore);
                drugItem.setItemMeta(meta);
            }
            gui.setItem(9 + i, drugItem);
        }

        setFillers(gui);
        player.openInventory(gui);
    }

    public void openSellMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "DrugCraft Sell Menu");

        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§7Back to Main Menu");
            backItem.setItemMeta(backMeta);
        }
        gui.setItem(22, backItem);

        List<Drug> drugs = plugin.getDrugManager().getDrugs();
        for (int i = 1; i < drugs.size() && i < 7; i++) {
            Drug drug = drugs.get(i);
            ItemStack drugItem = drug.getItem().clone();
            ItemMeta meta = drugItem.getItemMeta();
            if (meta != null) {
                List<String> lore = new ArrayList<>();
                lore.add("§7Sell Price: §a$" + String.format("%.2f", drug.getSellPrice()));
                lore.add("§7Addiction Strength: §c" + drug.getAddictionStrength());
                lore.add("§7Effects:");
                for (PotionEffect effect : drug.getEffects()) {
                    String effectName = effect.getType().getName().replace("_", " ");
                    int duration = effect.getDuration() / 20;
                    int amplifier = effect.getAmplifier() + 1;
                    lore.add("§7- " + effectName + " " + amplifier + " (" + duration + "s)");
                }
                lore.add("§eLeft-Click: Sell 1");
                lore.add("§eRight-Click: Sell custom amount");
                meta.setLore(lore);
                drugItem.setItemMeta(meta);
            }
            gui.setItem(9 + i, drugItem);
        }

        setFillers(gui);
        player.openInventory(gui);
    }

    public void openAdminGiveMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "DrugCraft Admin Give Menu");

        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§7Back to Main Menu");
            backItem.setItemMeta(backMeta);
        }
        gui.setItem(22, backItem);

        List<Drug> drugs = plugin.getDrugManager().getDrugs();
        for (int i = 1; i < drugs.size() && i < 7; i++) {
            Drug drug = drugs.get(i);
            ItemStack drugItem = drug.getItem().clone();
            ItemMeta meta = drugItem.getItemMeta();
            if (meta != null) {
                List<String> lore = new ArrayList<>();
                lore.add("§7Sell Price: §a$" + String.format("%.2f", drug.getSellPrice()));
                lore.add("§7Addiction Strength: §c" + drug.getAddictionStrength());
                lore.add("§7Effects:");
                for (PotionEffect effect : drug.getEffects()) {
                    String effectName = effect.getType().getName().replace("_", " ");
                    int duration = effect.getDuration() / 20;
                    int amplifier = effect.getAmplifier() + 1;
                    lore.add("§7- " + effectName + " " + amplifier + " (" + duration + "s)");
                }
                lore.add("§eLeft-Click: Give 1");
                lore.add("§eRight-Click: Give custom amount");
                meta.setLore(lore);
                drugItem.setItemMeta(meta);
            }
            gui.setItem(9 + i, drugItem);
        }

        setFillers(gui);
        player.openInventory(gui);
    }

    private void setFillers(Inventory gui) {
        ItemStack filler = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 0; i < 27; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        MenuType menuType = getMenuType(title);
        if (menuType == null) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getItemMeta() == null) return;

        String displayName = clickedItem.getItemMeta().getDisplayName();

        if (displayName.equals("§7Back to Main Menu")) {
            openMainMenu(player);
            return;
        }

        if (menuType == MenuType.MAIN) {
            if (displayName.equals("§aBuy Drugs")) {
                openBuyMenu(player);
            } else if (displayName.equals("§eSell Drugs")) {
                openSellMenu(player);
            } else if (displayName.equals("§bAdmin Give Drugs")) {
                if (!player.hasPermission("drugcraft.admin")) {
                    player.sendMessage("§cYou don't have permission!");
                    return;
                }
                openAdminGiveMenu(player);
            }
            return;
        }

        Drug drug = plugin.getDrugManager().getDrugByItem(clickedItem);
        if (drug == null) return;

        if (menuType == MenuType.BUY) {
            if (event.getClick() == ClickType.LEFT) {
                plugin.getEconomyManager().buyDrug(player, drug.getName(), 1);
            } else if (event.getClick() == ClickType.RIGHT) {
                player.closeInventory();
                plugin.getInputListener().requestInput(player, "Enter amount to buy for " + drug.getName(), input -> handleBuyInput(player, drug, input));
            }
        } else if (menuType == MenuType.SELL) {
            if (event.getClick() == ClickType.LEFT) {
                plugin.getEconomyManager().sellDrug(player, drug.getName(), 1);
            } else if (event.getClick() == ClickType.RIGHT) {
                player.closeInventory();
                plugin.getInputListener().requestInput(player, "Enter amount to sell for " + drug.getName(), input -> handleSellInput(player, drug, input));
            }
        } else if (menuType == MenuType.ADMIN_GIVE) {
            if (!player.hasPermission("drugcraft.give")) {
                player.sendMessage("§cYou don't have permission to give drugs!");
                return;
            }
            if (event.getClick() == ClickType.LEFT) {
                ItemStack item = drug.getItem().clone();
                item.setAmount(1);
                player.getInventory().addItem(item);
                player.sendMessage("§aGave 1 " + drug.getName());
            } else if (event.getClick() == ClickType.RIGHT) {
                player.closeInventory();
                plugin.getInputListener().requestInput(player, "Enter amount for " + drug.getName(), input -> handleGiveInput(player, drug, input));
            }
        }
    }

    private MenuType getMenuType(String title) {
        switch (title) {
            case "DrugCraft Menu":
                return MenuType.MAIN;
            case "DrugCraft Buy Menu":
                return MenuType.BUY;
            case "DrugCraft Sell Menu":
                return MenuType.SELL;
            case "DrugCraft Admin Give Menu":
                return MenuType.ADMIN_GIVE;
            default:
                return null;
        }
    }

    private void handleBuyInput(Player player, Drug drug, String input) {
        int amount;
        try {
            amount = Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            player.sendMessage("§cAmount must be a number!");
            new BukkitRunnable() {
                @Override
                public void run() {
                    openBuyMenu(player);
                }
            }.runTask(plugin);
            return;
        }
        if (amount <= 0) {
            player.sendMessage("§cAmount must be greater than 0!");
            new BukkitRunnable() {
                @Override
                public void run() {
                    openBuyMenu(player);
                }
            }.runTask(plugin);
            return;
        }
        plugin.getEconomyManager().buyDrug(player, drug.getName(), amount);
        player.sendMessage("§aOpening Buy Menu...");
        new BukkitRunnable() {
            @Override
            public void run() {
                openBuyMenu(player);
            }
        }.runTask(plugin);
    }

    private void handleSellInput(Player player, Drug drug, String input) {
        int amount;
        try {
            amount = Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            player.sendMessage("§cAmount must be a number!");
            new BukkitRunnable() {
                @Override
                public void run() {
                    openSellMenu(player);
                }
            }.runTask(plugin);
            return;
        }
        if (amount <= 0) {
            player.sendMessage("§cAmount must be greater than 0!");
            new BukkitRunnable() {
                @Override
                public void run() {
                    openSellMenu(player);
                }
            }.runTask(plugin);
            return;
        }
        plugin.getEconomyManager().sellDrug(player, drug.getName(), amount);
        player.sendMessage("§aOpening Sell Menu...");
        new BukkitRunnable() {
            @Override
            public void run() {
                openSellMenu(player);
            }
        }.runTask(plugin);
    }

    private void handleGiveInput(Player player, Drug drug, String input) {
        int amount;
        try {
            amount = Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            player.sendMessage("§cAmount must be a number!");
            new BukkitRunnable() {
                @Override
                public void run() {
                    openAdminGiveMenu(player);
                }
            }.runTask(plugin);
            return;
        }
        if (amount <= 0) {
            player.sendMessage("§cAmount must be greater than 0!");
            new BukkitRunnable() {
                @Override
                public void run() {
                    openAdminGiveMenu(player);
                }
            }.runTask(plugin);
            return;
        }
        ItemStack item = drug.getItem().clone();
        item.setAmount(amount);
        player.getInventory().addItem(item);
        player.sendMessage("§aGave " + amount + " " + drug.getName());
        player.sendMessage("§aOpening Admin Give Menu...");
        new BukkitRunnable() {
            @Override
            public void run() {
                openAdminGiveMenu(player);
            }
        }.runTask(plugin);
    }
}