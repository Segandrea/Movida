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

import movida.dicarlosegantini.array.DynamicArray;

import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class ArrayOrdinato<K extends Comparable<K>, V> implements IMap<K, V> {
    private final DynamicArray<V> values;
    private final DynamicArray<K> keys;

    public ArrayOrdinato() {
        this.values = new DynamicArray<>();
        this.keys = new DynamicArray<>();
    }

    public static <K1 extends Comparable<K1>, V1> IMap<K1, V1> from(final IMap<K1, V1> map) {
        final var newInstance = new ArrayOrdinato<K1, V1>();
        newInstance.reserve(map.size());
        map.stream().forEach(e -> newInstance.add(e.key, e.value));
        return newInstance;
    }

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

    @Override
    public V get(final K key) {
        assert null != key;
        final var index = this.keys.binarySearch(key, K::compareTo);
        return (0 <= index) ? this.values.get(index) : null;
    }

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

    @Override
    public boolean has(final K key) {
        assert null != key;
        return 0 <= this.keys.binarySearch(key, K::compareTo);
    }

    @Override
    public Stream<K> keys() {
        return this.keys.stream();
    }

    @Override
    public Stream<V> values() {
        return this.values.stream();
    }

    @Override
    public Stream<Entry<K, V>> stream() {
        return IntStream.range(0, this.size()).mapToObj(i -> new Entry<>(this.keys.get(i), this.values.get(i)));
    }

    public void reserve(final int additionalItems) {
        assert 0 <= additionalItems;
        this.values.reserve(additionalItems);
        this.keys.reserve(additionalItems);
    }

    @Override
    public int capacity() {
        return this.keys.capacity();
    }

    @Override
    public int size() {
        return this.keys.size();
    }

    @Override
    public void clear() {
        this.values.clear();
        this.keys.clear();
    }
}
