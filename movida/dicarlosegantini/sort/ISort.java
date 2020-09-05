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

package movida.dicarlosegantini.sort;

import java.util.Comparator;

/**
 * Interface made to abstract sorting algorithms.
 */
public interface ISort {
    default <T extends Comparable<T>> void sort(T[] array) {
        this.sort(array, 0, array.length, T::compareTo);
    }

    default <T extends Comparable<T>> void sort(T[] array, final int length) {
        this.sort(array, 0, length, T::compareTo);
    }

    /**
     * @param array array to be sorted
     * @param from  start index (inclusive)
     * @param to    end index (exclusive)
     */
    default <T extends Comparable<T>> void sort(T[] array, final int from, final int to) {
        this.sort(array, from, to, T::compareTo);
    }

    default <T> void sort(T[] array, final Comparator<T> comparator) {
        this.sort(array, 0, array.length, comparator);
    }

    default <T> void sort(T[] array, final int length, final Comparator<T> comparator) {
        this.sort(array, 0, length, comparator);
    }

    /**
     * @param array      array to be sorted
     * @param from       start index (inclusive)
     * @param to         end index (exclusive)
     * @param comparator function used to compare array items
     */
    <T> void sort(T[] array, final int from, final int to, final Comparator<T> comparator);
}
