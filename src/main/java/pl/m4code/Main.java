package pl.m4code;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;
import pl.m4code.commands.GeneratorCommand;
import pl.m4code.commands.api.CommandAPI;
import pl.m4code.system.GeneratorSystem;

import java.lang.reflect.Field;
import java.util.List;

@Getter
public final class Main extends JavaPlugin {
    @Getter private static Main instance;
    private GeneratorSystem generatorSystem;

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Initialize the GeneratorSystem before registering listeners
        generatorSystem = new GeneratorSystem();

        try {
            registerCommands();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        registerListeners();
        registerTasks();

        generatorSystem.loadGenerators();
    }

    @SneakyThrows
    private void registerCommands() throws NoSuchFieldException, IllegalAccessException {
        final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        bukkitCommandMap.setAccessible(true);
        final CommandMap commandMap = (CommandMap) bukkitCommandMap.get(getServer());
        for (CommandAPI commands : List.of(
                new GeneratorCommand()
        )) {
            commandMap.register(commands.getName(), commands);
        }
    }

    private void registerListeners() {
        if (generatorSystem != null) {
            getServer().getPluginManager().registerEvents(generatorSystem, this);
        } else {
            getLogger().severe("GeneratorSystem is null and cannot be registered as a listener.");
        }
    }

    private void registerTasks() {
    }

    public void reloadGenerators() {
        generatorSystem.reloadGenerators();
    }
    public GeneratorSystem getGeneratorSystem() {
        return generatorSystem;
    }
}