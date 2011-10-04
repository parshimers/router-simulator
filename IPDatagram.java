import java.net.InetAddress;
public class IPDatagram{
    private final byte vers;
    private byte hlen;
    private byte service;
    private short length;
    private short ident;
    private byte flags;
    private short frag;
    private byte ttl;
    private byte proto;
    private short chksum;
    private final InetAddress src;
    private final InetAddress dst;
    private byte[] options;
    public IPDatagram( byte vers, byte hlen, byte service, short length, 
                       short ident, byte flags, short frag, byte ttl, byte proto,
                       InetAddress src, InetAddress dst, byte[] options)
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
        computeChecksum();
    }
    public void computeChecksum(){
        return;

    }
    public InetAddress getSrc(){ return src;}
    public InetAddress getDst(){ return dst;}
    public void hopMade() { ttl--; }

    
}
