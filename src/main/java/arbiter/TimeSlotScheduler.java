package arbiter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static arbiter.FastPass.DELIMITER;
import static arbiter.FastPass.MAX_TIME;

class TimeSlotScheduler implements Runnable {
    int base_timeslot;
    HashMap<String, Integer> pair_timeslot_bitstrings;
    HashMap<Long, Set<Pair>> send_to_route_scheduler;
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
        Pair curr;
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
            while ((curr = FastPass.removeFromWaitListTimeslot()) == null) {
//                System.out.println(curr);
                updateCheckpoint();
            }
            curr_string = curr.src + DELIMITER + curr.dest;
//            System.out.println(curr_string);
            if (!(pair_timeslot_bitstrings.containsKey(curr_string))) {
                pair_timeslot_bitstrings.put(curr_string, 0);
            }
            //System.out.println("here?");
            keys = pair_timeslot_bitstrings.keySet().iterator();
            String nextkey;
            while (keys.hasNext()) {
                nextkey = keys.next();
                if (nextkey.equals(curr_string))
                    continue;
                runningValue = runningValue & pair_timeslot_bitstrings.get(nextkey);
//                System.out.println("Running value: " + runningValue);
            }
            int index = 0;
            while (test_zero == 1 && index < 16) {
                test_zero = runningValue & 1;
                runningValue = runningValue >> 1;
                if (!start) {
                    offset = 1;
                } else {
                    offset = offset << 1;
//                    System.out.println("Offset: " + offset);
                    timeslot_offset++;
                }
                start = true;
                index++;
            }

//            System.out.println("test_zero: " + test_zero);
            if (index >= 16) {
                //System.out.println("entered 1");
                schedule_later.add(curr);
            } else {
                //System.out.println("entered 2");

                if (test_zero == 1 && !start) {
//                    System.out.println("entered 3");
                    offset = 1;
                    timeslot_offset = 0;
                }

                repeat_ip = false;
                curr.last_assigned = last_checkpoint_timeslot + (long) timeslot_offset;
                if (send_to_route_scheduler.containsKey(curr.last_assigned)) {
                    Set<Pair> curr_set = send_to_route_scheduler.get(curr.last_assigned);
                    for (Pair temp : curr_set) {
                        if (temp.src.equals(curr.src) || temp.dest.equals(curr.dest)) {
                            schedule_later.add(curr);
                            repeat_ip = true;
                            updateCheckpoint();
                            break;
                        }
                    }
                }

                if (!repeat_ip) {
//                System.out.println("last checkpoint " + last_checkpoint_timeslot);
//                System.out.println("timeslot_offset " + timeslot_offset);
                    curr.last_assigned = last_checkpoint_timeslot + (long) timeslot_offset;
                    pair_timeslot_bitstrings.put(curr_string, pair_timeslot_bitstrings.get(curr_string) | offset);
                    //                System.out.println("bit_string " + Integer.toBinaryString(pair_timeslot_bitstrings.get(curr_string) | offset));
                    //                System.out.println("Last assigned: " + curr.last_assigned);
                    if (!send_to_route_scheduler.containsKey(curr.last_assigned)) {
                        send_to_route_scheduler.put(curr.last_assigned, new HashSet<Pair>());
                    }
                    send_to_route_scheduler.get(curr.last_assigned).add(curr);
                    updateCheckpoint();
                }
            }
        }
    }

    public boolean updateCheckpoint() {
        //System.out.println("no bottleneck");
        long curr_time = System.nanoTime();
        long diff;
        long schedule_route;
        Set<Pair> curr_timeslot;
        Iterator<String> keys;
        Iterator<Pair> put_backs;
        if ((diff = curr_time - last_checkpoint_time) >= MAX_TIME) {
            //System.out.println("changing of the guard");
            schedule_route = diff / MAX_TIME;
            for (int i = 0; i < schedule_route; i++) {
                if ((curr_timeslot = send_to_route_scheduler.get(last_checkpoint_timeslot + i)) != null) {
                    while (FastPass.addToWaitListRoute(curr_timeslot) == false) ;
                    send_to_route_scheduler.remove(last_checkpoint_timeslot + i);
                }

            }

            put_backs = schedule_later.iterator();
            Pair curr_pair;
            while (put_backs.hasNext()) {
                curr_pair = put_backs.next();
                FastPass.wait_list_timeslot.add(curr_pair);
            }
            schedule_later.clear();

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
            //System.out.println("done");
            return true;
        } else {
            //System.out.println("done");
            return false;
        }
    }
}