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

import movida.commons.*;
import movida.dicarlosegantini.map.ArrayOrdinato;
import movida.dicarlosegantini.map.HashIndirizzamentoAperto;
import movida.dicarlosegantini.map.IMap;
import movida.dicarlosegantini.sort.ISort;
import movida.dicarlosegantini.sort.QuickSort;
import movida.dicarlosegantini.sort.SelectionSort;

import java.io.*;
import java.util.Arrays;
import java.util.stream.Collectors;

public class MovidaCore implements IMovidaConfig {
    private final MapImplementation mapImplementation;
    private IMap<String, Person> people;
    private IMap<String, Movie> movies;
    private ISort sort;

    private MovidaCore() {
        this.mapImplementation = MapImplementation.HashIndirizzamentoAperto;
        this.people = new HashIndirizzamentoAperto<>();
        this.movies = new HashIndirizzamentoAperto<>();
        this.sort = new QuickSort();
    }

    @Override
    public boolean setSort(final SortingAlgorithm a) {
        switch (a) {
            case SelectionSort:
                this.sort = new SelectionSort();
                break;
            case QuickSort:
                this.sort = new QuickSort();
                break;
            default:
                return false;
        }

        return true;
    }

    @Override
    public boolean setMap(final MapImplementation m) {
        if (m != this.mapImplementation) {
            switch (m) {
                case ArrayOrdinato:
                    this.people = ArrayOrdinato.from(this.people);
                    this.movies = ArrayOrdinato.from(this.movies);
                    break;
                case HashIndirizzamentoAperto:
                    this.people = HashIndirizzamentoAperto.from(this.people);
                    this.movies = HashIndirizzamentoAperto.from(this.movies);
                    break;
                default:
                    return false;
            }
        }

        return true;
    }

    private <K extends Comparable<K>, V> IMap<K, V> instanceCurrentMap() {
        if (this.mapImplementation == MapImplementation.ArrayOrdinato) {
            return new ArrayOrdinato<>();
        }
        return new HashIndirizzamentoAperto<>();
    }

    private static void saveMovieToFile(BufferedWriter writer, final Movie movie) throws IOException {
        writer.append("title: ");
        writer.append(movie.getTitle());
        writer.newLine();

        writer.append("year: ");
        writer.append(movie.getYear().toString());
        writer.newLine();

        writer.append("director: ");
        writer.append(movie.getDirector().getName());
        writer.newLine();

        writer.append("cast: ");
        writer.append(
                Arrays.stream(movie.getCast())
                        .map(Person::getName)
                        .collect(Collectors.joining(", "))
        );
        writer.newLine();

        writer.append("votes: ");
        writer.append(movie.getVotes().toString());
        writer.newLine();

        writer.newLine();
    }

    private void loadMovie(final IMap<String, String> movieData) {
        final var title = movieData.get("title");
        final var year = Integer.parseInt(movieData.get("year"));
        final var director = new Person(movieData.get("director"));
        final var cast = (Person[]) Arrays.stream(movieData.get("cast").split("[\\W]*,[\\W]*"))
                .map(Person::new)
                .toArray(Person[]::new);
        final var votes = Integer.parseInt(movieData.get("votes"));

        final var movie = new Movie(title, year, votes, cast, director);
        this.movies.add(movie.getTitle(), movie);
        this.people.add(movie.getDirector().getName(), movie.getDirector());
        for (var p : movie.getCast()) {
            this.people.add(p.getName(), p);
        }
    }

    public void loadFromFile(File f) throws IOException {
        var reader = new BufferedReader(new FileReader(f));
        IMap<String, String> movieData = this.instanceCurrentMap();

        for (var line = reader.readLine(); line != null; line = reader.readLine()) {
            line = line.strip().toLowerCase();

            if (line.isEmpty()) {
                this.loadMovie(movieData);
                movieData.clear();
                continue;
            }

            final var keyValue = line.split("[\\W]*:[\\W]*");
            if (keyValue.length != 2) {
                throw new MovidaFileException(/*parse error: bad key-value supplied*/);
            }

            switch (keyValue[0]) {
                case "title":
                case "year":
                case "director":
                case "cast":
                case "votes":
                    movieData.add(keyValue[0], keyValue[1]);
                    break;
                default:
                    throw new MovidaFileException(/*parse error: unexpected key*/);
            }
        }

        if (!movieData.empty()) {
            this.loadMovie(movieData);
        }
    }

    public void saveToFile(File f) throws IOException {
        var writer = new BufferedWriter(new FileWriter(f));
        var iterator = this.movies.stream().iterator();

        while (iterator.hasNext()) {
            final var entry = iterator.next();
            saveMovieToFile(writer, entry.value);
        }

        writer.flush();
    }
}
