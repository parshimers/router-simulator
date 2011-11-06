
import java.net.InetAddress;
import java.io.IOException;
import java.util.HashMap;

public class Routing_Engine implements EventRegistration {
    
    private HashMap<InetAddress, RoutingTableEntry> routingTable;
    
    public Routing_Engine(HashMap<InetAddress, RoutingTableEntry> routingTable) {
        this.routingTable = routingTable;
    }
    
    @Override
    public void frameReceived(byte[] bytes, int jack) {
        IPDatagram datagram = null;
        try {
            datagram = new IPDatagram(bytes);
        } catch( IOException ioe ) {
            System.out.println("Corrupt frame received.");
        }
        
        InetAddress dest = datagram.getDst();
        //apply the routing algorithm
        
        //note: Wiegley said have to be able to handle IP datagram fragments.
        //Should only matter if our router is the final destination.
        
    }
    
}
