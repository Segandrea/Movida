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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BinarySearchTest {
    @Test
    void searchStringArray() {
        // String::compareTo may return values outside -1, 0, 1.
        final var array = new String[]{"nick nolte", "robert de niro"};
        assertEquals(0, BinarySearch.search(array, "nick nolte", String::compareTo));
        assertEquals(1, BinarySearch.search(array, "robert de niro", String::compareTo));
    }

    @Test
    void searchEmptyArray() {
        final var array = new Integer[0];
        for (int i = -1; 2 > i; ++i) {
            assertEquals(-1, BinarySearch.search(array, 0, array.length, i, Integer::compareTo));
        }
    }

    @Test
    void searchOneElement() {
        final var array = new Integer[]{0};
        assertEquals(0, BinarySearch.search(array, 0, array.length, 0, Integer::compareTo));
        assertEquals(-1, BinarySearch.search(array, 0, array.length, -1, Integer::compareTo));
        assertEquals(-2, BinarySearch.search(array, 0, array.length, 1, Integer::compareTo));
    }

    @Test
    void searchManyElements() {
        final var array = new Integer[]{0, 1, 2, 3, 4, 5};
        for (int i = 0; array.length > i; ++i) {
            assertEquals(i, BinarySearch.search(array, 0, array.length, i, Integer::compareTo));
        }

        assertEquals(-1, BinarySearch.search(array, 0, array.length, -1, Integer::compareTo));
        assertEquals(-7, BinarySearch.search(array, 0, array.length, 6, Integer::compareTo));
    }
}