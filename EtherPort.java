
import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

class EtherPort {
    final private int virtualPort;
    final private LinkedBlockingQueue<DatagramPacket> outQueue;
    
    private InetAddress virtualIP;
    private MACAddress virtualMAC;
    //private VirtualNetMask vnm;
    private DatagramSocket sock;
    private HashMap<EtherType, EventRegistration> typeListen;

    public EtherPort(int virtualPort, InetAddress virtualIP, 
                     MACAddress virtualMAC /*, VirtualNetMask vnm */ ){
        this.virtualIP = virtualIP;
        this.virtualPort = virtualPort;
        this.virtualMAC = virtualMAC;
        /*this.vnm = vnm*/  //don't know what this is but eventually we'll need it
        try{
            sock = new DatagramSocket(virtualPort, virtualIP);
        }
        catch(SocketException e){
            System.out.println("Could not establish socket on local port " 
                                + virtualPort);
        }
        outQueue = new LinkedBlockingQueue<DatagramPacket>();
        startConnection();
    }
    public EtherPort(int virtualPort){
        this.virtualPort = virtualPort;
        try{
            sock = new DatagramSocket(virtualPort);
        }
        catch(SocketException e){
        }
        catch(SecurityException e){
        }
        outQueue = new LinkedBlockingQueue<DatagramPacket>();
        startConnection();
    }
    private void parseDatagram(DatagramPacket pkt) throws IOException {
        //pick the packet apart
        byte[] payload = pkt.getData();
        ByteArrayInputStream bytes = new ByteArrayInputStream(payload);
        DataInputStream payloadStream = new DataInputStream(bytes);
        
        //get first char (the signal), then skip over it in the stream
        byte flagChar = payload[0];
        payloadStream.skipBytes(1);
        
        //"Accept": flagChar == 'a' 
        if( (flagChar & 0x61) == 0x61 ) {
            
        }
        //"Bye": flagChar == 'b'
        else if( (flagChar & 0x62) == 0x62 ) {
            
        }
        //"Connect": flagChar == 'c'
        else if( (flagChar & 0x63) == 0x63 ) {
            
        }
        //"Disconnect": flagChar == 'd'
        else if( (flagChar & 0x64) == 0x64 ) {
            
        }
        //"Ethernet frame": flagChar == 'e'
        else if( (flagChar & 0x65) == 0x65 ) {
            //dstToData will store all bytes in the virtual ethernet frame from
            //destination MAC up to and including the end of the data.
            //Subtract 8 bytes for preamble/SFD, 4 for CRC, and 1 for 'e'.
            byte[] dstToData = new byte[payload.length - 8 - 4 -1];
            //ignore the first 8 bytes of preamble, SFD
            payloadStream.skipBytes(8);
            payloadStream.read(dstToData, 0, dstToData.length);
            int fcs = payloadStream.readInt();
            //flip all bits except for CRC
            flipBits(dstToData);
            
            bytes = new ByteArrayInputStream(dstToData);
            payloadStream = new DataInputStream(bytes);
            
            byte[] dstBytes = new byte[6];
            byte[] srcBytes = new byte[6];
            payloadStream.read(dstBytes,0,6);
            payloadStream.read(srcBytes,0,6);
            MACAddress dst = new MACAddress(dstBytes);
            if( !(dst.getLongAddress() == virtualMAC.getLongAddress() 
                  || dst.getLongAddress() == MACAddress.getBroadcastAddress()) )
                return;     //packet wasn't meant for this port
            MACAddress src = new MACAddress(srcBytes);
            short type = (short) payloadStream.readUnsignedShort();
            byte[] data = new byte[dstToData.length-14];
            payloadStream.read(data,0,data.length);
            
            EtherFrame rcvdFrame = new EtherFrame(src,dst,data,type);
            //after FCS is complete
            /*if(rcvdFrame.computeFCS() == fcs){
                return rcvdFrame;
            }
            else throw new IOException("Corrupt frame");
            */
            //do whatever with data........
            
        }
        //"Don't want to talk to you": flagChar == 'f'
        else if( (flagChar & 0x66) == 0x66 ) {
            
        }
        
    }
    public boolean addRegistration(short type, EventRegistration evt){
        typeListen.put(new EtherType(type), evt);
        return true;
    }
    //making this private prevents "Overridable method in constructor" warning
    private void startConnection(){
        Thread receiveThread = new Thread (new Runnable() {
            @Override
            public void run() { receiveFrame(); }
        });
        receiveThread.start();
        Thread sendThread = new Thread (new Runnable() {
            @Override
            public void run() { sendFrame();}
        });
        sendThread.start();
    }
    public void enqueueFrame(EtherFrame eth, InetAddress dstAddr, int dstPort){
        byte[] payload = eth.asBytes();
        DatagramPacket pkt = new DatagramPacket(payload, payload.length, 
                                                dstAddr, dstPort);
        outQueue.offer(pkt);
    }
    private void receiveFrame(){
        DatagramPacket rcvd = null;
        
        while(true){
            //see if we can recieve anything...
            try{
                sock.receive(rcvd);
                parseDatagram(rcvd);
                //seems like this can all be handled in parseDatagram()
//                if( eth != null ) {
//                    EventRegistration evt = typeListen.get(
//                                               new EtherType(eth.getType()));
//                    if(evt != null) 
//                        evt.frameReceived(eth.asBytes());
//                }
            }
            catch(IOException e){
                System.out.println("fission mailed");
            }
        }
    }
    private void sendFrame(){
        while(true){
            DatagramPacket currpkt = outQueue.poll();
            while(currpkt != null){
                try{ sock.send(currpkt); }
                catch(IOException e){ /*maybe set a flag as warning that
                                        transmission failed. Probably 
                                        shouldn't return as this will kill
                                        any future sends.*/ }
                
                try{ currpkt = outQueue.take(); }
                catch(InterruptedException e){ /*same as above catch*/ }
            }
        }
    }
    private void flipBits( byte[] bytes ) {
        for( int i = 0; i < bytes.length; i++ ) {
            byte oldByte = bytes[i];
            bytes[i] = (byte) ( ((oldByte & 0x01)<<7) | ((oldByte & 0x02)<<5) |
                                ((oldByte & 0x04)<<3) | ((oldByte & 0x08)<<1) |
                                ((oldByte & 0x10)>>1) | ((oldByte & 0x20)>>3) |
                                ((oldByte & 0x40)>>5) | ((oldByte & 0x80)>>7) );
        }
    }
}
