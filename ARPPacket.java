import java.net.InetAddress;
import java.nio.ByteBuffer;

public class ARPPacket{
    private final static short REQUEST = 1, RESPONSE = 2;
    
    private short htype = 1;
    private short ptype = 0x0800;
    private byte hlen = 6;
    private byte plen = 4;
    private short oper;
    private MACAddress sha;
    private InetAddress spa;
    private MACAddress tha;
    private InetAddress tpa;
    
    public ARPPacket(byte[] frameBytes) {
        ByteBuffer buf = ByteBuffer.wrap(frameBytes);
        htype = buf.getShort();
        ptype = buf.getShort();
        hlen = buf.get();
        plen = buf.get();
        oper = buf.getShort();
        byte[] shabyt = new byte[6];
        byte[] spabyt = new byte[4];
        byte[] thabyt = new byte[6];
        byte[] tpabyt = new byte[4];
        buf.get(shabyt,0,6);
        buf.get(spabyt,0,4);
        buf.get(thabyt,0,6);
        buf.get(tpabyt,0,4);
        sha = new MACAddress(shabyt);
        try {
            spa = InetAddress.getByAddress(spabyt);
        } catch(Exception e) {}
        tha = new MACAddress(thabyt);
        try {
            tpa = InetAddress.getByAddress(tpabyt);
        } catch(Exception e) {}
        
    }
    public ARPPacket(MACAddress sha, InetAddress spa, InetAddress tpa){
        this.sha = sha;
        this.spa = spa;
        this.tha = new MACAddress(0L);
        this.tpa = tpa;
        oper = REQUEST; 
    }
    public ARPPacket(MACAddress sha, InetAddress spa, MACAddress tha, 
                     InetAddress tpa ){
        this.sha = sha;
        this.spa = spa;
        this.tha = tha;
        this.tpa = tpa;
        oper = RESPONSE;
    }
    
    public byte[] toByteArray() {
        ByteBuffer buf = ByteBuffer.allocate(28);
        byte[] bytes = new byte[28];
        
        buf.putShort(htype);
        buf.putShort(ptype);
        buf.put(hlen);
        buf.put(plen);
        buf.putShort(oper);
        buf.put(sha.getByteAddress());
        buf.put(spa.getAddress());
        buf.put(tha.getByteAddress());
        byte[] tpaBytes;
        if( tpa != null )
            tpaBytes = tpa.getAddress();
        else
            tpaBytes = new byte[]{ 0, 0, 0, 0 };
        buf.put(tpaBytes);
               
        return buf.array();
    }
    
    public short getOper(){return oper;};
    public MACAddress getSHA(){return sha;}
    public InetAddress getSPA(){return spa;}
    public MACAddress getTHA(){return tha;}
    public InetAddress getTPA(){return tpa;}
}
