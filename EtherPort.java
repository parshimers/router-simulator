import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Arrays;

class EtherPort{
    final private InetAddress dstAddr;
    private DatagramSocket sock;
    private HashMap<EtherType, EventRegistration> typeListen;
    private DatagramPacket rcvd;
    final private int port;
    final private LinkedBlockingQueue<DatagramPacket> outQueue;

    public EtherPort(InetAddress dstAddr, int port){
        this.dstAddr = dstAddr;
        this.port = port;
        try{
            sock = new DatagramSocket(port, dstAddr);
            sock.connect(dstAddr, port);
        }
        catch(SocketException e){
            //and do nothing with it yet!
        }
        outQueue = new LinkedBlockingQueue<DatagramPacket>();
        startConnection();
    }
    private EtherFrame parseDatagram(DatagramPacket pkt) throws IOException{
        //pick the packet apart
        byte[] payload = pkt.getData();
        ByteArrayInputStream bytes = new ByteArrayInputStream(payload);
        DataInputStream payloadStream = new DataInputStream(bytes);
        //ignore the first character
        payloadStream.skipBytes(1);
        byte[] dstBytes = new byte[6];
        byte[] srcBytes = new byte[6];
        payloadStream.read(dstBytes,0,6);
        payloadStream.read(srcBytes,0,6);
        MACAddress dst= new MACAddress(dstBytes);
        MACAddress src= new MACAddress(srcBytes);
        short type = (short)payloadStream.readUnsignedShort();
        byte[] data=new byte[payload.length-24];
        payloadStream.read(data,0,payload.length-24);
        int fcs=payloadStream.readInt();
        EtherFrame rcvdFrame;
        if(payload[0] == (byte) 0x65){
        }
        rcvdFrame = new EtherFrame(src,dst,data,type);
        //after FCS is complete
        /*if(rcvdFrame.computeFCS() == fcs){
            return rcvdFrame;
        }
        else throw new IOException("Corrupt frame");
        */
        return rcvdFrame;
    }
    public boolean addRegistration(short type, EventRegistration evt){
        typeListen.put(new EtherType(type), evt);
        return true;
    }
    public void startConnection(){
        Thread txThread = new Thread (new Runnable() {
         public void run() { recieveFrame();}
        });
        txThread.start();
        Thread rxThread = new Thread (new Runnable() {
         public void run() { sendFrame();}
        });
        rxThread.start();
    }
    public void enqueueFrame(EtherFrame eth){
        byte[] payload = eth.asBytes();
        DatagramPacket pkt = new DatagramPacket(payload, payload.length, dstAddr,
                                                port);
        outQueue.offer(pkt);
    }
    private void recieveFrame(){
        while(true){
          //see if we can recieve anything...
            try{
                 sock.receive(rcvd);
                 EtherFrame eth = parseDatagram(rcvd);
                 EventRegistration evt = typeListen.get(new EtherType(
                                                                  eth.getType()));
                 if(evt != null) evt.frameReceived(eth.asBytes());
            }
            catch(IOException e){
                System.out.println("fission mailed");
                return;
            }
        }
    }
    private void sendFrame(){
        while(true){
        DatagramPacket currpkt = outQueue.poll();
            while(currpkt != null){
                try{ sock.send(currpkt); }
                catch(IOException e){ return; }
                try{
                    currpkt = outQueue.take();
                }
                catch(InterruptedException e){return;}
            }
        }
    }

}
