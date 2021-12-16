import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;

public class ReadThread extends Thread{
    private int PORT;
    private InetAddress HOST;
    private ReadPacket readPacket;
    private DatagramSocket datagramSocket;
    private String fileName;
    private FileInputStream fileInputStream;

    private static final short OP_ACK = 4;

    public ReadThread(ReadPacket readPacket) {
        this.readPacket = readPacket;
        this.PORT = readPacket.PORT;
        this.HOST = readPacket.HOST;
        openFile();
    }

    public void openFile(){
        try {
            datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(5000);
            fileName = readPacket.getFileName();

            File sendFile = new File("."+ File.separator + "files"+ File.separator + fileName);
            fileInputStream = new FileInputStream(sendFile);
            this.start();
        } catch (Exception e) {
            sendError();
        }
    }

    @Override
    public void run(){
        try {
            int blockLength = 516;
            int blockNum = 1;
            while (blockLength == 516) {
                DataPacket data = new DataPacket((short) blockNum,fileInputStream);
                blockLength = data.length;
                DatagramPacket dataResponse = new DatagramPacket(data.message, data.length, HOST, PORT);
                datagramSocket.send(dataResponse);

                int tries = 5;
                while (tries != 0){
                    try {
                        byte[] ackBytes = new byte[4];
                        DatagramPacket ackPacket = new DatagramPacket(ackBytes,4);
                        datagramSocket.receive(ackPacket);
                        ackBytes = ackPacket.getData();
                        short op = (short) ((ackBytes[0] & 0xff) << 8 | ackBytes[1] & 0xff);

                        if (op == OP_ACK){
                            AckPacket receivedAck = new AckPacket();
                            receivedAck.message = ackBytes;
                            receivedAck.length = ackBytes.length;

                            if(receivedAck.getBlockNum() != blockNum){
                                throw new SocketTimeoutException("Потеря пакета");
                            }
                        } else {
                            throw new Exception("Не пришло подтверждение");
                        }
                        break;
                    } catch (SocketTimeoutException s){
                        System.out.println("Повторная отправка пакета данных");
                        datagramSocket.send(dataResponse);
                        tries--;
                    }
                }
                blockNum++;
                if (tries == 0){
                    throw new Exception("Превышен лимит повторной отправки");
                }
            }
            fileInputStream.close();
            System.out.println("Файл " + fileName + " успешно передан");
        } catch (Exception e){
            sendError();
        }
    }

    public void sendError(){
        System.out.println("Ошибка чтения файла");
        ErrorPacket error = new ErrorPacket((short) 1,"File not found");
        try {
            DatagramPacket errorResponse = new DatagramPacket(error.message, error.length, HOST, PORT);
            datagramSocket.send(errorResponse);
        } catch (IOException exception) {
            System.out.println("Ошибка отправки пакета клиенту");
        }
    }

}
