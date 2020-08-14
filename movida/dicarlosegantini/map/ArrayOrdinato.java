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

import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ArrayOrdinato<K extends Comparable<K>, V> implements IMap<K, V> {
    private V[] values;
    private K[] keys;
    private int size;

    @SuppressWarnings("unchecked")
    public ArrayOrdinato() {
        this.values = (V[]) new Object[0];
        this.keys = (K[]) new Comparable[0];
        this.size = 0;
    }

    public static <K1 extends Comparable<K1>, V1> IMap<K1, V1> from(final IMap<K1, V1> map) {
        var newInstance = new ArrayOrdinato<K1, V1>();
        newInstance.reserve(map.size());
        map.stream().forEach(e -> newInstance.add(e.key, e.value));
        return newInstance;
    }

    @Override
    public V add(final K key, final V value) {
        assert null != key;
        assert null != value;
        var index = this.binarySearch(key);

        if (index >= 0) {
            final var tmp = this.values[index];
            this.values[index] = value;
            return tmp;
        }

        index = -(index + 1);
        assert index <= this.size;

        this.reserve(1);
        System.arraycopy(this.values, index, this.values, index + 1, this.size - index);
        System.arraycopy(this.keys, index, this.keys, index + 1, this.size - index);

        this.values[index] = value;
        this.keys[index] = key;
        this.size += 1;

        return null;
    }

    @Override
    public V getOrAdd(final K key, final Supplier<V> supplier) {
        assert null != key;
        var index = this.binarySearch(key);

        if (index >= 0) {
            return this.values[index];
        }

        index = -(index + 1);
        assert index <= this.size;

        final var value = supplier.get();
        assert null != value;

        this.reserve(1);
        System.arraycopy(this.values, index, this.values, index + 1, this.size - index);
        System.arraycopy(this.keys, index, this.keys, index + 1, this.size - index);

        this.values[index] = value;
        this.keys[index] = key;
        this.size += 1;

        return value;
    }

    @Override
    public V get(final K key) {
        assert null != key;
        final var index = this.binarySearch(key);
        return (index >= 0) ? this.values[index] : null;
    }

    @Override
    public V del(final K key) {
        assert null != key;
        final var index = this.binarySearch(key);

        if (index < 0) {
            return null;
        }

        final var outValue = this.values[index];

        System.arraycopy(this.values, index + 1, this.values, index, this.size - index - 1);
        System.arraycopy(this.keys, index + 1, this.keys, index, this.size - index - 1);
        this.size -= 1;

        return outValue;
    }

    @Override
    public boolean has(final K key) {
        assert null != key;
        return this.binarySearch(key) >= 0;
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
        this.size = 0;
    }

    @Override
    public Stream<Entry<K, V>> stream() {
        return IntStream.range(0, this.size).mapToObj(i -> new Entry<>(this.keys[i], this.values[i]));
    }

    @SuppressWarnings({"unchecked"})
    public void reserve(final int additionalItems) {
        if ((this.size + additionalItems) <= this.keys.length) {
            return;
        }

        final var newCapacity = (int) Math.ceil((this.size + additionalItems) / 0.6);
        final var tmpValues = (V[]) new Object[newCapacity];
        final var tmpKeys = (K[]) new Comparable[newCapacity];

        if (this.size > 0) {
            System.arraycopy(this.values, 0, tmpValues, 0, this.size);
            System.arraycopy(this.keys, 0, tmpKeys, 0, this.size);
        }

        this.values = tmpValues;
        this.keys = tmpKeys;
    }

    /*
     * Binary search of the given key in the array.
     *
     * found     -> return the index of the key in the keys array (index in range [0, size - 1]).
     * not found -> return a negative index that indicates the slot in which the key should be placed in the keys array
     *              (index in range [-1, -size]).
     */
    private int binarySearch(final K key) {
        return this.binarySearch(key, 0, this.size - 1);
    }

    private int binarySearch(final K key, final int start, final int end) {
        if (start > end) {
            return -(start + 1);
        }

        final var middle = (start + end) / 2;
        switch (this.keys[middle].compareTo(key)) {
            case 1:
                return this.binarySearch(key, start, middle - 1);
            case -1:
                return this.binarySearch(key, middle + 1, end);
            default:
                return middle;
        }
    }
}
