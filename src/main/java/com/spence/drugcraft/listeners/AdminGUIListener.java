package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.crops.GrowLight;
import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.gui.AdminGUI;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class AdminGUIListener implements Listener {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final Map<UUID, String> pendingActions = new HashMap<>();
    private final Map<UUID, ItemStack> selectedItems = new HashMap<>();
    private final Map<UUID, String> selectedItemNames = new HashMap<>();
    private final Map<UUID, String> selectedItemTypes = new HashMap<>();
    private final Map<UUID, Boolean> isSeedItems = new HashMap<>();
    private final Map<UUID, Boolean> isGrowLights = new HashMap<>();
    private final Map<UUID, String> selectedQualities = new HashMap<>();
    private final Map<UUID, Integer> selectedQuantities = new HashMap<>();
    private final Map<UUID, Player> selectedPlayers = new HashMap<>();

    public AdminGUIListener(DrugCraft plugin, DrugManager drugManager) {
        this.plugin = plugin;
        this.drugManager = drugManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        if (!title.equals(MessageUtils.color("{#FFA500}Admin Drug Control")) &&
                !title.equals(MessageUtils.color("{#FFA500}Select Item")) &&
                !title.startsWith(MessageUtils.color("{#FFA500}Configure "))) {
            return;
        }
        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null ||
                clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE ||
                clickedItem.getType() == Material.YELLOW_STAINED_GLASS_PANE ||
                clickedItem.getType() == Material.ORANGE_STAINED_GLASS_PANE) {
            return;
        }

        if (title.equals(MessageUtils.color("{#FFA500}Admin Drug Control"))) {
            if (clickedItem.getType() == Material.EMERALD) {
                pendingActions.put(player.getUniqueId(), "give");
                player.openInventory(new AdminGUI(plugin, drugManager).createItemGUI());
            }
        } else if (title.equals(MessageUtils.color("{#FFA500}Select Item"))) {
            List<Drug> drugs = drugManager.getSortedDrugs();
            for (Drug drug : drugs) {
                if (clickedItem.getType() == drug.getItem(null).getType() &&
                        clickedItem.getItemMeta().getDisplayName().equals(drug.getItem(null).getItemMeta().getDisplayName())) {
                    selectedItems.put(player.getUniqueId(), drug.getItem(null));
                    selectedItemNames.put(player.getUniqueId(), drug.getName());
                    selectedItemTypes.put(player.getUniqueId(), "Drug");
                    isSeedItems.put(player.getUniqueId(), false);
                    isGrowLights.put(player.getUniqueId(), false);
                    selectedQualities.put(player.getUniqueId(), "Basic");
                    selectedQuantities.put(player.getUniqueId(), 1);
                    selectedPlayers.put(player.getUniqueId(), player);
                    player.openInventory(new AdminGUI(plugin, drugManager).createGiveGUI(
                            drug.getItem(null), drug.getName(), false, false, "Drug", "Basic", 1, player.getName()));
                    return;
                }
                if (drug.hasSeed() &&
                        clickedItem.getType() == drug.getSeedItem(null).getType() &&
                        clickedItem.getItemMeta().getDisplayName().equals(drug.getSeedItem(null).getItemMeta().getDisplayName())) {
                    selectedItems.put(player.getUniqueId(), drug.getSeedItem(null));
                    selectedItemNames.put(player.getUniqueId(), drug.getName() + " Seed");
                    selectedItemTypes.put(player.getUniqueId(), "Seed");
                    isSeedItems.put(player.getUniqueId(), true);
                    isGrowLights.put(player.getUniqueId(), false);
                    selectedQualities.put(player.getUniqueId(), "Basic");
                    selectedQuantities.put(player.getUniqueId(), 1);
                    selectedPlayers.put(player.getUniqueId(), player);
                    player.openInventory(new AdminGUI(plugin, drugManager).createGiveGUI(
                            drug.getSeedItem(null), drug.getName() + " Seed", true, false, "Seed", "Basic", 1, player.getName()));
                    return;
                }
            }
            if (clickedItem.getType() == Material.SHEARS) {
                String trimmerName = clickedItem.getItemMeta().getDisplayName();
                String quality = trimmerName.contains("Exotic") ? "Exotic" : trimmerName.contains("Standard") ? "Standard" : "Basic";
                selectedItems.put(player.getUniqueId(), clickedItem.clone());
                selectedItemNames.put(player.getUniqueId(), quality + " Trimmer");
                selectedItemTypes.put(player.getUniqueId(), "Trimmer");
                isSeedItems.put(player.getUniqueId(), false);
                isGrowLights.put(player.getUniqueId(), false);
                selectedQualities.put(player.getUniqueId(), quality);
                selectedQuantities.put(player.getUniqueId(), 1);
                selectedPlayers.put(player.getUniqueId(), player);
                player.openInventory(new AdminGUI(plugin, drugManager).createGiveGUI(
                        clickedItem.clone(), quality + " Trimmer", false, false, "Trimmer", quality, 1, player.getName()));
                return;
            }
            if (clickedItem.getType() == Material.REDSTONE_LAMP) {
                String growLightName = clickedItem.getItemMeta().getDisplayName();
                String quality = growLightName.contains("Exotic") ? "Exotic" : growLightName.contains("Standard") ? "Standard" : "Basic";
                selectedItems.put(player.getUniqueId(), clickedItem.clone());
                selectedItemNames.put(player.getUniqueId(), quality + " Grow Light");
                selectedItemTypes.put(player.getUniqueId(), "Grow Light");
                isSeedItems.put(player.getUniqueId(), false);
                isGrowLights.put(player.getUniqueId(), true);
                selectedQualities.put(player.getUniqueId(), quality);
                selectedQuantities.put(player.getUniqueId(), 1);
                selectedPlayers.put(player.getUniqueId(), player);
                player.openInventory(new AdminGUI(plugin, drugManager).createGiveGUI(
                        clickedItem.clone(), quality + " Grow Light", false, true, "Grow Light", quality, 1, player.getName()));
                return;
            }
        } else if (title.startsWith(MessageUtils.color("{#FFA500}Configure "))) {
            if (clickedItem.getType() == Material.DIAMOND) {
                String currentQuality = selectedQualities.get(player.getUniqueId());
                String newQuality = switch (currentQuality) {
                    case "Basic" -> "Standard";
                    case "Standard" -> "Exotic";
                    case "Exotic" -> "Prime";
                    case "Prime" -> "Legendary";
                    default -> "Basic";
                };
                selectedQualities.put(player.getUniqueId(), newQuality);
                updateGiveGUI(player);
            } else if (clickedItem.getType() == Material.PAPER) {
                player.closeInventory();
                startQuantityConversation(player);
            } else if (clickedItem.getType() == Material.PLAYER_HEAD) {
                player.closeInventory();
                startPlayerNameConversation(player);
            } else if (clickedItem.getType() == Material.EMERALD) {
                String quality = selectedQualities.get(player.getUniqueId());
                int quantity = selectedQuantities.get(player.getUniqueId());
                Player target = selectedPlayers.get(player.getUniqueId());
                boolean isSeed = isSeedItems.get(player.getUniqueId());
                boolean isGrowLight = isGrowLights.get(player.getUniqueId());
                String itemName = selectedItemNames.get(player.getUniqueId());
                String itemType = selectedItemTypes.get(player.getUniqueId());
                ItemStack giveItem;
                if (itemType.equals("Trimmer")) {
                    giveItem = selectedItems.get(player.getUniqueId()).clone();
                    ItemMeta meta = giveItem.getItemMeta();
                    meta.setDisplayName(MessageUtils.color("{#FFD700}" + quality + " Trimmer"));
                    meta.setLore(Arrays.asList(MessageUtils.color(getQualityColor(quality) + "Quality: " + quality)));
                    meta.setUnbreakable(true);
                    giveItem.setItemMeta(meta);
                } else if (itemType.equals("Grow Light")) {
                    giveItem = GrowLight.createGrowLight(quality);
                    giveItem.setAmount(quantity);
                } else {
                    String drugId = drugManager.getDrugIdFromItem(selectedItems.get(player.getUniqueId()));
                    if (drugId == null) {
                        drugId = drugManager.getDrugIdFromSeed(selectedItems.get(player.getUniqueId()));
                    }
                    Drug drug = drugManager.getDrug(drugId);
                    giveItem = isSeed ? drug.getSeedItem(quality) : drug.getItem(quality);
                }
                giveItem.setAmount(quantity);
                target.getInventory().addItem(giveItem);
                player.sendMessage(MessageUtils.color("{#00FF00}Gave " + quantity + " " + quality + " " + itemName + " to " + target.getName()));
                if (target != player) {
                    target.sendMessage(MessageUtils.color("{#00FF00}You received " + quantity + " " + quality + " " + itemName + " from " + player.getName()));
                }
                clearPlayerData(player);
                player.closeInventory();
            }
        }
    }

    private void updateGiveGUI(Player player) {
        String itemName = selectedItemNames.get(player.getUniqueId());
        boolean isSeed = isSeedItems.get(player.getUniqueId());
        boolean isGrowLight = isGrowLights.get(player.getUniqueId());
        String itemType = selectedItemTypes.get(player.getUniqueId());
        ItemStack item = selectedItems.get(player.getUniqueId());
        String quality = selectedQualities.get(player.getUniqueId());
        int quantity = selectedQuantities.get(player.getUniqueId());
        Player target = selectedPlayers.get(player.getUniqueId());
        Inventory giveGUI = new AdminGUI(plugin, drugManager).createGiveGUI(item, itemName, isSeed, isGrowLight, itemType, quality, quantity, target.getName());
        player.openInventory(giveGUI);
    }

    private void startPlayerNameConversation(Player player) {
        ConversationFactory factory = new ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(new PlayerNamePrompt())
                .withEscapeSequence("cancel")
                .withTimeout(30);
        Conversation conversation = factory.buildConversation(player);
        conversation.getContext().setSessionData("player", player);
        conversation.addConversationAbandonedListener(event -> {
            if (!event.gracefulExit()) {
                player.sendMessage(MessageUtils.color("{#FF5555}Action cancelled."));
                clearPlayerData(player);
            }
        });
        conversation.begin();
    }

    private void startQuantityConversation(Player player) {
        String itemName = selectedItemNames.get(player.getUniqueId());
        ConversationFactory factory = new ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(new QuantityPrompt(itemName))
                .withEscapeSequence("cancel")
                .withTimeout(30);
        Conversation conversation = factory.buildConversation(player);
        conversation.getContext().setSessionData("player", player);
        conversation.addConversationAbandonedListener(event -> {
            if (!event.gracefulExit()) {
                player.sendMessage(MessageUtils.color("{#FF5555}Action cancelled."));
                clearPlayerData(player);
            }
        });
        conversation.begin();
    }

    private void clearPlayerData(Player player) {
        pendingActions.remove(player.getUniqueId());
        selectedItems.remove(player.getUniqueId());
        selectedItemNames.remove(player.getUniqueId());
        selectedItemTypes.remove(player.getUniqueId());
        isSeedItems.remove(player.getUniqueId());
        isGrowLights.remove(player.getUniqueId());
        selectedQualities.remove(player.getUniqueId());
        selectedQuantities.remove(player.getUniqueId());
        selectedPlayers.remove(player.getUniqueId());
    }

    private String getQualityColor(String quality) {
        return switch (quality) {
            case "Legendary" -> "{#FF00FF}";
            case "Prime" -> "{#1E90FF}";
            case "Exotic" -> "{#FFA500}";
            case "Standard" -> "{#00FF00}";
            default -> "{#AAAAAA}"; // Basic
        };
    }

    private class PlayerNamePrompt extends StringPrompt {
        @Override
        public String getPromptText(ConversationContext context) {
            return MessageUtils.color("{#FFD700}Enter the target player's name (or 'cancel' to abort):");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getSessionData("player");
            Player target = Bukkit.getPlayerExact(input);
            if (target == null) {
                context.getForWhom().sendRawMessage(MessageUtils.color("{#FF5555}Player not found! Try again or type 'cancel'."));
                return this;
            }
            selectedPlayers.put(player.getUniqueId(), target);
            updateGiveGUI(player);
            return END_OF_CONVERSATION;
        }
    }

    private class QuantityPrompt extends NumericPrompt {
        private final String itemName;

        public QuantityPrompt(String itemName) {
            this.itemName = itemName;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return MessageUtils.color("{#FFD700}Enter the quantity for " + itemName + " (1-64, or 'cancel' to abort):");
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
            int quantity = input.intValue();
            if (quantity < 1 || quantity > 64) {
                context.getForWhom().sendRawMessage(MessageUtils.color("{#FF5555}Quantity must be between 1 and 64! Try again or type 'cancel'."));
                return this;
            }
            Player player = (Player) context.getSessionData("player");
            selectedQuantities.put(player.getUniqueId(), quantity);
            updateGiveGUI(player);
            return END_OF_CONVERSATION;
        }

        @Override
        protected boolean isInputValid(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("cancel")) {
                return true;
            }
            try {
                int quantity = Integer.parseInt(input);
                return quantity >= 1 && quantity <= 64;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        @Override
        protected String getFailedValidationText(ConversationContext context, String invalidInput) {
            return MessageUtils.color("{#FF5555}Invalid quantity! Enter a number between 1 and 64, or 'cancel'.");
        }
    }
}