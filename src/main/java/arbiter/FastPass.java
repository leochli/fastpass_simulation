package arbiter;


import Benchmark.FastPassBenchmark;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

public class FastPass implements Runnable {
    public static PriorityQueue<Pair> wait_list_timeslot;

    private static Queue<String> requests;
    private static Queue<Set<Pair>> wait_list_route;

    /**
     * mode means running mode:
     * 0 -> baseline (randomly allocated)
     * 1 -> fastpass
     */
    public static final int MODE = 1;

    public static final int INITIAL_CAPACITY = 10;
    public static final String DELIMITER = ":";
    public static final long MAX_TIME = 1000000000L;
    public static final long timeslot_cap = 1000;
    public static final long route_cap = 1000;
    public static final long request_cap = 1000;


    public FastPass() {
        requests = new LinkedList<String>();
        wait_list_timeslot = new PriorityQueue<Pair>(INITIAL_CAPACITY, new PairComparator());
        wait_list_route = new LinkedList<Set<Pair>>();
    }

    public void run() {
        // TODO
        Thread s = new Thread(new Server());
        Thread tss = new Thread(new TimeSlotScheduler());
        Thread ae = new Thread(new AddressExtractor());
        Thread rs = new Thread(new RouteScheduler());
        Thread bm = new Thread(new FastPassBenchmark());
        System.out.println("starting server");
        s.start();
        System.out.println("starting addess extractor");
        ae.start();
        System.out.println("starting timeslot scheduler");
        tss.start();
        System.out.println("starting route scheduler");
        rs.start();
        System.out.println("starting fastpass benchmark");
        bm.start();
		/*
		StaticFlowPusher sfp = new StaticFlowPusher();
		sfp.run();
		*/
    }
	
	/*
	class StaticFlowPusher implements Runnable
	{
		@Override
		public void run() 
		{
			// TODO Auto-generated method stub
			
		}
	}*/

    public static synchronized boolean addRequest(String pair) {
        if (requests.size() < request_cap)
            return requests.add(pair);
        else return false;
    }

    public static synchronized String getRequest() {
        return requests.poll();
    }

    public static synchronized boolean addToWaitListTimeslot(Pair p) {
        if (wait_list_timeslot.size() < timeslot_cap)
            return wait_list_timeslot.add(p);
        else return false;
    }

    public static synchronized Pair removeFromWaitListTimeslot() {
        return wait_list_timeslot.poll();
    }

    public static synchronized boolean addToWaitListRoute(Set<Pair> sp) {
        if (wait_list_route.size() < route_cap)
            return wait_list_route.add(sp);
        else return false;
    }

    public static synchronized Set<Pair> removeFromWaitListRoute() {
        return wait_list_route.poll();
    }
}
