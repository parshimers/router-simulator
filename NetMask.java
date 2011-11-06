import java.net.InetAddress;

public class NetMask {
    
    private InetAddress mask;
    
    public NetMask(String maskString) {   
        try{
            mask = InetAddress.getByName(maskString);
        }
        catch(Exception e){}
    }
    public byte[] getMask() {
        return mask.getAddress();
    }
}
