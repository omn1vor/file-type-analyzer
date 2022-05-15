package analyzer;

public class Hashing {
    private static final int KEY = 3;
    private static final int MOD = 1009;

    public static int hash(byte[] data) {
        int value = 0;
        int keyPowered;
        for (int i = 0; i < data.length; i++) {
            keyPowered = (int) (Math.pow(KEY, data.length - 1 - i) % MOD);
            value += (data[i] * keyPowered % MOD);
        }
        return Math.floorMod(value, MOD);
    }

    public static int rollingHash(int hash, byte minus, byte plus, int len) {
        int minusHash = hash - Math.floorMod(minus * (long) Math.pow(KEY, len - 1), MOD);
        return Math.floorMod(Math.floorMod(minusHash * KEY, MOD) + plus, MOD);
    }
}
