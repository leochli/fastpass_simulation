package arbiter;

/**
 * Pair class represents pair of sender and receiver server.
 */
public class Pair {
    String src;
    String dest;
    Long last_assigned;

    Pair(String src, String dest) {
        this.src = src;
        this.dest = dest;
        last_assigned = -1L;
    }
}
	
