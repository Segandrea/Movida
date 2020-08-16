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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class DynamicArrayTest {
    DynamicArray<Integer> sut = null;

    int seed(final int items) {
        for (int i = 0; i < items; ++i) {
            final var currentSize = this.sut.size();

            this.sut.add(i, this.sut.size());
            assertFalse(this.sut.empty());
            assertEquals(currentSize + 1, this.sut.size());
            assertTrue(this.sut.size() <= this.sut.capacity());

            for (int x = 0; 0 >= x; ++x) {
                assertEquals(x, this.sut.get(x));
            }
        }

        assertEquals(items, this.sut.size());
        return this.sut.capacity();
    }

    @BeforeEach
    void setUp() {
        this.sut = new DynamicArray<>();
        assertTrue(this.sut.empty());
        assertEquals(0, this.sut.size());
        assertEquals(0, this.sut.capacity());
    }

    @Test
    void reserve() {
        final var startCapacity = this.sut.capacity();
        final var startSize = this.sut.size();
        assertTrue(startSize <= startCapacity);

        this.sut.reserve(0);
        assertEquals(startSize, this.sut.size());
        assertEquals(startCapacity, this.sut.capacity());

        // force container expansion
        final var capacityWithTenAdditionalItems = startCapacity + 10;
        this.sut.reserve((startCapacity - startSize) + 10);
        assertEquals(startSize, this.sut.size());
        assertTrue(capacityWithTenAdditionalItems <= this.sut.capacity());

        // ensure no expansion occurs if there is enough space already allocated
        final var currentCapacity = this.sut.capacity();
        this.sut.reserve(1);
        assertEquals(startSize, this.sut.size());
        assertEquals(currentCapacity, this.sut.capacity());

        // ensure items in the array are left untouched after an expansion
        final var seedIndex = this.sut.size();
        final var capacity = this.seed(3);
        final var size = this.sut.size();

        this.sut.reserve((capacity - this.sut.size()) + 10);
        assertEquals(size, this.sut.size());
        assertTrue((capacity + 10) <= this.sut.capacity());

        for (int i = 0; 3 > i; ++i) {
            assertEquals(i, this.sut.get(seedIndex + i));
        }
    }

    @Test
    void add() {
        this.sut.add(1, 0);
        assertFalse(this.sut.empty());
        assertEquals(1, this.sut.size());
        assertTrue(this.sut.size() <= this.sut.capacity());
        assertEquals(1, this.sut.get(0));
        assertArrayEquals(new Integer[]{1}, this.sut.stream().toArray(Integer[]::new));

        this.sut.add(0, 0);
        assertFalse(this.sut.empty());
        assertEquals(2, this.sut.size());
        assertTrue(this.sut.size() <= this.sut.capacity());
        assertEquals(0, this.sut.get(0));
        assertEquals(1, this.sut.get(1));
        assertArrayEquals(new Integer[]{0, 1}, this.sut.stream().toArray(Integer[]::new));

        this.sut.add(2, this.sut.size());
        assertFalse(this.sut.empty());
        assertEquals(3, this.sut.size());
        assertTrue(this.sut.size() <= this.sut.capacity());
        assertEquals(0, this.sut.get(0));
        assertEquals(1, this.sut.get(1));
        assertEquals(2, this.sut.get(2));
        assertArrayEquals(new Integer[]{0, 1, 2}, this.sut.stream().toArray(Integer[]::new));
    }

    @Test
    void del() {
        final var capacity = this.seed(3);

        assertEquals(1, this.sut.del(1));
        assertFalse(this.sut.empty());
        assertEquals(2, this.sut.size());
        assertEquals(capacity, this.sut.capacity());
        assertEquals(0, this.sut.get(0));
        assertEquals(2, this.sut.get(1));
        assertArrayEquals(new Integer[]{0, 2}, this.sut.stream().toArray(Integer[]::new));

        assertEquals(2, this.sut.del(1));
        assertFalse(this.sut.empty());
        assertEquals(1, this.sut.size());
        assertEquals(capacity, this.sut.capacity());
        assertEquals(0, this.sut.get(0));
        assertArrayEquals(new Integer[]{0}, this.sut.stream().toArray(Integer[]::new));

        assertEquals(0, this.sut.del(0));
        assertTrue(this.sut.empty());
        assertEquals(0, this.sut.size());
        assertEquals(capacity, this.sut.capacity());
        assertArrayEquals(new Integer[]{}, this.sut.stream().toArray(Integer[]::new));
    }

    @Test
    void clear() {
        final var capacity = this.seed(5);

        this.sut.clear();
        assertTrue(this.sut.empty());
        assertEquals(0, this.sut.size());
        assertEquals(capacity, this.sut.capacity());
        assertEquals(0, this.sut.stream().count());
    }

    @Test
    void stream() {
        this.seed(5);

        for (int size = this.sut.size(); 0 < size; size = this.sut.size()) {
            final var items = this.sut.stream().toArray(Integer[]::new);

            for (int i = 0; size > i; ++i) {
                assertEquals(i, items[i]);
            }

            this.sut.del(size - 1);
        }

        assertEquals(0, this.sut.stream().count());
    }

    @Test
    void binarySearch() {
        this.seed(5);
        IntStream.range(0, this.sut.size()).forEach(i -> assertEquals(i, this.sut.binarySearch(i, Integer::compareTo)));
        assertEquals(-1, this.sut.binarySearch(-1, Integer::compareTo));
        assertEquals(-(this.sut.size() + 1), this.sut.binarySearch(this.sut.size(), Integer::compareTo));
    }

    @Test
    void binaryInsert() {
        assertTrue(this.sut.binaryInsert(1, Integer::compareTo));
        assertFalse(this.sut.empty());
        assertEquals(1, this.sut.size());
        assertTrue(this.sut.size() <= this.sut.capacity());
        assertEquals(1, this.sut.get(0));
        assertArrayEquals(new Integer[]{1}, this.sut.stream().toArray(Integer[]::new));

        assertTrue(this.sut.binaryInsert(0, Integer::compareTo));
        assertFalse(this.sut.empty());
        assertEquals(2, this.sut.size());
        assertTrue(this.sut.size() <= this.sut.capacity());
        assertEquals(0, this.sut.get(0));
        assertEquals(1, this.sut.get(1));
        assertArrayEquals(new Integer[]{0, 1}, this.sut.stream().toArray(Integer[]::new));

        assertTrue(this.sut.binaryInsert(2, Integer::compareTo));
        assertFalse(this.sut.empty());
        assertEquals(3, this.sut.size());
        assertTrue(this.sut.size() <= this.sut.capacity());
        assertEquals(0, this.sut.get(0));
        assertEquals(1, this.sut.get(1));
        assertEquals(2, this.sut.get(2));
        assertArrayEquals(new Integer[]{0, 1, 2}, this.sut.stream().toArray(Integer[]::new));

        for (int i = 0; i < this.sut.size(); ++i) {
            assertFalse(this.sut.binaryInsert(i, Integer::compareTo));
            assertFalse(this.sut.empty());
            assertEquals(3, this.sut.size());
            assertTrue(this.sut.size() <= this.sut.capacity());
            assertEquals(0, this.sut.get(0));
            assertEquals(1, this.sut.get(1));
            assertEquals(2, this.sut.get(2));
            assertArrayEquals(new Integer[]{0, 1, 2}, this.sut.stream().toArray(Integer[]::new));
        }
    }

    @Test
    void binaryRemove() {
        final var capacity = this.seed(3);

        assertFalse(this.sut.binaryRemove(-1, Integer::compareTo));
        assertFalse(this.sut.empty());
        assertEquals(3, this.sut.size());
        assertTrue(this.sut.size() <= this.sut.capacity());
        assertEquals(0, this.sut.get(0));
        assertEquals(1, this.sut.get(1));
        assertEquals(2, this.sut.get(2));
        assertArrayEquals(new Integer[]{0, 1, 2}, this.sut.stream().toArray(Integer[]::new));

        assertFalse(this.sut.binaryRemove(this.sut.size(), Integer::compareTo));
        assertFalse(this.sut.empty());
        assertEquals(3, this.sut.size());
        assertTrue(this.sut.size() <= this.sut.capacity());
        assertEquals(0, this.sut.get(0));
        assertEquals(1, this.sut.get(1));
        assertEquals(2, this.sut.get(2));
        assertArrayEquals(new Integer[]{0, 1, 2}, this.sut.stream().toArray(Integer[]::new));

        assertTrue(this.sut.binaryRemove(1, Integer::compareTo));
        assertFalse(this.sut.empty());
        assertEquals(2, this.sut.size());
        assertEquals(capacity, this.sut.capacity());
        assertEquals(0, this.sut.get(0));
        assertEquals(2, this.sut.get(1));
        assertArrayEquals(new Integer[]{0, 2}, this.sut.stream().toArray(Integer[]::new));

        assertTrue(this.sut.binaryRemove(2, Integer::compareTo));
        assertFalse(this.sut.empty());
        assertEquals(1, this.sut.size());
        assertEquals(capacity, this.sut.capacity());
        assertEquals(0, this.sut.get(0));
        assertArrayEquals(new Integer[]{0}, this.sut.stream().toArray(Integer[]::new));

        assertTrue(this.sut.binaryRemove(0, Integer::compareTo));
        assertTrue(this.sut.empty());
        assertEquals(0, this.sut.size());
        assertEquals(capacity, this.sut.capacity());
        assertArrayEquals(new Integer[]{}, this.sut.stream().toArray(Integer[]::new));

        for (int i = 0; 3 > i; ++i) {
            assertFalse(this.sut.binaryRemove(i, Integer::compareTo));
            assertTrue(this.sut.empty());
            assertEquals(0, this.sut.size());
            assertEquals(capacity, this.sut.capacity());
            assertArrayEquals(new Integer[]{}, this.sut.stream().toArray(Integer[]::new));
        }
    }

    @Test
    void slice() {
        final var capacity = this.seed(3);

        assertArrayEquals(new Integer[]{0, 1, 2}, this.sut.slice(Integer[]::new, 0, 2));
        assertArrayEquals(new Integer[]{1, 2}, this.sut.slice(Integer[]::new, 1, 2));
        assertArrayEquals(new Integer[]{2}, this.sut.slice(Integer[]::new, 2, 2));
        assertArrayEquals(new Integer[]{1}, this.sut.slice(Integer[]::new, 1, 1));
        assertArrayEquals(new Integer[]{0}, this.sut.slice(Integer[]::new, 0, 0));
        assertArrayEquals(new Integer[]{0, 1}, this.sut.slice(Integer[]::new, 0, 1));

        assertFalse(this.sut.empty());
        assertEquals(3, this.sut.size());
        assertEquals(capacity, this.sut.capacity());
    }
}
