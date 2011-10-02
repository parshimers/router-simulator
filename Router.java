
import java.util.ArrayList;

public class Router {
    
    //these are the real-live, actual ports through which our program
    //communicates with the host O.S. Everything in our router ultimately
    //has to come in the MASTER_RECEIVE_THREAD, and out the MASTER_SEND_THREAD.
    
    private ArrayList<EtherPort> ports;
    private ARP_Engine arpEngine;

    //systemPort is the real-life port number of the router application
    //on the local machine (for example, if Mac OS assigns 4060 on
    //our application's launch)
    public Router( int numPorts ) {
        ports = new ArrayList<EtherPort>(numPorts);
        arpEngine = new ARP_Engine();
    }
    
}