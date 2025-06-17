package com.spence.drugcraft.gui;

import com.spence.drugcraft.town.DealRequestGUI;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ActiveGUI {
    private final String guiType;
    private String menuSubType;
    private final Inventory inventory;
    private boolean awaitingChatInput;
    private String chatAction;
    private ItemStack selectedItem;
    private DealRequestGUI.DealRequest dealRequest;
    private DealerGUIHandler.PurchaseRequest purchaseRequest;

    public ActiveGUI(String guiType, String menuSubType) {
        this(guiType, null, menuSubType);
    }

    public ActiveGUI(String guiType, Inventory inventory) {
        this(guiType, inventory, null);
    }

    public ActiveGUI(String guiType, Inventory inventory, String menuSubType) {
        this.guiType = guiType;
        this.inventory = inventory;
        this.menuSubType = menuSubType;
        this.awaitingChatInput = false;
        this.chatAction = null;
    }

    public String getGuiType() {
        return guiType;
    }

    public String getMenuSubType() {
        return menuSubType;
    }

    public void setMenuSubType(String menuSubType) {
        this.menuSubType = menuSubType;
    }

    public Inventory getInventory() {
        return inventory;
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

    public ItemStack getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(ItemStack selectedItem) {
        this.selectedItem = selectedItem;
    }

    public DealRequestGUI.DealRequest getDealRequest() {
        return dealRequest;
    }

    public void setDealRequest(DealRequestGUI.DealRequest dealRequest) {
        this.dealRequest = dealRequest;
    }

    public DealerGUIHandler.PurchaseRequest getPurchaseRequest() {
        return purchaseRequest;
    }

    public void setPurchaseRequest(DealerGUIHandler.PurchaseRequest purchaseRequest) {
        this.purchaseRequest = purchaseRequest;
    }
}