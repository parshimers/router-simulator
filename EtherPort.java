
import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Arrays;
import java.nio.ByteBuffer;

class EtherPort {
    final private LinkedBlockingQueue<DatagramPacket> outQueue;
    private RouterHook routerHook;
    private int localRealPort, localVirtualPort; 
    private InetAddress localIP, dstAddr;
    private MACAddress virtualMAC;
    //private VirtualNetMask vnm;
    private DatagramSocket sock;
    private HashMap<Short, EventRegistration> typeListen;
    private boolean runThreads;

    public EtherPort(int localRealPort, int localVirtualPort,
                         MACAddress virtualMAC, RouterHook routerHook){
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
        typeListen = new HashMap<Short, EventRegistration>();
        startConnection();
    }
    public EtherPort(int localRealPort, int localVirtualPort, 
                     InetAddress localIP, MACAddress virtualMAC, 
                     RouterHook routerHook /*, VirtualNetMask vnm */ ){
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
        typeListen = new HashMap<Short, EventRegistration>();
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
    public boolean hasEndpoint(){
        return dstAddr == null;
    }
    public boolean addRegistration(short type, EventRegistration evt){
        typeListen.put(new Short(type), evt);
        return true;
    }
    public EtherFrame parseFrame(byte[] payload) {
        ByteBuffer bb = ByteBuffer.wrap(payload);
        int fcs = bb.getInt(payload.length-4);
        flipBits(payload);
        long preambleSFD = bb.getLong();
        MACAddress dst = new MACAddress(Arrays.copyOfRange(payload,9,15));
        MACAddress src = new MACAddress(Arrays.copyOfRange(payload,15,21));
        short type = toShort(Arrays.copyOfRange(payload,21,24));
        byte[] data = Arrays.copyOfRange(payload,23,payload.length-4);
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

        byte[] frame = eth.asBytes();
        byte[] payload = new byte[frame.length+1];
        System.arraycopy(frame,0,payload,1,frame.length-4);
        flipBits(payload);
        payload[0] = (byte)'e';
        System.arraycopy(frame,frame.length-4,payload,payload.length-4,4);
        DatagramPacket pkt = new DatagramPacket(payload, payload.length,
                                                dstAddr, dstPort);
        outQueue.offer(pkt);
        System.out.println(Arrays.toString(pkt.getData()));
    }
    public void enqueueCommand(byte[] payload, InetAddress dstAddr, int dstPort){
        DatagramPacket pkt = new DatagramPacket(payload, payload.length, 
                                                dstAddr, dstPort);
        outQueue.offer(pkt);
    }
    private void receiveFrame(){
        byte[] buf = new byte[1532];
        DatagramPacket rcvd = new DatagramPacket(buf,buf.length);
        while(runThreads){
            //see if we can recieve anything...
            try{
                sock.receive(rcvd);
                if( buf[0]  == (byte)101) {
                    byte[] frame = new byte[rcvd.getLength()];
                    System.arraycopy(rcvd.getData(),0,frame,0,rcvd.getLength());
                    //System.out.println(Arrays.toString(frame));
                    EtherFrame eth = parseFrame(frame);
                    //System.out.println(Arrays.toString(eth.asBytes()));
                    //System.out.println(eth.getType());
                    EventRegistration evt = typeListen.get(new Short(
                                                           eth.getType()));
                    if(evt != null) 
                        evt.frameReceived(eth.asBytes());
                }
                else {
                    routerHook.commandRcvd((char)buf[0], 
                                           rcvd.getAddress(),
                                           rcvd.getPort(),
                                           this.localVirtualPort);
                }
            }
            catch(IOException e){
                System.out.println("fission mailed");
            }
        }
    }
    private void sendFrame(){
        while(runThreads){
            DatagramPacket currpkt=null;
            try{
                currpkt = outQueue.take();
            }
            catch(InterruptedException e){
                System.out.println("interrupted");
            }
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
    private static void flipBits( byte[] bytes ) {
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
    
    public InetAddress getLocalIP() {
        return localIP;
    }
    
    public int getLocalRealPort() {
        return localRealPort;
    }
    private short toShort(byte [] b){
        short sh=0;
        sh |= b[0] & 0xFF;
        sh <<=8;
        sh |= b[1] & 0xFF;
        return sh;
    }
//    public void setVirtualNetMask( VirtualNetMask vnm ) {
//        this.vnva openbsd ppcm = vnm;
//    }
    
}
