package arbiter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static arbiter.FastPass.DELIMITER;
import static arbiter.FastPass.MAX_TIME;

/*
 * Time Slot Scheduler Class
 *
 * - Implement time slot allocation algorithm using bit string map and max-min fairness algorithm
 * - Work off WaitListTimeslot and send the allocated work to the RouteScheduler
 *
 */

class TimeSlotScheduler implements Runnable {
    int base_timeslot;
    HashMap<String, Integer> pair_timeslot_bitstrings; // the time slot bit map for each pair
    HashMap<Long, Set<Pair>> send_to_route_scheduler; // to allocate later in the list
    long last_checkpoint_time;
    static long last_checkpoint_timeslot;

    Set<Pair> schedule_later;

    public TimeSlotScheduler() {
        pair_timeslot_bitstrings = new HashMap<String, Integer>();
        send_to_route_scheduler = new HashMap<Long, Set<Pair>>();
        last_checkpoint_time = System.nanoTime();
        last_checkpoint_timeslot = 0;
        schedule_later = new HashSet<Pair>();
    }

    public void run() {
        Pair curr; // Current request pair
        String curr_string;
        Iterator<String> keys;
        int runningValue;
        int test_zero;
        int offset;
        boolean start;
        boolean repeat_ip;
        int timeslot_offset;
        while (true) {
            curr = null;
            curr_string = null;
            runningValue = 0;
            test_zero = 1;
            offset = 1;
            start = false;
            timeslot_offset = 0;
            // Work off the wait list time slot
            while ((curr = FastPass.removeFromWaitListTimeslot()) == null) {
                updateCheckpoint();
            }
            // bit string map to indicate the recent available time slots
            curr_string = curr.src + DELIMITER + curr.dest;
            if (!(pair_timeslot_bitstrings.containsKey(curr_string))) {
                pair_timeslot_bitstrings.put(curr_string, 0);
            }

            // Find the next available time slot for this src and destination
            runningValue = pair_timeslot_bitstrings.get(curr_string);
            int index = 0;
            while (test_zero == 1 && index < 32) {
                test_zero = runningValue & 1;
                runningValue = runningValue >> 1;
                if (!start) {
                    offset = 1;
                } else {
                    offset = offset << 1;
                    timeslot_offset++;
                }
                start = true;
                index++;
            }

            // Time slots exceed the integer size
            if (index >= 32) {
                schedule_later.add(curr);
            }
            else {
                if (test_zero == 1 && !start) {
                    offset = 1;
                    timeslot_offset = 0;
                }

                // Detect if the source or destination is already used in this time slot
                repeat_ip = false;
                curr.last_assigned = last_checkpoint_timeslot + (long) timeslot_offset;

                // Send to the route scheduler
                if (send_to_route_scheduler.containsKey(curr.last_assigned)) {
                    Set<Pair> curr_set = send_to_route_scheduler.get(curr.last_assigned);
                    for (Pair temp : curr_set) {
                        // If the src or destination is occupied, move to schedule later
                        if (temp.src.equals(curr.src) || temp.dest.equals(curr.dest)) {
                            schedule_later.add(curr);
                            repeat_ip = true;
                            updateCheckpoint();
                            break;
                        }
                    }
                }

                // Packet is okay to transmit in this time slot
                if (!repeat_ip) {
                    curr.last_assigned = last_checkpoint_timeslot + (long) timeslot_offset;
                    pair_timeslot_bitstrings.put(curr_string, pair_timeslot_bitstrings.get(curr_string) | offset);
                    if (!send_to_route_scheduler.containsKey(curr.last_assigned)) {
                        send_to_route_scheduler.put(curr.last_assigned, new HashSet<Pair>());
                    }
                    send_to_route_scheduler.get(curr.last_assigned).add(curr);
                    updateCheckpoint();
                }
            }
        }
    }

    // Method to update the time slots, refresh the bit map
    public boolean updateCheckpoint() {
        long curr_time = System.nanoTime();
        long diff;
        long schedule_route;
        Set<Pair> curr_timeslot;
        Iterator<String> keys;
        Iterator<Pair> put_backs;
        if ((diff = curr_time - last_checkpoint_time) >= MAX_TIME) {
            schedule_route = diff / MAX_TIME;
            for (int i = 0; i < schedule_route; i++) {
                if ((curr_timeslot = send_to_route_scheduler.get(last_checkpoint_timeslot + i)) != null) {
                    while (FastPass.addToWaitListRoute(curr_timeslot) == false) ;
                    send_to_route_scheduler.remove(last_checkpoint_timeslot + i);
                }

            }

            // Push back all the requests in hte schedule later into wait list timeslot
            put_backs = schedule_later.iterator();
            Pair curr_pair;
            while (put_backs.hasNext()) {
                curr_pair = put_backs.next();
                FastPass.wait_list_timeslot.add(curr_pair);
            }
            schedule_later.clear();

            // Refresh the time slot bit string
            keys = pair_timeslot_bitstrings.keySet().iterator();
            int curr;
            String currKey;
            while (keys.hasNext()) {
                currKey = keys.next();
                curr = pair_timeslot_bitstrings.get(currKey);
                curr = curr >> schedule_route;
                pair_timeslot_bitstrings.put(currKey, curr);
            }
            last_checkpoint_time = curr_time;
            last_checkpoint_timeslot = last_checkpoint_timeslot + schedule_route;
            return true;
        } else {
            return false;
        }
    }
}