package ffprobe;

/** Bootstrap-visible test counter shared by the isolated core and test plugin loaders. */
public final class SnapshotCounter {
    private static final ThreadLocal<long[]> SCOPE = new ThreadLocal<>();
    public static void begin() { SCOPE.set(new long[1]); }
    public static long end() { long count = SCOPE.get()[0]; SCOPE.remove(); return count; }
    public static void hit() { long[] count = SCOPE.get(); if (count != null) count[0]++; }
}
