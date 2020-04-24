import java.net.*;
import java.io.*;

public class UDPServer extends Thread {
    private DatagramSocket datagramSocket;
    private byte[] receive = new byte[65000];
    private DatagramPacket DpReceive = null;
    private String myDir;

    public UDPServer(int port, String my_dir) throws IOException {
        datagramSocket = new DatagramSocket(port);
        datagramSocket.setSoTimeout(15000);
        myDir = my_dir;
    }

    public void run() {
        System.out.println("Server Listening...");

        String outputFile;
        String logFile;
        String[] fileInfo;
        String startTime, endTime;
        int len;

        try {
            while (true) {
                // Receive fileInfo
                DpReceive = new DatagramPacket(receive, receive.length);
                datagramSocket.receive(DpReceive);
                fileInfo = data(receive).toString().split(",");
                receive = new byte[65000];
                sleep(2);
                System.out.println("File Info Recv.");
                System.out.println("File Info: " + fileInfo[0] + "," + fileInfo[1]);

                startTime = java.time.LocalDateTime.now().toString();

                // Create log file stream
                logFile = "log_" + fileInfo[0] + ".txt";
                OutputStream logStream = new FileOutputStream(logFile);

                // Create output file stream
                outputFile = myDir + fileInfo[0];
                OutputStream outputStream = new FileOutputStream(outputFile);

                int cnt = 0;
                len = Integer.parseInt(fileInfo[1]);

                logStream.write("\n------------".getBytes());
                logStream.write(("\n" + outputFile).getBytes());
                logStream.write(("\nStart Time: " + startTime).getBytes());
                logStream.write(("\n" + fileInfo[0]).getBytes());

                System.out.println("\nReceiving file contents...");
                double perc = 0.0;
                perc = (double) (cnt / len) * 100.0;
                System.out.println("Progress: " + perc);
                while (cnt <= len) {
                    // Receive 65000 bytes from client
                    DpReceive = new DatagramPacket(receive, receive.length);
                    datagramSocket.receive(DpReceive);

                    // Write to output file
                    outputStream.write(receive);
                    receive = new byte[65000];
                    sleep(2);

                    cnt = cnt + 65000;

                    perc = (double) ((cnt * 100) / len);
                    if (perc > 100)
                        perc = 100;
                    System.out.println("Progress: " + perc);

                    // Write to log file
                    logStream.write(("\nProgress: " + perc).getBytes());
                }

                endTime = java.time.LocalDateTime.now().toString();
                logStream.write(("\nEnd Time: " + endTime).getBytes());
                logStream.write("\n------------".getBytes());

                logStream.close();
                outputStream.close();

                System.out.println("\nFile: " + fileInfo[0] + " received.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\nServer done.\n");
    }

    public static void main(String[] args) {
        int my_port = 6070;
        int client_port = 6060;
        try {
            // Thread for receiving file
            Thread t = new UDPServer(my_port, "server_files/");
            t.start();

            // List of my files
            File curDir = new File("./server_files");
            String fileList = getAllFiles(curDir);
            String files[] = fileList.split(",");

            File curDir2 = new File("./client_files");
            String fileList2 = getAllFiles(curDir2);

            for (String f : files) {
                if (!fileList2.contains(f)) {
                    System.out.println("\nClient does not have " + f + "\nSending...");
                    // Thread for sending file
                    Thread t2 = new UDPClient(my_port + 1, "./server_files/" + f, client_port + 1);
                    t2.start();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static StringBuilder data(byte[] a) {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0) {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
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