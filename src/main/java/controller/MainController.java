package controller;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Paint;

import java.util.Arrays;

public class MainController {

    @FXML
    private ComboBox<String> choosePort;

    @FXML
    private TextField textToSend;

    @FXML
    private TextArea receivedText;

    @FXML
    private Label info;

    private SerialPort connectedPort;

    public void initialize() {
        SerialPort[] ports = SerialPort.getCommPorts();
        fillComboBox(ports);
    }

    private void fillComboBox(SerialPort[] ports) {
        ObservableList<String> namePorts = FXCollections.observableArrayList();
        Arrays.stream(ports)
                .forEach(p -> namePorts.add(p.getSystemPortName()));
        choosePort.setItems(namePorts);
    }

    public void connect() {
        connectedPort.openPort();
        connectedPort.setBaudRate(57600);
        connectedPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_RECEIVED; }
            @Override
            public void serialEvent(SerialPortEvent event)
            {
                byte[] getData = event.getReceivedData();

                for (byte data : getData) {
                    receivedText.setText(receivedText.getText() + String.format("%02x", data) + " ");
                }
                receivedText.setText(receivedText.getText() + "\n");
            }
        });

        checkOpenPort(connectedPort);
    }

    private void checkOpenPort(SerialPort port) {
        if (port.isOpen()) {
            info.setText("You are connected to the port " +port.getSystemPortName());
            info.setTextFill(Paint.valueOf("GREEN"));
        }
        else {
            info.setText("Unable to connect to port " + port.getSystemPortName());
            info.setTextFill(Paint.valueOf("RED"));
        }
    }

    public void reset() {
        final byte[] resetCode = {0x02, 0x00, 0x3C, 0x3C, 0x00};
        connectedPort.setDTR();
        connectedPort.setRTS();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        connectedPort.writeBytes(resetCode, resetCode.length);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        connectedPort.clearDTR();
        connectedPort.clearRTS();
    }

    public void send() {
        String message = textToSend.getText();
        connectedPort.writeBytes(message.getBytes(), message.getBytes().length);
    }

    public void disconnect() {
        connectedPort.removeDataListener();
        connectedPort.closePort();
        checkClosePort(connectedPort);
    }

    public void changeConnectedPort() {
        connectedPort = getPortFromCheckbox();
    }

    private void checkClosePort(SerialPort port) {
        if (port.isOpen()) {
            info.setText("Cannot disconnect from port " +port.getSystemPortName());
            info.setTextFill(Paint.valueOf("RED"));
        }
        else {
            info.setText("Disconnected from the port " + port.getSystemPortName());
            info.setTextFill(Paint.valueOf("GREEN"));
        }
    }

    private SerialPort getPortFromCheckbox() {
        return SerialPort.getCommPort(choosePort.getValue());
    }
}
