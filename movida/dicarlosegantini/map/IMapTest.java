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

import static org.junit.jupiter.api.Assertions.*;

class IMapTest {
    @org.junit.jupiter.api.Test
    void testHashIndirizzamentoAperto() {
        this.test(new HashIndirizzamentoAperto<>());
    }

    @org.junit.jupiter.api.Test
    void testArrayOrdinato() {
        // TODO
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