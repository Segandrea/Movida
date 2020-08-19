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

import java.io.File;
import java.util.stream.Stream;

public final class MovidaCore implements IMovidaConfig, IMovidaDB, IMovidaSearch {
    private final IPersistence persistenceDelegate;
    private final IConfigurableDB dbDelegate;
    private final IConfigurableSearch searchDelegate;

    public MovidaCore(final IPersistence persistenceDelegate, final IConfigurableDB dbDelegate,
                      final IConfigurableSearch searchDelegate) {
        this.persistenceDelegate = persistenceDelegate;
        this.dbDelegate = dbDelegate;
        this.searchDelegate = searchDelegate;
    }

    @Override
    public boolean setSort(final SortingAlgorithm a) {
        final var x = this.searchDelegate.setSort(a);
        final var y = this.dbDelegate.setSort(a);
        return x || y;
    }

    @Override
    public boolean setMap(final MapImplementation m) {
        final var x = this.searchDelegate.setMap(m);
        final var y = this.dbDelegate.setMap(m);
        return x || y;
    }

    @Override
    public void loadFromFile(final File f) {
        this.persistenceDelegate.load(f, m -> {
            this.dbDelegate.load(m);
            this.searchDelegate.load(m);
        });
        this.searchDelegate.finalizeLoad();
    }

    @Override
    public void saveToFile(final File f) {
        this.persistenceDelegate.store(f, this.dbDelegate.streamMovies());
    }

    @Override
    public void clear() {
        this.dbDelegate.clear();
        this.searchDelegate.clear();
    }

    @Override
    public int countMovies() {
        return this.dbDelegate.countMovies();
    }

    @Override
    public int countPeople() {
        return this.dbDelegate.countActors() + this.dbDelegate.countDirectors();
    }

    @Override
    public boolean deleteMovieByTitle(final String title) {
        return this.dbDelegate.deleteMovieByTitle(title);
    }

    @Override
    public Movie getMovieByTitle(final String title) {
        return this.dbDelegate.getMovieByTitle(title);
    }

    @Override
    public Person getPersonByName(final String name) {
        final var actor = this.dbDelegate.getActorByName(name);
        return (null != actor) ? actor : this.dbDelegate.getDirectorByName(name);
    }

    @Override
    public Movie[] getAllMovies() {
        return this.dbDelegate.getAllMovies();
    }

    @Override
    public Person[] getAllPeople() {
        return Stream.concat(this.dbDelegate.streamActors(), this.dbDelegate.streamDirectors()).toArray(Person[]::new);
    }

    @Override
    public Movie[] searchMoviesByTitle(final String title) {
        return this.searchDelegate.searchMoviesByTitle(title);
    }

    @Override
    public Movie[] searchMoviesInYear(final Integer year) {
        return this.searchDelegate.searchMoviesInYear(year);
    }

    @Override
    public Movie[] searchMoviesDirectedBy(final String name) {
        return this.searchDelegate.searchMoviesDirectedBy(name);
    }

    @Override
    public Movie[] searchMoviesStarredBy(final String name) {
        return this.searchDelegate.searchMoviesStarredBy(name);
    }

    @Override
    public Movie[] searchMostVotedMovies(final Integer N) {
        return this.searchDelegate.searchMostVotedMovies(N);
    }

    @Override
    public Movie[] searchMostRecentMovies(final Integer N) {
        return this.searchDelegate.searchMostRecentMovies(N);
    }

    @Override
    public Person[] searchMostActiveActors(final Integer N) {
        return this.searchDelegate.searchMostActiveActors(N);
    }
}
