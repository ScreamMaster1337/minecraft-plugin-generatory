package pl.m4code.system;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import pl.m4code.Main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GeneratorSystem implements Listener {
    private final Map<Integer, BukkitTask> generatorTasks = new HashMap<>();

    public void loadGenerators() {
        FileConfiguration config = Main.getInstance().getConfig();
        if (config.contains("generator")) {
            Set<String> generatorIds = config.getConfigurationSection("generator").getKeys(false);
            for (String id : generatorIds) {
                int generatorId = Integer.parseInt(id);
                loadGenerator(generatorId);
            }
        } else {
            Bukkit.getLogger().info("Brak generatorów w configu.");
        }
    }

    public void reloadGenerators() {
        Bukkit.getScheduler().cancelTasks(Main.getInstance());
        generatorTasks.clear();
        loadGenerators();
    }

    public void loadGenerator(int generatorId) {
        FileConfiguration config = Main.getInstance().getConfig();
        String path = "generator." + generatorId;

        if (config.contains(path)) {
            int time = config.getInt(path + ".time");
            double x = config.getDouble(path + ".spawn.x");
            double y = config.getDouble(path + ".spawn.y");
            double z = config.getDouble(path + ".spawn.z");
            String worldName = config.getString(path + ".spawn.world");
            Location location = new Location(Bukkit.getWorld(worldName), x, y, z);

            if (location.getWorld() == null) {
                return;
            }

            String materialName = config.getString(path + ".items.material");
            Material material = Material.getMaterial(materialName);
            if (material == null) {
                return;
            }

            int amount = config.getInt(path + ".items.amount");
            int modelData = config.getInt(path + ".items.modelData");
            String customName = ChatColor.translateAlternateColorCodes('&', config.getString(path + ".items.name"));
            List<String> lore = config.getStringList(path + ".items.lore").stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .collect(Collectors.toList());

            ItemStack item = new ItemStack(material, amount);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(customName);
                meta.setLore(lore);
                if (modelData > 0) {
                    meta.setCustomModelData(modelData);
                }

                // Check if the enchantments section exists
                if (config.contains(path + ".items.enchantments")) {
                    List<String> enchantments = config.getStringList(path + ".items.enchantments");
                    for (String enchantmentString : enchantments) {
                        String[] parts = enchantmentString.split(":");
                        if (parts.length == 2) {
                            Enchantment enchantment = Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft(parts[0].toLowerCase()));
                            if (enchantment != null) {
                                try {
                                    int level = Integer.parseInt(parts[1]);
                                    meta.addEnchant(enchantment, level, true);
                                } catch (NumberFormatException e) {
                                }
                            }
                        }
                    }
                }
                item.setItemMeta(meta);
            }

            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    location.getWorld().dropItem(location, item).setVelocity(new org.bukkit.util.Vector(0, 0, 0));
                }
            }.runTaskTimer(Main.getInstance(), 0, time * 20L);

            generatorTasks.put(generatorId, task);
        } else {
        }
    }

    public void stopGenerator(int generatorId) {
        BukkitTask task = generatorTasks.remove(generatorId);
        if (task != null) {
            task.cancel();
        }
    }

    public int getTimeRemaining(int generatorId) {
        FileConfiguration config = Main.getInstance().getConfig();
        String path = "generator." + generatorId + ".time";
        if (config.contains(path)) {
            return config.getInt(path);
        } else {
            return -1;
        }
    }
}