class EtherFrame implements Byteable
{
    MACAddress src;
    MACAddress dst;
    short type;
    byte[] data;
    int fcs;
    public EtherFrame(byte[] data){
        data = this.data;
        src = new MACAddress();
        dst = new MACAddress();
        type = 0x800;
        //fcs later
    }
    public EtherFrame(MACAddress src, MACAddress dest, byte[] data){}
    public EtherFrame(MACAddress src, MACAddress dest, byte[] data, short type){}
    byte[] asBytes(){};

}
