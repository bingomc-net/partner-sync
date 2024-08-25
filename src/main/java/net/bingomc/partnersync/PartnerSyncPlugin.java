package net.bingomc.partnersync;

import com.google.inject.Inject;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import lombok.Getter;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "bingomc-partner-sync",
        name = "BingoMCPartnerSync",
        version = BuildConstants.VERSION,
        description = "Syncs servers from the BingoMC Partner API to Velocity",
        url = "https://bingomc.net",
        authors = {"jensjeflensje"}
)
public class PartnerSyncPlugin {

    @Getter
    private static Logger logger;

    @Getter
    private static ProxyServer server;

    @Getter
    private static PartnerSyncPlugin instance;

    @Getter
    private static YamlDocument config;

    @Getter
    private static String apiKey;

    @Getter
    private static String apiUrl;

    @Inject
    public PartnerSyncPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        PartnerSyncPlugin.server = server;
        PartnerSyncPlugin.logger = logger;
        PartnerSyncPlugin.instance = this;

        setupConfig(dataDirectory);
        loadConfigValues();
;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getScheduler().buildTask(
                this, ServerHandler::updateServers).repeat(3L, TimeUnit.SECONDS).schedule();

        CommandManager commandManager = server.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("bingomc")
                .aliases("bmc")
                .plugin(this)
                .build();

        BrigadierCommand managementCommand = ServerManagementCommand.createBrigadierCommand(server);

        commandManager.register(commandMeta, managementCommand);
    }

    private void setupConfig(Path dataDirectory) {
        String filename = "config.yml";

        try {
            config = YamlDocument.create(
                    new File(dataDirectory.toFile(), filename),
                    Objects.requireNonNull(getClass().getResourceAsStream("/" + filename)),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder()
                            .setVersioning(new BasicVersioning("config-version"))
                            .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS).build()
            );

            config.update();
            config.save();
        } catch (IOException e) {
            logger.error("Could not create/load plugin configuration! This plugin will shutdown");
            shutdownPlugin();
        }
    }

    private void loadConfigValues() {
        apiKey = config.getString(Route.from("api-key"));
        apiUrl = config.getString(Route.from("api-url"));
    }

    private void shutdownPlugin() {
        Optional<PluginContainer> container = server.getPluginManager().getPlugin("bingomc-partner-sync");
        container.ifPresent(pluginContainer -> pluginContainer.getExecutorService().shutdown());
    }
}
