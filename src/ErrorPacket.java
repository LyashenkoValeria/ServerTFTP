public class ErrorPacket extends PacketTFTP{
    public ErrorPacket(byte[] errorMessage, int length) {
        OP_CODE = 5;
        this.message = errorMessage;
        this.length = length;
    }

    public ErrorPacket(short errorCode, String message) {
        OP_CODE = 5;
        length = 4 + message.length() + 1;
        this.message = new byte[length];
        put(0, OP_CODE);
        put(2, errorCode);
        put(4, message);
    }

    public String getMessage() {
        return this.getString(4);
    }
}
