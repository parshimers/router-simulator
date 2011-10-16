/**
* A class representing the data structure of an Ethernet frame
* @author Drake, Ian, Justin
*/

public class EtherFrame
{
    private MACAddress dst;
    private MACAddress src;
    private short type;
    private byte[] data;
    private int fcs;
    /**
        * Makes an EtherFrame with a bogus source, and broadcast address.
        * @param data The frame's data payload.
    */
    public EtherFrame(byte[] data){
        dst = new MACAddress();
        src = new MACAddress(0L);
        type = 0x800;
        data = this.data;
        //fcs later
    }
    /**
        * Makes an EthernetFrame in the way most likely to be used.
        * @param src The source MACAddress
        * @param dst The destination of the frame
        * @param data The frame's data payload.      
    */
    public EtherFrame(MACAddress dst, MACAddress src, byte[] data){
        this.dst = dst;
        this.src = src;
        type = 0x800;
        this.data = data;
    }
    /**
        * This constructor allows the ethernet frame type to be specified
        * @param src The source MACAddress
        * @param dst The destination MACAddress of the frame
        * @param data The frame's data payload.      
        * @param type The frame's EtherType. 
    */
    public EtherFrame(MACAddress dst, MACAddress src, short type, byte[] data){
        this.dst = dst;
        this.src = src;
        this.type = type;
        this.data = data;
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
    public int computeFCS(){
        //this'll get done when he covers how to do CRC32
        return 0;
    }
    /**
        * Checks the FCS of the packet by computing it and comparing it to the 
        * pre-recorded value 
    */
    public boolean checkFCS(){
        return computeFCS() == fcs;
    } 
    /**
        * Writes the FCS for a newly created packet
        * Shouldn't be used outside of packet creation.
    */
    private void writeFCS(){
        fcs = computeFCS();
    } 
    public short getType(){
        return type;
    }
}