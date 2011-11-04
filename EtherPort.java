/** 
* This class represents a virtual ethernet link between two hosts over UDP.
* @author Drake, Ian, Justin
*/

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
    private int port, jack; 
    private InetAddress dstAddr, bind, ifaceAddr;
    private NetMask nm;
    private MACAddress src;
    private DatagramSocket sock;
    private HashMap<Short, EventRegistration> typeListen;
    private boolean runThreads;
    /**
        * Makes a new EtherPort, listening on the specified port, on all interfaces
        * @param port The port to be listened on
        * @param jack The identifier for this interface 
        * @param src The MACAddress assigned to this interface
        * @param routerHook The callback pointer for this interface
    */

    public EtherPort(int port, int jack,
                     MACAddress src, RouterHook routerHook) throws SocketException{
        this.routerHook = routerHook;
        this.src = src;
        this.jack=jack;
        this.port = port;
        sock = new DatagramSocket(port);
        outQueue = new LinkedBlockingQueue<DatagramPacket>();
        typeListen = new HashMap<Short, EventRegistration>();
        startConnection();
    }
    /**
        * Makes a new EtherPort, listening on the specified port, on a specified 
          interface.
        * @param port The port to be listened on
        * @param jack The identifier for this interface 
        * @param src The MACAddress assigned to this interface
        * @param routerHook The callback pointer for this interface
        * @param iface The address of the interface to listen on.
    */

    public EtherPort(int port, int jack, MACAddress src, 
                     RouterHook routerHook, InetAddress iface){
        this.port = port;
        this.src = src;
        iface = bind;
        this.jack=jack;
        this.routerHook = routerHook;
        try{
            sock = new DatagramSocket(port, iface);
        }
        catch(SocketException e){
            System.out.println("Could not establish socket on local port " 
                                + port);
        }
        typeListen = new HashMap<Short, EventRegistration>();
        outQueue = new LinkedBlockingQueue<DatagramPacket>();
        startConnection();
    }
    /**
        * Safely stops the interface after sending pending transmits.
    */
    protected void stopThreads(){
        runThreads = false;
        while(outQueue.peek() != null) { } //wait to send everything
        sock.close(); 
    }
    /**
        * Begins transmission again on a halted interface.
    */
    protected void startThreads(){
        runThreads = true;
        // sock.connect()? later. 
    }
    /**
        * Sets the endpoint of this interface
        * @param dstAddr The new endpoint
    */
    protected void setDestIP(InetAddress dstAddr) {
        this.dstAddr = dstAddr;
    }
    /**
        * Returns the endpoint of this interface.
    */
    public InetAddress getDestIP() {
        return dstAddr;
    }
    /**
        * Returns the listening port for this interface
    */
    public int getDestPort() {
        return port;
    }
    /**
        * Specifies whether or not this interface has an endpoint currently.
    */
    public boolean hasEndpoint(){
        return !(dstAddr == null);
    }
    /**
        * Adds a callback for the specified ethernet type
        * @param type The ethernet type to listen for
        * @param evt The callback for the specified type
    */
    protected boolean addRegistration(short type, EventRegistration evt){
        typeListen.put(new Short(type), evt);
        return true;
    }
    /**
        * Parses a byte array into an EtherFrame
        * @param payload The byte array containing the frame 
    */
    private EtherFrame parseFrame(byte[] payload) throws IOException{
        ByteBuffer bb = ByteBuffer.wrap(payload);
        int fcs = bb.getInt(payload.length-4);
        flipBits(payload);
        long preambleSFD = bb.getLong();
        MACAddress dst = new MACAddress(Arrays.copyOfRange(payload,9,15));
        MACAddress src = new MACAddress(Arrays.copyOfRange(payload,15,21));
        short type = toShort(Arrays.copyOfRange(payload,21,24));
        byte[] data = Arrays.copyOfRange(payload,23,payload.length-4);
        EtherFrame rcvdFrame = new EtherFrame(dst,src,type,data);
        int crc = compCRC(Arrays.copyOfRange(payload,9,payload.length-4));
        if(crc != fcs) {
            System.out.println("Corrupt frame!");
            throw new IOException("Corrupt frame");
        }
        //right now we'll just return it anyways since CRC is probably buggy
        return rcvdFrame; 
    }
    //making this private prevents "Overridable method in constructor" warning
    /**
        * Starts the transmit and recieve threads of the interface.
    */
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
    /**
        * Adds a frame to be transmitted.
        * @param eth The EtherFrame to be enqueued
        * @param dstAddr The destination IP for the packet
    */
    protected void enqueueFrame(EtherFrame eth, InetAddress dstAddr){
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
                                                dstAddr, port);
        outQueue.offer(pkt);
        System.out.println("Packet offered");
    }
    /**
        * Adds a frame to be transmitted.
        * @param dst The destination MACAddress for this frame
        * @param type The type of this frame
        * @param data The data payload of this frame
    */
    protected void enqueueFrame(MACAddress dst, short type, byte[] data){
        EtherFrame eth = new EtherFrame(dst,src,type,data);
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
                                                dstAddr, port);
        outQueue.offer(pkt);
        System.out.println("Packet offered to "+ dstAddr.toString());
    }
    /**
        * Adds a control command to be transmitted over the interface
        * @param payload The payload of the command packet
        * @param dstAddr The destination address for the packet
    */
    protected void enqueueCommand(byte[] payload, InetAddress dstAddr){
        DatagramPacket pkt = new DatagramPacket(payload, payload.length, 
                                                dstAddr,port);
        outQueue.offer(pkt);
    }
    private void receiveFrame(){
        byte[] buf = new byte[1532];
        DatagramPacket rcvd = new DatagramPacket(buf,buf.length);
        while(runThreads){
            try{
                sock.receive(rcvd);
                int len = rcvd.getLength();
                if( buf[0]  == (byte) 'e' && 72<=len && len<=1526) {
                    byte[] frame = new byte[rcvd.getLength()];
                    System.arraycopy(rcvd.getData(),0,frame,0,rcvd.getLength());
                    EtherFrame eth = parseFrame(frame);
                    EventRegistration evt = typeListen.get(new Short(
                                                           eth.getType()));
                    if( evt != null && 
                          (eth.getDst().getLongAddress() == 
                           src.getLongAddress() )    ) {
                        evt.frameReceived(eth.getData()); //else, this isnt for us
                    }
                    else if(evt == null && 
                            eth.getDst().getLongAddress() == src.getLongAddress()
                            && eth.getType() == (short)0x0801)
                    {
                        System.out.println(new String(eth.getData()));
                    }
                }
                else { //if not an 'e' packet, give it to our controller
                    routerHook.commandRcvd((char)buf[0], 
                                           rcvd.getAddress(),
                                           rcvd.getPort(),
                                           getJack(),
                                           buf);
                }
            }
            catch(IOException e){
                System.out.println("fission mailed");
            }
        }
    }
    private void sendFrame(){
        DatagramPacket currpkt=null;
        while(runThreads){
            try{
                currpkt = outQueue.take();
            }
            catch(InterruptedException e){
                System.out.println(e);
            }
            try{ sock.send(currpkt);}
            catch(IOException e){System.out.println(e);}
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
    /**
        * Returns the port this interface is listening on
    */
    public int getPort() {
        return port;
    }
    /**
        * Returns the port identifier number of this interface.
    */
    public int getJack() {
        return jack;
    }
    /**
        * Returns the IP of the interface this EtherPort is listening on,
          if it is listening on a specific interface. 
    */
    public InetAddress getBound(){
        return bind;
    }
    /**
        * Sets the IP for the interface
        * Note: This IP is internal, it is unrelated to the one in getLocalIP()
        * It is used mainly as a data storage for the routing table. 
    */
    protected void setIP( InetAddress ip){
        ip = ifaceAddr;
    }
    /**
        * Returns the IP of this interface.
    */
    public InetAddress getIP(){
        return ifaceAddr;
    }
    /**
        * Sets the netmask for this interface.
    */
    protected void setNetMask( NetMask nm ){
        this.nm = nm;
    }
    /**
    */
    private short toShort(byte [] b){
        short sh=0;
        sh |= b[0] & 0xFF;
        sh <<=8;
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
        ByteBuffer buf = ByteBuffer.allocate(4).putInt((int)crc.getValue());
        byte[] flip = buf.array();
        flipBits(flip);
        return buf.getInt(0);
   }
    
}
