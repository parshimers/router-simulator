
public class FlexByte {

    //changed from short to int because OutputStream.write(), which is
    //used by java.net.Socket, can send one int at a time
    
    private int byteData;
    private boolean isSigned;
    
    public FlexByte( int data, boolean signed ) {
        byteData = data;
        isSigned = signed;
    }
    
    public int getByteData() {
        if( isSigned ) {    
            if( (byteData & 0x80) == 0x80 )     //first bit is a 1
                return (( byteData & 0x7F ) - 0x80);
            else                                //first bit is a 0
                return byteData;
        }
        else    //data is unsigned, so interpret "as is"
            return byteData;
    }
    
    public static FlexByte[] toFlexByteArray( byte[] bytes, boolean isSigned ) {
        int length = bytes.length;
        FlexByte[] flexBytes = new FlexByte[bytes.length];
        
        for( int i = 0; i < length; i++ )
            flexBytes[i] = new FlexByte( (int) (bytes[i]), isSigned );
        
        return flexBytes;
    }
    
    //takes a long and breaks it into length number of FlexBytes. For example,
    //(0xffffffffffffL, 6, false) gets broken into an array of 6 FlexBytes,
    //each of them unsigned.
    public static FlexByte[] toFlexByteArray( long longNum, int length,
                                              boolean isSigned ) {
        FlexByte[] flexBytes = new FlexByte[length];
        long allOnes = 0xFF;
        
        for( int i = 0; i < length; i++ ) {
            flexBytes[i] = new FlexByte( (int) ((longNum & allOnes) >> i*8), 
                                         isSigned );
            allOnes = allOnes << 8;
        }
        
        return flexBytes;
    }
    
    public static byte[] toByteArray( FlexByte[] flexBytes ) {
        int length = flexBytes.length;
        byte[] bytes = new byte[length];
        
        for( int i = 0; i < length; i++ )
            bytes[i] = (byte) flexBytes[i].byteData;
        
        return bytes;
    }

}

//Just for our own reference:
//
class Driver {
    public static void main(String[] args) {
         int leadingOne = 0xFF, leadingZero = 0x31;
         FlexByte fb1 = new FlexByte( leadingOne, true );
         FlexByte fb2 = new FlexByte( leadingZero, true );
         System.out.println( "Signed FlexBytes: fb1 = " + fb1.getByteData() + "   fb2 = " + fb2.getByteData() ); 
         FlexByte fb3 = new FlexByte( leadingOne, false );
         FlexByte fb4 = new FlexByte( leadingZero, false );
         System.out.println( "Unsigned FlexBytes: fb3 = " + fb3.getByteData() + "   fb4 = " + fb4.getByteData() );
         System.out.println();
         
         FlexByte[] flexBytes = { fb1, fb2, fb3, fb4 };
         byte[] bytes = FlexByte.toByteArray(flexBytes);
         byte[] comparisonBits = { (byte) 0x80, (byte) 0x40, (byte) 0x20, (byte) 0x10,
                                   (byte) 0x08, (byte) 0x04, (byte) 0x02, (byte) 0x01 };
         for( byte b: bytes ) {
             for( int i = 0; i < 8; i++ ) {
                System.out.print( ((b & comparisonBits[i]) == comparisonBits[i]) + ",");
             }
             System.out.println();
         }
         System.out.println();
         
         long longNum = 0xffffffffffffL;
         flexBytes = FlexByte.toFlexByteArray(longNum, 6, false);
         for( FlexByte flex: flexBytes )
             System.out.println( flex.getByteData() );
    }
}
