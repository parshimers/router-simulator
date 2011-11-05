
import java.net.InetAddress;
import java.util.HashMap;

public class ARP_Engine implements EventRegistration {
    
    private static HashMap<InetAddress, MACAddress> arpCache;
    private final EtherPort[] ports;
    
    public ARP_Engine(EtherPort[] ports) {
        arpCache = new HashMap<InetAddress, MACAddress>();
        this.ports = ports;
    }
    
    @Override
    public void frameReceived(byte[] frameData, int jackNum) {
        ARPPacket toProcess = new ARPPacket(frameData);
        
        //We received a request
        if( toProcess.getOper() == 1 ) {
            InetAddress tpa = toProcess.getTPA(), spa = toProcess.getSPA();
            MACAddress sha = toProcess.getSHA();
            
            //With a real router we'd send a broadcast out to everyone on our 
            //direct physical network; emulate this here by searching through 
            //EtherPort[] ports in class Router. 
            if( !arpCache.containsKey(tpa) )
                arpCache.put( tpa, Router.findMacByIP(tpa) );
            
            //Send response
            respond( arpCache.get(tpa), tpa, sha, spa, jackNum );
        }
        //We received a response, so store the sender's MAC and IP
        else if( toProcess.getOper() == 2 )
            arpCache.put( toProcess.getSPA(), toProcess.getSHA() );        
    }
    
    //Notice that roles have reversed from the call to respond() made above.
    //MAC/IP of the unknown host (us) are now sha/spa.
    //The host we're responding to (who made the request) is now tha/tpa.
    public void respond( MACAddress sha, InetAddress spa, 
                         MACAddress tha, InetAddress tpa, int jack) {
        
        //Make sure we learn the requestor and requestee's info
        arpCache.put(tpa, tha);
        arpCache.put(spa, sha);
        ARPPacket response = new ARPPacket(sha,spa,tha,tpa);
        EtherPort eth = ports[jack];
        eth.enqueueFrame(new MACAddress(),(short)0x0806,response.toByteArray());
    }
    
}
