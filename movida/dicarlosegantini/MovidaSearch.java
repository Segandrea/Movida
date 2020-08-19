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
import movida.dicarlosegantini.array.DynamicArray;
import movida.dicarlosegantini.map.ArrayOrdinato;
import movida.dicarlosegantini.map.HashIndirizzamentoAperto;
import movida.dicarlosegantini.map.IMap;
import movida.dicarlosegantini.set.HashSet;
import movida.dicarlosegantini.sort.ISort;
import movida.dicarlosegantini.sort.QuickSort;
import movida.dicarlosegantini.sort.SelectionSort;

import java.util.Comparator;

public final class MovidaSearch implements IConfigurableSearch {
    final private DynamicArray<Person> actorsOrderedByActivity;
    final private DynamicArray<Movie> moviesOrderedByVotes;
    final private DynamicArray<Movie> moviesOrderedByYear;
    private MapImplementation mapImplementation;
    private IMap<String, HashSet<Movie>> moviesByDirector;
    private IMap<String, HashSet<Movie>> moviesByActor;
    private IMap<Integer, HashSet<Movie>> moviesByYear;
    private ISort sortingAlgorithm;

    public MovidaSearch() {
        this.actorsOrderedByActivity = new DynamicArray<>();
        this.moviesOrderedByVotes = new DynamicArray<>();
        this.moviesOrderedByYear = new DynamicArray<>();

        this.mapImplementation = MapImplementation.HashIndirizzamentoAperto;
        this.moviesByDirector = new HashIndirizzamentoAperto<>();
        this.moviesByActor = new HashIndirizzamentoAperto<>();
        this.moviesByYear = new HashIndirizzamentoAperto<>();

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
    public void load(final Movie movie) {
        this.moviesByDirector.getOrAdd(movie.getDirector().getName().toLowerCase(), HashSet::new).add(movie);
        this.moviesByYear.getOrAdd(movie.getYear(), HashSet::new).add(movie);
        for (final var actor : movie.getCast()) {
            this.moviesByActor.getOrAdd(actor.getName().toLowerCase(), HashSet::new).add(movie);
        }

        this.moviesOrderedByVotes.append(movie);
        this.moviesOrderedByYear.append(movie);

        // TODO: sortUnique -> sort then unique
        for (final var actor : movie.getCast()) {
            this.actorsOrderedByActivity.binaryInsert(actor, (a, b) -> {
                final Integer aActivity = this.moviesByActor.get(a.getName()).size();
                final Integer bActivity = this.moviesByActor.get(b.getName()).size();

                return -(aActivity.compareTo(bActivity));
            });
        }
    }

    @Override
    public void finalizeLoad() {
        this.moviesOrderedByVotes.sort(this.sortingAlgorithm, Comparator.comparing(Movie::getVotes).reversed());
        this.moviesOrderedByYear.sort(this.sortingAlgorithm, Comparator.comparing(Movie::getYear).reversed());
    }

    @Override
    public void clear() {
        this.actorsOrderedByActivity.clear();
        this.moviesOrderedByVotes.clear();
        this.moviesOrderedByYear.clear();
        this.moviesByDirector.clear();
        this.moviesByActor.clear();
        this.moviesByYear.clear();
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
                    break;
                case HashIndirizzamentoAperto:
                    this.moviesByDirector = HashIndirizzamentoAperto.from(this.moviesByDirector);
                    this.moviesByActor = HashIndirizzamentoAperto.from(this.moviesByActor);
                    this.moviesByYear = HashIndirizzamentoAperto.from(this.moviesByYear);
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
        return this.moviesByYear.get(year).stream().toArray(Movie[]::new);
    }

    @Override
    public Movie[] searchMoviesDirectedBy(final String name) {
        return this.moviesByDirector.get(name.toLowerCase()).stream().toArray(Movie[]::new);
    }

    @Override
    public Movie[] searchMoviesStarredBy(final String name) {
        return this.moviesByActor.get(name.toLowerCase()).stream().toArray(Movie[]::new);
    }

    @Override
    public Movie[] searchMostVotedMovies(final Integer N) {
        return this.moviesOrderedByVotes.slice(Movie[]::new, 0, Math.max(N, this.moviesOrderedByVotes.size()));
    }

    @Override
    public Movie[] searchMostRecentMovies(final Integer N) {
        return this.moviesOrderedByYear.slice(Movie[]::new, 0, Math.max(N, this.moviesOrderedByYear.size()));
    }

    @Override
    public Person[] searchMostActiveActors(final Integer N) {
        return this.actorsOrderedByActivity
                .slice(Person[]::new, 0, Math.max(N, this.actorsOrderedByActivity.size()));
    }
}
