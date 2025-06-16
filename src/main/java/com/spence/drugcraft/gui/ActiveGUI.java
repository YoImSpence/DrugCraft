package com.spence.drugcraft.gui;

import org.bukkit.inventory.Inventory;

public class ActiveGUI {
    private final String guiType;
    private String menuSubType;
    private final Inventory inventory;
    private boolean awaitingChatInput;
    private String chatAction;

    public ActiveGUI(String guiType, String menuSubType) {
        this(guiType, null, menuSubType);
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
}