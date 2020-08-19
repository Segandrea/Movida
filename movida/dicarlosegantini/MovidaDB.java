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
import movida.dicarlosegantini.map.ArrayOrdinato;
import movida.dicarlosegantini.map.HashIndirizzamentoAperto;
import movida.dicarlosegantini.map.IMap;

import java.util.stream.Stream;

public final class MovidaDB implements IConfigurableDB {
    private IMap<String, Person> directors;
    private IMap<String, Person> actors;
    private IMap<String, Movie> movies;
    private MapImplementation mapImplementation;

    public MovidaDB() {
        this.directors = new HashIndirizzamentoAperto<>();
        this.actors = new HashIndirizzamentoAperto<>();
        this.movies = new HashIndirizzamentoAperto<>();
        this.mapImplementation = MapImplementation.HashIndirizzamentoAperto;
    }

    @Override
    public void load(final Movie movie) {
        for (final var actor : movie.getCast()) {
            this.actors.add(actor.getName().toLowerCase(), actor);
        }

        this.directors.add(movie.getDirector().getName().toLowerCase(), movie.getDirector());
        assert null == this.movies.add(movie.getTitle().toLowerCase(), movie);
    }

    @Override
    public void clear() {
        this.directors.clear();
        this.actors.clear();
        this.movies.clear();
    }

    @Override
    public int countMovies() {
        return this.movies.size();
    }

    @Override
    public int countActors() {
        return this.actors.size();
    }

    @Override
    public int countDirectors() {
        return this.directors.size();
    }

    @Override
    public boolean deleteMovieByTitle(final String title) {
        return null != this.movies.del(title.toLowerCase());
    }

    @Override
    public Movie getMovieByTitle(final String title) {
        return this.movies.get(title.toLowerCase());
    }

    @Override
    public Person getActorByName(final String name) {
        return this.actors.get(name.toLowerCase());
    }

    @Override
    public Person getDirectorByName(final String name) {
        return this.directors.get(name.toLowerCase());
    }

    @Override
    public Movie[] getAllMovies() {
        return this.movies.values().toArray(Movie[]::new);
    }

    @Override
    public Stream<Person> streamActors() {
        return this.actors.values();
    }

    @Override
    public Stream<Person> streamDirectors() {
        return this.directors.values();
    }

    @Override
    public Stream<Movie> streamMovies() {
        return this.movies.values();
    }

    @Override
    public boolean setSort(final SortingAlgorithm sortingAlgorithm) {
        return false;
    }

    @Override
    public boolean setMap(final MapImplementation mapImplementation) {
        if (mapImplementation != this.mapImplementation) {
            switch (mapImplementation) {
                case ArrayOrdinato:
                    this.directors = ArrayOrdinato.from(this.directors);
                    this.actors = ArrayOrdinato.from(this.actors);
                    this.movies = ArrayOrdinato.from(this.movies);
                    break;
                case HashIndirizzamentoAperto:
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
}
