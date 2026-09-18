package win.sonarus.helper.core;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

public final class SonarusServer {
    private SonarusServer() {
    }

    public static String currentAddress() {
        MinecraftClient client = MinecraftClient.getInstance();
        ServerInfo server = client.getCurrentServerEntry();
        return server == null ? null : server.address;
    }

    public static boolean isConnected() {
        return ServerAddressMatcher.isSonarus(currentAddress());
    }
}
