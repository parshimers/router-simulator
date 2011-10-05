
import java.util.ArrayList;

public class Router {
    
    private ArrayList<EtherPort> ports;
    private ARP_Engine arpEngine;

    public Router( int numPorts ) {
        ports = new ArrayList<EtherPort>(numPorts);
        arpEngine = new ARP_Engine();
    }
    
}