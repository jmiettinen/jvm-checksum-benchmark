package net.jpountz.jvm;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.Adler32;
import java.util.zip.CRC32;

import net.jpountz.xxhash.XXHash32;
import net.jpountz.xxhash.XXHashFactory;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class ChecksumBenchmark {

    private static final CRC32 crc32 = new CRC32();
    private static final Adler32 adler32 = new Adler32();
    private static final HashFunction murmur3 = Hashing.murmur3_32();
    private static final HashFunction sha1 = Hashing.sha1();
    private static final HashFunction sha256 = Hashing.sha256();
    private static final HashFunction sha512 = Hashing.sha512();
    private static final HashFunction md5 = Hashing.md5();
    private static final HashFunction goodFastHash32 = Hashing.goodFastHash(32);
    private static final HashFunction goodFastHash64 = Hashing.goodFastHash(64);
    private static final XXHash32 xxhash32JNI = XXHashFactory.nativeInstance().hash32();
    private static final XXHash32 xxhash32Unsafe = XXHashFactory.unsafeInstance().hash32();
    private static final XXHash32 xxhash32Safe = XXHashFactory.safeInstance().hash32();
    private static final XXHash32 xxhash64JNI = XXHashFactory.nativeInstance().hash32();
    private static final XXHash32 xxhash64Unsafe = XXHashFactory.unsafeInstance().hash32();
    private static final XXHash32 xxhash64Safe = XXHashFactory.safeInstance().hash32();

    @org.openjdk.jmh.annotations.State(Scope.Thread)
    public static class State {

        private final Random rng = new Random(0xCAFEBABE);
        @Param({"16", "128", "1024", "8196", "65536", "524288"})
        public int size;
        public byte[] bytes;

        @Setup(Level.Iteration)
        public void prepare() {
            bytes = new byte[size];
            rng.nextBytes(bytes);
        }

    }

    @Benchmark
    public long javaUtilArraysHashCode(State state) {
        return Arrays.hashCode(state.bytes);
    }

    @Benchmark
    public long adler32(State state) {
        adler32.reset();
        adler32.update(state.bytes, 0, state.size);
        return adler32.getValue();
    }

    @Benchmark
    public long crc32(State state) {
        crc32.reset();
        crc32.update(state.bytes, 0, state.size);
        return crc32.getValue();
    }

    @Benchmark
    public long murmur2(State state) {
        return MurmurHash2.hash32(state.bytes, 0, state.size);
    }

    @Benchmark
    public long murmur3(State state) {
        return murmur3.hashBytes(state.bytes, 0, state.size).asInt();
    }

    @Benchmark
    public long sha1(State state) {
        return sha1.hashBytes(state.bytes, 0, state.size).asLong();
    }

    @Benchmark
    public long sha256(State state) {
        return sha256.hashBytes(state.bytes, 0, state.size).asLong();
    }

    @Benchmark
    public long sha512(State state) {
        return sha512.hashBytes(state.bytes, 0, state.size).asLong();
    }

    @Benchmark
    public long md5(State state) {
        return md5.hashBytes(state.bytes, 0, state.size).asLong();
    }


    @Benchmark
    public long goodFastHash32(State state) {
        return goodFastHash32.hashBytes(state.bytes, 0, state.size).asInt();
    }

    @Benchmark
    public long goodFastHash64(State state) {
        return goodFastHash64.hashBytes(state.bytes, 0, state.size).asLong();
    }

    public long xxHash32JNI(State state) {
        return xxhash32JNI.hash(state.bytes, 0, state.size, 0x9747b28c);
    }

    public long xxHash32Unsafe(State state) {
        return xxhash32Unsafe.hash(state.bytes, 0, state.size, 0x9747b28c);
    }

    public long xxHash32(State state) {
        return xxhash32Safe.hash(state.bytes, 0, state.size, 0x9747b28c);
    }

    public long xxHash64JNI(State state) {
        return xxhash64JNI.hash(state.bytes, 0, state.size, 0x9747b28c);
    }

    public long xxHash64Unsafe(State state) {
        return xxhash64Unsafe.hash(state.bytes, 0, state.size, 0x9747b28c);
    }

    public long xxHash64(State state) {
        return xxhash64Safe.hash(state.bytes, 0, state.size, 0x9747b28c);
    }

    public static void main(String[] args) throws IOException, RunnerException {
        Options options = new OptionsBuilder()
                .include(ChecksumBenchmark.class.getSimpleName())
                .forks(1)
                .warmupIterations(5)
                .measurementIterations(5)
                .build();
        new Runner(options).run();
    }

}
