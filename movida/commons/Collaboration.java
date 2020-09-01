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

    public void addMovie(final Movie movie) {
        this.movies.add(movie);
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

    public Integer countMovies() {
        return this.movies.size();
    }

    public void removeMovie(final Movie movie) {
        this.movies.del(movie);
    }
}
