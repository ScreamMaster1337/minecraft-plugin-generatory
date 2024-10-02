package pl.m4code.commands;

import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import pl.m4code.Main;
import pl.m4code.commands.api.CommandAPI;
import pl.m4code.system.GeneratorSystem;
import pl.m4code.utils.TextUtil;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GeneratorCommand extends CommandAPI {
    public GeneratorCommand() {
        super(
                "generator",
                "",
                "",
                "/generator <create|delete|list|reload> [czas|id]",
                List.of("gen")
        );
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Sprawdzenie czy komenda została wysłana przez gracza
        if (!(sender instanceof Player player)) {
            TextUtil.sendMessage(sender, "&cPodana komenda jest dostępna tylko dla graczy!");
            return;
        }

        // Sprawdzenie czy gracz ma odpowiednie uprawnienia
        if (!player.hasPermission("m4code.generator")) {
            TextUtil.sendMessage(player, "&cNie masz uprawnień do używania tej komendy!");
            return;
        }

        if (args.length < 1) {
            TextUtil.sendMessage(player, "&4Błąd &cPoprawne użycie: &7/generator <create|delete|list|reload> [czas|id]");
            return;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "create":
                handleCreate(player, args);
                break;
            case "delete":
                handleDelete(player, args);
                break;
            case "list":
                handleList(player);
                break;
            case "reload":
                handleReload(player);
                break;
            default:
                TextUtil.sendMessage(player, "&4Błąd &cPoprawne użycie: &7/generator <create|delete|list|reload> [czas|id]");
                break;
        }
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            TextUtil.sendMessage(player, "&4Błąd &cPoprawne użycie: &7/generator create <czas>");
            return;
        }

        int czas;
        try {
            czas = parseTime(args[1]);
        } catch (IllegalArgumentException e) {
            TextUtil.sendMessage(player, "&cPodano nieprawidłowy czas. Użyj formatu: <czas>s, <czas>m, <czas>h, <czas>d, np. 10s, 5m, 1h, 2d");
            return;
        }

        // Pobranie itemu z ręki gracza
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            TextUtil.sendMessage(player, "&cMusisz trzymać przedmiot w ręce, aby utworzyć generator!");
            return;
        }

        // Pobranie informacji o itemie
        ItemMeta meta = item.getItemMeta();
        String customName = meta != null && meta.hasDisplayName() ? meta.getDisplayName().replace('§', '&') : "";
        List<String> lore = meta != null && meta.hasLore() ? meta.getLore().stream().map(line -> line.replace('§', '&')).collect(Collectors.toList()) : List.of();
        int modelData = meta != null && meta.hasCustomModelData() ? meta.getCustomModelData() : 0;
        int amount = item.getAmount();
        List<String> enchantments = meta != null && meta.hasEnchants() ? meta.getEnchants().entrySet().stream()
                .map(entry -> entry.getKey().getKey().getKey() + ":" + entry.getValue())
                .collect(Collectors.toList()) : List.of();

        // Pobranie pozycji gracza
        double x = player.getLocation().getX();
        double y = player.getLocation().getY();
        double z = player.getLocation().getZ();
        String world = player.getLocation().getWorld().getName();

        // Zapisanie informacji do configu
        FileConfiguration config = Main.getInstance().getConfig();
        int generatorId = config.getConfigurationSection("generator").getKeys(false).size() + 1;
        String path = "generator." + generatorId;
        config.set(path + ".time", czas);
        config.set(path + ".spawn.x", x);
        config.set(path + ".spawn.y", y);
        config.set(path + ".spawn.z", z);
        config.set(path + ".spawn.world", world);
        config.set(path + ".items.material", item.getType().name());
        config.set(path + ".items.name", customName);
        config.set(path + ".items.amount", amount);
        config.set(path + ".items.modelData", modelData);
        config.set(path + ".items.lore", lore);
        config.set(path + ".items.enchantments", enchantments.isEmpty() ? null : enchantments);
        Main.getInstance().saveConfig();

        // Załaduj nowo utworzony generator
        GeneratorSystem generatorSystem = Main.getInstance().getGeneratorSystem();
        generatorSystem.loadGenerator(generatorId);

        // Wysłanie wiadomości do gracza
        TextUtil.sendMessage(player, "&aPomyslnie utworzono generator!");
    }

    private void handleDelete(Player player, String[] args) {
        if (args.length < 2) {
            TextUtil.sendMessage(player, "&4Błąd &cPoprawne użycie: &7/generator delete <id>");
            return;
        }

        int generatorId;
        try {
            generatorId = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            TextUtil.sendMessage(player, "&cPodano nieprawidłowy ID generatora.");
            return;
        }

        FileConfiguration config = Main.getInstance().getConfig();
        String path = "generator." + generatorId;
        if (config.contains(path)) {
            config.set(path, null);
            Main.getInstance().saveConfig();

            // Stop the generator task
            GeneratorSystem generatorSystem = Main.getInstance().getGeneratorSystem();
            generatorSystem.stopGenerator(generatorId);

            TextUtil.sendMessage(player, "&aPomyslnie usunięto generator o ID: " + generatorId);
        } else {
            TextUtil.sendMessage(player, "&cGenerator o ID " + generatorId + " nie istnieje.");
        }
    }

    private void handleList(Player player) {
        FileConfiguration config = Main.getInstance().getConfig();
        if (config.contains("generator")) {
            List<String> generatorList = config.getConfigurationSection("generator").getKeys(false).stream()
                    .map(id -> {
                        String path = "generator." + id;
                        double x = config.getDouble(path + ".spawn.x");
                        double y = config.getDouble(path + ".spawn.y");
                        double z = config.getDouble(path + ".spawn.z");
                        String world = config.getString(path + ".spawn.world");
                        return "ID: " + id + " - Koordynaty: [" + x + ", " + y + ", " + z + "] w świecie " + world;
                    })
                    .collect(Collectors.toList());
            TextUtil.sendMessage(player, "&aLista generatorów:\n" + String.join("\n", generatorList));
        } else {
            TextUtil.sendMessage(player, "&cBrak zarejestrowanych generatorów.");
        }
    }

    private void handleReload(Player player) {
        if (!player.hasPermission("m4code.reload")) {
            TextUtil.sendMessage(player, "&cNie masz uprawnien do tej komendy!");
            return;
        }

        Main.getInstance().reloadConfig();
        Main.getInstance().getGeneratorSystem().reloadGenerators();
        TextUtil.sendMessage(player, "&aPliki konfiguracyjne oraz generatory zostały przeładowane!");
    }

    @Override
    public List<String> tab(@NonNull Player player, @NotNull @NonNull String[] args) {
        if (args.length == 1) {
            return List.of("create", "delete", "list", "reload");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            return List.of("10s");
        }
        return null;
    }

    private int parseTime(String timeString) {
        Pattern pattern = Pattern.compile("(?:(\\d+)d)?(?:(\\d+)h)?(?:(\\d+)m)?(?:(\\d+)s)?");
        Matcher matcher = pattern.matcher(timeString);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid time format");
        }

        int days = matcher.group(1) != null ? Integer.parseInt(matcher.group(1)) : 0;
        int hours = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 0;
        int minutes = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 0;
        int seconds = matcher.group(4) != null ? Integer.parseInt(matcher.group(4)) : 0;

        return days * 86400 + hours * 3600 + minutes * 60 + seconds;
    }
}