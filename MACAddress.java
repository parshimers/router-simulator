
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
        long ln =0;
        for(int i=0; i<5; i++){
            ln |= ary[i] & 0xFF;
            ln <<= 8;
        }
        ln |= ary[5] & 0xFF;
        return ln;
    }
 
    public long getLongAddress() {
        return addr;
    }
    
    public byte[] getByteAddress() {
        byte[] bytes = new byte[6];
        
        for( int i = 0; i < 6; i++ ) {
            bytes[i] = (byte)( (addr >> (40-(i*8))) & 0xff );
        }
        
        return bytes;
    }
    
    public static long getBroadcastAddress() {
        return 0xffffffffffffL;
    }
}
