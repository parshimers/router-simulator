
import java.net.InetAddress;

interface RouterHook{
    public void commandRcvd(char cmd, InetAddress from, int port);
}
