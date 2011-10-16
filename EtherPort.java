
import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

class EtherPort {
    final private LinkedBlockingQueue<DatagramPacket> outQueue;
    private RouterHook routerHook;
    private int localRealPort; 
    private InetAddress localIP, dstAddr;
    private MACAddress virtualMAC;
    //private VirtualNetMask vnm;
    private DatagramSocket sock;
    private HashMap<EtherType, EventRegistration> typeListen;
    private boolean runThreads;

    public EtherPort(int localRealPort, MACAddress virtualMAC, 
                                        RouterHook routerHook){
        this.routerHook = routerHook;
        this.virtualMAC = virtualMAC;
        this.localRealPort = localRealPort;
        try{
            sock = new DatagramSocket(localRealPort);
        }
        catch(SocketException e){
            System.out.println("Could not establish socket on local port " 
                                + localRealPort);
        }
        catch(SecurityException e){
            System.out.println("Security exception establishing socket "
                               + "on local port " + localRealPort);
        }
        outQueue = new LinkedBlockingQueue<DatagramPacket>();
        startConnection();
    }
    public EtherPort(int localRealPort, InetAddress localIP, 
                     MACAddress virtualMAC, RouterHook routerHook 
                                            /*, VirtualNetMask vnm */ ){
        this.localRealPort = localRealPort;
        this.localIP = localIP;
        this.virtualMAC = virtualMAC;
        this.routerHook = routerHook;
        /*this.vnm = vnm*/  //don't know what this is but eventually we'll need it
        try{
            sock = new DatagramSocket(localRealPort, localIP);
        }
        catch(SocketException e){
            System.out.println("Could not establish socket on local port " 
                                + localRealPort);
        }
        outQueue = new LinkedBlockingQueue<DatagramPacket>();
        startConnection();
    }
    public void stopThreads(){
        runThreads = false;
        while(outQueue.peek() != null) { } //wait to send everything
        sock.disconnect(); 
    }
    public void startThreads(){
        runThreads = true;
        // sock.connect()? later. 
    }
    public void setDest(InetAddress dstAddr){
        if( dstAddr == null && outQueue.size()==0 ){
            this.dstAddr = dstAddr;
        }
    }
    public boolean haveEndpoint(){
        return dstAddr == null;
    }
    private char parseDatagram(DatagramPacket pkt) {
        byte[] payload = pkt.getData();
        return (char) payload[0];   //this seems more efficient than code below
        
//        //"Accept": flagChar == 'a' 
//        if( (flagChar & 0x61) == 0x61 ) {
//            return 'a';
//        }
//        //"Bye": flagChar == 'b'
//        else if( (flagChar & 0x62) == 0x62 ) {
//            return 'b';
//        }
//        //"Connect": flagChar == 'c'
//        else if( (flagChar & 0x63) == 0x63 ) {
//            return 'c';
//        }
//        //"Disconnect": flagChar == 'd'
//        else if( (flagChar & 0x64) == 0x64 ) {
//            return 'd';
//        }
//        //"Ethernet frame": flagChar == 'e'
//        else if( (flagChar & 0x65) == 0x65 ) {
//            return 'e';
//        }
//        //"Don't want to talk to you": flagChar == 'f'
//        else if( (flagChar & 0x66) == 0x66 ) {
//            return 'f';
//        }
//        return 'X';
    }
    public boolean addRegistration(short type, EventRegistration evt){
        typeListen.put(new EtherType(type), evt);
        return true;
    }
    public EtherFrame parseFrame(byte[] payload) throws IOException{
        //dstToData will store all bytes in the virtual ethernet frame from
        //destination MAC up to and including the end of the data.
        //Subtract 8 bytes for preamble/SFD, 4 for CRC, and 1 for 'e'.
        ByteArrayInputStream  bytes = new ByteArrayInputStream(payload);
        DataInputStream payloadStream = new DataInputStream(bytes);
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
            return null;     //this isnt for us, toss it
        MACAddress src = new MACAddress(srcBytes);
        short type = (short) payloadStream.readUnsignedShort();
        byte[] data = new byte[dstToData.length-14];
        payloadStream.read(data,0,data.length);
    
        EtherFrame rcvdFrame = new EtherFrame(dst,src,type,data);
        //after FCS is complete
        /*if(rcvdFrame.computeFCS() == fcs){
            return rcvdFrame;
        }
        else throw new IOException("Corrupt frame");
        */
        //right now we'll just return it without checking the CRC
        return rcvdFrame; 
    }
    //making this private prevents "Overridable method in constructor" warning
    private void startConnection(){
        runThreads = true;
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
    public void enqueueCommand(char cmd, InetAddress dstAddr, int dstPort){
        byte[] payload = new byte[1];
        payload[0]= (byte) cmd;
        DatagramPacket pkt = new DatagramPacket(payload, 1, dstAddr, dstPort);
        outQueue.offer(pkt);
    }
    private void receiveFrame(){
        DatagramPacket rcvd = null;
        
        while(runThreads){
            //see if we can recieve anything...
            try{
                sock.receive(rcvd);
                if( rcvd == null )
                    continue;
                char flag = parseDatagram(rcvd);
                if( flag == 'e') {
                    EtherFrame eth = parseFrame(rcvd.getData());
                    EventRegistration evt = typeListen.get(
                                               new EtherType(eth.getType()));
                    if(evt != null) 
                        evt.frameReceived(eth.asBytes());
                }
                else {
                    routerHook.commandRcvd(flag, 
                                           rcvd.getAddress(),
                                           rcvd.getPort());
                }
            }
            catch(IOException e){
                System.out.println("fission mailed");
            }
        }
    }
    private void sendFrame(){
        while(runThreads){
            DatagramPacket currpkt = outQueue.poll();
            while(currpkt != null){
                try{ sock.send(currpkt); }
                catch(IOException e){ 
                    //handle this properly later
                }
                try{ currpkt = outQueue.take(); }
                catch(InterruptedException e){ 
                    //ditto
                }
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
    
    public void setLocalIP( InetAddress localIP ) {
        this.localIP = localIP;
    }
    
//    public void setVirtualNetMask( VirtualNetMask vnm ) {
//        this.vnm = vnm;
//    }
    
}