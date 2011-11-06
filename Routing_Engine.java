
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class Routing_Engine implements EventRegistration {
    
    private class RoutingEntry{
        InetAddress dst;
        InetAddress gw;
        NetMask nm;
        public RoutingEntry(InetAddress dst, InetAddress gw, NetMask nm){
            this.dst = dst;
            this.gw = gw;
            this.nm = nm;
        }
        InetAddress getDst() {return dst;}
        InetAddress getGate() {return gw;}
        NetMask getMask() {return nm;}

    }
    private class DatagramEntry{
        IPDatagram dgram;
        int jack;
        public DatagramEntry(IPDatagram dgram, int jack){
            this.dgram = dgram;
            this.jack=jack;
        }
        public IPDatagram getDgram() { return dgram; }
        public int getJack() { return jack;}
    }
    private ArrayList<RoutingEntry> rtable;
    private LinkedBlockingQueue<DatagramEntry> rqueue;
    private EtherPort[] ports;
    private boolean process;
    public Routing_Engine(EtherPort[] ports) {
       rtable = new ArrayList<RoutingEntry>(); 
       rqueue = new LinkedBlockingQueue<DatagramEntry>();
       this.ports = ports;
       process = true;
       start();
    }
    public void start(){
    }
    public void processEntries(){
        DatagramEntry entry=null;
        InetAddress dst;
        while(process){
            try{
                entry = rqueue.take();
            }
            catch(InterruptedException e){
                System.out.println(e);
            }

            dst = entry.getDgram().getDst();
            for(RoutingEntry r: rtable){
            }
        }
    }
    InetAddress computeNetID(InetAddress addr, NetMask nm){
        byte[] nmbyte = nm.getMask();
        byte[] byteaddr = addr.getAddress();
        byte[] netid= new byte[4];
        InetAddress nid = null;
        for(int i=0;i<4;i++){
           netid[i] = (byte)(byteaddr[i] & nmbyte[i]); 
        }
        try{
           nid =InetAddress.getByAddress(netid);
        }
        catch( Exception e) {} 
        return nid;  
    }
    @Override
    public void frameReceived(byte[] bytes, int jack) {
        IPDatagram datagram = new IPDatagram(bytes);
        rqueue.offer(new DatagramEntry(datagram,jack)); 
    }
    
}
