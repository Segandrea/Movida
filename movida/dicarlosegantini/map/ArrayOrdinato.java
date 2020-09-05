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
import movida.dicarlosegantini.array.DynamicArray;

import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A map implementation using an ordered array.
 *
 * @param <K> Type of the keys, must be comparable.
 * @param <V> Type of the values.
 */
public final class ArrayOrdinato<K extends Comparable<K>, V> implements IMap<K, V> {
    private final DynamicArray<V> values;
    private final DynamicArray<K> keys;

    public ArrayOrdinato() {
        this.values = new DynamicArray<>();
        this.keys = new DynamicArray<>();
    }

    /**
     * Makes an ArrayOrdinato from another map.
     * <p>
     * <pre>
     * Time complexity: O(n)
     * </pre>
     * <p>
     *
     * @param map  The instance of another map.
     * @param <K1> The type of the keys, must be comparable.
     * @param <V1> The type of the values.
     * @return An ArrayOrdinato made from the specified map.
     */
    public static <K1 extends Comparable<K1>, V1> IMap<K1, V1> from(final IMap<K1, V1> map) {
        final var newInstance = new ArrayOrdinato<K1, V1>();
        newInstance.reserve(map.size());
        map.stream().forEach(e -> newInstance.add(e.key, e.value));
        return newInstance;
    }

    /**
     * Adds a value into the map, with the specified key.
     * <p>
     * <pre>
     * Time complexity: O(n)
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
        var index = this.keys.binarySearch(key, K::compareTo);

        if (0 <= index) {
            return this.values.replace(index, value);
        }

        index = -(index + 1);
        this.keys.add(index, key);
        this.values.add(index, value);

        return null;
    }

    /**
     * Gets the value with the specified key if present, otherwise it adds it.
     * <p>
     * <pre>
     *                  worst                  best
     * Time complexity: O(n) -> due to add     O(log(n))
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
        var index = this.keys.binarySearch(key, K::compareTo);

        if (0 <= index) {
            return this.values.get(index);
        }

        final var value = supplier.get();
        assert null != value;

        index = -(index + 1);
        this.keys.add(index, key);
        this.values.add(index, value);

        return value;
    }

    /**
     * Gets the value with the specified key, null otherwise.
     * <p>
     * <pre>
     * Time complexity: O(log(n))
     * </pre>
     * <p>
     *
     * @param key The key associated to the value to get.
     * @return The value associated with the specified key if present, null otherwise.
     */
    @Override
    public V get(final K key) {
        assert null != key;
        final var index = this.keys.binarySearch(key, K::compareTo);
        return (0 <= index) ? this.values.get(index) : null;
    }

    /**
     * Removes the value with the specified key.
     * <p>
     * <pre>
     * Time complexity: O(n)
     * </pre>
     * <p>
     *
     * @param key The key associated to the value to remove.
     * @return The removed value if present, null otherwise.
     */
    @Override
    public V remove(final K key) {
        assert null != key;
        final var index = this.keys.binarySearch(key, K::compareTo);

        if (0 > index) {
            return null;
        }

        this.keys.remove(index);
        return this.values.remove(index);
    }

    /**
     * Checks If the specified key is in the map.
     * <p>
     * <pre>
     * Time complexity: O(log(n))
     * </pre>
     * <p>
     *
     * @param key The key to search in the map.
     * @return True if the key is found, false otherwise.
     */
    @Override
    public boolean has(final K key) {
        assert null != key;
        return 0 <= this.keys.binarySearch(key, K::compareTo);
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
        return this.keys.stream();
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
        return this.values.stream();
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
        return IntStream.range(0, this.size()).mapToObj(i -> new Entry<>(this.keys.get(i), this.values.get(i)));
    }

    /**
     * If needed, expands the map to support at least additionalItems more.
     * <p>
     * <pre>
     *                      Best      Worst
     * Time complexity:     O(1)      O(n)
     * </pre>
     * <p>
     *
     * @param additionalItems Minimum number of additional items that the map must be able to accommodate.
     */
    public void reserve(final int additionalItems) {
        assert 0 <= additionalItems;
        this.values.reserve(additionalItems);
        this.keys.reserve(additionalItems);
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
        return this.keys.capacity();
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
        return this.keys.size();
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
        this.values.clear();
        this.keys.clear();
    }
}
