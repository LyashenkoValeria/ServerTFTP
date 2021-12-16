import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class PacketTFTP {
    static int MAX_PACKET_LENGTH = 516;

    byte[] message = new byte[MAX_PACKET_LENGTH];
    int length = message.length;
    int PORT;
    InetAddress HOST;
    short OP_CODE;

    public PacketTFTP(){
    }

    int get(int index) {
        return (message[index] & 0xff) << 8 | message[index + 1] & 0xff;
    }

    public String getString(int start) {
        int end = start;
        while (message[end] != 0) end++;
        String resString = new String(Arrays.copyOfRange(message, start, end), StandardCharsets.UTF_8);
        return resString;
    }

    public void put(int i, short value) {
        message[i++] = (byte) (value / 256);
        message[i] = (byte) (value % 256);
    }

    public void put(int start, String string) {
        byte [] strBytes = string.getBytes();
        for (int i = start; i < length - 1; i++){
            message[i] = strBytes[i-4];
        }
        message[length-1] = 0;
    }
}
