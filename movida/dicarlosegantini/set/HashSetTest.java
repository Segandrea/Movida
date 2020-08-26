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

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class HashSetTest {
    HashSet<Integer> sut;

    @BeforeEach
    void setUp() {
        this.sut = new HashSet<>();
        assertTrue(this.sut.empty());
        assertEquals(0, this.sut.size());
        assertEquals(0, this.sut.capacity());
        assertEquals(0, this.sut.stream().count());
    }

    @Test
    void add() {
        for (int i = 0; 16 > i; ++i) {
            assertTrue(this.sut.add(i));
            assertFalse(this.sut.empty());

            final var capacity = this.sut.capacity();
            assertEquals(i + 1, this.sut.size());
            assertTrue(this.sut.size() <= capacity);
            assertTrue(this.sut.has(i));
            assertEquals(i * (i + 1) / 2, this.sut.stream().reduce(0, Integer::sum));

            assertFalse(this.sut.add(i));
            assertEquals(i + 1, this.sut.size());
            assertTrue(capacity <= this.sut.capacity());
            assertTrue(this.sut.has(i));
            assertEquals(i * (i + 1) / 2, this.sut.stream().reduce(0, Integer::sum));
        }
    }

    @Test
    void del() {
        final var keys = new ArrayList<>();
        final int SIZE = 16;

        for (int i = 0; SIZE > i; ++i) {
            keys.add(i);
            assertTrue(this.sut.add(i));
            assertEquals(i + 1, this.sut.size());
            assertTrue(this.sut.size() <= this.sut.capacity());
            assertTrue(this.sut.has(i));
            assertEquals(i * (i + 1) / 2, this.sut.stream().reduce(0, Integer::sum));
        }

        for (int i = 0; SIZE > i; ++i) {
            Integer k = -1;

            switch (i % 3) {
                case 0:
                    k = (Integer) keys.remove(0);
                    break;
                case 1:
                    k = (Integer) keys.remove(keys.size() / 2);
                    break;
                case 2:
                    k = (Integer) keys.remove(keys.size() - 1);
                    break;
                default:
                    fail();
            }

            assertFalse(this.sut.empty());
            assertTrue(this.sut.del(k));
            assertEquals(keys.size(), this.sut.size());
            assertFalse(this.sut.has(k));
            assertFalse(this.sut.del(k));
            assertEquals(keys.size(), this.sut.size());
            assertFalse(this.sut.has(k));
        }

        assertTrue(this.sut.empty());
    }

    @Test
    void stream() {
        final var SIZE = 32;

        for (int i = 0; SIZE > i; ++i) {
            assertTrue(this.sut.add(i));
        }

        assertTrue(this.sut.del(SIZE / 2));
        assertTrue(this.sut.del(SIZE / 4));
        assertTrue(this.sut.del(SIZE / 8));

        final var EXPECTED = ((SIZE - 1) * (SIZE) / 2) - (SIZE / 2 + SIZE / 4 + SIZE / 8);
        assertEquals(EXPECTED, this.sut.stream().reduce(0, Integer::sum));
    }

    @Test
    void clear() {
        final int SIZE = 16;

        for (int i = 0; SIZE > i; ++i) {
            assertTrue(this.sut.add(i));
        }

        this.sut.clear();
        assertTrue(this.sut.empty());
        assertEquals(0, this.sut.size());
        assertEquals(0, this.sut.stream().count());
    }
}
