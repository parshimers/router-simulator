
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.util.Arrays;

public class IPDatagram{
    private final byte vers,hlen;
    private byte service;
    private short length;
    private short ident;
    private byte flags;
    private short frag;
    private byte ttl;
    private byte proto;
    private short chksum;
    private InetAddress src;
    private InetAddress dst;
    private byte[] options;
    private byte[] data;
    public IPDatagram( byte vers, byte hlen, byte service, short length, 
                       short ident, byte flags, short frag, byte ttl, byte proto,
                       InetAddress src, InetAddress dst, byte[] options, byte[] data )
    {
        this.vers = vers;
        this.hlen = hlen;
        this.service = service;
        this.length = length;
        this.ident = ident;
        this.flags = flags;
        this.frag = frag;
        this.ttl = ttl;
        this.proto = proto;
        this.src = src;
        this.dst = dst;
        this.options = options;
        this.data = data;
        
        ByteBuffer buf = ByteBuffer.allocate(42);
        byte vers_hlen = (byte) ((vers<<4) | hlen);
        buf.put(vers_hlen);
        buf.put(service);
        buf.putShort(length);
        buf.putShort(ident);
        short flags_frag = (short) ((flags<<13) | frag);
        buf.putShort(flags_frag);
        buf.put(ttl);
        buf.put(proto);
        buf.put((byte) 0);
        buf.put((byte) 0);  //Checksum initially assumed to be zeros
        buf.put(src.getAddress());
        buf.put(dst.getAddress());
        if( hlen > 5 )
            buf.put(options);
        
        this.chksum = (short) getCRC(buf.array());
        
    }
    public IPDatagram( byte[] data ) throws IOException { 
        byte[] clone = Arrays.copyOf(data, data.length);
        
        ByteBuffer buf = ByteBuffer.wrap(data);
        byte ver_hlen = buf.get();
        vers = (byte)(ver_hlen >> 4); //high 4 bits 
        hlen = (byte)(ver_hlen & 0x0F); //low 4 bits
        service = buf.get();
        length = buf.getShort();
        ident = buf.getShort();
        short flags_frag = buf.getShort();
        flags = (byte)(flags_frag >> 13);
        frag = (short)((flags_frag << 3)>>3);
        ttl = buf.get();
        proto = buf.get();
        chksum = buf.getShort();
        byte[] srcbuf = new byte[4]; 
        byte[] dstbuf = new byte[4];
        buf.get(srcbuf,0,4);
        buf.get(dstbuf,0,4);
        try{
            src = InetAddress.getByAddress(srcbuf);
            dst = InetAddress.getByAddress(dstbuf);
        }
        catch( UnknownHostException e) { src = null; dst=null;}
        options = new byte[(hlen*4)-1];
        data = new byte[data.length-1];
        if( hlen > 5) 
            buf.get(options,20,(hlen*4)-1);
        buf.get(data,hlen*4,data.length-(hlen*4)-1);
        
        if( hlen > 5 )
            clone = Arrays.copyOfRange(options, 0, 42);
        else
            clone = Arrays.copyOfRange(clone, 0, 20);
        if( getCRC(clone) != 0 )
            throw new IOException("Corrupt frame");
    }
    public InetAddress getSrc(){ return src; }
    public InetAddress getDst(){ return dst; }
    public byte[] toBytes(){
       byte[] b = new byte[length];
       ByteBuffer buf = ByteBuffer.wrap(b);
       buf.put((byte)((vers << 4) | hlen));
       buf.put(service);
       buf.putShort(length);
       buf.putShort(ident);
       buf.putShort((short)((flags << 13) | frag));
       buf.put(ttl);
       buf.put(proto);
       buf.putShort(chksum);
       buf.put(src.getAddress(),0,4);
       buf.put(dst.getAddress(),0,4);
       if( hlen > 5 )
           buf.put(options,0,options.length);
       buf.put(data,0,data.length);
       return b;
    }
    public void hopMade() { ttl--; }
    public byte[] getData() { return data;}
    
    private long getCRC( byte[] crcCheckBytes ) {
        ByteBuffer buf = ByteBuffer.wrap(crcCheckBytes);
        long sum = 0;
        for(int i = 0; i < crcCheckBytes.length/2; i++) {
            sum += buf.getShort();
        }
        
        long leadingBits = sum;
        leadingBits = leadingBits >> 16;
        sum += leadingBits;
        
        System.out.println(~sum);
        return ~sum;
    }
    
//    public static void main(String[] args) {
//        IPDatagram ipd = null;
//        try {
//            ipd = new IPDatagram( (byte)4, (byte)5, (byte)0, (short)46, 
//                                             (short)5, (byte)1, (short)0, (byte)255, (byte)1, 
//                                             InetAddress.getByName("172.31.151.34"),
//                                             InetAddress.getByName("172.31.234.220"), null,
//                                             /*new byte[]{0,0,0,0,0,0,0,0,0,0,
//                                                        0,0,0,0,0,0,0,0,0,0,0,0}*/ new byte[]{ 1, 2, 3, 4,} );
//        } catch(UnknownHostException e) {
//            System.out.println(e.getMessage());
//        }
//        
//        byte[] packetBytes = ipd.toBytes();
//        System.out.println(Arrays.toString(packetBytes));
//        
//        try {
//            IPDatagram rcvd = new IPDatagram(packetBytes);
//        } catch( IOException ioe ) {
//            System.out.println("doh");
//        }
//        
//    }
    
}
