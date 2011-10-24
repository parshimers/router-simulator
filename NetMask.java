
public class NetMask {
    private int maskData;
    
    public NetMask(String maskString) {
        byte octet4 = Byte.parseByte( maskString.substring(0,3) ),
             octet3 = Byte.parseByte( maskString.substring(5,8) ),
             octet2 = Byte.parseByte( maskString.substring(10,13) ),
             octet1 = Byte.parseByte( maskString.substring(15,18) );
        
        maskData = (octet4 << 24) | (octet3 << 16) | (octet2 << 8) | octet1; 
    }
    
    public int getMask() {
        return maskData;
    }
}
