import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;

class EtherSegment{
    InetAddress dstAddr;
    InetAddress srcAddr;
    DatagramSocket sock;
    private HashMap<EtherType, EventRegistration> typeListen;
    DatagramPacket rcvd;
    public EtherSegment(InetAddress srcAddr){
        srcAddr = this.srcAddr;
        try{
            sock = new DatagramSocket(4000, srcAddr);
        }
        catch(SocketException e){
            //zzz
        }
    

    }
    //this is the class that starts a new thread to listen on a ethernet port
    private EtherFrame parseDatagram(DatagramPacket pkt){
        //pick the packet apart
        byte[] payload = {0,0,0};
        return new EtherFrame(payload);
    }
    public boolean addRegister(short type, EventRegistration evt){
        typeListen.put(new EtherType(type), evt);
        return true;
    }
    private class EtherPort extends Thread {
        public void run(){
            while(true){
            
             //see if we can recieve anything...
                try{
                    sock.receive(rcvd);
                    EtherFrame eth = parseDatagram(rcvd);
                    EventRegistration evt = typeListen.get(eth.getType());
                    if(evt != null) evt.frameRecieved(eth.asBytes());
                }
                catch(IOException e){
        
                }
            
            }
        }
    }
}
