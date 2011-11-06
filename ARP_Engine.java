
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Arrays;

public class ARP_Engine implements EventRegistration {
    
    private static HashMap<InetAddress, MACAddress> arpCache;
    private final EtherPort[] ports;
    
    public ARP_Engine(EtherPort[] ports) {
        arpCache = new HashMap<InetAddress, MACAddress>();
        this.ports = ports;
        //this is gonna be harder to make a thread than not right now
    }
    
    @Override
    public void frameReceived(byte[] frameData, int jack) {
        System.out.println("ARP_Engine: received frame data on jack " + jack);
        System.out.println( Arrays.toString(frameData) );
        
        ARPPacket toProcess = new ARPPacket(frameData);
        System.out.println( Arrays.toString( toProcess.toByteArray() ) );
        System.out.println(toProcess.getOper() == 1);
        
        //We received a request
        if( toProcess.getOper() == (short)1 ) {
            InetAddress tpa = toProcess.getTPA();
            
            if( arpCache.containsKey(tpa) )
                respond( arpCache.get(tpa), toProcess.getTPA(),
                         toProcess.getSHA(), toProcess.getSPA(),jack );
            System.out.println("MISSION SUCCESSFUL");
        }
        //We received a response, so store the sender's MAC and IP
        else if( toProcess.getOper() == (short)2 ){
            arpCache.put( toProcess.getSPA(), toProcess.getSHA() );
        }
        
    }
    
    public void requestMAC( MACAddress sha, InetAddress spa, 
                             InetAddress tpa, int jack) {

        ARPPacket request = new ARPPacket(sha, spa, tpa);
        EtherPort eth = ports[jack];
        eth.enqueueFrame(new MACAddress(),(short)0x0806,request.toByteArray());
        System.out.println(Arrays.toString(request.toByteArray()));
    }
    //Mystery router's MAC/IP are sha/spa.
    //The computer we're responding to (who made the original
    //request) is tha/tpa.
    public void respond( MACAddress sha, InetAddress spa, 
                         MACAddress tha, InetAddress tpa, int jack) {
        
        //make sure we learn the requestor and requestee's info
        arpCache.put(tpa, tha);
        arpCache.put(spa, sha);
        ARPPacket response = new ARPPacket(sha,spa,tha,tpa); 
        EtherPort eth = ports[jack];
        eth.enqueueFrame(new MACAddress(),(short)0x0806,response.toByteArray());
    }
    
    public void addToCache(InetAddress localIP, int localVirtualPort) {
        arpCache.put( localIP, ports[localVirtualPort].getMAC() );
    }
    
}
