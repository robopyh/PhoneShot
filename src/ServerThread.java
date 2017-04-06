import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.imageio.ImageIO;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

class ServerThread implements Runnable {
    private StreamConnection connection;
    private StreamConnectionNotifier notifier;

    ServerThread(){
        LocalDevice localDevice;

        //setting up server
        try {
            localDevice = LocalDevice.getLocalDevice();
            localDevice.setDiscoverable(DiscoveryAgent.GIAC);

            UUID uuid = new UUID("446118f08b1e11e29e960800200c9a66", false);
            String url = "btspp://localhost:" + uuid.toString() + ";name=PcServer";
            notifier = (StreamConnectionNotifier) Connector.open(url);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        //waiting for an incoming connection and sending a screenshot when it is established
        while (true) {
            try {
                System.out.println("waiting for connection...");
                assert notifier != null;
                connection = notifier.acceptAndOpen();
                sendScreenshot();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("close connection");
                return;
            }
        }
    }


    private void sendScreenshot() {
        try {
            //get rectangle for multiply monitors
            Rectangle rectangle = new Rectangle(0,0,0,0);
            for (GraphicsDevice graphicsDevice : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
                rectangle = rectangle.union(graphicsDevice.getDefaultConfiguration().getBounds());
            }

            //get screen capture
            Robot robot = new Robot();
            BufferedImage image = robot.createScreenCapture(rectangle);

            //convert buffered image into bytes array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", byteArrayOutputStream);
            byte[] bytes = byteArrayOutputStream.toByteArray();

            //write bytes to output stream
            DataOutputStream outputStream = new DataOutputStream(connection.openDataOutputStream());
            outputStream.writeInt(bytes.length);
            outputStream.write(bytes);

            System.out.println("send image (" + bytes.length + " bytes)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
