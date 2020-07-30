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

import java.util.Random;

public class QuickSort implements ISort {
    private static final Random rand = new Random();

    private static <T> void swap(T[] arr, final int firstIndex, final int lastIndex) {
        final var tmp = arr[firstIndex];
        arr[firstIndex] = arr[lastIndex];
        arr[lastIndex] = tmp;
    }

    private static <T extends Comparable<T>> int partition(T[] arr, final int firstIndex, final int lastIndex) {
        //swap firstIndex with a random pivot to optimize the quicksort
        swap(arr, firstIndex, rand.nextInt(lastIndex - firstIndex) + firstIndex);

        final var pivotValue = arr[firstIndex];
        var sup = lastIndex;
        var inf = firstIndex;

        while (true) {
            while (arr[inf].compareTo(pivotValue) < 0) {
                inf += 1;
            }

            while (arr[sup].compareTo(pivotValue) > 0) {
                sup -= 1;
            }

            if (inf >= sup) {
                return sup;
            }

            swap(arr, inf, sup);
        }
    }

    private static <T extends Comparable<T>> void recurse(T[] arr, final int firstIndex, final int lastIndex) {
        if (firstIndex < lastIndex) {
            final var pivotIndex = partition(arr, firstIndex, lastIndex);
            recurse(arr, firstIndex, pivotIndex - 1);
            recurse(arr, pivotIndex + 1, lastIndex);
        }
    }

    @Override
    public <T extends Comparable<T>> void sort(T[] arr) {
        recurse(arr, 0, arr.length - 1);
    }
}
