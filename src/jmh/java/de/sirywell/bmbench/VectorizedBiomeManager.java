package de.sirywell.bmbench;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.LongVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

public class VectorizedBiomeManager implements BiomeManager {
  private static final VectorSpecies<Long> LS = LongVector.SPECIES_PREFERRED;
  private static final VectorSpecies<Double> DS = DoubleVector.SPECIES_PREFERRED;
  private static final int LANES = LS.length();
  private final long biomeZoomSeed;

  public VectorizedBiomeManager(long biomeZoomSeed) {
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

    final boolean[] m4 = {false, false, false, false, true, true, true, true};
    final boolean[] m2 = {false, false, true, true, false, false, true, true};
    final boolean[] m1 = {false, true, false, true, false, true, false, true};

    // assuming 8 % length == 0
    for (int p = 0; p < (8 / LANES); p++) {
      int offset = p * LANES;
      LongVector q = LongVector.broadcast(LS, l).sub(VectorMask.fromArray(LS, m4, offset).toVector());
      LongVector r = LongVector.broadcast(LS, m).sub(VectorMask.fromArray(LS, m2, offset).toVector());
      LongVector s = LongVector.broadcast(LS, n).sub(VectorMask.fromArray(LS, m1, offset).toVector());
      DoubleVector h = DoubleVector.broadcast(DS, d).add(VectorMask.fromArray(DS, m4, offset).toVector());
      DoubleVector t = DoubleVector.broadcast(DS, e).add(VectorMask.fromArray(DS, m2, offset).toVector());
      DoubleVector u = DoubleVector.broadcast(DS, f).add(VectorMask.fromArray(DS, m1, offset).toVector());
      DoubleVector v = getFiddledDistance(this.biomeZoomSeed, q, r, s, h, t, u);
      double vMin = v.reduceLanes(VectorOperators.MIN);
      if (g > vMin) {
        g = vMin;
        o = v.eq(g).firstTrue() + offset;
      }
    }

    int w = (o & 4) == 0 ? l : l + 1;
    int x = (o & 2) == 0 ? m : m + 1;
    int y = (o & 1) == 0 ? n : n + 1;
    return new Position(w, x, y);
  }

  public static DoubleVector getFiddledDistance(long l,
                                                 LongVector i, LongVector j, LongVector k,
                                                 DoubleVector d, DoubleVector e, DoubleVector f
  ) {
    LongVector lv = LongVector.broadcast(LS, l);
    LongVector m = next(lv, i);
    m = next(m, j);
    m = next(m, k);
    m = next(m, i);
    m = next(m, j);
    m = next(m, k);
    DoubleVector g = getFiddle(m);
    m = next(m, lv);
    DoubleVector h = getFiddle(m);
    m = next(m, lv);
    DoubleVector n = getFiddle(m);
    return square(f.add(n)).add(square(e.add(h))).add(square(d.add(g)));
  }

  public static DoubleVector getFiddle(LongVector l) {
    LongVector ashr = l.lanewise(VectorOperators.ASHR, 24L);
    LongVector floorMod = ashr.and(1023L);
    DoubleVector cast = floorMod.convert(VectorOperators.L2D, 0).reinterpretAsDoubles();
    DoubleVector div = cast.div(1024d);
    return div.sub(0.5d).mul(0.9d);
  }

  public static LongVector next(LongVector seed, LongVector salt) {
    seed = seed.mul(seed.mul(6364136223846793005L).add(1442695040888963407L));
    return seed.add(salt);
  }

  private static DoubleVector square(DoubleVector v) {
    return v.mul(v);
  }
}
