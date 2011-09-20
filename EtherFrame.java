public class EtherFrame implements Shortable
{
    private static final short[] broadcast = {0xff,0xff,0xff,0xff,0xff,0xff};
    private MACAddress src;
    private MACAddress dst;
    private short type;
    private short[] data;
    private int fcs;
    public EtherFrame(short[] data){
        data = this.data;
        src = new MACAddress(broadcast);
        dst = new MACAddress(broadcast);
        type = 0x800;
        //fcs later
    }
    public EtherFrame(MACAddress src, MACAddress dst, short[] data){
        this.src = src;
        this.dst = dst;
        this.data = data;
        type = 0x800;
    }
    public EtherFrame(MACAddress src, MACAddress dst, short[] data, short type){
        this.src = src;
        this.dst = dst;
        this.data = data;
        this.type = type;
    }
    public short[] asBytes(){
        return data;
    }

}
