/*
 * Copyright (C) 2020 - Angelo Di Iorio
 *
 * Progetto Movida.
 * Corso di Algoritmi e Strutture Dati
 * Laurea in Informatica, UniBO, a.a. 2019/2020
 *
 */

package movida.commons;

/**
 * Classe usata per rappresentare un film
 * nell'applicazione Movida.
 * <p>
 * Un film e' identificato in modo univoco dal titolo
 * case-insensitive, senza spazi iniziali e finali, senza spazi doppi.
 * <p>
 * La classe puo' essere modicata o estesa ma deve implementare tutti i metodi getter
 * per recupare le informazioni caratterizzanti di un film.
 */
public class Movie {
    private final String title;
    private final Integer year;
    private final Integer votes;
    private final Person[] cast;
    private final Person director;

    public Movie(final String title, final Integer year, final Integer votes,
                 final Person[] cast, final Person director) {
        this.title = title;
        this.year = year;
        this.votes = votes;
        this.cast = cast;
        this.director = director;
    }

    public String getTitle() {
        return this.title;
    }

    public Integer getYear() {
        return this.year;
    }

    public Integer getVotes() {
        return this.votes;
    }

    public Person[] getCast() {
        return this.cast;
    }

    public Person getDirector() {
        return this.director;
    }

    @Override
    public int hashCode() {
        return this.title.toLowerCase().hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }

        if (object instanceof Movie) {
            final var other = (Movie) object;
            return 0 == this.title.compareToIgnoreCase(other.title);
        }

        return false;
    }

    @Override
    public String toString() {
        return "Movie(\"" + this.title + "\")";
    }
}
