package port;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import frame.Frame;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.InputStream;

public class PortCOM {
    private final Byte ACK = 0x06;
    private final Byte NACK = 0x15;

    private SerialPort port;
    private Frame frame;
    private TextArea receivedText;
    private DisplayData displayReceivedData;

    public PortCOM(SerialPort port, TextArea receivedText) {
        this.port = port;
        this.displayReceivedData = DisplayData.HEX;
        this.frame = new Frame();
        this.receivedText = receivedText;
    }

    public void activeListener() {
        this.port.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }
            @Override
            public void serialEvent(SerialPortEvent event)
            {
                byte[] getByte = new byte[1];
                InputStream in = port.getInputStream();


                while(port.bytesAvailable() > 0) {
                    try {
                        in.read(getByte, 0, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    frame.processFrame(getByte[0]);
                }

                if (frame.isCorrectFrame()) {
                    if (frame.isSendAck()) {
                        port.writeBytes(new byte[]{ACK}, 1);
                        System.out.println("Wysłano ACK ");
                    }

                    frame.getFrame().stream()
                            .forEach(data -> {
                                     if (displayReceivedData == DisplayData.HEX) {
                                         appendTextToTextField(String.format("0x%02x", data) + " ");
                                     } else {
                                         appendTextToTextField(String.valueOf((char)data.byteValue()));
                                     }
                            });

                    appendTextToTextField("\n");
                }
            }
        });
    }

    public void closeListener() {
        this.port.removeDataListener();
    }

    public void open() {
        activeListener();
        this.port.openPort();
    }

    public void close() {
        closeListener();
        this.port.closePort();
    }

    public void send(byte[] bytes) {
        this.port.setRTS();
        this.port.setDTR();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.port.writeBytes(bytes, bytes.length);
        this.port.clearRTS();
        this.port.clearDTR();
    }

    private void appendTextToTextField(String text) {
        javafx.application.Platform.runLater(() -> receivedText.appendText(text));
    }

    public SerialPort getPort() {
        return port;
    }

    public void setDisplayReceivedData(DisplayData displayReceivedData) {
        this.displayReceivedData = displayReceivedData;
    }

    public void setParam(Integer baudRate, Integer dataBits, Integer stopBits, Integer parity) {
        port.setBaudRate(baudRate);
        port.setNumDataBits(dataBits);
        port.setNumStopBits(stopBits);
        port.setParity(parity);
    }
}