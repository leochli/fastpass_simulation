package arbiter;

class Server implements Runnable {
    public void run() {
        int source;
        int dest;
        while (true) {
            source = -1;
            dest = -1;
            while (source == -1 || source == 17)
                source = (int) (Math.random() * 16);
            while (dest == -1 || dest == 17 || dest == source)
                dest = (int) (Math.random() * 16);
            while (FastPass.addRequest(source + FastPass.DELIMITER + dest) == false) ;
            //System.out.println(source + " " + dest);
        }
    }
}