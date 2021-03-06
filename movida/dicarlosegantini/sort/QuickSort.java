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
import java.util.Random;

/**
 * Sorting algorithm: Quicksort.
 * Implemented with randomized pivot.
 * <p>
 * <pre>
 * Time complexity: O(n*log(n)) due to randomization of the pivot.
 * Otherwise, without randomization: O(n^2) in the worst case.
 * </pre>
 * <p>
 */
public final class QuickSort implements ISort {
    private static final Random rand = new Random();
    private static QuickSort instance = null;

    private QuickSort() {}

    public static QuickSort getInstance() {
        if (null == instance) {
            instance = new QuickSort();
        }

        return instance;
    }

    private static <T> void swap(T[] array, final int firstIndex, final int lastIndex) {
        final var tmp = array[firstIndex];
        array[firstIndex] = array[lastIndex];
        array[lastIndex] = tmp;
    }

    private static <T> int partition(T[] array, final int firstIndex, final int lastIndex,
                                     final Comparator<T> comparator) {
        // Pivot will always be in the first position.

        // swap firstIndex with a random pivot to optimize the quicksort
        swap(array, firstIndex, rand.nextInt(lastIndex - firstIndex) + firstIndex);

        final var pivotValue = array[firstIndex];
        // because of decrement in the while
        var sup = lastIndex + 1;
        // because of increment in the while
        var inf = firstIndex - 1;

        // When inf match or goes over sup, the while ends.
        while (true) {
            do {
                inf += 1;
            } while (0 > comparator.compare(array[inf], pivotValue));

            do {
                sup -= 1;
            } while (0 < comparator.compare(array[sup], pivotValue));

            if (inf >= sup) {
                return sup;
            }

            swap(array, inf, sup);
        }
    }

    private static <T> void recurse(T[] array, final int firstIndex, final int lastIndex,
                                    final Comparator<T> comparator) {
        if (firstIndex < lastIndex) {
            final var pivotIndex = partition(array, firstIndex, lastIndex, comparator);
            recurse(array, firstIndex, pivotIndex, comparator);
            recurse(array, pivotIndex + 1, lastIndex, comparator);
        }
    }

    @Override
    public <T> void sort(T[] array, final int from, final int to, final Comparator<T> comparator) {
        assert 0 <= from;
        assert from <= to;
        assert to <= array.length;
        recurse(array, from, to - 1, comparator);
    }
}
