package arbiter;

import java.util.HashMap;

import static arbiter.FastPass.DELIMITER;

class AddressExtractor implements Runnable {
    String current_request;
    String[] addresses;
    Pair to_add;
    long to_add_last_assigned;
    HashMap<String, Long> last_assigned;

    public AddressExtractor() {
        last_assigned = new HashMap<String, Long>();
    }

    public void run() {
        while (true) {
            to_add_last_assigned = 0L;
            current_request = null;
            addresses = null;
            to_add = null;
            while ((current_request = FastPass.getRequest()) == null) ;
            addresses = current_request.split(DELIMITER);
            if (addresses.length != 2)
                continue;
            to_add = new Pair(addresses[0], addresses[1]);
            if (last_assigned.get(current_request) == null) {
                to_add.last_assigned = TimeSlotScheduler.last_checkpoint_timeslot;
                last_assigned.put(current_request, to_add.last_assigned);
            } else
                to_add.last_assigned = last_assigned.get(current_request);
            while (FastPass.addToWaitListTimeslot(to_add) == false) ;
        }
    }
}