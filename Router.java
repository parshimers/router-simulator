
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.net.SocketException;

public class Router extends Thread implements RouterHook {
    
    private EtherPort[] ports;
    //private ARP_Engine arpEngine; once ARP works
    private HashMap<InetAddress, RoutingTableEntry> routingTable;
    private int nextRealPortNum, nextIpSuffix;
    private long MACprefix;

    public Router( int numPorts ) {
        nextRealPortNum = 4000;
        //nextIpSuffix = 0x0001;
        MACprefix = 0xE10000000000L;  //E1 = our group's prefix
        ports = new EtherPort[numPorts];
        //arpEngine = new ARP_Engine();
    }
    
    @Override
    public void commandRcvd(char cmd, InetAddress remoteRealIP, 
                            int remoteRealPort, int jack, byte[] buf) {

        EtherPort ePort = ports[jack];
        System.out.println(cmd);
        System.out.println(ePort.hasEndpoint());
        switch(cmd) {
            case 'a': { //"Accept connection request from remote router"

                ePort.setDestIP(remoteRealIP);
                ePort.setDestPort(remoteRealPort);
                System.out.println("Accepted connection from: "
                                   +remoteRealIP.toString()+" on jack "+jack);
            }
            case 'b': { //"Bye"
                
                //Same question as in 'a'
                killPort(ePort);
                break;
            }
            case 'c': { //"Connection requested by remote router"           
                
                //if ePort is currently connected, reject the request
                if( ePort.hasEndpoint() ) {                 
                    String rejectString = "currently connected elsewhere";
                    
                    ePort.enqueueCommand(createRejectionMessage(rejectString), 
                                         remoteRealIP, remoteRealPort);
                }
                //else create and send 'a' response frame in response
                else {
                    byte[] payload = {(byte)'a'};
                    ePort.enqueueCommand(payload, remoteRealIP, remoteRealPort);
                    ePort.setDestIP(remoteRealIP);
                    ePort.setDestPort(remoteRealPort);
                    System.out.println("Accepting connection from: "+ 
                                        remoteRealIP.toString()+ " on jack "+jack);
                }

                break;
            }
            case 'd': { //"Disconnect"
                //Check that the remote host making the disconnect request
                //is host that is currently connected to this port
                if( ePort.getDestIP() == remoteRealIP ) {
                    //Create and send "bye" message
                    byte[] payload = createAcceptGoodbye(ePort, remoteRealIP,
                                                         remoteRealPort, 'b');

                    ePort.enqueueCommand(payload, remoteRealIP, remoteRealPort);
                    killPort(ePort);
                }
                
                break;
            }
            //case 'e' is handled in EtherPort function receiveFrame()
            case 'f': { //"Don't want to talk to you"
                String rejectString = "sorry, don't want to talk now";
                
                ePort.enqueueCommand(createRejectionMessage(rejectString), 
                                     remoteRealIP, remoteRealPort);              
                
                break;
            }
            default: System.out.println("Invalid command " + cmd 
                                        + " received from IP " + remoteRealIP + 
                                        ", port " + remoteRealPort);
        }
        
    }
    
    private byte[] createAcceptGoodbye(EtherPort ePort, InetAddress remoteRealIP,
                                       int remoteRealPort, char typeChar) {
        String byeString = typeChar 
                           + remoteRealIP.getAddress().toString()
                           + ":" + remoteRealPort + " " 
                           + ePort.getBound().toString() + ":"
                           + ePort.getPort();               
        byte[] payload = new byte[byeString.length()];
        for( int i = 0; i < payload.length; i++ )
            payload[i] = (byte) byeString.charAt(i);
        
        return payload;
    }
    
    private byte[] createRejectionMessage(String rejectString) {
        rejectString = "f <" + rejectString + ">";
        byte[] payload = new byte[rejectString.length()];
        for( int i = 0; i < payload.length; i++ )
            payload[i] = (byte) rejectString.charAt(i);

        return payload;
    }
    
    
    private int nextFreeVirtualPort() {
        int i = 0;
        
        while( ports[i] != null )
            i++;
        
        return i;
    }
    
    protected void connect( int jackNum, InetAddress realRemoteIP,
                         int realRemotePort ) {
        //Create a new port to deal with this connection
        try{
            createPort( jackNum, realRemotePort);
            EtherPort newPort = ports[jackNum];
            newPort.setDestIP(realRemoteIP);
            byte[] command = new byte[1];
            command[0] = (byte) 'c';
            newPort.enqueueCommand(command, realRemoteIP, realRemotePort);
            System.out.println("Trying to connect to: "+realRemoteIP.toString());
        }
        catch(SocketException e){
            System.out.println("Couldn't bind socket on requested port.");
        }
    }

    protected void listen( int jackNum, int port){
        try{
            createPort( jackNum, port);
            System.out.println("Listening on jack :"+jackNum+" ...");
        }
        catch(SocketException e){
            System.out.println("Couldn't bind socket onto requested port.");
        }
    }
    
    protected void createPort( int jack, int localRealPort )
              throws SocketException {
        //Gracefully stop and dereference the EtherPort currently at
        //index localVirtualPort
        /*if( localVirtualPort <= ports.size()-1 
              && ports.get(localVirtualPort) != null ) {
            ports.get(localVirtualPort).stopThreads();
            ports.set(localVirtualPort, null);
        }*/
        MACAddress mac = new MACAddress(MACprefix+jack);
        EtherPort newPort = new EtherPort(localRealPort, jack, mac, this);
        /*if( localVirtualPort <= ports.size()-1 )
            ports.set(localVirtualPort, newPort);
        else*/
        ports[jack]=newPort;
    }

    private void killPort( EtherPort ePort ) {
        int portIndex = ePort.getJack();
        
        //tell ePort to gracefully shut its threads and other processes down
        ePort.stopThreads();
        //make the port eligible for garbage collection
        ports[portIndex]=null;
    }
    
    //Disconnects a port on our local router.
    protected void disconnect( int localVirtualPort ) {
        //send packet with 'd' message
        EtherPort ePort = ports[localVirtualPort];
        byte[] payload = new byte[1];
        payload[0] = 'd';
        ePort.enqueueCommand(payload, ePort.getDestIP(), ePort.getDestPort());
        
        //port will be "killed" when 'b' reply is received, so no further code
        //is needed in this method
    }
    
    protected void ip( int localVirtualPort, InetAddress localIP, 
                    String netMask ) {
        EtherPort ePort = ports[localVirtualPort];
        ePort.setIP(localIP);
        ePort.setNetMask( new NetMask(netMask) );
    }
    
    protected void route( int jack, NetMask virtualNetMask,
                       InetAddress virtualGatewayAddress ) {
        
        //I could be wrong about how this part works, but I'm thinking we look
        //through our ports, see if any match the virtualGatewayAddress (the "target"),
        //and if so we are directly connected.
        EtherPort ePort = ports[jack];
        InetAddress virtualNetworkAddress = ePort.getIP();

        boolean isDirect = false;
        for( EtherPort e: ports ) {
            if( e != null && e.getIP().equals(virtualGatewayAddress) ) {
                isDirect = true;
                break;
            }
        }
        
        RoutingTableEntry rte = new RoutingTableEntry( virtualNetMask,
                                                       virtualGatewayAddress,
                                                       isDirect );
        routingTable.put(virtualNetworkAddress, rte);
        
    }
    protected void ethping( int jack, long dst){
        EtherPort eth = ports[jack];
        String tst = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
        eth.enqueueFrame(new MACAddress(dst),(short)0x0801,tst.getBytes());
    }

    protected void stopAllPorts() {
        for(EtherPort e: ports)
            e.stopThreads();
    }
    
}
//Thought we needed this, but on second thought doesn't seem like it...
//    private EtherPort findConnectee(byte[] buf) {
//        int localRealPort = Integer.parseInt( (char) buf[32] 
//                                                      + (char) buf[33] 
//                                                      + (char) buf[34] 
//                                                      + (char) buf[35] + "" );
//                
//        //Identify that port
//        EtherPort connectee = null;
//        for( EtherPort e: ports ) {
//            if( e.getPort() == localRealPort ) {
//                connectee = e;
//                break;
//            }
//        }
//        
//        return connectee;
//    }

//Actually we might not need this variable and method, but leaving them here in case they're useful.
//    private static final String IP_PREFIX = "176.37";
//    private String getNextIpSuffix() {
//        int firstOctet = 0xFF & nextIpSuffix;
//        int secondOctet = (0xFF00 & nextIpSuffix) >> 8;
//        nextIpSuffix++;
//        return Integer.toString(secondOctet) + "." 
//                + Integer.toString(firstOctet);
//    }

