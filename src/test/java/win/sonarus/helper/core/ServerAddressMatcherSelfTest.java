package win.sonarus.helper.core;

public final class ServerAddressMatcherSelfTest {
    public static void main(String[] args) {
        expect(true, ServerAddressMatcher.isSonarus("play.sonarus.win"));
        expect(true, ServerAddressMatcher.isSonarus("PLAY.SONARUS.WIN"));
        expect(true, ServerAddressMatcher.isSonarus(" play.sonarus.win "));
        expect(true, ServerAddressMatcher.isSonarus("play.sonarus.win:25565"));
        expect(true, ServerAddressMatcher.isSonarus("play.sonarus.win:26565"));
        expect(true, ServerAddressMatcher.isSonarus("play.sonarus.win."));
        expect(true, ServerAddressMatcher.isSonarus("10.29.240.51:26565"));
        expect(true, ServerAddressMatcher.isSonarus("178.168.208.14:26565"));

        expect(false, ServerAddressMatcher.isSonarus(null));
        expect(false, ServerAddressMatcher.isSonarus(""));
        expect(false, ServerAddressMatcher.isSonarus("sonarus.win"));
        expect(false, ServerAddressMatcher.isSonarus("evilplay.sonarus.win"));
        expect(false, ServerAddressMatcher.isSonarus("play.sonarus.win.evil.example"));
        expect(false, ServerAddressMatcher.isSonarus("10.29.240.51"));
        expect(false, ServerAddressMatcher.isSonarus("10.29.240.51:25565"));
        expect(false, ServerAddressMatcher.isSonarus("178.168.208.14"));
        expect(false, ServerAddressMatcher.isSonarus("178.168.208.14:25565"));

        System.out.println("ServerAddressMatcherSelfTest: OK");
    }

    private static void expect(boolean expected, boolean actual) {
        if (expected != actual) {
            throw new AssertionError("Expected " + expected + ", got " + actual);
        }
    }
}
