package arbiter;

import Benchmark.FastPassBenchmark;
import Benchmark.RouteInfo;
import Benchmark.TimeSlotSendingPlan;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.*;


/*
 * Route Scheduler Class
 *
 * - Implement path selection algorithm using edge-coloring method
 * - Store route information as RouteInfo and send to FastPassBenchmark
 *
 */
class RouteScheduler implements Runnable {

    // define switch num
    public static int ToRSwitchNum = 4;
    public static int coreSwitchNum = 4;
    public static int endPointsNum = 16;

    public void run() {
        Set<Pair> curr = null;
        Iterator<Pair> it = null;
        Pair next;

        // main logic
        while (true) {
            curr = null;
            it = null;

            // Get a set of pairs for one time slot
            while ((curr = FastPass.removeFromWaitListRoute()) == null) ;
            it = curr.iterator();

            // bipartite graph
            int[][] graph = new int[endPointsNum][endPointsNum];

            // record current timeslot
            long cur_timeslot = 0;

            // record a set of cores that have been used by each ToR as sender
            HashMap<Integer, Set<Integer>> ToR_send_CoreSet_map = new HashMap<>();

            // record a set of cores that have been used by each ToR as receiver
            HashMap<Integer, Set<Integer>> ToR_recv_CoreSet_map = new HashMap<>();

            List<RouteInfo> routeInfos = new ArrayList<RouteInfo>();

            for (int i = 0; i < ToRSwitchNum; i++) {
                ToR_send_CoreSet_map.put(i, new HashSet<>());
                ToR_recv_CoreSet_map.put(i, new HashSet<>());
            }

            // recursively store information and construct the bipartite graph
            while (it.hasNext()) {
                next = it.next();
                // row --> source, col --> destination
                graph[Integer.parseInt(next.src)][Integer.parseInt(next.dest)] = 1;
                cur_timeslot = next.last_assigned;
            }

            // Route Allocation
            for (int i = 0; i < endPointsNum; i++) {
                for (int j = 0; j < endPointsNum; j++) {
                    if (graph[i][j] == 1 && (i / ToRSwitchNum != j / ToRSwitchNum)) {
                        for (int k = 0; k < coreSwitchNum; k++) {
                            // check if the core has been used by this ToR
                            if (!ToR_send_CoreSet_map.get(i / ToRSwitchNum).contains(k)
                                    && !ToR_recv_CoreSet_map.get(j / ToRSwitchNum).contains(k)) {
                                ToR_send_CoreSet_map.get(i / ToRSwitchNum).add(k);
                                ToR_recv_CoreSet_map.get(j / ToRSwitchNum).add(k);

                                RouteInfo routeInfo = new RouteInfo(i, j,
                                        Arrays.asList(i / ToRSwitchNum, k, j / ToRSwitchNum));
                                routeInfos.add(routeInfo);
                                break;
                            }
                        }
                    }
                }
            }

            TimeSlotSendingPlan sendingPlan = new TimeSlotSendingPlan(cur_timeslot, routeInfos);
            FastPassBenchmark.pushToSendingPlan(sendingPlan);
        }
    }


}
