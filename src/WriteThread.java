import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class WriteThread extends Thread {
    private int PORT;
    private InetAddress HOST;
    private WritePacket writePacket;
    private DatagramSocket datagramSocket;
    private String fileName;
    private FileOutputStream fileOutputStream;

    private static final short OP_DATA = 3;
    private static final short OP_ERROR = 5;
    public static int MAX_PACKET_LENGTH = 516;

    public WriteThread(WritePacket writePacket) {
        this.writePacket = writePacket;
        this.PORT = writePacket.PORT;
        this.HOST = writePacket.HOST;
        openFile();
    }

    public void openFile() {
        try {
            datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(5000);
            fileName = writePacket.getFileName();

            File receiveFile = new File("." + File.separator + "files" + File.separator + fileName);
            if (!receiveFile.exists()) {
                fileOutputStream = new FileOutputStream(receiveFile);
                AckPacket sendAck = new AckPacket((short) 0);
                DatagramPacket responseAck = new DatagramPacket(sendAck.message, sendAck.length, HOST, PORT);
                datagramSocket.send(responseAck);
                this.start();
            } else {
                sendError(6, "File already exists");
            }
        } catch (Exception e) {
            sendError(0, e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            int blockLength = 512;
            int tries = 5;
            int blockNum = 1;
            while (blockLength == 512) {
                while (tries != 0) {
                    try {
                        byte[] message = new byte[MAX_PACKET_LENGTH];
                        DatagramPacket packet = new DatagramPacket(message, MAX_PACKET_LENGTH);
                        datagramSocket.receive(packet);

                        short op = (short) ((message[0] & 0xff) << 8 | message[1] & 0xff);

                        if (op == OP_DATA) {
                            DataPacket data = new DataPacket(packet.getData(), packet.getLength());
                            if (data.getBlockNum() != blockNum) {
                                throw new SocketTimeoutException();
                            }
                            blockLength = data.writeBlock(fileOutputStream);
                            AckPacket ackPacket = new AckPacket((short) (blockNum));
                            DatagramPacket ackResponse = new DatagramPacket(ackPacket.message, ackPacket.length, HOST, PORT);
                            datagramSocket.send(ackResponse);
                            break;

                        } else if (op == OP_ERROR) {
                            ErrorPacket error = new ErrorPacket(message, message.length);
                            throw new Exception(error.getMessage());
                        }
                    } catch (SocketTimeoutException s) {
                        System.out.println("Требуется повторная отправка пакета данных");
                        AckPacket ackPacket = new AckPacket((short) (blockNum - 1));
                        DatagramPacket errorResponse = new DatagramPacket(ackPacket.message, ackPacket.length, HOST, PORT);
                        datagramSocket.send(errorResponse);
                        tries--;
                    }

                }
                blockNum++;
                if (tries == 0) {
                    throw new Exception("Превышен лимит повторной отправки");
                }
            }
            fileOutputStream.close();
            System.out.println("Файл " + fileName + " успешно получен");
        } catch (Exception e) {
            sendError(0, e.getMessage());
        }

    }

    public void sendError(int code, String msg) {
        System.out.println("Ошибка получения файла: " + msg);
        ErrorPacket error = new ErrorPacket((short) code, msg);
        try {
            DatagramPacket errorResponse = new DatagramPacket(error.message, error.length, HOST, PORT);
            datagramSocket.send(errorResponse);
        } catch (IOException exception) {
            System.out.println("Ошибка отправки пакета клиенту");
        }
    }
}
