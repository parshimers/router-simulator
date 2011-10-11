
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

public class Router implements RouterHook {
    
    private ArrayList<EtherPort> ports;
    private ARP_Engine arpEngine;
    private HashMap<InetAddress, GatewayEntry> routingTable;
    private int nextPortNum, nextIpSuffix;
    private long nextMacLong;

    //Actually, maybe we should get rid of a size limit on the number of
    //ports, unless there's a good reason to keep it (memory issues, network bandwith maybe?)
    public Router( int numPorts ) {
        nextPortNum = 4000;
        nextIpSuffix = 0x0001;
        nextMacLong = 0xE10000000001L;
        ports = new ArrayList<EtherPort>(numPorts);
        arpEngine = new ARP_Engine();
    }
    
    @Override
    public void commandRcvd(char cmd, InetAddress from, int port) {
        
    }
    
    //Actually we might not need this variable and method, but leaving them here in case they're useful.
//    private static final String IP_PREFIX = "176.37";
//    private String getNextIpSuffix() {
//        int firstOctet = 0xFF & nextIpSuffix;
//        int secondOctet = (0xFF00 & nextIpSuffix) >> 8;
//        nextIpSuffix++;
//        return Integer.toString(secondOctet) + "." 
//                + Integer.toString(firstOctet);
//    }
    
    public void connect( int virtualRemotePort, InetAddress realRemoteIP,
                         int realRemotePort ) {
        //Create a new port to deal with this connection
        listen( ports.size(), nextPortNum++ );
        
        //do we need to pass the virtualRemotePort as well?
        ports.get(ports.size()-1).enqueueCommand('c', realRemoteIP, 
                                                 realRemotePort);
    }
    
    public void listen( int localVirtualPort, int localRealPort ) {
        if( ports.get(localVirtualPort) != null ) {
            //gracefully destory ports.get(localVirtualPort) here (stop threads, etc.)
            ports.set(localVirtualPort, null);
        }
        
        EtherPort newPort = new EtherPort(localRealPort, 
                                          new MACAddress(nextMacLong++), 
                                          this);
        
        if( localVirtualPort <= ports.size()-1 )
            ports.set(localVirtualPort, newPort);
        else
            ports.add(newPort);
    }
    
    //Disconnects a port on our local router.
    public void disconnect( int localVirtualPort ) {
        //send packet with 'd' message
        
        //wait for 'b' reply?
        
        //tell the targeted EtherPort to gracefully shut its threads
        //and other processes down
        //......
        
        //make the port eligible for garbage collection
        ports.set(localVirtualPort, null);
    }
    
    //Calling this autogenerates a virtual IP and virtual MAC address for
    //the port. Not sure about net mask!!!
//    public int ip() {
//        InetAddress localVirtualIP = null;
//        try {
//            localVirtualIP = InetAddress.getByName( IP_PREFIX + getNextIpSuffix() );
//        } catch( Exception e ) {}
//        
//        ports.add( new EtherPort(nextPortNum++, 
//                                 localVirtualIP,
//                                 new MACAddress(nextMacLong++),
//                                 this) );
//        
//        return ports.size()-1;
//    }
    
    public void ip( int localVirtualPort, InetAddress localVirtualIP
                    /*, VirtualNetMask vnm */ ) {
        ports.add( new EtherPort(localVirtualPort, 
                                 localVirtualIP,
                                 new MACAddress(nextMacLong++),
                                 this
                                 /*, vnm */) );
    }
    
}
