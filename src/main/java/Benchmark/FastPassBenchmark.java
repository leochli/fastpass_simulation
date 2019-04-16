package Benchmark;

import java.util.concurrent.PriorityBlockingQueue;

public class FastPassBenchmark implements Runnable {

    public static final int MTU_SIZE = 1500; // 1500 bytes

    /**
     * priorityqueue<timeslot, list of sending routes in this timeslot>
     */
    public static PriorityBlockingQueue<TimeSlotSendingPlan> timeslot_routes;

    public FastPassBenchmark() {
        timeslot_routes = new PriorityBlockingQueue<>();
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
            for (RouteInfo routeInfo : curr.plans) {
                int sender = routeInfo.startClient;
                int receiver = routeInfo.endClient;
                int startToR = routeInfo.pathOnSwitch.get(0);
                int coreSwitch = routeInfo.pathOnSwitch.get(1);
                int endToR = routeInfo.pathOnSwitch.get(2);
                System.out.println("    Path: server [" + sender + "] -> ToR [" + startToR + "] -> Core Swich [" + coreSwitch + "] -> " +
                        "ToR [" + endToR + "] -> receiver [" + receiver + "].");
            }


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
