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
import movida.commons.IMovidaCollaborations;
import movida.commons.Movie;
import movida.commons.Person;
import movida.dicarlosegantini.map.HashIndirizzamentoAperto;
import movida.dicarlosegantini.set.HashSet;

public final class MovidaCollaborations implements IMovidaCollaborations {
    private final HashSet<Collaboration> collaborations;
    private final HashIndirizzamentoAperto<Person, HashSet<Person>> graph;

    public MovidaCollaborations() {
        this.collaborations = new HashSet<>();
        this.graph = new HashIndirizzamentoAperto<>();
    }

    public void addCollaboration(final Movie movie, final Person actorA, final Person actorB) {
        final var collaboration = this.collaborations.getOrAdd(new Collaboration(actorA, actorB));
        collaboration.addMovie(movie);

        this.graph.getOrAdd(actorA, HashSet::new).add(actorB);
        this.graph.getOrAdd(actorB, HashSet::new).add(actorA);
    }

    public void removeCollaboration(final Movie movie, final Person actorA, final Person actorB) {
        final var collaboration = this.collaborations.get(new Collaboration(actorA, actorB));
        assert null != collaboration;

        final var actorACollaborators = this.graph.get(collaboration.getActorA());
        assert null != actorACollaborators;
        actorACollaborators.remove(actorB);
        if (actorACollaborators.isEmpty()) {
            this.graph.remove(collaboration.getActorA());
        }

        final var actorBCollaborators = this.graph.get(collaboration.getActorB());
        assert null != actorBCollaborators;
        actorBCollaborators.remove(actorA);
        if (actorBCollaborators.isEmpty()) {
            this.graph.remove(collaboration.getActorB());
        }

        collaboration.removeMovie(movie);
        if (0 == collaboration.countMovies()) {
            this.collaborations.remove(collaboration);
        }
    }

    public void clear() {
        this.collaborations.clear();
        this.graph.clear();
    }

    @Override
    public Person[] getDirectCollaboratorsOf(final Person actor) {
        final var collaborators = this.graph.get(actor);
        return (null != collaborators) ? collaborators.stream().toArray(Person[]::new) : null;
    }

    @Override
    public Person[] getTeamOf(final Person actor) {
        return new Person[0];
    }

    @Override
    public Collaboration[] maximizeCollaborationsInTheTeamOf(final Person actor) {
        return new Collaboration[0];
    }
}
