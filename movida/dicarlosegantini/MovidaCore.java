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
import movida.dicarlosegantini.set.HashSet;
import movida.dicarlosegantini.sort.ISort;
import movida.dicarlosegantini.sort.QuickSort;
import movida.dicarlosegantini.sort.SelectionSort;

import java.io.File;
import java.util.Comparator;
import java.util.stream.Stream;

public final class MovidaCore implements IMovidaConfig, IMovidaDB, IMovidaSearch {
    private static final Comparator<Movie> orderByTitle = (x, y) -> x.getTitle().compareToIgnoreCase(y.getTitle());
    private static final Comparator<Movie> orderByVotes =
            Comparator.comparing(Movie::getVotes).reversed().thenComparing(orderByTitle);
    private static final Comparator<Movie> orderByYear =
            Comparator.comparing(Movie::getYear).reversed().thenComparing(orderByTitle);
    // Hash of the actors in a collaboration combined with xor
    private static final Hasher<Collaboration> collaborationHasher =
            c -> c.getActorA().hashCode() ^ c.getActorB().hashCode();
    // Order of the actors in a collaboration is irrelevant
    private static final Eq<Collaboration> collaborationEquals =
            (c1, c2) -> {
                if (c1 == c2) {
                    return true;
                }
                if (c1.getActorA() == c2.getActorA() && c1.getActorB() == c2.getActorB()) {
                    return true;
                }
                return c1.getActorB() == c2.getActorA() && c1.getActorA() == c2.getActorB();
            };

    private final IPersistence persistence;

    private final HashSet<Collaboration> collaborations;
    private final HashIndirizzamentoAperto<Person, HashSet<Collaboration>> collaborationGraph;

    private final DynamicArray<Person> actorsOrderedByActivity;
    private final DynamicArray<Movie> moviesOrderedByVotes;
    private final DynamicArray<Movie> moviesOrderedByYear;
    private IMap<String, DynamicArray<Movie>> moviesByDirector;
    private IMap<String, DynamicArray<Movie>> moviesByActor;
    private IMap<Integer, DynamicArray<Movie>> moviesByYear;
    private IMap<String, Person> directors;
    private IMap<String, Person> actors;
    private IMap<String, Movie> movies;

    private ISort sortingAlgorithm;
    private MapImplementation mapImplementation;

    public MovidaCore() {
        this(new MovidaPersistence());
    }

    public MovidaCore(final IPersistence persistence) {
        this.persistence = persistence;

        this.collaborations = new HashSet<>(collaborationHasher, collaborationEquals);
        this.collaborationGraph = new HashIndirizzamentoAperto<>();

        this.actorsOrderedByActivity = new DynamicArray<>();
        this.moviesOrderedByVotes = new DynamicArray<>();
        this.moviesOrderedByYear = new DynamicArray<>();

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
        }

        return null;
    }

    private void deleteMovieDirectedBy(final Movie movie, final Person director) {
        final var directorName = director.getName().toLowerCase();
        final var moviesByDirector = this.moviesByDirector.get(directorName);

        moviesByDirector.binaryRemove(movie, orderByTitle);
        if (moviesByDirector.empty()) {
            this.moviesByDirector.del(directorName);
            this.directors.del(directorName);
        }
    }

    private void deleteMovieStarredBy(final Movie movie, final Person actor) {
        final var actorName = actor.getName().toLowerCase();
        final var moviesByActor = this.moviesByActor.get(actorName);

        moviesByActor.binaryRemove(movie, orderByTitle);
        if (moviesByActor.empty()) {
            this.moviesByActor.del(actorName);
            this.actors.del(actorName);
        }
    }

    private void deleteMovieInYear(final Movie movie, final int year) {
        final var moviesByYear = this.moviesByYear.get(year);

        moviesByYear.binaryRemove(movie, orderByTitle);
        if (moviesByYear.empty()) {
            this.moviesByYear.del(year);
        }

        this.moviesOrderedByYear.binaryRemove(movie, orderByYear);
    }

    protected void load(final Movie movie) {
        final var directorName = movie.getDirector().getName().toLowerCase();

        this.moviesOrderedByVotes.append(movie);
        this.moviesOrderedByYear.append(movie);
        this.moviesByDirector.getOrAdd(directorName, DynamicArray::new).append(movie);
        this.moviesByYear.getOrAdd(movie.getYear(), DynamicArray::new).append(movie);

        final var cast = movie.getCast();
        for (int i = 0; cast.length > i; ++i) {
            final var actor = cast[i];
            final var actorName = actor.getName().toLowerCase();
            this.moviesByActor.getOrAdd(actorName, DynamicArray::new).append(movie);
            this.actors.add(actorName, actor);

            for (int j = i + 1; cast.length > j; ++j) {
                final var collaboration = this.collaborations.getOrAdd(new Collaboration(actor, cast[j]));

                collaboration.addMovie(movie);
                this.collaborationGraph
                        .getOrAdd(actor, () -> new HashSet<>(collaborationHasher, collaborationEquals))
                        .add(collaboration);
                this.collaborationGraph
                        .getOrAdd(cast[j], () -> new HashSet<>(collaborationHasher, collaborationEquals))
                        .add(collaboration);
            }
        }

        this.directors.add(directorName, movie.getDirector());
        this.movies.add(movie.getTitle().toLowerCase(), movie);
    }

    private void recomputeActivities() {
        this.actorsOrderedByActivity.clear();
        this.streamActors().forEach(this.actorsOrderedByActivity::append);
        this.actorsOrderedByActivity.sort(this.sortingAlgorithm, (x, y) -> {
            final var xName = x.getName().toLowerCase();
            final var yName = y.getName().toLowerCase();
            final Integer xActivity = this.moviesByActor.get(xName).size();
            final Integer yActivity = this.moviesByActor.get(yName).size();
            final var cmp = -(xActivity.compareTo(yActivity));
            return (0 == cmp) ? xName.compareTo(yName) : cmp;
        });
    }

    protected void finalizeLoad() {
        this.moviesOrderedByVotes.sort(this.sortingAlgorithm, orderByVotes);
        this.moviesOrderedByYear.sort(this.sortingAlgorithm, orderByYear);

        this.moviesByDirector.values().forEach(m -> m.sort(this.sortingAlgorithm, orderByTitle));
        this.moviesByActor.values().forEach(m -> m.sort(this.sortingAlgorithm, orderByTitle));
        this.moviesByYear.values().forEach(m -> m.sort(this.sortingAlgorithm, orderByTitle));

        // activities must be recomputed after any update to actors map
        this.recomputeActivities();
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

    @Override
    public void loadFromFile(final File f) {
        this.persistence.load(f, this::load);
        this.finalizeLoad();
    }

    @Override
    public void saveToFile(final File f) {
        this.persistence.store(f, this.streamMovies());
    }

    @Override
    public void clear() {
        this.collaborations.clear();
        this.collaborationGraph.clear();

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

    public int countDirectors() {
        return this.directors.size();
    }

    public int countActors() {
        return this.actors.size();
    }

    @Override
    public int countPeople() {
        return this.countActors() + this.countDirectors();
    }

    @Override
    public int countMovies() {
        return this.movies.size();
    }

    @Override
    public boolean deleteMovieByTitle(final String title) {
        final var movie = this.movies.del(title.toLowerCase());

        if (null != movie) {
            this.moviesOrderedByVotes.binaryRemove(movie, orderByVotes);
            this.deleteMovieDirectedBy(movie, movie.getDirector());
            this.deleteMovieInYear(movie, movie.getYear());

            final var cast = movie.getCast();
            for (int i = 0; i < cast.length; ++i) {
                this.deleteMovieStarredBy(movie, cast[i]);

                for (int j = i + 1; j < cast.length; ++j) {
                    final var collaboration = this.collaborations.get(new Collaboration(cast[i], cast[j]));
                    assert null != collaboration;

                    //TODO: make a function
                    final var actorACollaborations =
                            this.collaborationGraph.get(collaboration.getActorA());
                    assert null != actorACollaborations;
                    actorACollaborations.del(collaboration);
                    if (actorACollaborations.empty()) {
                        this.collaborationGraph.del(collaboration.getActorA());
                    }

                    //TODO: insert in the function above
                    final var actorBCollaborations =
                            this.collaborationGraph.get(collaboration.getActorB());
                    assert null != actorBCollaborations;
                    actorBCollaborations.del(collaboration);
                    if (actorBCollaborations.empty()) {
                        this.collaborationGraph.del(collaboration.getActorB());
                    }

                    collaboration.removeMovie(movie);
                    if (0 == collaboration.countMovies()) {
                        this.collaborations.del(collaboration);
                    }
                }
            }

            // activities must be recomputed after any update to actors map
            this.recomputeActivities();
            return true;
        }

        return false;
    }

    public Person getDirectorByName(final String name) {
        return this.directors.get(name.toLowerCase());
    }

    public Person getActorByName(final String name) {
        return this.actors.get(name.toLowerCase());
    }

    @Override
    public Person getPersonByName(final String name) {
        final var actor = this.getActorByName(name);
        return (null != actor) ? actor : this.getDirectorByName(name);
    }

    @Override
    public Movie getMovieByTitle(final String title) {
        return this.movies.get(title.toLowerCase());
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
        return this.actorsOrderedByActivity.slice(Person[]::new, 0, Math.min(N, this.actorsOrderedByActivity.size()));
    }

    public Stream<Person> streamDirectors() {
        return this.directors.values();
    }

    public Stream<Person> streamActors() {
        return this.actors.values();
    }

    public Stream<Movie> streamMovies() {
        return this.movies.values();
    }
}
