package model;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class PortCOM {
    private SerialPort port;
    private Frame frame;
    private TextArea receivedText;

    public PortCOM(SerialPort port, TextArea receivedText) {
        this.port = port;
        this.port.setBaudRate(57600);
        this.frame = new Frame();
        this.receivedText = receivedText;
    }

    private void activeListener() {
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
                    Arrays.stream(frame.getData())
                            .forEach(data -> receivedText.setText(receivedText.getText() + String.format("%02x", data) + " "));

                    receivedText.setText(receivedText.getText() + "\n");
                }
            }
        });
    }

    private void closeListener() {
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
        this.port.setDTR();
        this.port.setRTS();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.port.writeBytes(bytes, bytes.length);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.port.clearDTR();
        this.port.clearRTS();
    }

    public SerialPort getPort() {
        return port;
    }

    public void setPort(SerialPort port) {
        this.port = port;
    }

    public TextArea getReceivedText() {
        return receivedText;
    }

    public void setReceivedText(TextArea receivedText) {
        this.receivedText = receivedText;
    }
}
