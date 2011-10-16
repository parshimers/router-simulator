
import java.net.InetAddress;

public interface RouterHook{
    public void commandRcvd(char cmd, InetAddress ipRcvd, int portRcvd);
}
