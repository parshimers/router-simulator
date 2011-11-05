/*
 * Main Driver to run the program
 */

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;

/**
 *
 * @author Ian, Drake, Justin
 */
public class Main {
    private static boolean quit;
    public static void main(String[] args) throws IOException {
        Router rt;
        if( args.length> 1 ) rt = new Router( Integer.parseInt(args[1]) );
        else rt = new Router(10); 
        System.out.println("\n\nWelcome to the Virtural Router Project. "
                           + "\nType 'help' for commands.");

        while (quit != true)
        {
            BufferedReader in = 
                           new BufferedReader(new InputStreamReader(System.in));
            String userInput = in.readLine();
            String virtualPort;
            String realIp;
            String realPort;
            String virtualIp;
            String virtualMask;
            String virtualGate;

            if (userInput.equalsIgnoreCase("connect"))
            {
                System.out.println("<virtual port number>: ");
                virtualPort = in.readLine();
                System.out.println("<real ip address> :");
                realIp = in.readLine();
                System.out.println("<real port address> :");
                realPort = in.readLine();
                int jack = Integer.parseInt(virtualPort);
                InetAddress dst = InetAddress.getByName(realIp);
                int port = Integer.parseInt(realPort);
                rt.connect(jack,dst,port);
            }
            else if(userInput.equalsIgnoreCase("run"))
            {
                try {
                    /* Change Filepath here  */
                    FileReader fr = new FileReader("C:/Users/Drake/School/Comp 429/data.txt");
                    BufferedReader br = new BufferedReader(fr); 
                    String strLine;
                        while ((strLine = br.readLine())!= null)
                        {
                            strLine = br.readLine();
                            if (strLine.equalsIgnoreCase("connect"))
                            {
                                System.out.println("connect");
                                System.out.println("<virtual port number>: ");
                                virtualPort = br.readLine();
                                System.out.println(virtualPort);
                                System.out.println("<real ip address> :");
                                realIp = br.readLine();
                                System.out.println(realIp);
                                System.out.println("<real port address> :");
                                realPort = br.readLine();
                                System.out.println(realPort);
                                int jack = Integer.parseInt(virtualPort);
                                InetAddress dst = InetAddress.getByName(realIp);
                                int port = Integer.parseInt(realPort);
                                rt.connect(jack,dst,port);
                                System.out.println();
                            }
                             else if (strLine.equalsIgnoreCase("ip"))
                            {
                                System.out.println("ip");
                                System.out.println("<virtual port number> : ");
                                virtualPort = br.readLine();
                                System.out.println(virtualPort);
                                System.out.println("<virtual ip address> : ");
                                virtualIp = br.readLine();
                                System.out.println(virtualIp);
                                System.out.println("<virtual net mask> : " );
                                virtualMask = br.readLine();
                                System.out.println(virtualMask);
                                int jack = Integer.parseInt(virtualPort);
                                InetAddress ip = InetAddress.getByName(virtualIp);
                                rt.ip(jack,ip,virtualMask);
                                System.out.println();
                            }
                             else if (strLine.equalsIgnoreCase("disconnect"))
                            {
                                System.out.println("disconnect");
                                System.out.println("<virtual port number> : ");
                                virtualPort = br.readLine();
                                System.out.println(virtualPort);
                                int jack = Integer.parseInt(virtualPort);
                                rt.disconnect(jack);
                                System.out.println();
                            }
                            else if (strLine.equalsIgnoreCase("listen"))
                            {
                                System.out.println("listen");
                                System.out.println("<virtual port number> : ");
                                virtualPort = br.readLine();
                                System.out.println(virtualPort);
                                System.out.println("<real port number> ");
                                realPort = br.readLine();
                                System.out.println(realPort);
                                int jack = Integer.parseInt(virtualPort);
                                int port = Integer.parseInt(realPort);
                                rt.listen(jack,port);
                                System.out.println();
                            }
                            else if (strLine.equalsIgnoreCase("route"))
                            {
                                System.out.println("route");
                                System.out.println("<virtual network number> : ");
                                InetAddress vna = InetAddress.getByName(br.readLine());
                                System.out.println(vna);
                                System.out.println("<virtual net mask> : ");
                                virtualMask = br.readLine();
                                System.out.println(virtualMask);
                                System.out.println("<virtual gateway address> : ");
                                virtualGate = br.readLine();
                                System.out.println(virtualGate);
                                NetMask nm = new NetMask(virtualMask);
                                InetAddress gw = InetAddress.getByName(virtualGate);
                                rt.route(vna,nm,gw);
                                System.out.println();
                            }
                        }
                        fr.close();
                      }
                    catch (Exception e){
                        System.err.println("Error: " + e.getMessage());
                    }
                }

            else if(userInput.equalsIgnoreCase("listen"))
            {
                System.out.println("<virtual port number> : ");
                virtualPort = in.readLine();
                System.out.println("<real port number> ");
                realPort = in.readLine();
                int jack = Integer.parseInt(virtualPort);
                int port = Integer.parseInt(realPort);
                rt.listen(jack,port);
            }

            else if(userInput.equalsIgnoreCase("disconnect"))
            {
                System.out.println("<virtual port number> : ");
                virtualPort = in.readLine();
                int jack = Integer.parseInt(virtualPort);
                rt.disconnect(jack);
            }
            else if(userInput.equalsIgnoreCase("ip"))
            {
                System.out.println("<virtual port number> : ");
                virtualPort = in.readLine();
                System.out.println("<virtual ip address> : ");
                virtualIp = in.readLine();
                System.out.println("<virtual net mask> : " );
                virtualMask = in.readLine();
                int jack = Integer.parseInt(virtualPort);
                InetAddress ip = InetAddress.getByName(virtualIp);
                rt.ip(jack,ip,virtualMask);
            }

            else if(userInput.equalsIgnoreCase("route"))
            {
                System.out.println("<virtual network number> : ");
                InetAddress vna = InetAddress.getByName( in.readLine() );
                System.out.println("<virtual net mask> : ");
                virtualMask = in.readLine();
                System.out.println("<virtual gateway address> : ");
                virtualGate = in.readLine();
                NetMask nm = new NetMask(virtualMask);
                InetAddress gw = InetAddress.getByName(virtualGate);
                rt.route(vna,nm,gw);
            }

            else if(userInput.equalsIgnoreCase("show config"))
            {

            }

            else if(userInput.equalsIgnoreCase("ethping")){
                System.out.print("jack: ");
                int jack = Integer.parseInt(in.readLine());
                System.out.println("target mac: ");
                long mac = Long.parseLong(in.readLine(),16);
                rt.ethping(jack,mac);
            }
            else if(userInput.equalsIgnoreCase("arp")){
                System.out.print("jack: ");
                int jack = Integer.parseInt(in.readLine());
                System.out.println("target IP: ");
                rt.findMAC(InetAddress.getByName(in.readLine()),jack);
            }

            else if(userInput.equalsIgnoreCase("quit"))
                quit = true;

            else if(userInput.equalsIgnoreCase("help"))
                commandHelp();

            else
                System.out.println("Not a valid input, retry");
            
            }
        
        }
    
        public static void commandHelp()
            // Prints out the command list for appropriate action
        {
            System.out.println("connect <virtual port number> <real ip address>:<real port number>");
            System.out.println("listen <virtual port number> <real port number>");
            System.out.println("disconnect <virtual port number>");
            System.out.println("ip <virtual port number number> <virtual ip address> <virtual net mask>");
            System.out.println("route <virtual network address> <virtual net mask> <virtual gateway address>");
            System.out.println("show config");
        }
}

