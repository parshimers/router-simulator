
public class MACAddress {
    //Our prefix = E1
    private long addr;
    
    public MACAddress(long addr){
        this.addr = addr;
    }
    
    public MACAddress(byte[] byteAddr){
        addr = byteToLong(byteAddr);
    }
    
    public MACAddress(){
        addr = 0xffffffffffffL;
    }
    
    private long byteToLong(byte[] ary){
        return
        ((long)(ary[0] & 0xff) << 56) |
        ((long)(ary[1] & 0xff) << 48) |
        ((long)(ary[2] & 0xff) << 40) |
        ((long)(ary[3] & 0xff) << 32) |
        ((long)(ary[4] & 0xff) << 24) |
        ((long)(ary[5] & 0xff) << 16);
    }
 
    public long getLongAddress() {
        return addr;
    }
    
    public byte[] getByteAddress() {
        byte[] bytes = new byte[6];
        
        for( int i = 0; i < 6; i++ ) {
            bytes[i] = (byte) ( (addr >> i*8) & 0xff );
        }
        
        return bytes;
    }
    
    public static long getBroadcastAddress() {
        return 0xffffffffffffL;
    }
}
