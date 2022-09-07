package de.sirywell.bmbench;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.IntSupplier;
import java.util.stream.Stream;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 3)
@State(Scope.Benchmark)
@Fork(1)
@Measurement(iterations = 3)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class BiomeManagerBenchmark {

  @State(Scope.Benchmark)
  public static class BiomeManagerBenchmarkState {
    public BiomeManager biomeManager;
    public Position[] positions;
    @Param({"original", "vectorized"})
    public String manager;

    @Setup
    public void setup() {
      Random random = new Random(321);
      this.biomeManager = switch (manager) {
        case "original" -> new OriginalBiomeManager(random.nextLong());
        case "vectorized" -> new VectorizedBiomeManager(random.nextLong());
        default -> throw new IllegalArgumentException("Unknown manager: " + manager);
      };
      IntSupplier next = () -> random.nextInt(-1000000, 1000000);
      this.positions = Stream.generate(() -> new Position(next.getAsInt(), next.getAsInt(), next.getAsInt()))
          .limit(1000)
          .toArray(Position[]::new);
    }
  }

  @Benchmark
  public Object getBiome(BiomeManagerBenchmarkState state) {
    int length = state.positions.length;
    Object[] results = new Object[length];
    for (int i = 0; i < length; i++) {
      results[i] = state.biomeManager.getBiome(state.positions[i]);
    }
    return results;
  }
}
