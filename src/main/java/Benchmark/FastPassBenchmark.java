package Benchmark;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.HashMap;

public class FastPassBenchmark implements Runnable {

    public static final int MTU_SIZE = 1500; // 1500 bytes

    /**
     * priorityqueue<timeslot, list of sending routes in this timeslot>
     */
    public static PriorityBlockingQueue<TimeSlotSendingPlan> timeslot_routes;
    private HashMap<String, Integer> edge_queue_map;

    public FastPassBenchmark() {
        timeslot_routes = new PriorityBlockingQueue<TimeSlotSendingPlan>();
        edge_queue_map = new HashMap<String, Integer>();
    }

    /**
     * calculate the benchmark statistics
     */
    public void run() {
        int time_passed = 0;
        int avg_queue_size_per_switch = 0;

        while (true) {
            TimeSlotSendingPlan curr;
            while ((curr = getSendingPlan()) == null) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            time_passed++;
            System.out.println("[ Timeslot #" + curr.timeslot + " ]");
            edge_queue_map.clear();
            for (RouteInfo routeInfo : curr.plans) {
                int sender = routeInfo.startClient;
                int receiver = routeInfo.endClient;
                int startToR = routeInfo.pathOnSwitch.get(0);
                int coreSwitch = routeInfo.pathOnSwitch.get(1);
                int endToR = routeInfo.pathOnSwitch.get(2);

                String edge1 = String.valueOf(startToR) + "-" + String.valueOf(coreSwitch);
                String edge2 = String.valueOf(endToR) + "-" + String.valueOf(coreSwitch);
                if(!edge_queue_map.containsKey(edge1)){
                    edge_queue_map.put(edge1, 1);
                } else{
                    edge_queue_map.put(edge1, edge_queue_map.get(edge1)+1);
                }
                if(!edge_queue_map.containsKey(edge2)){
                    edge_queue_map.put(edge1, edge_queue_map.get(edge1)+1);
                }
//                System.out.println("    Path: server [" + sender + "] -> ToR [" + startToR + "] -> Core Switch [" + coreSwitch + "] -> " +
//                        "ToR [" + endToR + "] -> receiver [" + receiver + "].");
            }

            Integer edge_total = 0;

            for (String edge: edge_queue_map.keySet()){
                System.out.println("    Active Edge (Tor-Core Switch): " + edge
                        + " , Queue Length: " + edge_queue_map.get(edge));
                edge_total += edge_queue_map.get(edge);
            }
            System.out.println("    Queuing rate: "
                    + String.format("%.0f%%",(float)(edge_total-edge_queue_map.size())/edge_queue_map.size()));


            System.out.println();
        }
    }


    public static synchronized boolean pushToSendingPlan(TimeSlotSendingPlan plan) {
        return timeslot_routes.offer(plan);
    }

    public static synchronized TimeSlotSendingPlan getSendingPlan() {
        return timeslot_routes.poll();
    }
}
