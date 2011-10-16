
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ARP_Engine implements EventRegistration {
    
    final private LinkedBlockingQueue<ARPPacket> outQueue;
    private static HashMap<InetAddress, MACAddress> arpCache;
    private Thread queueThread;
    
    public ARP_Engine() {
        outQueue = new LinkedBlockingQueue<ARPPacket>();
        arpCache = new HashMap<InetAddress, MACAddress>();
        queueThread = new Thread() {
            @Override
            public void run() {
                while(true) {
                    //Items in the queue are continously checked, on a 
                    //rotating basis, if a response has arrived specifically 
                    //for them. If yes, reply to the original requestor and 
                    //remove item from the queue. 
                    ARPPacket frontItem = outQueue.peek();
                    InetAddress tpa = frontItem.getTPA();
                    //See if a response has arrived yet for this item
                    if( arpCache.containsKey(tpa) ) {
                        respond( arpCache.get(tpa), frontItem.getTPA(),
                                 frontItem.getSHA(), frontItem.getSPA() );
                        outQueue.poll();
                    }
                    else {  //Move frontItem to the back of outQueue
                        //Is this procedure thread-safe?
                        frontItem = outQueue.poll();
                        outQueue.add(frontItem);
                    }
                        
                }
            }
        };
        
        queueThread.start();
    }
    
    @Override
    public void frameReceived(byte[] frameData) {
        ARPPacket toProcess = new ARPPacket(frameData);
        
        //We received a request
        if( toProcess.getOper() == 1 ) {
            InetAddress tpa = toProcess.getTPA();
            
            if( arpCache.containsKey(tpa) )
                respond( arpCache.get(tpa), toProcess.getTPA(),
                         toProcess.getSHA(), toProcess.getSPA() );
            else
                requestMAC( toProcess.getSHA(), toProcess.getSPA(),
                            toProcess.getTPA() );
        }
        //We received a response, so store the sender's MAC and IP
        else if( toProcess.getOper() == 2 )
            arpCache.put( toProcess.getSPA(), toProcess.getSHA() );
        
    }
    
    private void requestMAC( MACAddress sha, InetAddress spa, 
                                             InetAddress tpa ) {
        
        ARPPacket request = new ARPPacket(sha, spa, tpa);
        
        //new MACAddress() with empty constructor = broadcast address
        EtherFrame frame = new EtherFrame( new MACAddress(), 
                                           sha, request.toByteArray() );
        
        //transmit frame
        //not quite sure how we're going to transmit.....
        
        //Place this request into rotating queue outQueue.
        outQueue.add(request);
            
    }
    
    //Mystery router's MAC/IP are sha/spa.
    //The computer we're responding to (who made the original
    //request) is tha/tpa.
    public void respond( MACAddress sha, InetAddress spa, 
                         MACAddress tha, InetAddress tpa ) {
        
        //make sure we learn the requestor and requestee's info
        arpCache.put(tpa, tha);
        arpCache.put(spa, sha);
        
        ARPPacket response = new ARPPacket(sha,spa,tha,tpa); 
        //transmit response....       (again, not entirely sure how the transmit process works here)  
    }
    
}
