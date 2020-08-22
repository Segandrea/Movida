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
import movida.dicarlosegantini.array.DynamicArray;
import movida.dicarlosegantini.map.ArrayOrdinato;
import movida.dicarlosegantini.map.HashIndirizzamentoAperto;
import movida.dicarlosegantini.map.IMap;
import movida.dicarlosegantini.sort.ISort;
import movida.dicarlosegantini.sort.QuickSort;
import movida.dicarlosegantini.sort.SelectionSort;

import java.io.File;
import java.util.Comparator;
import java.util.stream.Stream;

public final class MovidaCore implements IMovidaConfig, IMovidaDB, IMovidaSearch {
    final private IPersistence persistence;

    final private DynamicArray<Movie> moviesOrderedByVotes;
    final private DynamicArray<Movie> moviesOrderedByYear;
    final private DynamicArray<Person> actorsOrderedByActivity;

    private IMap<String, DynamicArray<Movie>> moviesByDirector;
    private IMap<String, DynamicArray<Movie>> moviesByActor;
    private IMap<Integer, DynamicArray<Movie>> moviesByYear;

    private IMap<String, Person> directors;
    private IMap<String, Person> actors;
    private IMap<String, Movie> movies;

    private MapImplementation mapImplementation;
    private ISort sortingAlgorithm;

    public MovidaCore(final IPersistence persistence) {
        this.persistence = persistence;

        this.moviesOrderedByVotes = new DynamicArray<>();
        this.moviesOrderedByYear = new DynamicArray<>();
        this.actorsOrderedByActivity = new DynamicArray<>();

        this.moviesByDirector = new HashIndirizzamentoAperto<>();
        this.moviesByActor = new HashIndirizzamentoAperto<>();
        this.moviesByYear = new HashIndirizzamentoAperto<>();

        this.directors = new HashIndirizzamentoAperto<>();
        this.actors = new HashIndirizzamentoAperto<>();
        this.movies = new HashIndirizzamentoAperto<>();

        this.mapImplementation = MapImplementation.HashIndirizzamentoAperto;
        this.sortingAlgorithm = QuickSort.getInstance();
    }

    static private ISort instanceSortingAlgorithm(final SortingAlgorithm sortingAlgorithm) {
        switch (sortingAlgorithm) {
            case SelectionSort:
                return SelectionSort.getInstance();

            case QuickSort:
                return QuickSort.getInstance();

            default:
                return null;
        }
    }

    @Override
    public boolean setSort(final SortingAlgorithm sortingAlgorithm) {
        if (instanceSortingAlgorithm(sortingAlgorithm) != this.sortingAlgorithm) {
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

        return false;
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
            return true;
        }

        return false;
    }

    protected void load(final Movie movie) {
        final var directorName = movie.getDirector().getName().toLowerCase();

        this.moviesByDirector.getOrAdd(directorName, DynamicArray::new).append(movie);
        this.moviesByYear.getOrAdd(movie.getYear(), DynamicArray::new).append(movie);

        for (final var actor : movie.getCast()) {
            final var actorName = actor.getName().toLowerCase();
            this.moviesByActor.getOrAdd(actorName, DynamicArray::new).append(movie);
            this.actors.add(actorName, actor);
        }

        this.moviesOrderedByVotes.append(movie);
        this.moviesOrderedByYear.append(movie);

        this.directors.add(directorName, movie.getDirector());
        this.movies.add(movie.getTitle().toLowerCase(), movie);
    }

    protected void finalizeLoad() {
        this.moviesOrderedByVotes.sort(this.sortingAlgorithm, (x, y) -> {
            final var votesCmp = -(x.getVotes().compareTo(y.getVotes()));
            return (0 == votesCmp) ? x.getTitle().compareToIgnoreCase(y.getTitle()) : votesCmp;
        });

        this.moviesOrderedByYear.sort(this.sortingAlgorithm, (x, y) -> {
            final var yearCmp = -(x.getYear().compareTo(y.getYear()));
            return (0 == yearCmp) ? x.getTitle().compareToIgnoreCase(y.getTitle()) : yearCmp;
        });

        this.moviesByDirector
                .values()
                .forEach(m -> m.sort(this.sortingAlgorithm, Comparator.comparing(Movie::getTitle)));

        this.moviesByActor
                .values()
                .forEach(m -> m.sort(this.sortingAlgorithm, Comparator.comparing(Movie::getTitle)));

        this.moviesByYear
                .values()
                .forEach(m -> m.sort(this.sortingAlgorithm, Comparator.comparing(Movie::getTitle)));

        this.actorsOrderedByActivity.clear();
        this.streamActors().forEach(this.actorsOrderedByActivity::append);
        this.actorsOrderedByActivity.sort(this.sortingAlgorithm, (a, b) -> {
            final var aName = a.getName().toLowerCase();
            final var bName = b.getName().toLowerCase();
            final Integer aActivity = this.moviesByActor.get(aName).size();
            final Integer bActivity = this.moviesByActor.get(bName).size();
            final var cmp = -(aActivity.compareTo(bActivity));

            return (0 == cmp) ? aName.compareTo(bName) : cmp;
        });
    }

    @Override
    public void loadFromFile(final File f) {
        this.persistence.load(f, this::load);
        this.finalizeLoad();
    }

    public Stream<Person> streamActors() {
        return this.actors.values();
    }

    public Stream<Person> streamDirectors() {
        return this.directors.values();
    }

    public Stream<Movie> streamMovies() {
        return this.movies.values();
    }

    public void saveToFile(final File f) {
        this.persistence.store(f, this.streamMovies());
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

    public int countActors() {
        return this.actors.size();
    }

    public int countDirectors() {
        return this.directors.size();
    }

    @Override
    public int countPeople() {
        return this.countActors() + this.countDirectors();
    }

    @Override
    public boolean deleteMovieByTitle(final String title) {
        final var lowerTitle = title.toLowerCase();
        final var movie = this.movies.del(lowerTitle);

        if (null != movie) {
            final var directorName = movie.getDirector().getName().toLowerCase();
            final var moviesByDirector = this.moviesByDirector.get(directorName);
            moviesByDirector.binaryRemove(movie, Comparator.comparing(Movie::getTitle));
            if (moviesByDirector.empty()) {
                this.moviesByDirector.del(directorName);
                this.directors.del(directorName);
            }

            final var year = movie.getYear();
            final var moviesByYear = this.moviesByYear.get(year);
            moviesByYear.binaryRemove(movie, Comparator.comparing(Movie::getTitle));
            if (moviesByYear.empty()) {
                this.moviesByYear.del(year);
            }
            this.moviesOrderedByYear.binaryRemove(movie, (x, y) -> {
                final var yearCmp = -(x.getYear().compareTo(y.getYear()));
                return (0 == yearCmp) ? x.getTitle().compareToIgnoreCase(y.getTitle()) : yearCmp;
            });

            this.moviesOrderedByVotes.binaryRemove(movie, (x, y) -> {
                final var votesCmp = -(x.getVotes().compareTo(y.getVotes()));
                return (0 == votesCmp) ? x.getVotes().compareTo(y.getVotes()) : votesCmp;
            });

            for (final var actor : movie.getCast()) {
                final var actorName = actor.getName().toLowerCase();
                final var moviesByActor = this.moviesByActor.get(actorName);
                moviesByActor.binaryRemove(movie, Comparator.comparing(Movie::getTitle));
                if (moviesByActor.empty()) {
                    this.actors.del(actorName);
                    this.moviesByActor.del(actorName);
                }
            }

            this.actorsOrderedByActivity.clear();
            this.streamActors().forEach(this.actorsOrderedByActivity::append);
            this.actorsOrderedByActivity.sort(this.sortingAlgorithm, (a, b) -> {
                final var aName = a.getName().toLowerCase();
                final var bName = b.getName().toLowerCase();
                final Integer aActivity = this.moviesByActor.get(aName).size();
                final Integer bActivity = this.moviesByActor.get(bName).size();
                final var cmp = -(aActivity.compareTo(bActivity));

                return (0 == cmp) ? aName.compareTo(bName) : cmp;
            });

            return true;
        }

        return false;
    }

    @Override
    public Movie getMovieByTitle(final String title) {
        return this.movies.get(title.toLowerCase());
    }

    public Person getActorByName(final String name) {
        return this.actors.get(name.toLowerCase());
    }

    public Person getDirectorByName(final String name) {
        return this.directors.get(name.toLowerCase());
    }

    @Override
    public Person getPersonByName(final String name) {
        final var actor = this.getActorByName(name);
        return (null != actor) ? actor : this.getDirectorByName(name);
    }

    @Override
    public Movie[] getAllMovies() {
        return this.movies.values().toArray(Movie[]::new);
    }

    @Override
    public Person[] getAllPeople() {
        return Stream.concat(this.streamActors(), this.streamDirectors()).toArray(Person[]::new);
    }

    @Override
    public Movie[] searchMoviesByTitle(final String title) {
        final var lowerCaseTitle = title.toLowerCase();
        return this.moviesOrderedByYear
                .stream()
                .parallel()
                .filter(m -> m.getTitle().toLowerCase().contains(lowerCaseTitle))
                .toArray(Movie[]::new);
    }

    @Override
    public Movie[] searchMoviesInYear(final Integer year) {
        return this.moviesByYear.getOrDefault(year, DynamicArray::new).stream().toArray(Movie[]::new);
    }

    @Override
    public Movie[] searchMoviesDirectedBy(final String name) {
        return this.moviesByDirector.getOrDefault(name.toLowerCase(), DynamicArray::new).stream().toArray(Movie[]::new);
    }

    @Override
    public Movie[] searchMoviesStarredBy(final String name) {
        return this.moviesByActor.getOrDefault(name.toLowerCase(), DynamicArray::new).stream().toArray(Movie[]::new);
    }

    @Override
    public Movie[] searchMostVotedMovies(final Integer N) {
        return this.moviesOrderedByVotes.slice(Movie[]::new, 0, Math.min(N, this.moviesOrderedByVotes.size()));
    }

    @Override
    public Movie[] searchMostRecentMovies(final Integer N) {
        return this.moviesOrderedByYear.slice(Movie[]::new, 0, Math.min(N, this.moviesOrderedByYear.size()));
    }

    @Override
    public Person[] searchMostActiveActors(final Integer N) {
        return this.actorsOrderedByActivity
                .slice(Person[]::new, 0, Math.min(N, this.actorsOrderedByActivity.size()));
    }
}
