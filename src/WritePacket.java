import java.net.InetAddress;

public class WritePacket extends PacketTFTP{

    public WritePacket(byte[] receivedMessage, int port, InetAddress host) {
        this.OP_CODE = 2;
        this.message = receivedMessage;
        this.length = receivedMessage.length;
        this.PORT = port;
        this.HOST = host;
    }
    public String getFileName() {
        return this.getString(2);
    }
}
