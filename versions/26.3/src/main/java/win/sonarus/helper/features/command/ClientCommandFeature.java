package win.sonarus.helper.features.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import win.sonarus.helper.SonarusHelperClient;
import win.sonarus.helper.core.SonarusServer;

public final class ClientCommandFeature {
    private ClientCommandFeature() {
    }

    public static void registerIfNeeded() {
        if (!SonarusServer.isConnected()) {
            return;
        }

        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return;
        }

        CommandDispatcher<ClientSuggestionProvider> commands = connection.getCommands();
        if (commands == null || commands.getRoot().getChild("shelp") != null) {
            return;
        }

        commands.register(LiteralArgumentBuilder.<ClientSuggestionProvider>literal("shelp")
                .executes(context -> {
                    SonarusHelperClient.requestSettingsMenu();
                    return 1;
                }));
    }
}
