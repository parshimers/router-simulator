
import java.net.InetAddress;

public interface RouterHook{
    public void commandRcvd(char cmd, InetAddress remoteRealIP, 
                            int remoteRealPort, int localVirtualPort, 
                            byte[] buf);
}
