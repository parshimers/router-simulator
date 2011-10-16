
import java.util.Scanner;

public class Driver {
    
    public static void main(String[] args) {
        Router router = new Router(100);
        router.start();
        
        Scanner keyboardScan = new Scanner(System.in);
        String input = "";
        
        while( !(input = keyboardScan.nextLine()).startsWith("exit") ) {
            
        }
    }
    
}
