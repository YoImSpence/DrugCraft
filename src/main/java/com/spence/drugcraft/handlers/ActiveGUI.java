package com.spence.drugcraft.handlers;

import com.spence.drugcraft.town.DealRequest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ActiveGUI {
    private final String guiType;
    private final Inventory inventory;
    private String menuSubType;
    private ItemStack selectedItem;
    private boolean awaitingChatInput;
    private String chatAction;
    private DealRequest dealRequest;

    public ActiveGUI(String guiType, Inventory inventory) {
        this.guiType = guiType;
        this.inventory = inventory;
    }

    public ActiveGUI(String guiType, Inventory inventory, String menuSubType) {
        this.guiType = guiType;
        this.inventory = inventory;
        this.menuSubType = menuSubType;
    }

    public String getGuiType() {
        return guiType;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public String getMenuSubType() {
        return menuSubType;
    }

    public void setMenuSubType(String menuSubType) {
        this.menuSubType = menuSubType;
    }

    public ItemStack getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(ItemStack selectedItem) {
        this.selectedItem = selectedItem;
    }

    public boolean isAwaitingChatInput() {
        return awaitingChatInput;
    }

    public void setAwaitingChatInput(boolean awaitingChatInput) {
        this.awaitingChatInput = awaitingChatInput;
    }

    public String getChatAction() {
        return chatAction;
    }

    public void setChatAction(String chatAction) {
        this.chatAction = chatAction;
    }

    public DealRequest getDealRequest() {
        return dealRequest;
    }

    public void setDealRequest(DealRequest dealRequest) {
        this.dealRequest = dealRequest;
    }
}