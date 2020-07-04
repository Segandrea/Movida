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

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class IMapTest {
    @org.junit.jupiter.api.Test
    void testHashIndirizzamentoAperto() {
        this.testAdd(new HashIndirizzamentoAperto<>());
        this.testDel(new HashIndirizzamentoAperto<>());
        this.test(new HashIndirizzamentoAperto<>());
    }

    @org.junit.jupiter.api.Test
    void testArrayOrdinato() {
        this.testAdd(new ArrayOrdinato<>());
        this.testDel(new ArrayOrdinato<>());
        this.test(new ArrayOrdinato<>());
    }

    void testDel(IMap<Integer, Integer> sut) {
        final int SIZE = 16;
        for (int i = 0; i < SIZE; ++i) {
            assertNull(sut.add(i, i));
            assertEquals(i + 1, sut.size());
            assertTrue(sut.has(i));
            assertEquals(i, sut.get(i));
        }

        var keys = new ArrayList<>();
        for (int i = 0; i < SIZE; ++i) {
            keys.add(i);
        }

        for (int i = 0; i < SIZE; ++i) {
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

            assertEquals(k, sut.del(k));
            assertEquals(keys.size(), sut.size());
            assertFalse(sut.has(k));
            assertNull(sut.get(k));
            assertNull(sut.del(k));
            assertEquals(keys.size(), sut.size());
            assertFalse(sut.has(k));
        }
    }

    void testAdd(IMap<Integer, Integer> sut) {
        for (int i = 0; i < 16; ++i) {
            assertNull(sut.add(i, i));
            assertEquals(i + 1, sut.size());
            assertTrue(sut.has(i));
            assertEquals(i, sut.get(i));
        }

        assertEquals(0, sut.add(0, -1));
        assertEquals(16, sut.size());
        assertTrue(sut.has(0));
        assertEquals(-1, sut.get(0));

        assertEquals(7, sut.add(7, -1));
        assertEquals(16, sut.size());
        assertTrue(sut.has(7));
        assertEquals(-1, sut.get(7));

        assertEquals(15, sut.add(15, -1));
        assertEquals(16, sut.size());
        assertTrue(sut.has(15));
        assertEquals(-1, sut.get(15));
    }

    void test(IMap<String, Integer> sut) {
        assertTrue(sut.empty());
        assertEquals(0, sut.size());
        assertEquals(0, sut.capacity());

        assertNull(sut.add("k1", 15));
        assertFalse(sut.empty());
        assertEquals(1, sut.size());
        assertTrue(sut.capacity() >= sut.size());
        assertTrue(sut.has("k1"));
        assertEquals(15, sut.get("k1"));

        assertNull(sut.add("k2", 26));
        assertFalse(sut.empty());
        assertEquals(2, sut.size());
        assertTrue(sut.capacity() >= sut.size());
        assertTrue(sut.has("k2"));
        assertEquals(26, sut.get("k2"));

        assertEquals(15, sut.del("k1"));
        assertFalse(sut.empty());
        assertEquals(1, sut.size());
        assertTrue(sut.capacity() >= sut.size());
        assertFalse(sut.has("k1"));
        assertNull(sut.get("k1"));

        assertNull(sut.add("k1", 15));
        assertFalse(sut.empty());
        assertEquals(2, sut.size());
        assertTrue(sut.capacity() >= sut.size());
        assertTrue(sut.has("k1"));
        assertEquals(15, sut.get("k1"));

        assertEquals(15, sut.add("k1", 99));
        assertFalse(sut.empty());
        assertEquals(2, sut.size());
        assertTrue(sut.capacity() >= sut.size());
        assertTrue(sut.has("k1"));
        assertEquals(99, sut.get("k1"));
    }
}