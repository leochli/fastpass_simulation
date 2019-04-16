package arbiter;

import Benchmark.FastPassBenchmark;
import Benchmark.RouteInfo;
import Benchmark.TimeSlotSendingPlan;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.*;

class RouteScheduler implements Runnable {

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

            // ? get a set of pairs for one time slot
            while ((curr = FastPass.removeFromWaitListRoute()) == null) ;
            it = curr.iterator();

            //int[][] graph = new int[endPointsNum][endPointsNum];
            long cur_timeslot = 0;

            HashMap<Integer, Set<Integer>> ToR_send_CoreSet_map = new HashMap<>();
            List<RouteInfo> routeInfos = new ArrayList<RouteInfo>();

            for (int i = 0; i < ToRSwitchNum; i++) {
                ToR_send_CoreSet_map.put(i, new HashSet<>());
            }

            // recursively store information
            while (it.hasNext()) {
                next = it.next();
                // mark one src --> dest
                //graph[Integer.parseInt(next.src)][Integer.parseInt(next.dest)] = 1;
                cur_timeslot = next.last_assigned;
                //System.out.println("Source: " + next.src + " Destination: " + next.dest + " Timeslot: " + next.last_assigned);
                int src_endpoint = Integer.parseInt(next.src);
                int dst_endpoint = Integer.parseInt(next.dest);
                if(src_endpoint / ToRSwitchNum != dst_endpoint / ToRSwitchNum){
                    for (int k = 0; k < coreSwitchNum; k++) {
                        if (!ToR_send_CoreSet_map.get(src_endpoint / ToRSwitchNum).contains(k)
                                && !ToR_send_CoreSet_map.get(dst_endpoint / ToRSwitchNum).contains(k)) {
                            ToR_send_CoreSet_map.get(src_endpoint / ToRSwitchNum).add(k);
                            ToR_send_CoreSet_map.get(dst_endpoint / ToRSwitchNum).add(k);

                            RouteInfo routeInfo = new RouteInfo
                                    (src_endpoint, dst_endpoint, Arrays.asList(src_endpoint / ToRSwitchNum, k, dst_endpoint / ToRSwitchNum));
                            routeInfos.add(routeInfo);
                            break;
                        }
                    }
                }

            }



//            for (int i = 0; i < endPointsNum; i++) {
//                for (int j = 0; j < endPointsNum; j++) {
//                    if (graph[i][j] == 1 && (i / ToRSwitchNum != j / ToRSwitchNum)) {
//                        for (int k = 0; k < coreSwitchNum; k++) {
//                            if (!ToR_send_CoreSet_map.get(i / ToRSwitchNum).contains(k)
//                                    && !ToR_send_CoreSet_map.get(j / ToRSwitchNum).contains(k)) {
//                                ToR_send_CoreSet_map.get(i / ToRSwitchNum).add(k);
//                                ToR_send_CoreSet_map.get(j / ToRSwitchNum).add(k);
//
//                                RouteInfo routeInfo = new RouteInfo(i, j, Arrays.asList(i / ToRSwitchNum, k, j / ToRSwitchNum));
//                                routeInfos.add(routeInfo);
//                                break;
//                            }
//                        }
//                    }
//                }
//            }

            TimeSlotSendingPlan sendingPlan = new TimeSlotSendingPlan(cur_timeslot, routeInfos);
            FastPassBenchmark.pushToSendingPlan(sendingPlan);
        }
    }


}
