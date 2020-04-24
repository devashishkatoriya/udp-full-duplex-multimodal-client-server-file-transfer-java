import java.net.*;
import java.io.*;

public class UDPClient extends Thread {

    private DatagramSocket datagramSocket;

    int server_port;
    InetAddress clientAddress = InetAddress.getLocalHost();

    String inputFile;

    public UDPClient(int port, String fileName, int serverPort) throws IOException {
        datagramSocket = new DatagramSocket(port);
        datagramSocket.setSoTimeout(9000);
        server_port = serverPort;

        inputFile = fileName;
    }

    public void run() {
        System.out.println("Client started.");

        byte buf[] = null;

        int byteRead;
        int cnt = 0;
        buf = new byte[65000];

        try {
            sleep(3000);
            // Calculate file name and file size
            File f = new File(inputFile);
            long fileSize = f.length();
            String fileInfo = f.getName() + "," + fileSize;

            // Send fileInfo
            buf = fileInfo.getBytes();
            DatagramPacket DpSend = new DatagramPacket(buf, buf.length, clientAddress, server_port);
            datagramSocket.send(DpSend);
            sleep(10);
            System.out.println("File Info: " + fileInfo);
            System.out.println("File Info Sent.");

            // Open input file for reading contents
            InputStream inputStream = new FileInputStream(inputFile);

            System.out.println("\nSending file contents...");
            buf = new byte[65000];
            while ((byteRead = inputStream.read()) != -1) {

                buf[cnt % 65000] = (byte) byteRead;
                if ((cnt + 1) % 65000 == 0) {
                    // Send 65000 bytes to server
                    DpSend = new DatagramPacket(buf, buf.length, clientAddress, server_port);
                    datagramSocket.send(DpSend);
                    sleep(10);

                    buf = new byte[65000];
                    System.out.println("Bytes Sent: " + cnt);
                }
                cnt = cnt + 1;
            }
            // Send final buffer
            if (cnt != 0) {
                DpSend = new DatagramPacket(buf, (cnt % 65000) + 1, clientAddress, server_port);
                datagramSocket.send(DpSend);
                buf = new byte[cnt + 1];
                sleep(10);
                System.out.println("Final Bytes Sent: " + cnt);
            }
            inputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\nClient done!");
    }

    public static void main(String[] args) throws IOException {
        int my_port = 6060;
        int server_port = 6070;
        try {

            // List of my files
            File curDir = new File("./client_files");
            String fileList = getAllFiles(curDir);
            String files[] = fileList.split(",");

            File curDir2 = new File("./server_files");
            String fileList2 = getAllFiles(curDir2);

            for (String f : files) {
                if (!fileList2.contains(f)) {
                    System.out.println("\nServer does not have " + f + "\nSending...");
                    // Thread for sending file
                    Thread t = new UDPClient(my_port, "./client_files/" + f, server_port);
                    t.start();
                }
            }

            // Thread for receiving file
            Thread t2 = new UDPServer(my_port + 1, "client_files/");
            t2.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getAllFiles(File curDir) {

        String files = "";

        File[] filesList = curDir.listFiles();
        for (File f : filesList) {
            if (f.isDirectory())
                getAllFiles(f);
            if (f.isFile()) {
                files = files + "," + f.getName();
                // System.out.println(f.getName());
            }
        }
        return files;
    }

}