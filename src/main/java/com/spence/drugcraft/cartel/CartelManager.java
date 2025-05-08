package com.spence.drugcraft.cartel;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
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
                try {
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
                    Map<String, Object> stash = new HashMap<>();
                    ConfigurationSection stashSection = cartelSection.getConfigurationSection("stash");
                    if (stashSection != null) {
                        for (String key : stashSection.getKeys(false)) {
                            stash.put(key, stashSection.get(key));
                        }
                    }
                    Cartel cartel = new Cartel(cartelName, leader, members, level, stashedMoney, permissions, upgrades, stash);
                    cartels.put(cartelName, cartel);
                    for (UUID member : members) {
                        playerCartels.put(member, cartelName);
                    }
                    playerCartels.put(leader, cartelName);
                    logger.fine("Loaded cartel: " + cartelName);
                } catch (IllegalArgumentException e) {
                    logger.warning("Failed to load cartel " + cartelName + ": Invalid UUID format (" + e.getMessage() + ")");
                }
            }
        }
        logger.info("Loaded " + cartels.size() + " cartels");
    }

    public void createCartel(Player player, String cartelName) {
        if (playerCartels.containsKey(player.getUniqueId())) {
            player.sendMessage(MessageUtils.color("&#FF4040You are already in a cartel."));
            return;
        }
        if (cartelName == null || cartelName.trim().isEmpty()) {
            player.sendMessage(MessageUtils.color("&#FF4040Cartel name cannot be empty."));
            return;
        }
        if (cartels.containsKey(cartelName)) {
            player.sendMessage(MessageUtils.color("&#FF4040A cartel with this name already exists."));
            return;
        }
        Map<UUID, Map<String, Boolean>> permissions = new HashMap<>();
        Map<String, Boolean> leaderPermissions = new HashMap<>();
        leaderPermissions.put("Harvest Crops", true);
        leaderPermissions.put("Plant Crops", true);
        leaderPermissions.put("Access Stash", true);
        permissions.put(player.getUniqueId(), leaderPermissions);
        Cartel cartel = new Cartel(cartelName, player.getUniqueId(), new ArrayList<>(), 1, 0.0, permissions, new HashMap<>(), new HashMap<>());
        cartels.put(cartelName, cartel);
        playerCartels.put(player.getUniqueId(), cartelName);
        dataManager.saveCartels();
        player.sendMessage(MessageUtils.color("&#00FF7FCreated cartel: " + cartelName));
        logger.info("Player " + player.getName() + " created cartel: " + cartelName);
    }

    public void invitePlayer(Player leader, Player target) {
        String cartelName = playerCartels.get(leader.getUniqueId());
        if (cartelName == null) {
            leader.sendMessage(MessageUtils.color("&#FF4040You are not in a cartel."));
            return;
        }
        Cartel cartel = cartels.get(cartelName);
        if (!cartel.getLeader().equals(leader.getUniqueId())) {
            leader.sendMessage(MessageUtils.color("&#FF4040Only the cartel leader can invite players."));
            return;
        }
        if (target == null) {
            leader.sendMessage(MessageUtils.color("&#FF4040Target player not found."));
            return;
        }
        if (playerCartels.containsKey(target.getUniqueId())) {
            leader.sendMessage(MessageUtils.color("&#FF4040This player is already in a cartel."));
            return;
        }
        pendingInvites.computeIfAbsent(target.getUniqueId(), k -> new ArrayList<>()).add(cartelName);
        target.sendMessage(MessageUtils.color("&#00FF7FYou have been invited to join the cartel: " + cartelName + ". Use /cartel join " + cartelName + " to accept."));
        leader.sendMessage(MessageUtils.color("&#00FF7FInvited " + target.getName() + " to your cartel."));
        logger.info("Player " + leader.getName() + " invited " + target.getName() + " to cartel: " + cartelName);
    }

    public void joinCartel(Player player, String cartelName) {
        if (playerCartels.containsKey(player.getUniqueId())) {
            player.sendMessage(MessageUtils.color("&#FF4040You are already in a cartel."));
            return;
        }
        Cartel cartel = cartels.get(cartelName);
        if (cartel == null) {
            player.sendMessage(MessageUtils.color("&#FF4040Cartel not found."));
            return;
        }
        List<String> invites = pendingInvites.getOrDefault(player.getUniqueId(), new ArrayList<>());
        if (!invites.contains(cartelName)) {
            player.sendMessage(MessageUtils.color("&#FF4040You have not been invited to this cartel."));
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
            leader.sendMessage(MessageUtils.color("&#00FF7F" + player.getName() + " has joined your cartel."));
        }
        player.sendMessage(MessageUtils.color("&#00FF7FYou have joined the cartel: " + cartelName));
        logger.info("Player " + player.getName() + " joined cartel: " + cartelName);
    }

    public void leaveCartel(Player player) {
        String cartelName = playerCartels.get(player.getUniqueId());
        if (cartelName == null) {
            player.sendMessage(MessageUtils.color("&#FF4040You are not in a cartel."));
            return;
        }
        Cartel cartel = cartels.get(cartelName);
        if (cartel.getLeader().equals(player.getUniqueId())) {
            cartels.remove(cartelName);
            playerCartels.remove(player.getUniqueId());
            dataManager.saveCartels();
            player.sendMessage(MessageUtils.color("&#00FF7FDisbanded cartel: " + cartelName));
            logger.info("Player " + player.getName() + " disbanded cartel: " + cartelName);
            return;
        }
        cartel.getMembers().remove(player.getUniqueId());
        playerCartels.remove(player.getUniqueId());
        dataManager.saveCartels();
        Player leader = Bukkit.getPlayer(cartel.getLeader());
        if (leader != null) {
            leader.sendMessage(MessageUtils.color("&#FF4040" + player.getName() + " has left your cartel."));
        }
        player.sendMessage(MessageUtils.color("&#00FF7FYou have left the cartel: " + cartelName));
        logger.info("Player " + player.getName() + " left cartel: " + cartelName);
    }

    public void showCartelInfo(Player player) {
        String cartelName = playerCartels.get(player.getUniqueId());
        if (cartelName == null) {
            player.sendMessage(MessageUtils.color("&#FF4040You are not in a cartel."));
            return;
        }
        Cartel cartel = cartels.get(cartelName);
        Player leader = Bukkit.getPlayer(cartel.getLeader());
        player.sendMessage(MessageUtils.color("&#FFFF00Cartel Info:"));
        player.sendMessage(MessageUtils.color("&#D3D3D3Name: " + cartelName));
        player.sendMessage(MessageUtils.color("&#D3D3D3Leader: " + (leader != null ? leader.getName() : "Offline")));
        player.sendMessage(MessageUtils.color("&#D3D3D3Level: " + cartel.getLevel()));
        player.sendMessage(MessageUtils.color("&#D3D3D3Stashed Money: $" + cartel.getStashedMoney()));
        player.sendMessage(MessageUtils.color("&#D3D3D3Members: " + cartel.getMembers().size()));
    }

    public void updatePermissions(String cartelName, UUID memberId, Map<String, Boolean> permissions) {
        Cartel cartel = cartels.get(cartelName);
        if (cartel != null) {
            if (memberId.equals(cartel.getLeader())) return;
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
            return level * 0.1;
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
        private final Map<String, Object> stash;

        public Cartel(String name, UUID leader, List<UUID> members, int level, double stashedMoney,
                      Map<UUID, Map<String, Boolean>> permissions, Map<String, Integer> upgrades, Map<String, Object> stash) {
            this.name = name;
            this.leader = leader;
            this.members = members;
            this.level = level;
            this.stashedMoney = stashedMoney;
            this.permissions = permissions;
            this.upgrades = upgrades;
            this.stash = stash;
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

        public Map<String, Object> getStash() {
            return stash;
        }

        public double getPoliceReduction() {
            int level = upgrades.getOrDefault("Police Reduction", 0);
            return level * 0.2;
        }
    }
}