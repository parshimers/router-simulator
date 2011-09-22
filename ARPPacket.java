import java.net.InetAddress;
class ARPPacket{
    private short ptype = 0x0800;
    private short htype = 1;
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
        oper = 1; 
    }
    public ARPPacket(MACAddress sha, InetAddress spa, MACAddress tha, 
                     InetAddress tpa ){
        this.sha = sha;
        this.spa = spa;
        this.tha = tha;
        this.tpa = tpa;
        oper = 2;
    }
    public MACAddress getSHA(){return sha;}
    public InetAddress getSPA(){return spa;}
    public MACAddress getTHA(){return tha;}
    public InetAddress getTPA(){return tpa;}
}
