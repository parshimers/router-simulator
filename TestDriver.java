
import java.math.BigInteger;
import java.net.InetAddress;

public class TestDriver {
   /* 
    //On sender's computer
    public static void main(String[] args) {
        Router router = new Router(10);
        try {
            router.connect(0, InetAddress.getByName("192.168.5.241"), 4000);
        } catch(Exception e) {}
    
        router.stopAllPorts();
    }
  */
  
    //On receivers's computer
    public static void main(String[] args) {
        Router router = new Router(10);
        try {
            router.createPort(0, 4000);
        } catch(Exception e) {}
        
        //When packet is received, class EtherPort will call iGotAPacket() below
        while( !proceed )
            ;  //just wait
        
        router.stopAllPorts();
    }
    
    private static boolean proceed = false;
    public static void iGotAPacket(char c) {
        System.out.println("I received packet with char " + c);
        proceed = true;
    }
 
}
