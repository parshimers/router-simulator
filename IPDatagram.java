
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

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
                       InetAddress src, InetAddress dst, byte[] options,byte[] data)
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
    }
    public IPDatagram( byte[] data ){        
        ByteBuffer buf = ByteBuffer.wrap(data);
        byte ver_hlen = buf.get();
        vers = (byte)(ver_hlen >> 4); //high 4 bits 
        hlen = (byte)((ver_hlen << 4)>>4); //low 4 bits
        service = buf.get();
        length = buf.getShort();
        ident = buf.getShort();
        short flags_frag = buf.getShort();
        flags = (byte)(flags_frag >> 13);
        frag = (short)((flags_frag << 3)>>3);
        ttl = buf.get();
        proto = buf.get();
        chksum = buf.getShort();
        byte[] srcbuf = new byte[4]; byte[] dstbuf=new byte[4];
        buf.get(srcbuf,0,4);
        buf.get(dstbuf,0,4);
        try{
            src = InetAddress.getByAddress(srcbuf);
            dst = InetAddress.getByAddress(dstbuf);
        }
        catch( UnknownHostException e) { src = null; dst=null;}
        if( hlen > 5) buf.get(options,20,(hlen*4)-1);
        buf.get(data,hlen*4,this.data.length-1);
    }
    public InetAddress getSrc(){ return src;}
    public InetAddress getDst(){ return dst;}
    public byte[] toBytes(){
       byte[] b = new byte[length];
       ByteBuffer buf = ByteBuffer.wrap(b);
       buf.put((byte)(vers + (byte)(hlen << 4)));
       buf.put(service);
       buf.putShort(length);
       buf.putShort(ident);
       buf.putShort((short)((flags << 13)+frag));
       buf.put(ttl);
       buf.put(proto);
       buf.putShort(chksum);
       buf.put(src.getAddress(),0,4);
       buf.put(dst.getAddress(),0,4);
       buf.put(options,0,options.length);
       buf.put(data,0,data.length);
       return b;
    }
    public void hopMade() { ttl--; }
    public byte[] getData() { return data;}

    
}
