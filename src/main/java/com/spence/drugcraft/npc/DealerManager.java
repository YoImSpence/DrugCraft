package com.spence.drugcraft.npc;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class DealerManager {
    // Thread-safe collection to handle potential concurrent access.
    private final Map<Integer, DealerData> dealerDataMap = new ConcurrentHashMap<>();
    private final Logger logger;

    // Constants for configuration keys
    private static final String NPC_ID_KEY = "npcId";
    private static final String SUCCESS_CHANCE_KEY = "successChance";
    private static final String ACCEPTED_DRUGS_KEY = "acceptedDrugs";
    private static final String PAYOUT_KEY = "payout";

    public DealerManager(JavaPlugin plugin) {
        this.logger = plugin.getLogger();
        ConfigurationSection dealers = plugin.getConfig().getConfigurationSection("dealers");

        if (dealers != null) {
            for (String key : dealers.getKeys(false)) {
                ConfigurationSection section = dealers.getConfigurationSection(key);
                if (section == null) {
                    logger.warning("Missing configuration section for dealer: " + key);
                    continue;
                }

                int npcId = section.getInt(NPC_ID_KEY, -1);
                if (npcId == -1) {
                    logger.warning("Invalid or missing NPC ID for dealer: " + key);
                    continue;
                }

                double successChance = section.getDouble(SUCCESS_CHANCE_KEY, 0.5);
                if (successChance < 0.0 || successChance > 1.0) {
                    logger.warning("Invalid success chance for NPC ID " + npcId + ". Using default value 0.5.");
                    successChance = 0.5;
                }

                List<String> acceptedDrugs = section.getStringList(ACCEPTED_DRUGS_KEY);
                if (acceptedDrugs.isEmpty()) {
                    logger.warning("Dealer NPC ID " + npcId + " has no accepted drugs specified.");
                    acceptedDrugs = Collections.emptyList(); // Avoid null issues
                }

                Map<String, Double> payoutMap = new HashMap<>();
                ConfigurationSection payoutSection = section.getConfigurationSection(PAYOUT_KEY);
                if (payoutSection != null) {
                    for (String drug : payoutSection.getKeys(false)) {
                        double payoutValue = payoutSection.getDouble(drug, 0.0);
                        payoutMap.put(drug, payoutValue);
                    }

                    // Validate that every accepted drug has a payout defined
                    for (String drug : acceptedDrugs) {
                        if (!payoutMap.containsKey(drug)) {
                            logger.warning("Dealer NPC ID " + npcId + " accepts drug '" + drug + "' but has no payout value set. Defaulting to $0.");
                            payoutMap.put(drug, 0.0); // Default payout to $0 if not provided
                        }
                    }
                } else {
                    logger.warning("Dealer NPC ID " + npcId + " has no payout section in the configuration.");
                }

                // Add the dealer data
                dealerDataMap.put(npcId, new DealerData(acceptedDrugs, successChance, payoutMap));
            }
        } else {
            logger.warning("No dealers section found in the configuration file.");
        }
    }

    /**
     * Get the `DealerData` for a specific NPC by its ID.
     * @param npcId The ID of the NPC to retrieve dealer data for.
     * @return The `DealerData` instance, or null if none exists for the given ID.
     */
    public DealerData getDealerData(int npcId) {
        return dealerDataMap.get(npcId);
    }

    /**
     * Remove dealer data for a specific NPC by its ID. Useful for reloads or updates.
     * @param npcId The ID of the dealer to remove.
     */
    public void removeDealerData(int npcId) {
        dealerDataMap.remove(npcId);
    }

    /**
     * Reload all dealer data from the configuration.
     * Clears existing data, then re-populates it using the current plugin configuration.
     * @param plugin The plugin instance to fetch configuration from.
     */
    public void reloadDealerData(JavaPlugin plugin) {
        dealerDataMap.clear();
        ConfigurationSection dealers = plugin.getConfig().getConfigurationSection("dealers");
        if (dealers != null) {
            for (String key : dealers.getKeys(false)) {
                ConfigurationSection section = dealers.getConfigurationSection(key);
                if (section == null) continue;

                int npcId = section.getInt(NPC_ID_KEY, -1);
                double successChance = section.getDouble(SUCCESS_CHANCE_KEY, 0.5);

                List<String> acceptedDrugs = section.getStringList(ACCEPTED_DRUGS_KEY);
                Map<String, Double> payoutMap = new HashMap<>();
                ConfigurationSection payoutSection = section.getConfigurationSection(PAYOUT_KEY);
                if (payoutSection != null) {
                    for (String drug : payoutSection.getKeys(false)) {
                        payoutMap.put(drug, payoutSection.getDouble(drug));
                    }
                }

                dealerDataMap.put(npcId, new DealerData(acceptedDrugs, successChance, payoutMap));
            }
        }
    }

    /**
     * Represents the data for an individual dealer NPC.
     */
    public static class DealerData {
        private final List<String> acceptedDrugs;
        private final double successChance;
        private final Map<String, Double> payoutMap;

        public DealerData(List<String> acceptedDrugs, double successChance, Map<String, Double> payoutMap) {
            this.acceptedDrugs = acceptedDrugs;
            this.successChance = successChance;
            this.payoutMap = payoutMap;
        }

        public List<String> getAcceptedDrugs() {
            return acceptedDrugs;
        }

        public double getSuccessChance() {
            return successChance;
        }

        public Map<String, Double> getPayoutMap() {
            return payoutMap;
        }
    }

}