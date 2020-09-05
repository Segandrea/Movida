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

import movida.dicarlosegantini.Entry;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A map implementation using hashing and linear probing.
 *
 * @param <K> Type of the keys.
 * @param <V> Type of the values.
 */
public final class HashIndirizzamentoAperto<K, V> implements IMap<K, V> {
    // Special marker to signal a deleted value.
    @SuppressWarnings("unchecked")
    private final K DELETED = (K) new Object();
    private V[] values;
    private K[] keys;
    private int size;

    @SuppressWarnings({"unchecked"})
    public HashIndirizzamentoAperto() {
        this.values = (V[]) new Object[0];
        this.keys = (K[]) new Object[0];
        this.size = 0;
    }

    /**
     * Makes an HashIndirizzamentoAperto from another map.
     * <p>
     * <pre>
     * Time complexity: O(n)
     * </pre>
     * <p>
     *
     * @param map  The instance of another map.
     * @param <K1> The type of the keys, must be comparable.
     * @param <V1> The type of the values.
     * @return An HashIndirizzamentoAperto made from the specified map.
     */
    public static <K1, V1> IMap<K1, V1> from(final IMap<K1, V1> map) {
        final var newInstance = new HashIndirizzamentoAperto<K1, V1>();
        newInstance.reserve(map.size());
        map.stream().forEach(e -> newInstance.add(e.key, e.value));
        return newInstance;
    }

    /**
     * Computes hashes using java's hashCode which has the problem of giving a signed hash.
     * This problem is solved making the abs of the hashCode and adding to it the biggest positive Integer
     * in case the hashCode was negative, returning the result as a long.
     * In this way we can avoid collisions while keeping the hash positive.
     *
     * @param key The item to hash.
     * @return The hash of the item.
     */
    private long computeHash(final K key) {
        final var hashCode = key.hashCode();
        return ((long) Math.abs(hashCode)) + ((0 > hashCode) ? ((long) (Integer.MAX_VALUE)) : 0L);
    }

    /**
     * Adds a value into the map, with the specified key.
     * <p>
     * <pre>
     *                   worst   best
     * Time complexity:  O(n)    O(1)
     *
     * Worst case: when the (key-value) is not in the map and an expansion + rehashing is needed.
     * Best case: when expansion + rehashing is not needed.
     * </pre>
     * <p>
     *
     * @param key   The key associated to the value to add.
     * @param value The value to add.
     * @return if there where already a value associated with the specified key, it returns its value, otherwise null.
     */
    @Override
    public V add(final K key, final V value) {
        assert null != key;
        assert null != value;
        this.reserve(1);
        return this.rawAdd(key, value);
    }

    /**
     * Gets the value with the specified key if present, otherwise it adds it.
     * <p>
     * <pre>
     *                   worst   best
     * Time complexity:  O(n)    O(1)
     *
     * Worst case: when the (key-value) is not in the map and an expansion + rehashing is needed.
     * Best case: when expansion + rehashing is not needed.
     * </pre>
     * <p>
     *
     * @param key      The key associated to the value to get.
     * @param supplier The function used to create the value if not present in the map.
     * @return The value associated with the specified key if present, the supplied value otherwise.
     */
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

    /**
     * Gets the value with the specified key, null otherwise.
     * <p>
     * <pre>
     *                  worst                              best
     * Time complexity: O(n) -> due to linear probing      O(1)
     * </pre>
     * <p>
     *
     * @param key The key associated to the value to get.
     * @return The value associated with the specified key if present, null otherwise.
     */
    @Override
    public V get(final K key) {
        assert null != key;
        final var index = this.indexOf(key);
        return (0 <= index) ? this.values[index] : null;
    }

    /**
     * Removes the value with the specified key.
     * <p>
     * <pre>
     *                  worst                              best
     * Time complexity: O(n) -> due to linear probing      O(1)
     * </pre>
     * <p>
     *
     * @param key The key associated to the value to remove.
     * @return The removed value if present, null otherwise.
     */
    @Override
    public V remove(final K key) {
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

    /**
     * Checks If the specified key is in the map.
     * <p>
     * <pre>
     *                  worst                              best
     * Time complexity: O(n) -> due to linear probing      O(1)
     * </pre>
     * <p>
     *
     * @param key The key to search in the map.
     * @return True if the key is found, false otherwise.
     */
    @Override
    public boolean has(final K key) {
        assert null != key;
        return 0 <= this.indexOf(key);
    }

    /**
     * Steams the keys of the map.
     * <p>
     * <pre>
     * Time complexity: O(n)
     * </pre>
     * <p>
     *
     * @return A stream of the keys in the map.
     */
    @Override
    public Stream<K> keys() {
        return Arrays.stream(this.keys)
                .filter(k -> null != k && this.DELETED != k)
                .limit(this.size);
    }

    /**
     * Steams the values of the map.
     * <p>
     * <pre>
     * Time complexity: O(n)
     * </pre>
     * <p>
     *
     * @return A stream of the values in the map.
     */
    @Override
    public Stream<V> values() {
        return Arrays.stream(this.values)
                .filter(Objects::nonNull)
                .limit(this.size);
    }

    /**
     * Streams the pairs key-value of the map.
     * <p>
     * <pre>
     * Time complexity: O(n)
     * </pre>
     * <p>
     *
     * @return A stream of entries made of the key-value pairs.
     */
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

    /**
     * If needed, expands the map to support at least additionalItems more.
     * <p>
     * <pre>
     *                   worst   best
     * Time complexity:  O(n)    O(1)
     *
     * Worst case: when expansion + rehashing is needed.
     * Best case: when expansion + rehashing is not needed.
     * </pre>
     * <p>
     *
     * @param additionalItems Minimum number of additional items that the map must be able to accommodate.
     */
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

    /**
     * Gets the capacity of the map.
     * <p>
     * <pre>
     * Time complexity: O(1)
     * </pre>
     * <p>
     *
     * @return The capacity of the map.
     */
    @Override
    public int capacity() {
        return this.keys.length;
    }

    /**
     * Gets the size of the map.
     * <p>
     * <pre>
     * Time complexity: O(1)
     * </pre>
     * <p>
     *
     * @return The size of the map.
     */
    @Override
    public int size() {
        return this.size;
    }

    /**
     * Clears the map making it empty.
     * <p>
     * <pre>
     * Time complexity: O(n)
     * </pre>
     * <p>
     */
    @Override
    public void clear() {
        Arrays.fill(this.values, null);
        Arrays.fill(this.keys, null);
        this.size = 0;
    }

    /*
     * Gets the index of a specified key if present, otherwise it indicates where to place it.
     * <pre>
     * present -> return the index of the key in the array (index is in range [0, length - 1]).
     * absent  -> return the index in which the key would be placed in the array (index is in range [-1, -length]).
     * </pre>
     * <p>
     * <pre>
     *                  worst                              best
     * Time complexity: O(n) -> due to linear probing      O(1)
     * </pre>
     * <p>
     *
     * @param key The key to locate.
     * @return A positive index if the key is in the map, a negative index if the key is not in the map.
     */
    private int indexOf(final K key) {
        assert null != key;
        final var capacity = this.capacity();

        if (0 == capacity) {
            return -1;
        }

        final var hash = this.computeHash(key);
        var emptyIndex = (int) (hash % capacity);

        if (!this.isEmpty()) {
            var deletedNotAlreadyEncountered = true;

            for (int i = 0, index = emptyIndex; i < capacity; ++i, index = (int) ((hash + i) % capacity)) {
                final var keyItem = this.keys[index];

                if (null == keyItem) {
                    if (deletedNotAlreadyEncountered) {
                        emptyIndex = index;
                    }
                    break;
                }
                if (this.DELETED == keyItem) {
                    if (deletedNotAlreadyEncountered) {
                        emptyIndex = index;
                    }
                    deletedNotAlreadyEncountered = false;
                    continue;
                }
                if (key.equals(keyItem)) {
                    return index;
                }
            }
        }

        return -(emptyIndex + 1);
    }

    /*
     * Adds a (key-value) into the map.
     *
     * Note.1: this add does not call reserve method.
     * Note.2:
     *                  worst                              best
     * Time complexity: O(n) -> due to linear probing      O(1)
     */
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
