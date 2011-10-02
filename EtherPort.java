import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

class EtherPort{
    private InetAddress dstAddr;
    private InetAddress srcAddr;
    private DatagramSocket sock;
    private HashMap<EtherType, EventRegistration> typeListen;
    private DatagramPacket rcvd;
    private int port= 4000;
    final private Queue<DatagramPacket> outQueue;

    public EtherPort(InetAddress srcAddr){
        srcAddr = this.srcAddr;
        try{
            sock = new DatagramSocket(4000, srcAddr);
            sock.connect(srcAddr, 4000);
        }
        catch(SocketException e){
            //zzz
        }
        outQueue = new LinkedList();
    }
    //this is the class that starts a new thread to listen on a ethernet port
    private EtherFrame parseDatagram(DatagramPacket pkt){
        //pick the packet apart
        byte[] payload = {0,0,0};
        return new EtherFrame(payload);
    }
    public boolean addRegistration(short type, EventRegistration evt){
        typeListen.put(new EtherType(type), evt);
        return true;
    }
    public void startConnection(){
        Thread EtherPort = new Thread (new Runnable() {
         @Override
         public void run() { datagramDepot();}
        });
        EtherPort.start();
    }
    public void enqueueFrame(EtherFrame eth){
        byte[] payload = eth.asBytes();
        DatagramPacket pkt = new DatagramPacket(payload, payload.length, dstAddr,
                                                port);
        outQueue.add(pkt);
    }
    private void datagramDepot(){
        while(true){
          //see if we can recieve anything...
            try{
                 sock.receive(rcvd);
                 EtherFrame eth = parseDatagram(rcvd);
                 EventRegistration evt = typeListen.get(new EtherType(eth.getType()));
                 if(evt != null) evt.frameReceived(eth.asBytes());
            }
            catch(IOException e){
                System.out.println("fission mailed"); 
                return;
            }
            //send out the queued packets
            DatagramPacket currpkt = outQueue.poll();
            while(currpkt != null){
                try{ sock.send(currpkt); }
                catch(IOException e){ return; }
            }
        }
    }
    
}
