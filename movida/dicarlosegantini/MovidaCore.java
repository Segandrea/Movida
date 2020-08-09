package movida.dicarlosegantini;


import movida.commons.*;
import movida.dicarlosegantini.map.ArrayOrdinato;
import movida.dicarlosegantini.map.HashIndirizzamentoAperto;
import movida.dicarlosegantini.map.IMap;
import movida.dicarlosegantini.sort.ISort;
import movida.dicarlosegantini.sort.QuickSort;
import movida.dicarlosegantini.sort.SelectionSort;

public class MovidaCore implements IMovidaConfig {
    private final MapImplementation mapImplementation;
    private IMap<String, Person> people;
    private IMap<String, Movie> movies;
    private ISort sort;

    private MovidaCore() {
        this.mapImplementation = MapImplementation.HashIndirizzamentoAperto;
        this.people = new HashIndirizzamentoAperto<>();
        this.movies = new HashIndirizzamentoAperto<>();
        this.sort = new QuickSort();
    }

    @Override
    public boolean setSort(final SortingAlgorithm a) {
        switch (a) {
            case SelectionSort:
                this.sort = new SelectionSort();
                break;
            case QuickSort:
                this.sort = new QuickSort();
                break;
            default:
                return false;
        }

        return true;
    }

    @Override
    public boolean setMap(final MapImplementation m) {
        if (m != this.mapImplementation) {
            switch (m) {
                case ArrayOrdinato:
                    this.people = ArrayOrdinato.from(this.people);
                    this.movies = ArrayOrdinato.from(this.movies);
                    break;
                case HashIndirizzamentoAperto:
                    this.people = HashIndirizzamentoAperto.from(this.people);
                    this.movies = HashIndirizzamentoAperto.from(this.movies);
                    break;
                default:
                    return false;
            }
        }

        return true;
    }
}
