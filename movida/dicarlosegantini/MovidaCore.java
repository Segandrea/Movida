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

// TODO: replace java.util.HashSet with custom set implementation
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
import java.util.HashSet;
import java.util.stream.Collectors;

public final class MovidaCore implements IMovidaConfig, IMovidaDB {
    private MapImplementation mapImplementation;
    private IMap<String, HashSet<Movie>> moviesByDirector;
    private IMap<String, HashSet<Movie>> moviesByActor;
    private IMap<Integer, HashSet<Movie>> moviesByYear;
    private IMap<String, Person> people;
    private IMap<String, Movie> movies;
    private ISort sortingAlgorithm;

    private MovidaCore() {
        this.mapImplementation = MapImplementation.HashIndirizzamentoAperto;

        this.moviesByDirector = new HashIndirizzamentoAperto<>();
        this.moviesByActor = new HashIndirizzamentoAperto<>();
        this.moviesByYear = new HashIndirizzamentoAperto<>();
        this.people = new HashIndirizzamentoAperto<>();
        this.movies = new HashIndirizzamentoAperto<>();

        this.sortingAlgorithm = QuickSort.getInstance();
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

    private <K extends Comparable<K>, V> IMap<K, V> instanceMap() {
        if (MapImplementation.ArrayOrdinato == this.mapImplementation) {
            return new ArrayOrdinato<>();
        }
        return new HashIndirizzamentoAperto<>();
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

        this.moviesByDirector.getOrAdd(director.getName(), HashSet::new).add(movie);
        this.moviesByYear.getOrAdd(year, HashSet::new).add(movie);
        for (final var actor : movie.getCast()) {
            this.moviesByActor.getOrAdd(actor.getName(), HashSet::new).add(movie);
            this.people.add(actor.getName(), actor);
        }
        this.people.add(movie.getDirector().getName(), movie.getDirector());
        this.movies.add(movie.getTitle(), movie);
    }

    @Override
    public boolean setSort(final SortingAlgorithm sortingAlgorithm) {
        switch (sortingAlgorithm) {
            case SelectionSort:
                this.sortingAlgorithm = SelectionSort.getInstance();
                break;
            case QuickSort:
                this.sortingAlgorithm = QuickSort.getInstance();
                break;
            default:
                return false;
        }

        return true;
    }

    @Override
    public boolean setMap(final MapImplementation mapImplementation) {
        if (mapImplementation != this.mapImplementation) {
            switch (mapImplementation) {
                case ArrayOrdinato:
                    this.moviesByDirector = ArrayOrdinato.from(this.moviesByDirector);
                    this.moviesByActor = ArrayOrdinato.from(this.moviesByActor);
                    this.moviesByYear = ArrayOrdinato.from(this.moviesByYear);
                    this.people = ArrayOrdinato.from(this.people);
                    this.movies = ArrayOrdinato.from(this.movies);
                    break;
                case HashIndirizzamentoAperto:
                    this.moviesByDirector = HashIndirizzamentoAperto.from(this.moviesByDirector);
                    this.moviesByActor = HashIndirizzamentoAperto.from(this.moviesByActor);
                    this.moviesByYear = HashIndirizzamentoAperto.from(this.moviesByYear);
                    this.people = HashIndirizzamentoAperto.from(this.people);
                    this.movies = HashIndirizzamentoAperto.from(this.movies);
                    break;
                default:
                    return false;
            }
            this.mapImplementation = mapImplementation;
        }

        return true;
    }

    @Override
    public void loadFromFile(File f) {
        IMap<String, String> movieData = this.instanceMap();

        try {
            final var reader = new BufferedReader(new FileReader(f));

            for (var line = reader.readLine(); null != line; line = reader.readLine()) {
                line = line.strip().toLowerCase();

                if (line.isEmpty()) {
                    this.loadMovie(movieData);
                    movieData.clear();
                    continue;
                }

                final var keyValue = line.split("[\\W]*:[\\W]*");
                if (2 != keyValue.length) {
                    throw new MovidaFileException(/* parse error: bad key-value supplied */);
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
                        throw new MovidaFileException(/* parse error: unexpected key */);
                }
            }
        } catch (final IOException e) {
            final var x = new MovidaFileException();
            x.initCause(x);
            throw x;
        }

        if (!movieData.empty()) {
            this.loadMovie(movieData);
        }
    }

    @Override
    public void saveToFile(File f) {
        try {
            final var writer = new BufferedWriter(new FileWriter(f));
            final var iterator = this.movies.stream().iterator();

            while (iterator.hasNext()) {
                final var entry = iterator.next();
                saveMovieToFile(writer, entry.value);
            }

            writer.flush();
        } catch (final IOException e) {
            final var x = new MovidaFileException();
            x.initCause(x);
            throw x;
        }
    }

    @Override
    public void clear() {
        this.people.clear();
        this.movies.clear();
    }

    @Override
    public int countMovies() {
        return this.movies.size();
    }

    @Override
    public int countPeople() {
        return this.people.size();
    }

    @Override
    public boolean deleteMovieByTitle(final String title) {
        final var movie = this.movies.del(title.toLowerCase());

        if (null != movie) {
            this.moviesByDirector.get(movie.getDirector().getName()).remove(movie);
            this.moviesByYear.get(movie.getYear()).remove(movie);

            for (final var actor : movie.getCast()) {
                this.moviesByActor.get(actor.getName()).remove(movie);
            }

            return true;
        }

        return false;
    }

    @Override
    public Movie getMovieByTitle(final String title) {
        return this.movies.get(title.toLowerCase());
    }

    @Override
    public Person getPersonByName(final String name) {
        return this.people.get(name.toLowerCase());
    }

    @Override
    public Movie[] getAllMovies() {
        return this.movies.stream().map(entry -> entry.value).toArray(Movie[]::new);
    }

    @Override
    public Person[] getAllPeople() {
        return this.people.stream().map(entry -> entry.value).toArray(Person[]::new);
    }
}
