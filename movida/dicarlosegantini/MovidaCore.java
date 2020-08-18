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
import movida.dicarlosegantini.array.DynamicArray;
import movida.dicarlosegantini.map.ArrayOrdinato;
import movida.dicarlosegantini.map.HashIndirizzamentoAperto;
import movida.dicarlosegantini.map.IMap;
import movida.dicarlosegantini.sort.ISort;
import movida.dicarlosegantini.sort.QuickSort;
import movida.dicarlosegantini.sort.SelectionSort;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MovidaCore implements IMovidaConfig, IMovidaDB {
    final private DynamicArray<Person> actorsOrderedByActivity;
    final private DynamicArray<Movie> moviesOrderedByVotes;
    final private DynamicArray<Movie> moviesOrderedByYear;
    private MapImplementation mapImplementation;
    private IMap<String, HashSet<Movie>> moviesByDirector;
    private IMap<String, HashSet<Movie>> moviesByActor;
    private IMap<Integer, HashSet<Movie>> moviesByYear;
    private IMap<String, Person> directors;
    private IMap<String, Person> actors;
    private IMap<String, Movie> movies;
    private ISort sortingAlgorithm;

    private MovidaCore() {
        this.actorsOrderedByActivity = new DynamicArray<>();
        this.moviesOrderedByVotes = new DynamicArray<>();
        this.moviesOrderedByYear = new DynamicArray<>();

        this.mapImplementation = MapImplementation.HashIndirizzamentoAperto;

        this.moviesByDirector = new HashIndirizzamentoAperto<>();
        this.moviesByActor = new HashIndirizzamentoAperto<>();
        this.moviesByYear = new HashIndirizzamentoAperto<>();
        this.directors = new HashIndirizzamentoAperto<>();
        this.actors = new HashIndirizzamentoAperto<>();
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

    private void loadMovie(final IMap<String, String> movieData) {
        final var title = movieData.get("title");
        final var year = Integer.parseInt(movieData.get("year"));
        final var votes = Integer.parseInt(movieData.get("votes"));
        final var director = new Person(movieData.get("director"));
        final var cast = (Person[]) Arrays.stream(movieData.get("cast").split("[\\W]*,[\\W]*"))
                .map(Person::new)
                .toArray(Person[]::new);

        final var movie = new Movie(title, year, votes, cast, director);

        this.moviesOrderedByVotes.append(movie);
        this.moviesOrderedByYear.append(movie);

        this.moviesByDirector.getOrAdd(director.getName(), HashSet::new).add(movie);
        this.moviesByYear.getOrAdd(year, HashSet::new).add(movie);

        for (final var actor : movie.getCast()) {
            this.moviesByActor.getOrAdd(actor.getName(), HashSet::new).add(movie);
            this.actors.add(actor.getName(), actor);
        }

        this.directors.add(movie.getDirector().getName(), movie.getDirector());
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
                    this.directors = ArrayOrdinato.from(this.directors);
                    this.actors = ArrayOrdinato.from(this.actors);
                    this.movies = ArrayOrdinato.from(this.movies);
                    break;
                case HashIndirizzamentoAperto:
                    this.moviesByDirector = HashIndirizzamentoAperto.from(this.moviesByDirector);
                    this.moviesByActor = HashIndirizzamentoAperto.from(this.moviesByActor);
                    this.moviesByYear = HashIndirizzamentoAperto.from(this.moviesByYear);
                    this.directors = HashIndirizzamentoAperto.from(this.directors);
                    this.actors = HashIndirizzamentoAperto.from(this.actors);
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
        final var movieData = new HashIndirizzamentoAperto<String, String>();
        movieData.reserve(5);

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

        this.moviesOrderedByVotes.sort(this.sortingAlgorithm, Comparator.comparing(Movie::getVotes).reversed());
        this.moviesOrderedByYear.sort(this.sortingAlgorithm, Comparator.comparing(Movie::getYear).reversed());

        this.actorsOrderedByActivity.clear();
        this.actors.values().forEach(this.actorsOrderedByActivity::append);
        this.actorsOrderedByActivity.sort(this.sortingAlgorithm, (a, b) -> {
            final Integer aActivity = this.moviesByActor.get(a.getName()).size();
            final Integer bActivity = this.moviesByActor.get(b.getName()).size();

            return -(aActivity.compareTo(bActivity));
        });
    }

    @Override
    public void saveToFile(File f) {
        try {
            final var writer = new BufferedWriter(new FileWriter(f));
            final var iterator = this.movies.values().iterator();

            while (iterator.hasNext()) {
                saveMovieToFile(writer, iterator.next());
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
        this.actorsOrderedByActivity.clear();
        this.moviesOrderedByVotes.clear();
        this.moviesOrderedByYear.clear();
        this.moviesByDirector.clear();
        this.moviesByActor.clear();
        this.moviesByYear.clear();
        this.directors.clear();
        this.actors.clear();
        this.movies.clear();
    }

    @Override
    public int countMovies() {
        return this.movies.size();
    }

    @Override
    public int countPeople() {
        return this.directors.size() + this.actors.size();
    }

    @Override
    public boolean deleteMovieByTitle(final String title) {
        final var movie = this.movies.del(title.toLowerCase());

        if (null != movie) {
            this.moviesOrderedByVotes.binaryRemove(movie, Comparator.comparing(Movie::getVotes).reversed());
            this.moviesOrderedByYear.binaryRemove(movie, Comparator.comparing(Movie::getYear).reversed());

            this.moviesByDirector.get(movie.getDirector().getName()).remove(movie);
            this.moviesByYear.get(movie.getYear()).remove(movie);

            boolean actorsOrderedByActivityNeedsToBeSorted = false;
            for (final var actor : movie.getCast()) {
                final var moviesPerActor = this.moviesByActor.get(actor.getName());
                moviesPerActor.remove(movie);

                if (moviesPerActor.isEmpty()) {
                    this.actorsOrderedByActivity.binaryRemove(actor, Comparator.comparing(Person::getName));
                } else {
                    actorsOrderedByActivityNeedsToBeSorted = true;
                }
            }

            if (actorsOrderedByActivityNeedsToBeSorted) {
                this.actorsOrderedByActivity.sort(this.sortingAlgorithm, (a, b) -> {
                    final Integer aActivity = this.moviesByActor.get(a.getName()).size();
                    final Integer bActivity = this.moviesByActor.get(b.getName()).size();

                    return -(aActivity.compareTo(bActivity));
                });
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
        final var personName = name.toLowerCase();
        final var actor = this.actors.get(personName);

        return (null != actor) ? actor : this.directors.get(personName);
    }

    @Override
    public Movie[] getAllMovies() {
        return this.movies.values().toArray(Movie[]::new);
    }

    @Override
    public Person[] getAllPeople() {
        return Stream.concat(this.actors.values(), this.directors.values()).toArray(Person[]::new);
    }
}
