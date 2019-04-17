package Benchmark;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.HashMap;

public class FastPassBenchmark implements Runnable {

    public static final int MTU_SIZE = 1500; // 1500 bytes
    int time_passed = 0;
    int total_queue_size = 0;

    /**
     * priorityqueue<timeslot, list of sending routes in this timeslot>
     */
    public static PriorityBlockingQueue<TimeSlotSendingPlan> timeslot_routes;
    private ConcurrentHashMap<String, Integer> ingress_edge_queue_map;
    private ConcurrentHashMap<String, Integer> egress_edge_queue_map;
    private HashMap<String, LinkedList<String>> queue_next_step;

    public FastPassBenchmark() {
        timeslot_routes = new PriorityBlockingQueue<TimeSlotSendingPlan>();
        ingress_edge_queue_map = new ConcurrentHashMap<String, Integer>();
        egress_edge_queue_map = new ConcurrentHashMap<String, Integer>();
        queue_next_step = new HashMap<String, LinkedList<String>>();
    }

    /**
     * calculate the benchmark statistics
     */
    public void run() {

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

            for (RouteInfo routeInfo : curr.plans) {
                int sender = routeInfo.startClient;
                int receiver = routeInfo.endClient;
                int startToR = routeInfo.pathOnSwitch.get(0);
                int coreSwitch = routeInfo.pathOnSwitch.get(1);
                int endToR = routeInfo.pathOnSwitch.get(2);

                String edge1 = String.valueOf(startToR) + "-" + String.valueOf(coreSwitch);
                String edge2 = String.valueOf(coreSwitch) + "-" + String.valueOf(endToR);
                if (!ingress_edge_queue_map.containsKey(edge1)) {
                    ingress_edge_queue_map.put(edge1, 1);
                } else {
                    ingress_edge_queue_map.put(edge1, ingress_edge_queue_map.get(edge1) + 1);
                }
                if (!egress_edge_queue_map.containsKey(edge2)) {
                    egress_edge_queue_map.put(edge2, 1);
                } else {
                    egress_edge_queue_map.put(edge2, egress_edge_queue_map.get(edge2) + 1);
                }

                System.out.println("    Path: server [" + sender + "] -> ToR [" + startToR + "] -> Core Switch [" + coreSwitch + "] -> " +
                        "ToR [" + endToR + "] -> receiver [" + receiver + "].");
            }

            calculateQueue(ingress_edge_queue_map, egress_edge_queue_map);
            processQueue(ingress_edge_queue_map);
            processQueue(egress_edge_queue_map);
        }
    }

    public void processQueue(ConcurrentHashMap<String, Integer> edge_queue_map) {
        for (Map.Entry<String, Integer> entry : edge_queue_map.entrySet()) {
            int val = entry.getValue();
            String key = entry.getKey();
            if (val > 1) {
                edge_queue_map.put(key, val - 1);
            } else {
                edge_queue_map.remove(key);
            }
        }
    }

    public void calculateQueue(ConcurrentHashMap<String, Integer> ingress_edge_queue_map, ConcurrentHashMap<String, Integer>
            egress_edge_queue_map) {
        int edge_total = 0;
        for (String edge : ingress_edge_queue_map.keySet()) {
            System.out.println("    Active Edge (Tor-Core Switch): " + edge
                    + " , Core Ingress port Queue Length: " + ingress_edge_queue_map.get(edge));
            edge_total += ingress_edge_queue_map.get(edge);
        }
        for (String edge : egress_edge_queue_map.keySet()) {
            System.out.println("    Active Edge (Core-Tor Switch): " + edge
                    + " , Core Egress port Queue Length: " + egress_edge_queue_map.get(edge));
            edge_total += egress_edge_queue_map.get(edge);
        }

        int map_total_size = ingress_edge_queue_map.size() + egress_edge_queue_map.size();
        System.out.println("    Queueing size: " + (edge_total - map_total_size) + ", Using " +
                "edge: " + map_total_size);
        System.out.println("    Queuing rate: "
                + String.format("%,.2f%%", ((double) (edge_total - map_total_size)) / map_total_size * 100));
        int queue_size = (edge_total - map_total_size) * MTU_SIZE;
        total_queue_size += queue_size;
        System.out.println("    Queuing size: " + queue_size + " KB");
        System.out.println("    Average queuing size: " + ((double) total_queue_size) / time_passed + " KB");
        System.out.println();
    }


    public static synchronized boolean pushToSendingPlan(TimeSlotSendingPlan plan) {
        return timeslot_routes.offer(plan);
    }

    public static synchronized TimeSlotSendingPlan getSendingPlan() {
        return timeslot_routes.poll();
    }
}
