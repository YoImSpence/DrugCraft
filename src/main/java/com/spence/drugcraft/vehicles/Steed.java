package com.spence.drugcraft.vehicles;

import de.tr7zw.nbtapi.NBTItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Mule;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Steed {
    private final UUID ownerUUID;
    private final String type;
    private final double speed;
    private final double jumpStrength;
    private final double maxHealth;
    private final String displayName;
    private final Particle summonParticle;
    private final String summonSound;
    private Entity entity;
    private ItemStack saddleItem;
    private long cooldownUntil;

    public Steed(UUID ownerUUID, String type) {
        this.ownerUUID = ownerUUID;
        this.type = type;
        this.cooldownUntil = 0;

        switch (type.toLowerCase()) {
            case "swiftwind":
                this.speed = 0.3;
                this.jumpStrength = 1.0;
                this.maxHealth = 20.0;
                this.displayName = "<#55FF55>Swiftwind";
                this.summonParticle = Particle.HAPPY_VILLAGER;
                this.summonSound = "minecraft:entity.horse.ambient";
                break;
            case "ironhoof":
                this.speed = 0.25;
                this.jumpStrength = 1.2;
                this.maxHealth = 25.0;
                this.displayName = "<#AAAAAA>Ironhoof";
                this.summonParticle = Particle.BLOCK_CRUMBLE;
                this.summonSound = "minecraft:entity.horse.armor";
                break;
            case "shadowmare":
                this.speed = 0.35;
                this.jumpStrength = 0.9;
                this.maxHealth = 20.0;
                this.displayName = "<#333333>Shadowmare";
                this.summonParticle = Particle.SMOKE;
                this.summonSound = "minecraft:entity.horse.breathe";
                break;
            case "drug mule":
                this.speed = 0.2;
                this.jumpStrength = 0.7;
                this.maxHealth = 15.0;
                this.displayName = "<#8B4513>Drug Mule";
                this.summonParticle = Particle.DUST;
                this.summonSound = "minecraft:entity.mule.ambient";
                break;
            case "blazefury":
                this.speed = 0.32;
                this.jumpStrength = 1.1;
                this.maxHealth = 30.0;
                this.displayName = "<#FF4040>Blazefury";
                this.summonParticle = Particle.FLAME;
                this.summonSound = "minecraft:entity.horse.gallop";
                break;
            case "starbolt":
                this.speed = 0.34;
                this.jumpStrength = 1.0;
                this.maxHealth = 20.0;
                this.displayName = "<#00CED1>Starbolt";
                this.summonParticle = Particle.FIREWORK;
                this.summonSound = "minecraft:entity.horse.breathe";
                break;
            default:
                this.speed = 0.225;
                this.jumpStrength = 0.7;
                this.maxHealth = 15.0;
                this.displayName = "Unknown Steed";
                this.summonParticle = Particle.HAPPY_VILLAGER;
                this.summonSound = "minecraft:entity.horse.ambient";
        }
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public String getType() {
        return type;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
        if (entity instanceof Tameable tameable) {
            tameable.setOwner(Bukkit.getOfflinePlayer(ownerUUID));
            tameable.setTamed(true);
            if (tameable instanceof Horse horse) {
                horse.setAdult();
                horse.setAI(false);
                horse.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(speed);
                horse.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(jumpStrength);
                horse.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
                horse.setHealth(maxHealth);
                horse.customName(Component.text(displayName.replaceAll("<#[0-9A-Fa-f]{6}>", ""), TextColor.fromHexString(displayName.substring(2, 8))));
                horse.setCustomNameVisible(true);
                horse.getInventory().setSaddle(null);
            } else if (tameable instanceof Mule mule) {
                mule.setAdult();
                mule.setAI(false);
                mule.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(speed);
                mule.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(jumpStrength);
                mule.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
                mule.setHealth(maxHealth);
                mule.customName(Component.text(displayName.replaceAll("<#[0-9A-Fa-f]{6}>", ""), TextColor.fromHexString(displayName.substring(2, 8))));
                mule.setCustomNameVisible(true);
                mule.getInventory().setSaddle(new ItemStack(Material.SADDLE));
                if (type.equals("Drug Mule")) {
                    mule.setCarryingChest(true);
                }
            }
        }
    }

    public ItemStack getSaddleItem() {
        return saddleItem;
    }

    public void setSaddleItem(ItemStack saddleItem) {
        this.saddleItem = saddleItem;
        updateSaddleItemLore();
    }

    public long getCooldownUntil() {
        return cooldownUntil;
    }

    public void setCooldownUntil(long cooldownUntil) {
        this.cooldownUntil = cooldownUntil;
        updateSaddleItemLore();
    }

    public void updateSaddleItemLore() {
        if (saddleItem == null) return;
        NBTItem nbtItem = new NBTItem(saddleItem);
        ItemMeta meta = saddleItem.getItemMeta();
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Steed: " + type, TextColor.fromHexString("#FFD700")));
        if (cooldownUntil > System.currentTimeMillis()) {
            long secondsLeft = (cooldownUntil - System.currentTimeMillis()) / 1000;
            lore.add(Component.text("Cooldown: " + secondsLeft + "s", TextColor.fromHexString("#FF5555")));
        } else {
            lore.add(Component.text("Shift + Right-Click to Summon", TextColor.fromHexString("#55FF55")));
            lore.add(Component.text("Shift + Left-Click to Despawn", TextColor.fromHexString("#55FF55")));
        }
        meta.lore(lore);
        saddleItem.setItemMeta(meta);
    }

    public boolean isOnCooldown() {
        return cooldownUntil > System.currentTimeMillis();
    }

    public boolean canBeRiddenBy(Player player) {
        return player.getUniqueId().equals(ownerUUID);
    }

    public void applyEffects(Player player) {
        if (entity != null) {
            entity.getWorld().spawnParticle(summonParticle, entity.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.05);
            entity.getWorld().playSound(entity.getLocation(), summonSound, 1.0f, 1.0f);
        }
    }
}