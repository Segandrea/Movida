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

import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Interface for a map.
 *
 * @param <K> Type of the keys.
 * @param <V> Type of the values.
 */
public interface IMap<K, V> {
    /**
     * Adds a value into the map, with the specified key.
     *
     * @param key   The key associated to the value to add.
     * @param value The value to add.
     * @return if there where already a value associated with the specified key, it returns its value, otherwise null.
     */
    V add(final K key, final V value);

    /**
     * Gets the value with the specified key if present, otherwise it adds it.
     *
     * @param key      The key associated to the value to get.
     * @param supplier The function used to create the value if not present in the map.
     * @return The value associated with the specified key if present, the supplied value otherwise.
     */
    V getOrAdd(final K key, final Supplier<V> supplier);

    /**
     * Gets the value with the specified key if present, otherwise it returns a supplied value.
     *
     * @param key      The key associated to the value to get.
     * @param supplier The function used to create the value if not present in the map.
     * @return The value associated with the specified key if present, the supplied value otherwise.
     */
    default V getOrDefault(final K key, final Supplier<V> supplier) {
        final var value = this.get(key);
        return (null != value) ? value : supplier.get();
    }

    /**
     * Gets the value with the specified key, null otherwise.
     *
     * @param key The key associated to the value to get.
     * @return The value associated with the specified key if present, null otherwise.
     */
    V get(final K key);

    /**
     * Removes the value with the specified key.
     *
     * @param key The key associated to the value to remove.
     * @return The removed value if present, null otherwise.
     */
    V remove(final K key);

    /**
     * Checks If the specified key is in the map.
     *
     * @param key The key to search in the map.
     * @return True if the key is found, false otherwise.
     */
    boolean has(final K key);

    /**
     * Steams the keys of the map.
     *
     * @return A stream of the keys in the map.
     */
    Stream<K> keys();

    /**
     * Streams the values of the map.
     *
     * @return A stream of the keys in the map.
     */
    Stream<V> values();

    /**
     * Streams the pairs key-value of the map.
     *
     * @return A stream of entries made of the key-value pairs.
     */
    Stream<Entry<K, V>> stream();

    /**
     * If needed, expands the map to support at least additionalItems more.
     *
     * @param additionalItems Minimum number of additional items that the map must be able to accommodate.
     */
    void reserve(final int additionalItems);

    /**
     * Gets the capacity of the map.
     *
     * @return The capacity of the map.
     */
    int capacity();

    /**
     * Gets the size of the map.
     *
     * @return The size of the map.
     */
    int size();

    /**
     * Clears the map making it empty.
     */
    void clear();

    /**
     * Check if the map is empty.
     *
     * @return true if the map is empty, false otherwise.
     */
    default boolean isEmpty() {
        return 0 == this.size();
    }
}
