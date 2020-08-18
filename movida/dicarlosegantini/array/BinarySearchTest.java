package movida.dicarlosegantini.array;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BinarySearchTest {
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