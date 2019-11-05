package controller;

import com.fazecast.jSerialComm.SerialPort;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Paint;
import model.DisplayData;
import model.PortCOM;

import java.util.Arrays;

public class MainController {

    @FXML
    private ComboBox<String> choosePort;

    @FXML
    private ComboBox<Integer> cbBaudRate;

    @FXML
    private ComboBox<Integer> cbDataBits;

    @FXML
    private ComboBox<Integer> cbStopBits;

    @FXML
    private ComboBox<Integer> cbPartity;

    @FXML
    private RadioButton rbReceiveASCII;

    @FXML
    private RadioButton rbReceiveHEX;

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

    private PortCOM connectedPort;
    private ToggleGroup groupDisplayReceivedData;

    public void initialize() {
        groupDisplayReceivedData = new ToggleGroup();
        rbReceiveASCII.setToggleGroup(groupDisplayReceivedData);
        rbReceiveHEX.setToggleGroup(groupDisplayReceivedData);
        rbReceiveHEX.setSelected(true);
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
        connectedPort = new PortCOM(getPortFromCheckbox(), receivedText);
        connectedPort.open();
        checkOpenPort(connectedPort);
    }

    @FXML
    public void reset() {
        final byte[] resetCode = {0x02, 0x00, 0x3C, 0x3C, 0x00};
        connectedPort.send(resetCode);
    }

    @FXML
    public void send() {
        String message = textToSend.getText();
        connectedPort.send(message.getBytes());
    }

    @FXML
    public void disconnect() {
        connectedPort.close();
        checkClosePort(connectedPort);
    }

    private void checkOpenPort(PortCOM port) {
        if (port.getPort().isOpen()) {
            info.setText("You are connected to the port " +port.getPort().getSystemPortName());
            info.setTextFill(Paint.valueOf("GREEN"));
            disableButtons(false);
        }
        else {
            info.setText("Unable to connect to port " + port.getPort().getSystemPortName());
            info.setTextFill(Paint.valueOf("RED"));
        }
    }

    private void checkClosePort(PortCOM port) {
        if (port.getPort().isOpen()) {
            info.setText("Cannot disconnect from port " +port.getPort().getSystemPortName());
            info.setTextFill(Paint.valueOf("RED"));
        }
        else {
            info.setText("Disconnected from the port " + port.getPort().getSystemPortName());
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

    @FXML
    public void setReceiveInAscii() {
        connectedPort.setDisplayReceivedData(DisplayData.ASCII);
        resetReceiveData();
    }

    @FXML
    public void setReceiveInHex() {
        connectedPort.setDisplayReceivedData(DisplayData.HEX);
        resetReceiveData();
    }

    private void resetReceiveData() {
        receivedText.clear();
        connectedPort.closeListener();
        connectedPort.activeListener();
    }
}
