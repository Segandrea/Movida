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

public interface IMap<K, V> {
    V add(final K key, final V value);

    V getOrAdd(final K key, final Supplier<V> supplier);

    default V getOrDefault(final K key, final Supplier<V> supplier) {
        final var value = this.get(key);
        return (null != value) ? value : supplier.get();
    }

    V get(final K key);

    V remove(final K key);

    boolean has(final K key);

    Stream<K> keys();

    Stream<V> values();

    Stream<Entry<K, V>> stream();

    void reserve(final int additionalItems);

    int capacity();

    int size();

    void clear();

    default boolean isEmpty() {
        return 0 == this.size();
    }
}
