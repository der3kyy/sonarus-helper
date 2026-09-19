package win.sonarus.helper.features.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import win.sonarus.helper.SonarusHelperClient;
import win.sonarus.helper.core.SonarusServer;

public final class ClientCommandFeature {
    private ClientCommandFeature() {
    }

    public static void registerIfNeeded() {
        if (!SonarusServer.isConnected()) {
            return;
        }

        ClientPlayNetworkHandler connection = MinecraftClient.getInstance().getNetworkHandler();
        if (connection == null) {
            return;
        }

        CommandDispatcher<ClientCommandSource> commands = connection.getCommandDispatcher();
        if (commands == null || commands.getRoot().getChild("shelp") != null) {
            return;
        }

        commands.register(LiteralArgumentBuilder.<ClientCommandSource>literal("shelp")
                .executes(context -> {
                    SonarusHelperClient.requestSettingsMenu();
                    return 1;
                }));
    }
}
