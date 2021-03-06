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
 * Sorting algorithm: Selection sort.
 * <p>
 * <pre>
 * Time complexity: O(n^2)
 * </pre>
 * <p>
 */
public final class SelectionSort implements ISort {
    private static SelectionSort instance = null;

    private SelectionSort() {}

    public static SelectionSort getInstance() {
        if (null == instance) {
            instance = new SelectionSort();
        }

        return instance;
    }

    @Override
    public <T> void sort(T[] array, final int from, final int to, final Comparator<T> comparator) {
        assert 0 <= from;
        assert from <= to;
        assert to <= array.length;

        for (int lastIndex = from; lastIndex < to; ++lastIndex) {
            var minIndex = lastIndex;

            for (int i = lastIndex + 1; i < to; ++i) {
                if (0 > comparator.compare(array[i], array[minIndex])) {
                    minIndex = i;
                }
            }

            final var tmp = array[lastIndex];
            array[lastIndex] = array[minIndex];
            array[minIndex] = tmp;
        }
    }
}
