package arbiter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.*;

class RouteScheduler implements Runnable {

    public static int ToRSwitchNum = 4;
    public static int coreSwitchNum = 4;
    public static int endPointsNum = 16;

    public static List aggSwitchList = new ArrayList<>();
    public static List coreSwitchList = new ArrayList<>();


    public void run() {
        Set<Pair> curr = null;
        Iterator<Pair> it = null;
        Pair next;

        // main logic
        while (true) {
            curr = null;
            it = null;

            // ? get a set of pairs for one time slot
            while ((curr = FastPass.removeFromWaitListRoute()) == null) ;
            it = curr.iterator();

            int [][] graph = new int[endPointsNum][endPointsNum];
            long cur_timeslot = 0;

            HashMap<Integer, Set<Integer>> ToR_send_CoreSet_map = new HashMap<>();
            HashMap<Integer, Set<Integer>> ToR_recv_CoreSet_map = new HashMap<>();

            for(int i = 0; i < ToRSwitchNum; i++){
                ToR_send_CoreSet_map.put(i, new HashSet<Integer>());
                ToR_recv_CoreSet_map.put(i, new HashSet<Integer>());
            }

            // recursively store information
            while (it.hasNext()) {
                next = it.next();
                // mark one src --> dest
                graph[Integer.parseInt(next.src)][Integer.parseInt(next.dest)] = 1;
                cur_timeslot = next.last_assigned;
                //System.out.println("Source: " + next.src + " Destination: " + next.dest + " Timeslot: " + next.last_assigned);
            }

            for(int i = 0; i < endPointsNum; i++){
                for(int j = 0; j < endPointsNum; j++) {
                    if(graph[i][j] == 1){
                        for(int k = 0; k < coreSwitchNum; k++){
                            if(!ToR_send_CoreSet_map.get(i / ToRSwitchNum).contains(k)
                                    && !ToR_recv_CoreSet_map.get(j / ToRSwitchNum).contains(k)){
                                System.out.println("Source endpoint: " + i + " Destination endpoint: " + j + " Timeslot: " + cur_timeslot);
                                System.out.println("Path: " + i / ToRSwitchNum + " -- " + k + " -- " + j / ToRSwitchNum);
                                ToR_send_CoreSet_map.get(i / ToRSwitchNum).add(k);
                                ToR_recv_CoreSet_map.get(j / ToRSwitchNum).add(k);
                                break;
                            }
                        }
                    }
                }
            }
            System.out.println("Current Timeslot End.");
        }
    }


}
