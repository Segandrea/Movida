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

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Hashset implementation using linear probing.
 *
 * @param <K> Type of the items in the hashset
 */
public final class HashSet<K> implements ISet<K> {
    // Special marker to signal a deleted value.
    @SuppressWarnings("unchecked")
    private final K DELETED = (K) new Object();
    private K[] keys;
    private int size;

    @SuppressWarnings({"unchecked"})
    public HashSet() {
        this.keys = (K[]) new Object[0];
        this.size = 0;
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
     * Gets the specified item if it is in the set, otherwise adds the item into the set and returns it.
     * <p>
     * <pre>
     *                   worst   best
     * Time complexity:  O(n)    O(1)
     *
     * Worst case: when the item is not in the set and an expansion + rehashing is needed.
     * Best case: when expansion + rehashing is not needed.
     * </pre>
     * <p>
     *
     * @param key The item to get/add.
     * @return The item in the set.
     * @implNote This function calls a reserve even if there's no need to add.
     */
    public K getOrAdd(final K key) {
        assert null != key;
        this.reserve(1);
        final var index = this.rawAdd(key);
        return (0 > index) ? this.keys[-(index + 1)] : this.keys[index];
    }

    /**
     * Gets the specified item if present.
     * <p>
     * <pre>
     *                  worst                              best
     * Time complexity: O(n) -> due to linear probing      O(1)
     * </pre>
     * <p>
     *
     * @param key The item to get.
     * @return The specified item if found, null otherwise.
     */
    public K get(final K key) {
        assert null != key;
        final var index = this.indexOf(key);

        if (0 > index) {
            return null;
        }

        return this.keys[index];
    }

    /**
     * Adds an item into the set.
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
     * @param key The item to add.
     * @return True if the item is added, false if the item was already in the set.
     */
    @Override
    public boolean add(final K key) {
        assert null != key;
        this.reserve(1);
        return 0 > this.rawAdd(key);
    }

    /**
     * Removes an item from the set.
     * <p>
     * <pre>
     *                  worst                              best
     * Time complexity: O(n) -> due to linear probing      O(1)
     * </pre>
     * <p>
     *
     * @param key The item to remove.
     * @return True if the item is removed, false if the item is not in the set.
     */
    @Override
    public boolean remove(final K key) {
        assert null != key;
        final var index = this.indexOf(key);

        if (0 <= index) {
            this.keys[index] = this.DELETED;
            this.size -= 1;
            return true;
        }

        return false;
    }

    /**
     * Checks if the item is already in the set.
     * <p>
     * <pre>
     *                  worst                              best
     * Time complexity: O(n) -> due to linear probing      O(1)
     * </pre>
     * <p>
     *
     * @param key The item to check for.
     * @return True if the item is in the set, false if the item is not in the set.
     */
    @Override
    public boolean has(final K key) {
        assert null != key;
        return 0 <= this.indexOf(key);
    }

    /**
     * Streams the items in the set.
     * <p>
     * <pre>
     * Time complexity: O(n)
     * </pre>
     * <p>
     *
     * @return A stream of the items in the set.
     */
    @Override
    public Stream<K> stream() {
        return Arrays.stream(this.keys)
                .filter(k -> null != k && this.DELETED != k)
                .limit(this.size);
    }

    /**
     * If needed, expands the set to support at least additionalItems more.
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
     * @param additionalItems Minimum number of additional items that the set must be able to accommodate.
     * @implNote To prevent worst case we ensure that the load factor of the set is below 0.7.
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
        final var tmpKeys = this.keys;

        this.keys = (K[]) new Object[newCapacity];
        this.size = 0;

        for (final K k : tmpKeys) {
            if (null != k && this.DELETED != k) {
                this.rawAdd(k);
            }
        }
    }

    /**
     * Gets the capacity of the set.
     * <p>
     * <pre>
     * Time complexity: O(1)
     * </pre>
     * <p>
     *
     * @return The capacity of the set.
     */
    @Override
    public int capacity() {
        return this.keys.length;
    }

    /**
     * Gets the size of the set.
     * <p>
     * <pre>
     * Time complexity: O(1)
     * </pre>
     * <p>
     *
     * @return The size of the set.
     */
    @Override
    public int size() {
        return this.size;
    }

    /**
     * Clears the set making it empty.
     * <p>
     * <pre>
     * Time complexity: O(n)
     * </pre>
     * <p>
     */
    @Override
    public void clear() {
        Arrays.fill(this.keys, null);
        this.size = 0;
    }

    /*
     * Gets the index of a specified item if present, otherwise it indicates where to place it.
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
     * @param key The item to locate.
     * @return A positive index if the item is in the set, a negative index if the item is not in the set.
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
     * Adds an item into the set.
     *
     * Note.1: this add does not call reserve method.
     * Note.2:
     *                  worst                              best
     * Time complexity: O(n) -> due to linear probing      O(1)
     */
    private int rawAdd(final K key) {
        assert null != key;
        final var index = this.indexOf(key);

        if (0 <= index) {
            return index;
        }

        final var keyIndex = -(index + 1);
        assert keyIndex < this.capacity();

        this.keys[keyIndex] = key;
        this.size += 1;

        return index;
    }
}
