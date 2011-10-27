
public class NetMask {
    
    private int maskData;
    
    public NetMask(String maskString) {   
        maskData = RoutingTableEntry.ipToBinaryInt(maskString);
    }
    
    public int getMask() {
        return maskData;
    }
    
}
