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
    public static void main(String[] args) {
        //
        System.out.println("\n\nWelcome to the Virtural Router Project. \nType 'help' for commands.");

        while (quit != true)
        {
             try
            {
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
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
                }

                else if(userInput.equalsIgnoreCase("listen"))
                {
                    System.out.println("<virtual port number> : ");
                    virtualPort = in.readLine();
                    System.out.println("<real port number> ");
                    realPort = in.readLine();
                }

                else if(userInput.equalsIgnoreCase("disconnect"))
                {
                    System.out.println("<virtual port number> : ");
                    virtualPort = in.readLine();
                }
                else if(userInput.equalsIgnoreCase("ip"))
                {
                    System.out.println("<virtual port number> : ");
                    virtualPort = in.readLine();
                    System.out.println("<virtual ip address> : ");
                    virtualIp = in.readLine();
                    System.out.println("<virtual net mask> : " );
                    virtualMask = in.readLine();
                }

                else if(userInput.equalsIgnoreCase("route"))
                {
                    System.out.println("<virtual port number> : ");
                    virtualPort = in.readLine();
                    System.out.println("<virtual net mask> : ");
                    virtualMask = in.readLine();
                    System.out.println("<virtual gateway address> : ");
                    virtualGate = in.readLine();
                }

                else if(userInput.equalsIgnoreCase("show config"))
                {

                }

                else if(userInput.equalsIgnoreCase("quit"))
                    quit = true;

                else if(userInput.equalsIgnoreCase("help"))
                    commandHelp();

                else
                    System.out.println("Not a valid input, retry");


            }
            catch(Exception e)
            {
                System.out.println("IOException has been caught");
            }
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

