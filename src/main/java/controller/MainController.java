package controller;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.Arrays;

public class MainController {

    @FXML
    private ComboBox<String> choosePort;

    @FXML
    private TextField textToSend;

    @FXML
    private TextArea receivedText;

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
        String port = choosePort.getValue();
        connectedPort = SerialPort.getCommPort(port);
        connectedPort.openPort();
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
    }

    public void reset() {
        String resetCode = "02003C3C00";
        connectedPort.writeBytes(resetCode.getBytes(), resetCode.getBytes().length);
    }

    public void send() {
        String message = textToSend.getText();
        connectedPort.writeBytes(message.getBytes(), message.getBytes().length);
    }
}
