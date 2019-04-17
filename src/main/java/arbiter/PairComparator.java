package arbiter;

import java.util.Comparator;

/**
 * PairComparator is used for comparing Pair class in priority queue
 */
public class PairComparator implements Comparator<Pair> {
    public int compare(Pair a, Pair b) {
        if (a.last_assigned < b.last_assigned)
            return -1;
        else if (a.last_assigned > b.last_assigned)
            return 1;
        else return 0;
    }
}