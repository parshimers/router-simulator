
import java.net.InetAddress;

public class GatewayEntry {
    private InetAddress ip;
    private boolean isDirect;
    
    public GatewayEntry( InetAddress ip, boolean isDirect ) {
        this.ip = ip;
        this.isDirect = isDirect;
    }
    
    public InetAddress getGateway() {
//        if( isDirect )
//            return ....   //we'll have to think of something to return that indicates "DIRECT"
//        else
            return ip;
    }
}
