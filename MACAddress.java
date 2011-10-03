class MACAddress {
    long addr;
    public MACAddress(long addr){
        this.addr = addr;
    }
    public MACAddress(byte[] byteAddr){
        addr = byteToLong(byteAddr);
    }
    private long byteToLong(byte[] ary){
        return
        ((long)(ary[0] & 0xff) << 56) |
        ((long)(ary[1] & 0xff) << 48) |
        ((long)(ary[2] & 0xff) << 40) |
        ((long)(ary[3] & 0xff) << 32) |
        ((long)(ary[4] & 0xff) << 24) |
        ((long)(ary[5] & 0xff) << 16);
    }
    public MACAddress(){
        addr = 0xffffffffffffL;
    }
}
