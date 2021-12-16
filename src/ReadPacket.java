import java.net.InetAddress;

public class ReadPacket extends PacketTFTP{
    public ReadPacket(byte[] receivedMessage, int port, InetAddress host) {
        this.OP_CODE = 1;
        this.message = receivedMessage;
        this.length = receivedMessage.length;
        this.PORT = port;
        this.HOST = host;
    }

    public String getFileName() {
        return this.getString(2);
    }
}
