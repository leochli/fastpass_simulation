package Benchmark;

import java.util.List;

public class RouteInfo {
    int startClient;
    int endClient;
    List<Integer> pathOnSwitch;

    public RouteInfo(int startClient, int endClient, List<Integer> pathOnSwitch) {
        this.startClient = startClient;
        this.endClient = endClient;
        this.pathOnSwitch = pathOnSwitch;
    }
}
