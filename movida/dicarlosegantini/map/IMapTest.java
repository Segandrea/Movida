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

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class IMapTest {
    @Test
    void testHashIndirizzamentoAperto() {
        this.testAdd(new HashIndirizzamentoAperto<>());
        this.testDel(new HashIndirizzamentoAperto<>());
        this.testBasicOp(new HashIndirizzamentoAperto<>());
        this.testStream(new HashIndirizzamentoAperto<>());
        this.testClear(new HashIndirizzamentoAperto<>());
        this.testGetOrAdd(new HashIndirizzamentoAperto<>());
        this.testToHashIndirizzamentoAperto(new HashIndirizzamentoAperto<>());
        this.testToHashIndirizzamentoAperto(new ArrayOrdinato<>());
    }

    @Test
    void testArrayOrdinato() {
        this.testAdd(new ArrayOrdinato<>());
        this.testDel(new ArrayOrdinato<>());
        this.testBasicOp(new ArrayOrdinato<>());
        this.testStream(new ArrayOrdinato<>());
        this.testClear(new ArrayOrdinato<>());
        this.testGetOrAdd(new ArrayOrdinato<>());
        this.testToArrayOrdinato(new ArrayOrdinato<>());
        this.testToArrayOrdinato(new HashIndirizzamentoAperto<>());
    }

    void testDel(IMap<Integer, Integer> sut) {
        final int SIZE = 16;
        for (int i = 0; SIZE > i; ++i) {
            assertNull(sut.add(i, i));
            assertEquals(i + 1, sut.size());
            assertTrue(sut.has(i));
            assertEquals(i, sut.get(i));
        }

        var keys = new ArrayList<>();
        for (int i = 0; SIZE > i; ++i) {
            keys.add(i);
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
        for (int i = 0; 16 > i; ++i) {
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

    void testBasicOp(IMap<String, Integer> sut) {
        assertTrue(sut.empty());
        assertEquals(0, sut.size());
        assertEquals(0, sut.capacity());

        assertNull(sut.add("k1", 15));
        assertFalse(sut.empty());
        assertEquals(1, sut.size());
        assertTrue(sut.capacity() >= sut.size());
        assertTrue(sut.has("k1"));
        assertEquals(15, sut.get("k1"));

        assertEquals(15, sut.getOrAdd("k1", () -> 7));
        assertFalse(sut.empty());
        assertEquals(1, sut.size());
        assertTrue(sut.capacity() >= sut.size());
        assertTrue(sut.has("k1"));
        assertEquals(15, sut.get("k1"));

        assertEquals(26, sut.getOrAdd("k2", () -> 26));
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

    void testStream(IMap<Integer, Integer> sut) {
        assertTrue(sut.empty());
        assertEquals(0, sut.stream().count());
        for (int i = 1; 10 > i; ++i) {
            sut.add(i, i * 10);
            assertEquals(
                    sut.size(),
                    sut.stream()
                            .filter(e -> e.value == e.key * 10)
                            .count()
            );
        }
    }

    void testToHashIndirizzamentoAperto(IMap<Integer, Integer> source) {
        var sut = HashIndirizzamentoAperto.from(source);

        assertTrue(sut.empty());
        assertEquals(0, sut.size());
        assertEquals(0, sut.capacity());
        for (int i = 1; 10 > i; ++i) {
            source.add(i, i * 10);
        }

        sut = HashIndirizzamentoAperto.from(source);
        assertFalse(sut.empty());
        assertEquals(9, sut.size());
        assertTrue(sut.capacity() >= sut.size());
        assertEquals(
                sut.size(),
                sut.stream()
                        .filter(e -> e.value == e.key * 10)
                        .count()
        );
    }

    void testToArrayOrdinato(IMap<Integer, Integer> source) {
        var sut = ArrayOrdinato.from(source);

        assertTrue(sut.empty());
        assertEquals(0, sut.size());
        assertEquals(0, sut.capacity());
        for (int i = 1; 10 > i; ++i) {
            source.add(i, i * 10);
        }

        sut = ArrayOrdinato.from(source);
        assertFalse(sut.empty());
        assertEquals(9, sut.size());
        assertTrue(sut.capacity() >= sut.size());
        assertEquals(
                sut.size(),
                sut.stream()
                        .filter(e -> e.value == e.key * 10)
                        .count()
        );
    }

    void testClear(IMap<Integer, Integer> sut) {
        assertTrue(sut.empty());
        for (int i = 1; 10 > i; ++i) {
            sut.add(i, i * 10);
        }
        assertFalse(sut.empty());
        assertEquals(9, sut.size());

        sut.clear();
        assertTrue(sut.empty());
        assertEquals(0, sut.size());
        assertEquals(0, sut.stream().count());

        for (int i = 1; 10 > i; ++i) {
            assertFalse(sut.has(i));
            assertNull(sut.get(i));
        }

        for (int i = 1; 10 > i; ++i) {
            sut.add(i, i * 100);
        }
        assertFalse(sut.empty());
        assertEquals(9, sut.size());

        assertEquals(
                sut.size(),
                sut.stream()
                        .filter(e -> e.value == e.key * 100)
                        .count()
        );
    }

    void testGetOrAdd(IMap<Integer, Integer> sut) {
        assertTrue(sut.empty());
        assertEquals(0, sut.size());

        assertEquals(42, sut.getOrAdd(42, () -> 42));
        assertTrue(sut.has(42));
        assertEquals(42, sut.get(42));
        assertFalse(sut.empty());
        assertEquals(1, sut.size());

        assertEquals(42, sut.getOrAdd(42, () -> 7));
        assertTrue(sut.has(42));
        assertEquals(42, sut.get(42));
        assertFalse(sut.empty());
        assertEquals(1, sut.size());
    }
}
