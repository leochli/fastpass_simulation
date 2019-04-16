package Benchmark;

import java.util.List;

public class TimeSlotSendingPlan implements Comparable<TimeSlotSendingPlan> {
    long timeslot;
    List<RouteInfo> plans;

    public TimeSlotSendingPlan(long timeslot, List<RouteInfo> plans) {
        this.timeslot = timeslot;
        this.plans = plans;
    }

    @Override
    public int compareTo(TimeSlotSendingPlan o) {
        if (this.timeslot > o.timeslot) {
            return 1;
        } else if (this.timeslot < o.timeslot) {
            return -1;
        } else {
            return 0;
        }
    }
}
