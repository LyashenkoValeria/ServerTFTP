public class AckPacket extends PacketTFTP{
    public AckPacket() {
        OP_CODE = 4;
    }

    public AckPacket(short blockNum) {
        OP_CODE = 4;
        length = 4;
        this.message = new byte[length];
        put(0, OP_CODE);
        put(2, blockNum);
    }
    public int getBlockNum() {
        return this.get(2);
    }
}