
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

public class Router implements RouterHook {
    
    private ArrayList<EtherPort> ports;
    private ARP_Engine arpEngine;
    private HashMap<InetAddress, GatewayEntry> routingTable;

    public Router( int numPorts ) {
        ports = new ArrayList<EtherPort>(numPorts);
        arpEngine = new ARP_Engine();
    }
    
    @Override
    public void commandRcvd(char cmd, InetAddress from, int port) {
        
    }
    
}