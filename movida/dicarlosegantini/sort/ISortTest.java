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

import static org.junit.jupiter.api.Assertions.assertEquals;

class ISortTest {
    @org.junit.jupiter.api.Test
    void testSelectionSort() {
        final var sut = SelectionSort.getInstance();

        this.testNoElements(sut);
        this.testOneElement(sut);
        this.testTwoElements(sut);
        this.testManyElements(sut);
    }

    @org.junit.jupiter.api.Test
    void testQuickSort() {
        final var sut = QuickSort.getInstance();

        this.testNoElements(sut);
        this.testOneElement(sut);
        this.testTwoElements(sut);
        this.testManyElements(sut);
    }

    void testNoElements(final ISort sut) {
        Integer[] arr = {};

        sut.sort(arr);
        assertEquals(0, arr.length);
    }

    void testOneElement(final ISort sut) {
        Integer[] arr = {0};

        sut.sort(arr);
        assertEquals(1, arr.length);
        assertEquals(0, arr[0]);
    }

    void testTwoElements(final ISort sut) {
        Integer[] arr = {1, 0};

        sut.sort(arr);
        assertEquals(2, arr.length);
        assertEquals(0, arr[0]);
        assertEquals(1, arr[1]);

        // check that sorting an array already sorted should be fine
        sut.sort(arr);
        assertEquals(2, arr.length);
        assertEquals(0, arr[0]);
        assertEquals(1, arr[1]);
    }

    void testManyElements(final ISort sut) {
        Integer[] arr = {4, 7, 2, 5, 1, 0, 6, 3};

        sut.sort(arr);
        assertEquals(8, arr.length);
        for (int i = 0; i < arr.length; ++i) {
            assertEquals(i, arr[i]);
        }

        // check that sorting an array already sorted should be fine
        sut.sort(arr);
        assertEquals(8, arr.length);
        for (int i = 0; i < arr.length; ++i) {
            assertEquals(i, arr[i]);
        }
    }
}