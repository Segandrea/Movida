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

import movida.commons.MovidaFileException;
import movida.commons.Movie;
import movida.commons.Person;
import movida.dicarlosegantini.map.HashIndirizzamentoAperto;
import movida.dicarlosegantini.map.IMap;

import java.io.*;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MovidaPersistence {
    private static Movie loadMovie(final IMap<String, String> movieData) {
        final var title = movieData.get("title");
        final var year = Integer.parseInt(movieData.get("year"));
        final var votes = Integer.parseInt(movieData.get("votes"));
        final var director = new Person(movieData.get("director"));
        final var cast = (Person[]) Arrays.stream(movieData.get("cast").split("[\\W]*,[\\W]*"))
                .map(Person::new)
                .toArray(Person[]::new);

        return new Movie(title, year, votes, cast, director);
    }

    private static void storeMovie(BufferedWriter writer, final Movie movie) throws IOException {
        writer.append("Title: ");
        writer.append(movie.getTitle());
        writer.newLine();

        writer.append("Year: ");
        writer.append(movie.getYear().toString());
        writer.newLine();

        writer.append("Director: ");
        writer.append(movie.getDirector().getName());
        writer.newLine();

        writer.append("Cast: ");
        writer.append(
                Arrays.stream(movie.getCast())
                        .map(Person::getName)
                        .collect(Collectors.joining(", "))
        );
        writer.newLine();

        writer.append("Votes: ");
        writer.append(movie.getVotes().toString());
        writer.newLine();
    }

    public void loadMovies(final File f, final Consumer<Movie> consumer) throws MovidaFileException {
        final var movieData = new HashIndirizzamentoAperto<String, String>();
        movieData.reserve(5);

        try {
            final var reader = new BufferedReader(new FileReader(f));

            for (var line = reader.readLine(); null != line; line = reader.readLine()) {
                line = line.strip();

                if (line.isEmpty()) {
                    consumer.accept(loadMovie(movieData));
                    movieData.clear();
                    continue;
                }

                final var keyValue = line.split("[\\W]*:[\\W]*");
                if (2 != keyValue.length) {
                    throw new MovidaFileException(/* parse error: bad key-value supplied */);
                }

                final var key = keyValue[0].toLowerCase();
                switch (key) {
                    case "title":
                    case "year":
                    case "director":
                    case "cast":
                    case "votes":
                        movieData.add(key, keyValue[1]);
                        break;
                    default:
                        throw new MovidaFileException(/* parse error: unexpected key */);
                }
            }
        } catch (final IOException e) {
            final var x = new MovidaFileException();
            x.initCause(x);
            throw x;
        }

        if (!movieData.empty()) {
            consumer.accept(loadMovie(movieData));
        }
    }

    public void storeMovies(final File f, final Stream<Movie> movies) throws MovidaFileException {
        try {
            final var writer = new BufferedWriter(new FileWriter(f));
            final var iterator = movies.iterator();

            while (iterator.hasNext()) {
                storeMovie(writer, iterator.next());
                writer.newLine();
            }

            writer.flush();
        } catch (final IOException e) {
            final var x = new MovidaFileException();
            x.initCause(x);
            throw x;
        }
    }
}
