import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws IOException {
        //starting server thread
        Thread ServerThread = new Thread(new ServerThread());
        ServerThread.start();

        //initiate connection to android device
        Scanner keyboard = new Scanner(System.in);
        while(true) {
            System.out.println("Enter 'get' to get screenshot");
            String input = keyboard.nextLine();
            if (input.equals("get")) {
                if (inputStream == null)
                    SetupConnection();
                new Thread(getScreenFromMobile).start();
            }
            else if (input.equals("end")) {
                System.exit(0);
            }

        }
    }

    private static DataInputStream inputStream;
    private static DataOutputStream outputStream;

    private static void SetupConnection() {
        try {
            UUID uuid = new UUID("0000110100001000800000805f9b34fb", false);
            //get remote device
            LocalDevice localDevice = LocalDevice.getLocalDevice();
            DiscoveryAgent agent = localDevice.getDiscoveryAgent();
            String url = agent.selectService(uuid, ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);

            try {
                //get input stream
                StreamConnection streamConnection = (StreamConnection) Connector.open(url);
                inputStream = streamConnection.openDataInputStream();
                outputStream = streamConnection.openDataOutputStream();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static Runnable getScreenFromMobile = () -> {
        try {
            outputStream.writeInt(1);

            //create new file
            Date date = new Date();
            File imageFile = new File("D:\\" + new SimpleDateFormat("dd-MM-yyyy_hh-mm").format(date) + ".png");
            FileOutputStream fileOutputStream = new FileOutputStream(imageFile);

            //get array for input bytes
            byte[] bytes = new byte[1000000];

            //image is ready to load
            System.out.println("Connecting...");
            inputStream.readInt();
            TimeUnit.SECONDS.sleep(3);

            //read bytes and write it into file
            while(inputStream.available() > 0) {
                int len = inputStream.read(bytes);
                TimeUnit.SECONDS.sleep(3);
                fileOutputStream.write(bytes, 0, len);
                System.out.println("bytes saved: " + len);
            }
            fileOutputStream.flush();
            fileOutputStream.close();
            System.out.println("receive screen");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    };
}
