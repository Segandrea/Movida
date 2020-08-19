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

import movida.commons.MapImplementation;
import movida.commons.Movie;
import movida.commons.Person;
import movida.commons.SortingAlgorithm;
import movida.dicarlosegantini.sort.ISort;
import movida.dicarlosegantini.sort.QuickSort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class MovidaDBTest {
    static final ISort sortingAlgorithm = QuickSort.getInstance();
    final Person[] DIRECTORS = makePeople(new String[]{
            "Martin Scorsese", "Quentin Tarantino"
    });
    final Person[] ACTORS = makePeople(new String[]{
            "Robert De Niro", "Nick Nolte", "Jessica Lange", "Juliette Lewis",
            "Jodie Foster", "Cybill Shepherd", "Albert Brooks",
            "John Travolta", "Uma Thurman"
    });
    final Movie[] MOVIES = new Movie[]{
            new Movie("Cape Fear", 1991, 163093, makePeople(new String[]{
                    "Robert De Niro", "Nick Nolte", "Jessica Lange", "Juliette Lewis"
            }), new Person("Martin Scorsese")),
            new Movie("Taxi Driver", 1976, 684728, makePeople(new String[]{
                    "Robert De Niro", "Jodie Foster", "Cybill Shepherd", "Albert Brooks"
            }), new Person("Martin Scorsese")),
            new Movie("Pulp Fiction", 1994, 1743616, makePeople(new String[]{
                    "John Travolta", "Uma Thurman"
            }), new Person("Quentin Tarantino")),
    };
    MovidaDB sut;

    MovidaDBTest() {
        sortingAlgorithm.sort(this.DIRECTORS, Comparator.comparing(Person::getName));
        sortingAlgorithm.sort(this.ACTORS, Comparator.comparing(Person::getName));
        sortingAlgorithm.sort(this.MOVIES, Comparator.comparing(Movie::getTitle));
    }

    static Person[] makePeople(final String[] names) {
        return Arrays.stream(names).map(Person::new).toArray(Person[]::new);
    }

    @BeforeEach
    void setUp() {
        this.sut = new MovidaDB();
        this.sut.setMap(MapImplementation.ArrayOrdinato);
        Arrays.stream(this.MOVIES).forEach(m -> this.sut.load(m));
    }

    @Test
    void clear() {
        this.sut.clear();
        assertEquals(0, this.sut.countDirectors());
        assertEquals(0, this.sut.countActors());
        assertEquals(0, this.sut.countMovies());
        Arrays.stream(this.MOVIES).forEach(m -> assertFalse(this.sut.deleteMovieByTitle(m.getTitle())));
        Arrays.stream(this.MOVIES).forEach(m -> assertNull(this.sut.getMovieByTitle(m.getTitle())));
        Arrays.stream(this.ACTORS).forEach(a -> assertNull(this.sut.getActorByName(a.getName())));
        Arrays.stream(this.DIRECTORS).forEach(d -> assertNull(this.sut.getDirectorByName(d.getName())));
        assertEquals(0, this.sut.getAllMovies().length);
        assertEquals(0, this.sut.streamActors().count());
        assertEquals(0, this.sut.streamDirectors().count());
        assertEquals(0, this.sut.streamMovies().count());
    }

    @Test
    void countMovies() {
        assertEquals(this.MOVIES.length, this.sut.countMovies());
    }

    @Test
    void countActors() {
        assertEquals(this.ACTORS.length, this.sut.countActors());
    }

    @Test
    void countDirectors() {
        assertEquals(this.DIRECTORS.length, this.sut.countDirectors());
    }

    @Test
    void deleteMovieByTitle() {
        for (final var movie : this.MOVIES) {
            final var moviesCount = this.sut.countMovies();
            assertTrue(this.sut.deleteMovieByTitle(movie.getTitle()));
            assertEquals(moviesCount - 1, this.sut.countMovies());
            assertNull(this.sut.getMovieByTitle(movie.getTitle()));
        }
    }

    @Test
    void getMovieByTitle() {
        for (final var movie : this.MOVIES) {
            assertEquals(movie.getTitle(), this.sut.getMovieByTitle(movie.getTitle()).getTitle());
        }
    }

    @Test
    void getActorByName() {
        for (final var actor : this.ACTORS) {
            assertEquals(actor.getName(), this.sut.getActorByName(actor.getName()).getName());
        }
    }

    @Test
    void getDirectorByName() {
        for (final var director : this.DIRECTORS) {
            assertEquals(director.getName(), this.sut.getDirectorByName(director.getName()).getName());
        }
    }

    @Test
    void getAllMovies() {
        final var allMovies = this.sut.getAllMovies();
        sortingAlgorithm.sort(allMovies, Comparator.comparing(Movie::getTitle));
        assertArrayEquals(this.MOVIES, allMovies);
    }

    @Test
    void streamActors() {
        final var allActors = this.sut.streamActors().toArray(Person[]::new);
        sortingAlgorithm.sort(allActors, Comparator.comparing(Person::getName));
        assertEquals(this.ACTORS.length, allActors.length);
        IntStream.range(0, this.ACTORS.length).forEach(i -> {
            assertEquals(this.ACTORS[i].getName(), allActors[i].getName());
        });
    }

    @Test
    void streamDirectors() {
        final var allDirectors = this.sut.streamDirectors().toArray(Person[]::new);
        sortingAlgorithm.sort(allDirectors, Comparator.comparing(Person::getName));
        assertEquals(this.DIRECTORS.length, allDirectors.length);
        IntStream.range(0, this.DIRECTORS.length).forEach(i -> {
            assertEquals(this.DIRECTORS[i].getName(), allDirectors[i].getName());
        });
    }

    @Test
    void streamMovies() {
        final var allMovies = this.sut.streamMovies().toArray(Movie[]::new);
        assertEquals(this.MOVIES.length, allMovies.length);

        sortingAlgorithm.sort(allMovies, Comparator.comparing(Movie::getTitle));
        IntStream.range(0, this.MOVIES.length).forEach(i -> {
            final var expectedMovie = this.MOVIES[i];
            final var actualMovie = allMovies[i];

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
    void setSort() {
        assertFalse(this.sut.setSort(SortingAlgorithm.SelectionSort));
        assertFalse(this.sut.setSort(SortingAlgorithm.QuickSort));
    }

    @Test
    void setMap() {
        this.sut.setMap(MapImplementation.HashIndirizzamentoAperto);

        assertFalse(this.sut.setMap(MapImplementation.HashIndirizzamentoAperto));
        assertTrue(this.sut.setMap(MapImplementation.ArrayOrdinato));
        assertFalse(this.sut.setMap(MapImplementation.ArrayOrdinato));
        assertTrue(this.sut.setMap(MapImplementation.HashIndirizzamentoAperto));
    }
}
