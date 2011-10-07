
import java.net.InetAddress;
import java.util.HashMap;

public class ARP_Engine {
    
    private static HashMap<InetAddress, MACAddress> arpCache;
    
    public ARP_Engine() {
        arpCache = new HashMap<InetAddress, MACAddress>();
    }
    
    public static MACAddress getMAC( MACAddress sha, InetAddress spa,
                                                     InetAddress tpa ) {
        
        //check arpCache, see if MAC for tpa is already there
        if( arpCache.containsKey(tpa) )
            return arpCache.get(tpa);
        
        //if not, send request, wait for and return results
        return requestMAC(sha, spa, tpa);
    }
    
    private static MACAddress requestMAC( MACAddress sha, InetAddress spa, 
                                                          InetAddress tpa ) {
        
        ARPPacket request = new ARPPacket(sha, spa, tpa);
        
        //new MACAddress() with empty constructor = broadcast address
        EtherFrame frame = new EtherFrame( new MACAddress(), 
                                           sha, request.toByteArray() );
        
        //transmit frame
        
        //wait for response? (not exactly sure how this part works)
        
        //get response
        //ARPPacket response = ....
        //MACAddress macResponse = response.getSHA();
        
        //arpCache.put(tpa,macResponse);
        
        //return macResponse;
        
        //dummy return value so it compiles for the moment
        return new MACAddress();
        
    }
    
    public void respond( MACAddress sha, InetAddress spa, 
                         MACAddress tha, InetAddress tpa ) {
        
        //make sure we learn the requesting machine's info before we
        //send it our own info
        arpCache.put(tpa, tha);
        
        //transmit new ARPPacket(sha,spa,tha,tpa); 
        //Again, not entirely sure how the transmit process works here
        
    }
    
}
