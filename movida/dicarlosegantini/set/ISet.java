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

package movida.dicarlosegantini.set;

import java.util.stream.Stream;

/**
 * Interface for a set of items.
 *
 * @param <K> Type of the items in the set.
 */
public interface ISet<K> {
    /**
     * Adds an item into the set.
     *
     * @param key The item to add.
     * @return True if the item is added, false if the item was already in the set.
     */
    boolean add(final K key);

    /**
     * Removes an item from the set.
     *
     * @param key The item to remove.
     * @return True if the item is removed, false if the item is not in the set.
     */
    boolean remove(final K key);

    /**
     * Checks if the item is already in the set.
     *
     * @param key The item to check for.
     * @return True if the item is in the set, false if the item is not in the set.
     */
    boolean has(final K key);

    /**
     * Streams the items in the set.
     *
     * @return A stream of the items in the set.
     */
    Stream<K> stream();

    /**
     * If needed, expands the set to support at least additionalItems more.
     *
     * @param additionalItems Minimum number of additional items that the set must be able to accommodate.
     */
    void reserve(final int additionalItems);

    /**
     * Gets the capacity of the set.
     *
     * @return The capacity of the set.
     */
    int capacity();

    /**
     * Gets the size of the set.
     *
     * @return The size of the set.
     */
    int size();

    /**
     * Clears the set making it empty.
     */
    void clear();

    /**
     * Check if the set is empty.
     *
     * @return true if the set is empty, false otherwise.
     */
    default boolean isEmpty() {
        return 0 == this.size();
    }
}
