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

import movida.commons.Collaboration;
import movida.commons.Movie;
import movida.commons.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MovidaCollaborationsTest {
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

    private MovidaCollaborations sut;

    static Person[] makePeople(final String[] names) {
        return Arrays.stream(names).map(Person::new).toArray(Person[]::new);
    }

    @BeforeEach
    void setUp() {
        this.sut = new MovidaCollaborations();
        this.seed();
    }

    void seed() {
        for (final var movie : this.MOVIES) {
            final var cast = movie.getCast();

            for (int a = 0; a < cast.length; ++a) {
                for (int b = a + 1; b < cast.length; ++b) {
                    this.sut.addCollaboration(movie, cast[a], cast[b]);
                }
            }
        }
    }

    @Test
    void clear() {
        final var deNiro = new Person("Robert De Niro");

        this.sut.clear();
        assertEquals(0, this.sut.getDirectCollaboratorsOf(deNiro).length);
        assertEquals(0, this.sut.maximizeCollaborationsInTheTeamOf(deNiro).length);
        assertEquals(0, this.sut.getTeamOf(deNiro).length);
    }

    @Test
    void getDirectCollaboratorsOf() {
        assertArrayEquals(
                makePeople(new String[]{"Uma Thurman"}),
                this.sut.getDirectCollaboratorsOf(new Person("John Travolta"))
        );
        assertArrayEquals(
                makePeople(new String[]{"John Travolta"}),
                this.sut.getDirectCollaboratorsOf(new Person("Uma Thurman"))
        );
        assertArrayEquals(
                makePeople(new String[]{
                        "Jodie Foster",
                        "Jessica Lange",
                        "Nick Nolte",
                        "Cybill Shepherd",
                        "Albert Brooks",
                        "Juliette Lewis",
                }),
                this.sut.getDirectCollaboratorsOf(new Person("Robert De Niro"))
        );
        assertArrayEquals(
                makePeople(new String[]{
                        "Albert Brooks",
                        "Robert De Niro",
                        "Cybill Shepherd",
                }),
                this.sut.getDirectCollaboratorsOf(new Person("Jodie Foster"))
        );
    }

    @Test
    void getTeamOf() {
        assertArrayEquals(
                makePeople(new String[]{"Uma Thurman"}),
                this.sut.getTeamOf(new Person("John Travolta"))
        );
        assertArrayEquals(
                makePeople(new String[]{"John Travolta"}),
                this.sut.getTeamOf(new Person("Uma Thurman"))
        );
        assertArrayEquals(
                makePeople(new String[]{
                        "Jodie Foster",
                        "Jessica Lange",
                        "Nick Nolte",
                        "Cybill Shepherd",
                        "Albert Brooks",
                        "Juliette Lewis",
                }),
                this.sut.getTeamOf(new Person("Robert De Niro"))
        );
        assertArrayEquals(
                makePeople(new String[]{
                        "Albert Brooks",
                        "Robert De Niro",
                        "Cybill Shepherd",
                        "Jessica Lange",
                        "Nick Nolte",
                        "Juliette Lewis",
                }),
                this.sut.getTeamOf(new Person("Jodie Foster"))
        );
    }

    @Test
    void maximizeCollaborationsInTheTeamOf() {
        assertArrayEquals(
                new Collaboration[]{
                        new Collaboration(new Person("John Travolta"), new Person("Uma Thurman"))
                },
                this.sut.maximizeCollaborationsInTheTeamOf(new Person("John Travolta"))
        );
        assertArrayEquals(
                new Collaboration[]{
                        new Collaboration(new Person("John Travolta"), new Person("Uma Thurman"))
                },
                this.sut.maximizeCollaborationsInTheTeamOf(new Person("Uma Thurman"))
        );
        assertArrayEquals(
                new Collaboration[]{
                        new Collaboration(new Person("Robert De Niro"), new Person("Juliette Lewis")),
                        new Collaboration(new Person("Robert De Niro"), new Person("Nick Nolte")),
                        new Collaboration(new Person("Robert De Niro"), new Person("Cybill Shepherd")),
                        new Collaboration(new Person("Robert De Niro"), new Person("Jodie Foster")),
                        new Collaboration(new Person("Robert De Niro"), new Person("Albert Brooks")),
                        new Collaboration(new Person("Robert De Niro"), new Person("Jessica Lange")),
                },
                this.sut.maximizeCollaborationsInTheTeamOf(new Person("Robert De Niro"))
        );
        assertArrayEquals(
                new Collaboration[]{
                        new Collaboration(new Person("Robert De Niro"), new Person("Jodie Foster")),
                        new Collaboration(new Person("Robert De Niro"), new Person("Jessica Lange")),
                        new Collaboration(new Person("Jodie Foster"), new Person("Albert Brooks")),
                        new Collaboration(new Person("Robert De Niro"), new Person("Juliette Lewis")),
                        new Collaboration(new Person("Jodie Foster"), new Person("Cybill Shepherd")),
                        new Collaboration(new Person("Robert De Niro"), new Person("Nick Nolte")),
                },
                this.sut.maximizeCollaborationsInTheTeamOf(new Person("Jodie Foster"))
        );
    }
}