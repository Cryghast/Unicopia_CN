package com.minelittlepony.unicopia.util.serialization;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.Registries;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public interface CodecUtils {
    Codec<ItemConvertible> ITEM = Registries.ITEM.getCodec().xmap(i -> () -> i, ItemConvertible::asItem);
    Codec<Optional<BlockPos>> OPTIONAL_POS = Codecs.optional(BlockPos.CODEC);
    Codec<Vec3d> VECTOR = Codec.withAlternative(RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("x").forGetter(Vec3d::getX),
            Codec.DOUBLE.fieldOf("y").forGetter(Vec3d::getY),
            Codec.DOUBLE.fieldOf("z").forGetter(Vec3d::getZ)
    ).apply(instance, Vec3d::new)), Codec.DOUBLE.listOf(3, 3), list -> new Vec3d(list.get(0), list.get(1), list.get(2)));
    /**
     * Combines the result of two unrelated codecs into a single object.
     * <p>
     * The first codec serves as the "base" whilst the second codec serves as an additional field to merge into
     * that object when serializing. Deserializing produces a pair with the parent value and the extra value.
     * <p>
     * Recommended usage:
     * <code>
     * Codec<MyObject> CODEC = CodecUtils.extend(SOME_CODEC, MY_FIELD_CODEC.fieldOf("my_extra_field")).xmap(
     *      pair -> new MyObject(pair.getLeft(), pair.getRight()),
     *      myObject -> Pair.of(myObject.parent(), myObject.myExtraField());
     * </code>
     * <p>
     * Json:
     * <code>
     * {
     *   "something": "something",
     *   "something_else": 1,
     *
     *   "my_extra_field": "HAH EAT THAT CODECS"
     * }
     * </code>
     * @param <A> The base type
     * @param <B> Type of the field to append
     * @param baseCodec   Codec for the base type
     * @param fieldCodec  Codec for the appended field
     * @return A codec for serializing objects of the base with the extra field inserted
     */
    static <A, B> Codec<Pair<Optional<A>, Optional<B>>> extend(Codec<A> baseCodec, MapCodec<B> fieldCodec) {
        return Codec.of(new Encoder<Pair<Optional<A>, Optional<B>>>() {
            @Override
            public <T> DataResult<T> encode(Pair<Optional<A>, Optional<B>> input, DynamicOps<T> ops, T prefix) {
                return baseCodec.encode(input.getFirst().get(), ops, prefix)
                        .flatMap(l -> input.getSecond()
                            .map(r -> fieldCodec.encode(r, ops, ops.mapBuilder()).build(prefix).flatMap(rr -> ops.getMap(rr).flatMap(rrr -> ops.mergeToMap(l, rrr))))
                            .orElse(DataResult.success(l)));
            }
        }, new Decoder<Pair<Optional<A>, Optional<B>>>() {
            @Override
            public <T> DataResult<Pair<Pair<Optional<A>, Optional<B>>, T>> decode(DynamicOps<T> ops, T input) {
                return DataResult.success(new Pair<>(new Pair<>(
                        baseCodec.decode(ops, input).map(Pair::getFirst).result(),
                        fieldCodec.decode(ops, ops.getMap(input).result().get()).result()
                ), input));
            }
        });
    }

    static <K> Codec<K> apply(Codec<K> codec, UnaryOperator<Codec<K>> func) {
        return func.apply(codec);
    }

    static <K> Codec<Set<K>> setOf(Codec<K> codec) {
        return codec.listOf().xmap(
                l -> l.stream().distinct().collect(Collectors.toUnmodifiableSet()),
                s -> new ArrayList<>(s)
        );
    }

    static <K> Codec<Supplier<K>> supplierOf(Codec<K> codec) {
        return codec.xmap(k -> () -> k, Supplier::get);
    }

    static MapCodec<TriState> tristateOf(String fieldName) {
        return Codec.BOOL.optionalFieldOf(fieldName).xmap(
                b -> b.map(TriState::of).orElse(TriState.DEFAULT),
                t -> Optional.ofNullable(t.get())
        );
    }

    static <T> Codec<DefaultedList<T>> defaultedList(Codec<T> elementCodec, T empty) {
        return elementCodec.listOf().xmap(
                elements -> new DefaultedList<>(elements, empty) {},
                Function.identity()
        );
    }
}