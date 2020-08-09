package movida.dicarlosegantini.map;

public class Entry<K, V> {
    public final K key;
    public final V value;

    public Entry(final K key, final V value) {
        this.key = key;
        this.value = value;
    }
}
