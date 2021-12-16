import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class DataPacket extends PacketTFTP{
    public DataPacket(byte[] receivedData, int length) {
        this.OP_CODE = 3;
        this.message = receivedData;
        this.length = length;
    }

    public DataPacket(short blockNumber, FileInputStream data) throws IOException {
        this.OP_CODE = 3;
        this.message = new byte[MAX_PACKET_LENGTH];
        this.put(0, OP_CODE);
        this.put(2, blockNumber);
        length = 4 + data.read(message, 4, 512);
    }

    public int getBlockNum() {
        return this.get(2);
    }

    public int writeBlock(FileOutputStream fileOutputStream) throws IOException {
        fileOutputStream.write(message, 4, length - 4);
        return length - 4;
    }
}
