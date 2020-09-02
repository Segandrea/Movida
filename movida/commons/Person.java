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
 * Classe usata per rappresentare una persona, attore o regista,
 * nell'applicazione Movida.
 * <p>
 * Una persona e' identificata in modo univoco dal nome
 * case-insensitive, senza spazi iniziali e finali, senza spazi doppi.
 * <p>
 * Semplificazione: <code>name</code> e' usato per memorizzare il nome completo (nome e cognome)
 * <p>
 * La classe puo' essere modicata o estesa ma deve implementare il metodo getName().
 */
public class Person {
    final private String name;

    public Person(final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public int hashCode() {
        return this.name.toLowerCase().hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }

        if (object instanceof Person) {
            final var other = (Person) object;
            return 0 == this.name.compareToIgnoreCase(other.name);
        }

        return false;
    }

    @Override
    public String toString() {
        return "Person(\"" + this.name + "\")";
    }
}
