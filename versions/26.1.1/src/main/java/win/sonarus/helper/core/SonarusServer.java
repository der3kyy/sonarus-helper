package win.sonarus.helper.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

public final class SonarusServer {
    private SonarusServer() {
    }

    public static String currentAddress() {
        Minecraft client = Minecraft.getInstance();
        ServerData server = client.getCurrentServer();
        return server == null ? null : server.ip;
    }

    public static boolean isConnected() {
        return ServerAddressMatcher.isSonarus(currentAddress());
    }
}
