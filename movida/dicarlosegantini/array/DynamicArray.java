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

package movida.dicarlosegantini.array;

import movida.dicarlosegantini.sort.ISort;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.IntFunction;
import java.util.stream.Stream;

public final class DynamicArray<T> {
    private T[] array;
    private int size;

    @SuppressWarnings("unchecked")
    public DynamicArray() {
        this.array = (T[]) new Object[0];
        this.size = 0;
    }

    @SuppressWarnings({"unchecked"})
    public void reserve(final int additionalItems) {
        assert 0 <= additionalItems;

        if ((this.size + additionalItems) <= this.capacity()) {
            return;
        }

        final var newCapacity = (int) Math.ceil((this.size + additionalItems) / 0.6);
        final var tmpArray = (T[]) new Object[newCapacity];

        if (0 < this.size) {
            System.arraycopy(this.array, 0, tmpArray, 0, this.size);
        }

        this.array = tmpArray;
    }

    public void add(final T item, final int index) {
        assert index <= this.size;

        this.reserve(1);
        System.arraycopy(this.array, index, this.array, index + 1, this.size - index);
        this.array[index] = item;
        this.size += 1;
    }

    public T del(final int index) {
        assert index < this.size;

        final var item = this.array[index];
        System.arraycopy(this.array, index + 1, this.array, index, this.size - index - 1);
        this.size -= 1;

        return item;
    }

    public T get(final int index) {
        return this.array[index];
    }

    public void clear() {
        this.size = 0;
    }

    public Stream<T> stream() {
        return Arrays.stream(this.array).limit(this.size);
    }

    public void sort(final ISort sortAlgorithm, final Comparator<T> comparator) {
        sortAlgorithm.sort(this.array, this.size, comparator);
    }

    // TODO: docs
    public int binarySearch(final T item, final Comparator<T> comparator) {
        return BinarySearch.search(this.array, this.size, item, comparator);
    }

    // TODO: docs
    @SuppressWarnings("UnusedReturnValue")
    public boolean binaryInsert(final T item, final Comparator<T> comparator) {
        final var index = this.binarySearch(item, comparator);

        if (0 <= index) {
            return false;
        }

        this.add(item, -(index + 1));
        return true;
    }

    // TODO: docs
    @SuppressWarnings("UnusedReturnValue")
    public boolean binaryRemove(final T item, final Comparator<T> comparator) {
        final var index = this.binarySearch(item, comparator);

        if (0 > index) {
            return false;
        }

        this.del(index);
        return true;
    }

    public T[] slice(final IntFunction<T[]> sliceBuilder, final int startIndex, final int endIndex) {
        assert 0 <= endIndex;
        assert 0 <= startIndex;
        assert endIndex < this.size;
        assert startIndex <= endIndex;

        final var SIZE = endIndex - startIndex;
        final var sliceArray = sliceBuilder.apply(SIZE);
        System.arraycopy(this.array, startIndex, sliceArray, 0, SIZE);

        return sliceArray;
    }

    public int capacity() {
        return this.array.length;
    }

    public int size() {
        return this.size;
    }

    public boolean empty() {
        return 0 == this.size;
    }
}
