/**
* A class representing the data structure of an Ethernet frame
* @author Drake, Ian, Justin
*/

public class EtherFrame
{
        private MACAddress src;
    private MACAddress dst;
    private short type;
    private byte[] data;
    private int fcs;
    /**
        * Makes an EtherFrame with a bogus source, and broadcast address.
        * @param data The frame's data payload.
    */
    public EtherFrame(byte[] data){
        data = this.data;
        src = new MACAddress(0L);
        dst = new MACAddress();
        type = 0x800;
        //fcs later
    }
    /**
        * Makes an EthernetFrame in the way most likely to be used.
        * @param src The source MACAddress
        * @param dst The destination of the frame
        * @param data The frame's data payload.      
    */
    public EtherFrame(MACAddress src, MACAddress dst, byte[] data){
        this.src = src;
        this.dst = dst;
        this.data = data;
        type = 0x800;
    }
    /**
        * This constructor allows the ethernet frame type to be specified
        * @param src The source MACAddress
        * @param dst The destination MACAddress of the frame
        * @param data The frame's data payload.      
        * @param type The frame's EtherType. 
    */
    public EtherFrame(MACAddress src, MACAddress dst, byte[] data, short type){
        this.src = src;
        this.dst = dst;
        this.data = data;
        this.type = type;
    }
    /**
        * This method simply returns the data payload of the frame 
    */
    public byte[] asBytes(){
        return data;
    }
    /**
        * Computes the Frame Check Sequence of the packet 
    */ 
    private int computeFCS(){
        //this'll get done when he covers how to do CRC32
        return 0;
    }
    /**
        * Checks the FCS of the packet by computing it and comparing it to the 
        * pre-recorded value 
    */
    public boolean checkFCS(){
        int newFCS = computeFCS();
        if(newFCS == fcs) return true;
        return false; 
    } 
    /**
        * Writes the FCS for a newly created packet
        * Shouldn't be used outside of packet creation.
    */
    private void writeFCS(){
        fcs = computeFCS();
    } 

}
