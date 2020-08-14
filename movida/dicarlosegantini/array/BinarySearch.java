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

import java.util.Comparator;

/**
 * Binary search the given item in the array.
 * <p>
 * present  -> return the index of the item in the array (index is in range [0, length - 1]).
 * absent   -> return the index in which the item would be placed in the array (index is in range [-1, -length]).
 */
public final class BinarySearch {
    public static final BinarySearch instance = new BinarySearch();

    private BinarySearch() {}

    public <T> int binarySearch(final T[] array, final int length, final T item, final Comparator<T> comparator) {
        return this.binarySearchRecursive(array, item, comparator, 0, length - 1);
    }

    public <T extends Comparable<T>> int binarySearch(final T[] array, final int length, final T item) {
        return this.binarySearch(array, length, item, T::compareTo);
    }

    private <T> int binarySearchRecursive(final T[] array, final T item, final Comparator<T> comparator,
                                          final int start, final int end) {
        if (start > end) {
            return -(start + 1);
        }

        final var middle = (start + end) / 2;
        switch (comparator.compare(array[middle], item)) {
            case 1:
                return this.binarySearchRecursive(array, item, comparator, start, middle - 1);
            case -1:
                return this.binarySearchRecursive(array, item, comparator, middle + 1, end);
            default:
                return middle;
        }
    }
}
