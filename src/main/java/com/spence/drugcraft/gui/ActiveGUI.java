package com.spence.drugcraft.gui;

import org.bukkit.inventory.Inventory;

public class ActiveGUI {
    private final String type;
    private final Inventory inventory;
    private boolean awaitingChatInput;
    private String chatAction;
    private String subType;

    public ActiveGUI(String type, Inventory inventory) {
        this(type, inventory, null);
    }

    public ActiveGUI(String type, Inventory inventory, String subType) {
        this.type = type;
        this.inventory = inventory;
        this.subType = subType;
        this.awaitingChatInput = false;
        this.chatAction = null;
    }

    public String getType() {
        return type;
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

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }
}