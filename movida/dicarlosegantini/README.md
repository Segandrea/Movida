# MOVIDA

Movida is a project with educational purposes, developed during the course of "Algoritmi e Strutture dati" at Alma Mater Studiorum University of Bologna, whose main target is the study of the applications of different algorithms and data structures.

##### Authors

Davide Di Carlo, Andrea Segantini.

## Design notes

**Algorithms:**

- BinarySearch: To perform a binary search in an ordered array.
- QuickSort: To sort an array using quick sort algorithm.
- SelectionSort: To sort an array using selection sort algorithm.

We defined the ISort interface in order to abstract the actual sorting algorithm used.

**Data structures:**

- Entry: A key-value pair.
- DynamicArray: A simple resizable array implementation.
- HashSet: Set of items implemented using hashing and linear probing.
- HashIndirizzamentoAperto: A map implemented using hashing and linear probing.
- ArrayOrdinato: A map implemented using a sorted array.

We defined the IMap interface in order to abstract the actual data structure used.

#### Project Structure

To reduce the complexity of MovidaCore class, we defined MovidaPersistence and MovidaCollaborations classes to which MovidaCore delegates the following operations:

**MovidaPersistence:** Responsible for the file-related operations (load/save from/to file).
**MovidaCollaborations:** Implementation of IMovidaCollaborations.
