package controller;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Paint;
import model.Frame;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class MainController {

    @FXML
    private ComboBox<String> choosePort;

    @FXML
    private Button btnRefresh;

    @FXML
    private Button btnConnect;

    @FXML
    private Button btnDisconnect;

    @FXML
    private Button btnSend;

    @FXML
    private Button btnResetModem;

    @FXML
    private Button btnClear;

    @FXML
    private TextField textToSend;

    @FXML
    private TextArea receivedText;

    @FXML
    private Label info;

    private SerialPort connectedPort;
    private Frame frame;

    public void initialize() {
        fillComboBox();
        disableButtons(true);
    }

    private void fillComboBox() {
        SerialPort[] ports = SerialPort.getCommPorts();
        ObservableList<String> namePorts = FXCollections.observableArrayList();
        Arrays.stream(ports)
                .forEach(p -> namePorts.add(p.getSystemPortName()));
        choosePort.setItems(namePorts);
    }

    @FXML
    public void connect() {
        connectedPort.openPort();
        connectedPort.setBaudRate(57600);
        frame = new Frame();
        connectedPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }
            @Override
            public void serialEvent(SerialPortEvent event)
            {
                byte[] getByte = new byte[1];
                InputStream in = connectedPort.getInputStream();


                while(connectedPort.bytesAvailable() > 0) {
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

        checkOpenPort(connectedPort);
    }

    @FXML
    public void reset() {
        final byte[] resetCode = {0x02, 0x00, 0x3C, 0x3C, 0x00};
        sendText(resetCode);
    }

    @FXML
    public void send() {
        String message = textToSend.getText();
        sendText(message.getBytes());
    }

    public void sendText(byte[] bytes) {
        connectedPort.setDTR();
        connectedPort.setRTS();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        connectedPort.writeBytes(bytes, bytes.length);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        connectedPort.clearDTR();
        connectedPort.clearRTS();
    }

    @FXML
    public void disconnect() {
        connectedPort.removeDataListener();
        connectedPort.closePort();
        checkClosePort(connectedPort);
    }

    @FXML
    public void changeConnectedPort() {
        connectedPort = getPortFromCheckbox();
    }

    private void checkOpenPort(SerialPort port) {
        if (port.isOpen()) {
            info.setText("You are connected to the port " +port.getSystemPortName());
            info.setTextFill(Paint.valueOf("GREEN"));
            disableButtons(false);
        }
        else {
            info.setText("Unable to connect to port " + port.getSystemPortName());
            info.setTextFill(Paint.valueOf("RED"));
        }
    }

    private void checkClosePort(SerialPort port) {
        if (port.isOpen()) {
            info.setText("Cannot disconnect from port " +port.getSystemPortName());
            info.setTextFill(Paint.valueOf("RED"));
        }
        else {
            info.setText("Disconnected from the port " + port.getSystemPortName());
            info.setTextFill(Paint.valueOf("GREEN"));
            disableButtons(true);
        }
    }

    private SerialPort getPortFromCheckbox() {
        return SerialPort.getCommPort(choosePort.getValue());
    }

    @FXML
    public void disableButtons(boolean bool) {
        btnRefresh.setDisable(!bool);
        btnConnect.setDisable(!bool);
        choosePort.setDisable(!bool);

        btnDisconnect.setDisable(bool);
        btnResetModem.setDisable(bool);
        btnSend.setDisable(bool);
    }

    @FXML
    public void refreshPort() {
        fillComboBox();
    }

    @FXML
    public void clear() {
        textToSend.clear();
        receivedText.clear();
    }
}
