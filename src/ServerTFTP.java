import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class ServerTFTP {
    private static final int PORT = 69;
    private static final String HOST = "127.0.0.1";
    public static int MAX_PACKET_LENGTH = 516;

    private static final short OP_RRQ = 1;
    private static final short OP_WRQ = 2;

    private DatagramSocket serverSocket;
    private static BufferedReader readerServer;
    private boolean isClose = false;

    Runnable readServerCommand = () -> {
        try {
            readerServer = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String command = readerServer.readLine();
                if (command.equals("/stop")) {
                    System.out.println("Сервер закончил работу");
                    isClose = true;
                    serverSocket.close();
                    System.exit(1);
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка чтения команды серверу");
        }
    };

    public static void main(String[] args) {
        new ServerTFTP().launch();
    }

    public void launch() {
        System.out.println("Сервер начал работу");
        Thread commandThread = new Thread(readServerCommand);
        commandThread.start();

        try {
            serverSocket = new DatagramSocket(PORT, InetAddress.getByName(HOST));

            while (true) {
                byte[] message = new byte[MAX_PACKET_LENGTH];
                DatagramPacket packet = new DatagramPacket(message, message.length);
                serverSocket.receive(packet);
                short operationCode = getOpCode(message);

                switch (operationCode) {
                    case OP_RRQ -> {
                        System.out.println("\nКлиент отправил запрос на чтение файла с сервер");
                        ReadPacket readPacket = new ReadPacket(message, packet.getPort(), packet.getAddress());
                        new ReadThread(readPacket);
                    }
                    case OP_WRQ -> {
                        System.out.println("\nКлиент отправил запрос на запись файла на сервер");
                        WritePacket writePacket = new WritePacket(message, packet.getPort(), packet.getAddress());
                        new WriteThread(writePacket);
                    }
                }
            }
        } catch (Exception e) {
            if (!isClose) {
                System.out.println("Ошибка подключения сервера");
                System.exit(1);
            }
        }
    }


    public short getOpCode(byte[] message) {
        return (short) ((message[0] & 0xff) << 8 | message[1] & 0xff);
    }
}
