
import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Arrays;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public class EtherPort {
    final private LinkedBlockingQueue<DatagramPacket> outQueue;
    private RouterHook routerHook;
    private int localRealPort, localVirtualPort, dstPort; 
    private InetAddress localIP, dstAddr;
    private MACAddress virtualMAC;
    private NetMask virtualNetMask;
    private DatagramSocket sock;
    private HashMap<Short, EventRegistration> typeListen;
    private boolean runThreads;

    public EtherPort(int localRealPort, int localVirtualPort,
                     MACAddress virtualMAC, RouterHook routerHook) {
        this.routerHook = routerHook;
        this.virtualMAC = virtualMAC;
        this.localRealPort = localRealPort;
        
        try{
            sock = new DatagramSocket(localRealPort);
        }
        catch(SocketException e) {
            System.out.println("Could not establish socket on local port " 
                                + localRealPort);
        }
        catch(SecurityException e) {
            System.out.println("Security exception establishing socket "
                               + "on local port " + localRealPort);
        }
        
        outQueue = new LinkedBlockingQueue<DatagramPacket>();
        typeListen = new HashMap<Short, EventRegistration>();
        startConnection();
    }
    public EtherPort(int localRealPort, int localVirtualPort, 
                     InetAddress localIP, MACAddress virtualMAC, 
                     RouterHook routerHook, NetMask virtualNetMask ) {
        this.localRealPort = localRealPort; 
        this.localIP = localIP;
        this.virtualMAC = virtualMAC;
        this.routerHook = routerHook;
        this.virtualNetMask = virtualNetMask;
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
    public void setDestIP(InetAddress dstAddr) {
        this.dstAddr = dstAddr;
    }
    public InetAddress getDestIP() {
        return dstAddr;
    }
    public void setDestPort(int dstPort) {
        this.dstPort = dstPort;
    }
    public int getDestPort() {
        return dstPort;
    }
    public boolean hasEndpoint(){
        return dstAddr == null;
    }
    public boolean addRegistration(short type, EventRegistration evt){
        typeListen.put(new Short(type), evt);
        return true;
    }
    public EtherFrame parseFrame(byte[] payload) throws IOException{
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
        int crc = compCRC(Arrays.copyOfRange(payload,9,payload.length-4));
        if(crc != fcs) 
            throw new IOException("Corrupt frame");
        //right now we'll just return it anyways since CRC is probably buggy
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
        int crc = compCRC(Arrays.copyOfRange(frame,8,frame.length-4));
        eth.writeFCS(crc);
        frame = eth.asBytes();
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
            try{
                sock.receive(rcvd);
                int len = rcvd.getLength();
                
                if( buf[0]  == (byte) 'e' && 72 <= len && len <= 1526) {
                    byte[] frame = new byte[rcvd.getLength()];
                    System.arraycopy(rcvd.getData(),0,frame,0,rcvd.getLength());
                    EtherFrame eth = parseFrame(frame);
                    EventRegistration evt = typeListen.get( 
                                                     new Short(eth.getType()) );
                    
                    if( evt != null && 
                          ( ( eth.getDst().getLongAddress() 
                              == virtualMAC.getLongAddress() ) 
                            ||
                            ( eth.getDst().getLongAddress() 
                              == MACAddress.BROADCAST_ADDRESS ) ) ) {
                        evt.frameReceived(eth.getData()); 
                    } //else, this isn't for us
                }
                else { //if not an 'e' packet, give it to our controller
                    routerHook.commandRcvd((char)buf[0], 
                                           rcvd.getAddress(),
                                           rcvd.getPort(),
                                           this.localVirtualPort,
                                           buf);
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
    public int getLocalVirtualPort() {
        return localVirtualPort;
    }
    public void setVirtualNetMask( NetMask vnm ) {
        this.virtualNetMask = vnm;
        //this.vnva openbsd ppcm = vnm;
    }
    private short toShort(byte [] b){
        short sh = 0;
        sh |= b[0] & 0xFF;
        sh <<= 8;
        sh |= b[1] & 0xFF;
        return sh;
   }
   private int compCRC(byte[] b){
        byte[] first32 = new byte[32];
        byte[] quot = new byte[b.length];
        System.arraycopy(b,0,first32,0,32);
        flipBits(first32);
        System.arraycopy(first32,0,quot,0,32);
        System.arraycopy(b,32,quot,32,b.length-32);
        CRC32 crc = new CRC32();
        crc.update(quot);
        int notcomp = (int) crc.getValue(); //crc32 is 32 bits, oracle. 
        ByteBuffer buf = ByteBuffer.allocate(4).putInt( (int)crc.getValue() );
        byte[] flip = buf.array();
        flipBits(flip);
        return buf.getInt(0);
   }
    
}
