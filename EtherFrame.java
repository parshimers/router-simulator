/**
* A class representing the data structure of an Ethernet frame
* @author Drake, Ian, Justin
*/

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class EtherFrame
{
    public static final long preambleSFD = 0xAAAAAAAAAAAAAAABL;
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
        type = 0x0800;
        this.data = data;
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
        type = 0x0800;
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
    public byte[] asBytes() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream byteAry = new DataOutputStream(bytes);
        try{
            byteAry.writeLong(preambleSFD);
            byteAry.write(dst.getByteAddress(),0,dst.getByteAddress().length);
            byteAry.write(src.getByteAddress(),0,src.getByteAddress().length);
            byteAry.writeShort((int)type);
            byteAry.write(data,0,data.length);
            byteAry.writeInt(fcs);
        }
        catch (IOException e){
            //this won't happen!
        }
        return bytes.toByteArray();
    
    }
    /**
        * Returns the data payload of this frame.
    */
    public byte[] getData() {
        return data;
    }
    /**
        * Writes the FCS for a newly created packet
        * Shouldn't be used outside of packet creation.
    */
    public void writeFCS(int fcs){
        this.fcs = fcs;
    }
    /**
        * Returns the type of this frame.
    */
    public short getType(){
        return type;
    }
    /**
        * Return the destination for this frame.
    */
    public MACAddress getDst(){return dst;}
    /**
        * Return the source of this frame.
    */
    public MACAddress getSrc(){return src;}
}
