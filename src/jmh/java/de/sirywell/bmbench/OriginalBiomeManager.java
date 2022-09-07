package de.sirywell.bmbench;

public class OriginalBiomeManager implements BiomeManager {

  private final long biomeZoomSeed;

  public OriginalBiomeManager(long biomeZoomSeed) {
    this.biomeZoomSeed = biomeZoomSeed;
  }

  @Override
  public Object getBiome(Position position) {
    int i = position.x() - 2;
    int j = position.y() - 2;
    int k = position.z() - 2;
    int l = i >> 2;
    int m = j >> 2;
    int n = k >> 2;
    double d = (double) (i & 3) / 4.0D;
    double e = (double) (j & 3) / 4.0D;
    double f = (double) (k & 3) / 4.0D;
    int o = 0;
    double g = Double.POSITIVE_INFINITY;

    for (int p = 0; p < 8; ++p) {
      boolean bl = (p & 4) == 0;
      boolean bl2 = (p & 2) == 0;
      boolean bl3 = (p & 1) == 0;
      int q = bl ? l : l + 1;
      int r = bl2 ? m : m + 1;
      int s = bl3 ? n : n + 1;
      double h = bl ? d : d - 1.0D;
      double t = bl2 ? e : e - 1.0D;
      double u = bl3 ? f : f - 1.0D;
      double v = getFiddledDistance(this.biomeZoomSeed, q, r, s, h, t, u);
      if (g > v) {
        o = p;
        g = v;
      }
    }
    int w = (o & 4) == 0 ? l : l + 1;
    int x = (o & 2) == 0 ? m : m + 1;
    int y = (o & 1) == 0 ? n : n + 1;
    return new Position(w, x, y);
  }

  public static double getFiddledDistance(long l, int i, int j, int k, double d, double e, double f) {
    long m = next(l, i);
    m = next(m, j);
    m = next(m, k);
    m = next(m, i);
    m = next(m, j);
    m = next(m, k);
    double g = getFiddle(m);
    m = next(m, l);
    double h = getFiddle(m);
    m = next(m, l);
    double n = getFiddle(m);
    return square(f + n) + square(e + h) + square(d + g);
  }

  public static double getFiddle(long l) {
    double d = (double)Math.floorMod(l >> 24, 1024) / 1024.0D;
    return (d - 0.5D) * 0.9D;
  }

  public static long next(long seed, long salt) {
    seed *= seed * 6364136223846793005L + 1442695040888963407L;
    return seed + salt;
  }

  private static double square(double d) {
    return d * d;
  }
}
