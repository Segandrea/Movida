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

package movida.dicarlosegantini;

import movida.commons.Movie;
import movida.commons.Person;
import movida.dicarlosegantini.array.DynamicArray;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class MovidaPersistenceTest {
    static final Movie[] MOVIES = new Movie[]{
            new Movie("Cape Fear", 1991, 163093, makeCast(new String[]{
                    "Robert De Niro", "Nick Nolte", "Jessica Lange", "Juliette Lewis"
            }), new Person("Martin Scorsese")),
            new Movie("Taxi Driver", 1976, 684728, makeCast(new String[]{
                    "Robert De Niro", "Jodie Foster", "Cybill Shepherd", "Albert Brooks"
            }), new Person("Martin Scorsese")),
            new Movie("Pulp Fiction", 1994, 1743616, makeCast(new String[]{
                    "John Travolta", "Uma Thurman"
            }), new Person("Quentin Tarantino")),
    };

    static Person[] makeCast(final String[] names) {
        return Arrays.stream(names).map(Person::new).toArray(Person[]::new);
    }

    @Test
    void load() {
        final var sut = new MovidaPersistence();
        final var actual = new DynamicArray<Movie>();
        sut.load(new File("movida/dicarlosegantini/test.txt"), actual::append);

        assertEquals(MOVIES.length, actual.size());
        IntStream.range(0, MOVIES.length).forEach(i -> {
            final var expectedMovie = MOVIES[i];
            final var actualMovie = actual.get(i);

            assertEquals(expectedMovie.getTitle(), actualMovie.getTitle());
            assertEquals(expectedMovie.getYear(), actualMovie.getYear());
            assertEquals(expectedMovie.getVotes(), actualMovie.getVotes());
            assertEquals(expectedMovie.getDirector().getName(), actualMovie.getDirector().getName());

            final var expectedCast = expectedMovie.getCast();
            final var actualCast = actualMovie.getCast();
            assertEquals(expectedCast.length, actualCast.length);
            IntStream.range(0, expectedCast.length)
                    .forEach(x -> assertEquals(expectedCast[x].getName(), actualCast[x].getName()));
        });
    }

    @Test
    void store() {
        final var sut = new MovidaPersistence();
        final var expectedFile = new File("movida/dicarlosegantini/test.txt");

        try {
            final var actualFile = File.createTempFile("temp", null);
            actualFile.deleteOnExit();

            sut.store(actualFile, Arrays.stream(MOVIES.clone()));
            assertEquals(Files.readString(expectedFile.toPath()), Files.readString(actualFile.toPath()));
        } catch (IOException e) {
            fail();
        }
    }
}
