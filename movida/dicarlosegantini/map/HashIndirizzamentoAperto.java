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

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class HashIndirizzamentoAperto<K, V> implements IMap<K, V> {
    @SuppressWarnings("unchecked")
    private final K DELETED = (K) new Object();
    private V[] values;
    private K[] keys;
    private int size;

    @SuppressWarnings({"unchecked"})
    public HashIndirizzamentoAperto() {
        this.size = 0;
        this.keys = (K[]) new Object[0];
        this.values = (V[]) new Object[0];
    }

    public static <K1, V1> IMap<K1, V1> from(final IMap<K1, V1> map) {
        var newInstance = new HashIndirizzamentoAperto<K1, V1>();
        newInstance.reserve(map.size());
        map.stream().forEach(e -> newInstance.add(e.key, e.value));
        return newInstance;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void clear() {
        Arrays.fill(this.keys, null);
        this.size = 0;
    }

    @Override
    public int capacity() {
        return this.keys.length;
    }

    @Override
    public boolean has(final K key) {
        assert null != key;

        return (this.indexOf(key) >= 0);
    }

    @Override
    public V add(final K key, final V value) {
        assert null != key;
        assert null != value;

        this.reserve(1);
        return this.rawAdd(key, value);
    }

    @Override
    public V del(final K key) {
        assert null != key;

        final var index = this.indexOf(key);
        final var value = (index >= 0) ? this.values[index] : null;

        if (null != value) {
            this.size -= 1;
            this.keys[index] = this.DELETED;
        }

        return value;
    }

    @Override
    public V get(final K key) {
        assert null != key;

        final var index = this.indexOf(key);
        return (index >= 0) ? this.values[index] : null;
    }

    @Override
    public Stream<Entry<K, V>> stream() {
        return IntStream
                .range(0, this.capacity())
                .filter(i -> this.keys[i] != null)
                .limit(this.size)
                .mapToObj(i -> new Entry<>(this.keys[i], this.values[i]));
    }

    @SuppressWarnings({"unchecked"})
    public void reserve(final int numOfItems) {
        final float capacity = Math.max(this.capacity(), 1);
        final var loadFactor = (this.size + numOfItems) / capacity;

        if (0.7 > loadFactor) {
            return;
        }

        final var tmpKeys = this.keys;
        final var tmpValues = this.values;
        final var newCapacity = (int) Math.ceil((this.size + numOfItems) / 0.6);

        this.size = 0;
        this.keys = (K[]) new Object[newCapacity];
        this.values = (V[]) new Object[newCapacity];

        for (int i = 0; i < tmpKeys.length; ++i) {
            final var k = tmpKeys[i];
            if (null != k && this.DELETED != k) {
                assert tmpValues[i] != null;
                this.rawAdd(k, tmpValues[i]);
            }
        }
    }

    private int indexOf(final K key) {
        assert null != key;

        if (this.empty()) {
            return -1;
        }

        final var hash = key.hashCode();
        for (int i = 0; i < this.capacity(); ++i) {
            final var index = (hash + i) % this.capacity();
            final var entry = this.keys[index];

            if (null == entry) {
                break;
            }
            if (this.DELETED == entry) {
                continue;
            }
            if (key.equals(entry)) {
                return index;
            }
        }

        return -1;
    }

    private V rawAdd(final K key, final V value) {
        assert null != key;
        assert null != value;

        final var hash = ((long) Math.abs(key.hashCode())) + ((key.hashCode() < 0) ? (long) (Integer.MAX_VALUE) : 0L);
        for (int i = 0; i < this.capacity(); ++i) {
            final var index = (int) ((hash + i) % this.capacity());
            final var entry = this.keys[index];

            if (null == entry || this.DELETED == entry) {
                this.keys[index] = key;
                this.values[index] = value;
                this.size += 1;
                break;
            } else if (key.equals(entry)) {
                final var oldValue = this.values[index];
                this.values[index] = value;
                return oldValue;
            }
        }

        return null;
    }
}
