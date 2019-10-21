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

                StringBuilder message = new StringBuilder();

                for (byte data : getData) {
                    message.append((char) data);
                }
                receivedText.setText(receivedText.getText() +  message + "\n");
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
        String resetCode = "02003C3C00";
        connectedPort.writeBytes(resetCode.getBytes(), resetCode.getBytes().length);
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
