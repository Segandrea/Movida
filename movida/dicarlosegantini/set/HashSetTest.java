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

package movida.dicarlosegantini.set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HashSetTest {
    HashSet<Integer> sut = null;

    @BeforeEach
    void setUp() {
        this.sut = new HashSet<>();
        assertTrue(this.sut.empty());
        assertEquals(0, this.sut.size());
        assertEquals(0, this.sut.capacity());
    }

    @Test
    void add() {
        for (int i = 0; 10 > i; ++i) {
            assertTrue(this.sut.add(i));
            assertFalse(this.sut.empty());
            assertEquals(i + 1, this.sut.size());
            assertTrue(this.sut.has(i));

            assertFalse(this.sut.add(i));
            assertFalse(this.sut.empty());
            assertEquals(i + 1, this.sut.size());
            assertTrue(this.sut.has(i));
        }
    }

    @Test
    void del() {
        for (int i = 0; 10 >= i; ++i) {
            assertTrue(this.sut.add(i));
            assertFalse(this.sut.empty());
            assertEquals(i + 1, this.sut.size());
            assertTrue(this.sut.has(i));
        }

        for (int i = 10; 0 < i; --i) {
            assertTrue(this.sut.del(i));
            assertFalse(this.sut.empty());
            assertEquals(i, this.sut.size());
            assertFalse(this.sut.has(i));

            assertFalse(this.sut.del(i));
            assertFalse(this.sut.empty());
            assertEquals(i, this.sut.size());
            assertFalse(this.sut.has(i));
        }

        assertTrue(this.sut.del(0));
        assertTrue(this.sut.empty());
        assertEquals(0, this.sut.size());
        assertFalse(this.sut.has(0));

        assertFalse(this.sut.del(0));
        assertTrue(this.sut.empty());
        assertEquals(0, this.sut.size());
        assertFalse(this.sut.has(0));
    }

    @Test
    void stream() {
        for (int i = 0; 10 > i; ++i) {
            assertTrue(this.sut.add(i));
            assertFalse(this.sut.empty());
            assertEquals(i + 1, this.sut.size());
            assertTrue(this.sut.has(i));
        }

        assertArrayEquals(
                new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
                this.sut.stream().sorted().toArray(Integer[]::new)
        );
    }

    @Test
    void clear() {
        for (int i = 0; 10 > i; ++i) {
            assertTrue(this.sut.add(i));
            assertFalse(this.sut.empty());
            assertEquals(i + 1, this.sut.size());
            assertTrue(this.sut.has(i));
        }

        var capacity = this.sut.capacity();

        this.sut.clear();
        assertTrue(this.sut.empty());
        assertEquals(0, this.sut.size());
        assertEquals(capacity, this.sut.capacity());
        assertEquals(0, this.sut.stream().count());
    }
}