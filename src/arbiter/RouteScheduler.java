package arbiter;

import java.util.Iterator;
import java.util.Set;

class RouteScheduler implements Runnable {
    @Override
    public void run() {
        Set<Pair> curr = null;
        Iterator<Pair> it = null;
        Pair next;
        while (true) {
            curr = null;
            it = null;
            while ((curr = FastPass.removeFromWaitListRoute()) == null) ;
            it = curr.iterator();
            while (it.hasNext()) {
                next = it.next();
                System.out.println("Source: " + next.src + " Destination: " + next.dest + " Timeslot: " + next.last_assigned);
            }
        }
    }
}
