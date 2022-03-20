import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class SocketNetworkTrafficSimulatorJava {
    public static void main(String[] args) throws Exception {
        Random rn = new Random();
        ServerSocket welcomeSocket = new ServerSocket(9999);
        int[] possiblePortTypes = new int[]{21, 22, 80, 8080, 463};
        int numberOfRandomIps=100;
        String[] randomIps = new String[numberOfRandomIps];
        for (int i=0;i<numberOfRandomIps;i++)
            randomIps[i] = (rn.nextInt(250)+1) +"." +
                                (rn.nextInt(250)+1) +"." +
                                (rn.nextInt(250)+1) +"." +
                                (rn.nextInt(250)+1);
        System.err.println("Server started");
        while (true) {
            try {
                Socket connectionSocket = welcomeSocket.accept();
                System.err.println("Server accepted connection");
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                while (true) {
                    String str = "" + possiblePortTypes[rn.nextInt(possiblePortTypes.length)] + ","
                            + randomIps[rn.nextInt(numberOfRandomIps)] + ","
                            + randomIps[rn.nextInt(numberOfRandomIps)] + "\n";
                    outToClient.writeBytes(str);
                    Thread.sleep(10);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
