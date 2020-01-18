package port;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import frame.Frame;
import javafx.scene.control.TextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class PortCOM {
    private final Byte ACK = 0x06;
    private final Byte NACK = 0x15;

    private SerialPort port;
    private Frame frame;
    private TextArea receivedText;
    private DisplayData displayReceivedData;

    private Logger log = LoggerFactory.getLogger(PortCOM.class);

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

                    frame.processFrame((int)getByte[0] & 0xff);

                    if (frame.isCorrectFrame()) {
                        if (frame.isSendAck()) {
                            port.writeBytes(new byte[]{ACK}, 1);
                            log.info("Wysłano ACK ");
                        }

                        frame.getFrame().stream()
                                .forEach(data -> displayData(data));

                        appendTextToTextField("\n");
                    }
                    else if(frame.getStatus()!=null) {
                        displayData(0x3F);
                        displayData(frame.getStatus());

                        appendTextToTextField("\n");
                    } else if (frame.getReceiveAck()!=null) {
                        displayData(frame.getReceiveAck());
                        appendTextToTextField("\n");
                        frame.setReceiveAck(null);
                    }
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

    private void displayData(Integer data) {
        if (displayReceivedData == DisplayData.HEX) {
            appendTextToTextField(String.format("0x%02x", data) + " ");
        } else {
            appendTextToTextField(String.valueOf((char)data.intValue()));
        }
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
