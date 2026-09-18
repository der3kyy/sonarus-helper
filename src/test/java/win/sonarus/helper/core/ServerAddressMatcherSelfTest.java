package win.sonarus.helper.core;

public final class ServerAddressMatcherSelfTest {
    public static void main(String[] args) {
        expect(true, ServerAddressMatcher.isSonarus("play.sonarus.win"));
        expect(true, ServerAddressMatcher.isSonarus("PLAY.SONARUS.WIN"));
        expect(true, ServerAddressMatcher.isSonarus(" play.sonarus.win "));
        expect(true, ServerAddressMatcher.isSonarus("play.sonarus.win:25565"));
        expect(true, ServerAddressMatcher.isSonarus("play.sonarus.win."));

        expect(false, ServerAddressMatcher.isSonarus(null));
        expect(false, ServerAddressMatcher.isSonarus(""));
        expect(false, ServerAddressMatcher.isSonarus("sonarus.win"));
        expect(false, ServerAddressMatcher.isSonarus("evilplay.sonarus.win"));
        expect(false, ServerAddressMatcher.isSonarus("play.sonarus.win.evil.example"));

        System.out.println("ServerAddressMatcherSelfTest: OK");
    }

    private static void expect(boolean expected, boolean actual) {
        if (expected != actual) {
            throw new AssertionError("Expected " + expected + ", got " + actual);
        }
    }
}
