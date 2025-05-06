package com.spence.drugcraft.utils;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.data.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CartelManager {
    private final DrugCraft plugin;
    private final DataManager dataManager;
    private final EconomyManager economyManager;
    private final Logger logger;
    private final Map<String, Cartel> cartels = new HashMap<>();
    private final Map<UUID, String> playerCartels = new HashMap<>();
    private final Map<UUID, List<String>> pendingInvites = new HashMap<>();

    public CartelManager(DrugCraft plugin, DataManager dataManager, EconomyManager economyManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.economyManager = economyManager;
        this.logger = plugin.getLogger();
        loadCartels();
    }

    private void loadCartels() {
        ConfigurationSection cartelsSection = dataManager.getCartelsConfig().getConfigurationSection("cartels");
        if (cartelsSection == null) {
            logger.info("No cartels found in cartels.yml");
            return;
        }
        for (String cartelName : cartelsSection.getKeys(false)) {
            ConfigurationSection cartelSection = cartelsSection.getConfigurationSection(cartelName);
            if (cartelSection != null) {
                UUID leader = UUID.fromString(cartelSection.getString("leader"));
                List<UUID> members = cartelSection.getStringList("members").stream()
                        .map(UUID::fromString)
                        .collect(Collectors.toList());
                int level = cartelSection.getInt("level", 1);
                double stashedMoney = cartelSection.getDouble("stashed_money", 0.0);
                Map<UUID, Map<String, Boolean>> permissions = new HashMap<>();
                ConfigurationSection permsSection = cartelSection.getConfigurationSection("permissions");
                if (permsSection != null) {
                    for (String memberId : permsSection.getKeys(false)) {
                        Map<String, Boolean> memberPerms = new HashMap<>();
                        ConfigurationSection memberPermsSection = permsSection.getConfigurationSection(memberId);
                        if (memberPermsSection != null) {
                            for (String perm : memberPermsSection.getKeys(false)) {
                                memberPerms.put(perm, memberPermsSection.getBoolean(perm));
                            }
                        }
                        permissions.put(UUID.fromString(memberId), memberPerms);
                    }
                }
                Map<String, Integer> upgrades = new HashMap<>();
                ConfigurationSection upgradesSection = cartelSection.getConfigurationSection("upgrades");
                if (upgradesSection != null) {
                    for (String upgrade : upgradesSection.getKeys(false)) {
                        upgrades.put(upgrade, upgradesSection.getInt(upgrade));
                    }
                }
                Cartel cartel = new Cartel(cartelName, leader, members, level, stashedMoney, permissions, upgrades);
                cartels.put(cartelName, cartel);
                for (UUID member : members) {
                    playerCartels.put(member, cartelName);
                }
                playerCartels.put(leader, cartelName);
                logger.fine("Loaded cartel: " + cartelName);
            }
        }
        logger.info("Loaded " + cartels.size() + " cartels");
    }

    public void createCartel(Player player, String cartelName) {
        if (playerCartels.containsKey(player.getUniqueId())) {
            player.sendMessage(MessageUtils.color("{#FF5555}You are already in a cartel."));
            return;
        }
        if (cartels.containsKey(cartelName)) {
            player.sendMessage(MessageUtils.color("{#FF5555}A cartel with this name already exists."));
            return;
        }
        Cartel cartel = new Cartel(cartelName, player.getUniqueId(), new ArrayList<>(), 1, 0.0, new HashMap<>(), new HashMap<>());
        cartels.put(cartelName, cartel);
        playerCartels.put(player.getUniqueId(), cartelName);
        dataManager.saveCartels();
        player.sendMessage(MessageUtils.color("{#00FF00}Created cartel: " + cartelName));
        logger.info("Player " + player.getName() + " created cartel: " + cartelName);
    }

    public void invitePlayer(Player leader, Player target) {
        String cartelName = playerCartels.get(leader.getUniqueId());
        if (cartelName == null) {
            leader.sendMessage(MessageUtils.color("{#FF5555}You are not in a cartel."));
            return;
        }
        Cartel cartel = cartels.get(cartelName);
        if (!cartel.getLeader().equals(leader.getUniqueId())) {
            leader.sendMessage(MessageUtils.color("{#FF5555}Only the cartel leader can invite players."));
            return;
        }
        if (playerCartels.containsKey(target.getUniqueId())) {
            leader.sendMessage(MessageUtils.color("{#FF5555}This player is already in a cartel."));
            return;
        }
        pendingInvites.computeIfAbsent(target.getUniqueId(), k -> new ArrayList<>()).add(cartelName);
        target.sendMessage(MessageUtils.color("{#00FF00}You have been invited to join the cartel: " + cartelName + ". Use /cartel join " + cartelName + " to accept."));
        leader.sendMessage(MessageUtils.color("{#00FF00}Invited " + target.getName() + " to your cartel."));
        logger.info("Player " + leader.getName() + " invited " + target.getName() + " to cartel: " + cartelName);
    }

    public void joinCartel(Player player, String cartelName) {
        if (playerCartels.containsKey(player.getUniqueId())) {
            player.sendMessage(MessageUtils.color("{#FF5555}You are already in a cartel."));
            return;
        }
        Cartel cartel = cartels.get(cartelName);
        if (cartel == null) {
            player.sendMessage(MessageUtils.color("{#FF5555}Cartel not found."));
            return;
        }
        List<String> invites = pendingInvites.getOrDefault(player.getUniqueId(), new ArrayList<>());
        if (!invites.contains(cartelName)) {
            player.sendMessage(MessageUtils.color("{#FF5555}You have not been invited to this cartel."));
            return;
        }
        cartel.getMembers().add(player.getUniqueId());
        playerCartels.put(player.getUniqueId(), cartelName);
        invites.remove(cartelName);
        if (invites.isEmpty()) {
            pendingInvites.remove(player.getUniqueId());
        }
        dataManager.saveCartels();
        Player leader = Bukkit.getPlayer(cartel.getLeader());
        if (leader != null) {
            leader.sendMessage(MessageUtils.color("{#00FF00}" + player.getName() + " has joined your cartel."));
        }
        player.sendMessage(MessageUtils.color("{#00FF00}You have joined the cartel: " + cartelName));
        logger.info("Player " + player.getName() + " joined cartel: " + cartelName);
    }

    public void leaveCartel(Player player) {
        String cartelName = playerCartels.get(player.getUniqueId());
        if (cartelName == null) {
            player.sendMessage(MessageUtils.color("{#FF5555}You are not in a cartel."));
            return;
        }
        Cartel cartel = cartels.get(cartelName);
        if (cartel.getLeader().equals(player.getUniqueId())) {
            player.sendMessage(MessageUtils.color("{#FF5555}As the leader, you must disband the cartel or transfer leadership."));
            return;
        }
        cartel.getMembers().remove(player.getUniqueId());
        playerCartels.remove(player.getUniqueId());
        dataManager.saveCartels();
        Player leader = Bukkit.getPlayer(cartel.getLeader());
        if (leader != null) {
            leader.sendMessage(MessageUtils.color("{#FF5555}" + player.getName() + " has left your cartel."));
        }
        player.sendMessage(MessageUtils.color("{#00FF00}You have left the cartel: " + cartelName));
        logger.info("Player " + player.getName() + " left cartel: " + cartelName);
    }

    public void showCartelInfo(Player player) {
        String cartelName = playerCartels.get(player.getUniqueId());
        if (cartelName == null) {
            player.sendMessage(MessageUtils.color("{#FF5555}You are not in a cartel."));
            return;
        }
        Cartel cartel = cartels.get(cartelName);
        Player leader = Bukkit.getPlayer(cartel.getLeader());
        player.sendMessage(MessageUtils.color("{#FFD700}Cartel Info:"));
        player.sendMessage(MessageUtils.color("{#AAAAAA}Name: " + cartelName));
        player.sendMessage(MessageUtils.color("{#AAAAAA}Leader: " + (leader != null ? leader.getName() : "Offline")));
        player.sendMessage(MessageUtils.color("{#AAAAAA}Level: " + cartel.getLevel()));
        player.sendMessage(MessageUtils.color("{#AAAAAA}Stashed Money: $" + cartel.getStashedMoney()));
        player.sendMessage(MessageUtils.color("{#AAAAAA}Members: " + cartel.getMembers().size()));
    }

    public void updatePermissions(String cartelName, UUID memberId, Map<String, Boolean> permissions) {
        Cartel cartel = cartels.get(cartelName);
        if (cartel != null) {
            cartel.getPermissions().put(memberId, permissions);
            dataManager.saveCartels();
            logger.info("Updated permissions for member " + memberId + " in cartel: " + cartelName);
        }
    }

    public void upgradeCartel(String cartelName, String upgrade, int level, double cost) {
        Cartel cartel = cartels.get(cartelName);
        if (cartel != null) {
            cartel.setStashedMoney(cartel.getStashedMoney() - cost);
            cartel.getUpgrades().put(upgrade, level);
            if (level > cartel.getLevel()) {
                cartel.setLevel(level);
            }
            dataManager.saveCartels();
            logger.info("Upgraded " + upgrade + " to level " + level + " for cartel: " + cartelName);
        }
    }

    public double getGrowthBonus(Location location) {
        for (Cartel cartel : cartels.values()) {
            int level = cartel.getUpgrades().getOrDefault("Crop Growth Speed", 0);
            return level * 0.1; // 10% reduction per level
        }
        return 0.0;
    }

    public Cartel getCartel(String cartelName) {
        return cartels.get(cartelName);
    }

    public String getPlayerCartel(UUID playerId) {
        return playerCartels.get(playerId);
    }

    public Map<String, Cartel> getCartels() {
        return cartels;
    }

    public static class Cartel {
        private final String name;
        private final UUID leader;
        private final List<UUID> members;
        private int level;
        private double stashedMoney;
        private final Map<UUID, Map<String, Boolean>> permissions;
        private final Map<String, Integer> upgrades;

        public Cartel(String name, UUID leader, List<UUID> members, int level, double stashedMoney,
                      Map<UUID, Map<String, Boolean>> permissions, Map<String, Integer> upgrades) {
            this.name = name;
            this.leader = leader;
            this.members = members;
            this.level = level;
            this.stashedMoney = stashedMoney;
            this.permissions = permissions;
            this.upgrades = upgrades;
        }

        public String getName() {
            return name;
        }

        public UUID getLeader() {
            return leader;
        }

        public List<UUID> getMembers() {
            return members;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public double getStashedMoney() {
            return stashedMoney;
        }

        public void setStashedMoney(double stashedMoney) {
            this.stashedMoney = stashedMoney;
        }

        public Map<UUID, Map<String, Boolean>> getPermissions() {
            return permissions;
        }

        public Map<String, Integer> getUpgrades() {
            return upgrades;
        }

        public double getPoliceReduction() {
            int level = upgrades.getOrDefault("Police Reduction", 0);
            return level * 0.2; // 20% reduction per level
        }
    }
}