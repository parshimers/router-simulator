
import java.net.InetAddress;

public class RoutingTableEntry {
    private NetMask netMask;
    private InetAddress nextHopIP;
    private boolean isDirect;
    
    public RoutingTableEntry( NetMask netMask, InetAddress nextHopIP, 
                              boolean isDirect ) {
        this.netMask = netMask;
        this.nextHopIP = nextHopIP;
        this.isDirect = isDirect;
    }
    
    public boolean isDirect() {
        return isDirect;
    }
    
    public InetAddress getGateway() {
        return nextHopIP;
    }
    
    public boolean isOnNetwork(InetAddress outgoingIP, 
                                      InetAddress networkAddress) {
        int outgoingInt = ipToBinaryInt(outgoingIP.getHostAddress()),
            networkInt = ipToBinaryInt(networkAddress.getHostAddress());
        
        return (outgoingInt & netMask.getMask()) == networkInt;
    }
    
    public static int ipToBinaryInt(String ipString) {
        //Get each octet seperated by periods (note the "\\" is necessary part
        //of the regular expression)
        String[] octets = ipString.split("\\.");
        for( int i = 0; i < 4; i++ ) {
            octets[i] = Integer.toBinaryString( Integer.parseInt(octets[i]) );
            int difference = 8 - octets[i].length();
            for( int j = 0; j < difference; j++ )
                octets[i] = "0" + octets[i];
        }
        
        //Bytes.parseByte() doesn't work well; use Integer.parseInt() instead
        int[] maskBytes = new int[4];
        maskBytes[0] = Integer.parseInt(octets[0], 2);
        maskBytes[1] = Integer.parseInt(octets[1], 2);
        maskBytes[2] = Integer.parseInt(octets[2], 2);
        maskBytes[3] = Integer.parseInt(octets[3], 2);
        
        return (maskBytes[0] << 24) | (maskBytes[1] << 16) 
                  | (maskBytes[2] << 8) | maskBytes[3]; 
    }
    
}
