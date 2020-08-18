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

public final class HashSet<K> {
    @SuppressWarnings("unchecked")
    private final K DELETED = (K) new Object();
    private K[] keys;
    private int size;

    @SuppressWarnings({"unchecked"})
    public HashSet() {
        this.keys = (K[]) new Object[0];
        this.size = 0;
    }

    private static <K1> long computeHash(final K1 key) {
        final var hashCode = key.hashCode();
        return ((long) Math.abs(hashCode)) + ((0 > hashCode) ? ((long) (Integer.MAX_VALUE)) : 0L);
    }

    public boolean add(final K key) {
        assert null != key;
        this.reserve(1);
        return this.rawAdd(key);
    }

    private boolean rawAdd(final K key) {
        assert null != key;
        var index = this.indexOf(key);
        assert index < this.capacity();

        if (0 > index) {
            index = -(index + 1);
            this.keys[index] = key;
            this.size += 1;
            return true;
        }
        return false;
    }

    /*
     * present -> return the index of the key in the array (index is in range [0, length - 1]).
     * absent  -> return the index in which the key would be placed in the array (index is in range [-1, -length]).
     */
    private int indexOf(final K key) {
        assert null != key;
        final var hash = computeHash(key);
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
                if (key.equals(keyItem)) {
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

    public boolean del(final K key) {
        assert null != key;
        final var index = this.indexOf(key);

        if (0 <= index) {
            this.keys[index] = this.DELETED;
            this.size -= 1;
            return true;
        }

        return false;
    }

    public boolean has(final K key) {
        assert null != key;
        return 0 <= this.indexOf(key);
    }

    public Stream<K> stream() {
        return Arrays.stream(this.keys)
                .filter(k -> null != k && this.DELETED != k)
                .limit(this.size);
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
        final var tmpKeys = this.keys;

        this.keys = (K[]) new Object[newCapacity];
        this.size = 0;

        for (final var k : tmpKeys) {
            if (null != k && this.DELETED != k) {
                this.rawAdd(k);
            }
        }
    }

    public int capacity() {
        return this.keys.length;
    }

    public int size() {
        return this.size;
    }

    public void clear() {
        Arrays.fill(this.keys, null);
        this.size = 0;
    }

    public boolean empty() {
        return 0 == this.size();
    }
}
