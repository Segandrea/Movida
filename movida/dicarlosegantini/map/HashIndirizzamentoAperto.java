/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 - Davide Di Carlo, Andrea Segantini
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package movida.dicarlosegantini.map;

import movida.dicarlosegantini.Eq;
import movida.dicarlosegantini.Hasher;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class HashIndirizzamentoAperto<K, V> implements IMap<K, V> {
    @SuppressWarnings("unchecked")
    private final K DELETED = (K) new Object();
    private final Hasher<K> hasher;
    private final Eq<K> eq;
    private V[] values;
    private K[] keys;
    private int size;

    public HashIndirizzamentoAperto() {
        this(K::hashCode, K::equals);
    }

    @SuppressWarnings({"unchecked"})
    public HashIndirizzamentoAperto(final Hasher<K> hasher, final Eq<K> eq) {
        this.hasher = hasher;
        this.eq = eq;
        this.values = (V[]) new Object[0];
        this.keys = (K[]) new Object[0];
        this.size = 0;
    }

    public static <K1, V1> IMap<K1, V1> from(final IMap<K1, V1> map) {
        final var newInstance = new HashIndirizzamentoAperto<K1, V1>();
        newInstance.reserve(map.size());
        map.stream().forEach(e -> newInstance.add(e.key, e.value));
        return newInstance;
    }

    private long computeHash(final K key) {
        final var hashCode = this.hasher.hash(key);
        return ((long) Math.abs(hashCode)) + ((0 > hashCode) ? ((long) (Integer.MAX_VALUE)) : 0L);
    }

    @Override
    public V add(final K key, final V value) {
        assert null != key;
        assert null != value;
        this.reserve(1);
        return this.rawAdd(key, value);
    }

    @Override
    public V getOrAdd(final K key, final Supplier<V> supplier) {
        assert null != key;
        this.reserve(1);
        var index = this.indexOf(key);

        if (0 <= index) {
            return this.values[index];
        }

        index = -(index + 1);
        assert index < this.capacity();

        final var value = supplier.get();
        assert null != value;

        this.values[index] = value;
        this.keys[index] = key;
        this.size += 1;

        return value;
    }

    @Override
    public V get(final K key) {
        assert null != key;
        final var index = this.indexOf(key);
        return (0 <= index) ? this.values[index] : null;
    }

    @Override
    public V del(final K key) {
        assert null != key;
        final var index = this.indexOf(key);

        if (0 <= index) {
            final var value = this.values[index];
            this.values[index] = null;
            this.keys[index] = this.DELETED;
            this.size -= 1;
            return value;
        }

        return null;
    }

    @Override
    public boolean has(final K key) {
        assert null != key;
        return 0 <= this.indexOf(key);
    }

    @Override
    public Stream<K> keys() {
        return Arrays.stream(this.keys)
                .filter(k -> null != k && this.DELETED != k)
                .limit(this.size);
    }

    @Override
    public Stream<V> values() {
        return Arrays.stream(this.values)
                .filter(Objects::nonNull)
                .limit(this.size);
    }

    @Override
    public Stream<Entry<K, V>> stream() {
        return IntStream
                .range(0, this.capacity())
                .filter(i -> {
                    final var k = this.keys[i];
                    return null != k && this.DELETED != k;
                })
                .limit(this.size)
                .mapToObj(i -> new Entry<>(this.keys[i], this.values[i]));
    }

    @SuppressWarnings({"unchecked"})
    public void reserve(final int additionalItems) {
        assert 0 <= additionalItems;

        final float capacity = Math.max(this.capacity(), 1);
        final var loadFactor = (this.size + additionalItems) / capacity;

        if (0.7 > loadFactor) {
            return;
        }

        final var newCapacity = (int) Math.ceil((this.size + additionalItems) / 0.6);
        final var tmpValues = this.values;
        final var tmpKeys = this.keys;

        this.values = (V[]) new Object[newCapacity];
        this.keys = (K[]) new Object[newCapacity];
        this.size = 0;

        for (int i = 0; i < tmpKeys.length; ++i) {
            final var k = tmpKeys[i];

            if (null != k && this.DELETED != k) {
                this.rawAdd(k, tmpValues[i]);
            }
        }
    }

    @Override
    public int capacity() {
        return this.keys.length;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void clear() {
        Arrays.fill(this.values, null);
        Arrays.fill(this.keys, null);
        this.size = 0;
    }

    /*
     * present -> return the index of the key in the array (index is in range [0, length - 1]).
     * absent  -> return the index in which the key would be placed in the array (index is in range [-1, -length]).
     */
    private int indexOf(final K key) {
        assert null != key;
        final var hash = this.computeHash(key);
        final var capacity = this.capacity();
        var emptyIndex = (int) (hash % capacity);

        if (!this.empty()) {
            var iter = 0;
            var index = emptyIndex;
            var deletedNotAlreadyEncountered = true;

            do {
                final var keyItem = this.keys[index];

                if (null == keyItem) {
                    if (deletedNotAlreadyEncountered) {
                        emptyIndex = index;
                    }
                    break;
                }
                if (this.eq.test(key, keyItem)) {
                    return index;
                }
                if (this.DELETED == keyItem && deletedNotAlreadyEncountered) {
                    emptyIndex = index;
                    deletedNotAlreadyEncountered = false;
                }

                iter += 1;
                index = (int) ((hash + iter) % capacity);
            } while (iter < capacity);
        }

        return -(emptyIndex + 1);
    }

    private V rawAdd(final K key, final V value) {
        assert null != key;
        assert null != value;
        var index = this.indexOf(key);

        if (0 <= index) {
            final var oldValue = this.values[index];
            this.values[index] = value;
            return oldValue;
        }

        index = -(index + 1);
        assert index < this.capacity();

        this.keys[index] = key;
        this.values[index] = value;
        this.size += 1;

        return null;
    }
}
