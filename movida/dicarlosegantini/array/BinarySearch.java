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
 * present -> return the index of the item in the array (index is in range [0, length - 1]).
 * absent  -> return the index in which the item would be placed in the array (index is in range [-1, -length]).
 */
public final class BinarySearch {
    private BinarySearch() {}

    public static <T> int search(final T[] array, final int length, final T item, final Comparator<T> comparator) {
        return search(array, 0, length - 1, item, comparator);
    }

    public static <T extends Comparable<T>> int search(final T[] array, final int length, final T item) {
        return search(array, length, item, T::compareTo);
    }

    public static <T> int search(final T[] array, final int startIndex, final int endIndex,
                                 final T item, final Comparator<T> comparator) {
        assert 0 <= startIndex;
        assert -1 <= endIndex;
        assert endIndex < array.length;

        if (startIndex > endIndex) {
            return -(startIndex + 1);
        }

        final var middle = (startIndex + endIndex) / 2;
        switch (comparator.compare(array[middle], item)) {
            case 1:
                return search(array, startIndex, middle - 1, item, comparator);
            case -1:
                return search(array, middle + 1, endIndex, item, comparator);
            default:
                return middle;
        }
    }

    public static <T extends Comparable<T>> int search(final T[] array, final int startIndex, final int endIndex,
                                                       final T item) {
        return search(array, startIndex, endIndex, item, T::compareTo);
    }
}
