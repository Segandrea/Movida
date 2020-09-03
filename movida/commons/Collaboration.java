package movida.commons;

import movida.dicarlosegantini.set.HashSet;

public class Collaboration {
    private final Person actorA;
    private final Person actorB;
    private final HashSet<Movie> movies;

    public Collaboration(final Person actorA, final Person actorB) {
        this.actorA = actorA;
        this.actorB = actorB;
        this.movies = new HashSet<>();
    }

    @Override
    public int hashCode() {
        // Hash of the actors in a collaboration combined with xor
        return this.actorA.hashCode() ^ this.actorB.hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }

        if (object instanceof Collaboration) {
            final var other = (Collaboration) object;

            // Order of the actors in a collaboration is irrelevant
            return (this.actorA == other.actorA && this.actorB == other.actorB) ||
                    (this.actorB == other.actorA && this.actorA == other.actorB);
        }

        return false;
    }

    @Override
    public String toString() {
        return "Collaboration(" + this.actorA + ", " + this.actorB + ")";
    }

    public void addMovie(final Movie movie) {
        this.movies.add(movie);
    }

    public void removeMovie(final Movie movie) {
        this.movies.del(movie);
    }

    public Integer countMovies() {
        return this.movies.size();
    }

    public Person getActorA() {
        return this.actorA;
    }

    public Person getActorB() {
        return this.actorB;
    }

    public Double getScore() {
        final double score = this.movies.stream().map(Movie::getVotes).reduce(0, Integer::sum);
        return score / this.movies.size();
    }
}
