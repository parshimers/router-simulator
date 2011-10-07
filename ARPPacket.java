
import java.net.InetAddress;

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
    
    public ARPPacket(MACAddress sha, InetAddress spa, InetAddress tpa){
        this.sha = sha;
        this.spa = spa;
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
    public MACAddress getSHA(){return sha;}
    public InetAddress getSPA(){return spa;}
    public MACAddress getTHA(){return tha;}
    public InetAddress getTPA(){return tpa;}
    
    public byte[] toByteArray() {
        byte[] bytes = new byte[28];
        
        bytes[0] = (byte) ( 0x00FF & htype );
        bytes[1] = (byte) ((0xFF00 & htype) >> 8);
        bytes[2] = (byte) ( 0x00FF & ptype );
        bytes[3] = (byte) ((0xFF00 & ptype) >> 8);
        bytes[4] = hlen;
        bytes[5] = plen;
        bytes[6] = (byte) ( 0x00FF & oper );
        bytes[7] = (byte) ((0xFF00 & oper) >> 8);
        
        byte[] shaBytes = sha.getByteAddress();
        System.arraycopy(shaBytes, 0, bytes, 8, 6);
        byte[] spaBytes = spa.getAddress();
        System.arraycopy(spaBytes, 0, bytes, 14, 4);
        byte[] thaBytes = tha.getByteAddress();
        System.arraycopy(thaBytes, 0, bytes, 18, 6);
        if( tpa != null ) {
            byte[] tpaBytes = tpa.getAddress();
            System.arraycopy(tpaBytes, 0, bytes, 24, 4);
        }
        else {
            byte[] empty = {0,0,0,0};
            System.arraycopy(empty, 0, bytes, 24, 4);
        } 
               
        return bytes;
    }
}
