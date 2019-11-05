package controller;

import com.fazecast.jSerialComm.SerialPort;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Paint;
import model.DisplayData;
import model.Parity;
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
    private ComboBox<Parity> cbParity;

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
        fillComboBoxPorts();
        fillComboBoxBaudRate();
        fillComboBoxDataBits();
        fillComboBoxStopBits();
        fillComboBoxParity();
        setDefaultParam();
        disableButtons(true);
    }

    private void fillComboBoxPorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        ObservableList<String> namePorts = FXCollections.observableArrayList();
        Arrays.stream(ports)
                .forEach(p -> namePorts.add(p.getSystemPortName()));
        choosePort.setItems(namePorts);
    }

    private void fillComboBoxBaudRate() {
        Integer[] baudRates = {600, 1200, 2400, 4800, 9600, 14400, 19200, 28800, 38400, 56000, 57600, 115200, 12800, 256000};
        cbBaudRate.setItems(FXCollections.observableArrayList(baudRates));
    }

    private void fillComboBoxDataBits() {
        Integer[] dataBits = {5, 6, 7, 8};
        cbDataBits.setItems(FXCollections.observableArrayList(dataBits));
    }

    private void fillComboBoxStopBits() {
        Integer[] stopBits = {1, 2};
        cbStopBits.setItems(FXCollections.observableArrayList(stopBits));
    }

    private void fillComboBoxParity() {
        cbParity.setItems(FXCollections.observableArrayList(Parity.values()));
    }

    @FXML
    public void connect() {
        connectedPort = new PortCOM(getPortFromCheckbox(), receivedText);
        connectedPort.setParam(cbBaudRate.getValue(), cbDataBits.getValue(), cbStopBits.getValue(), cbParity.getValue().ordinal());
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
        cbBaudRate.setDisable(!bool);
        cbDataBits.setDisable(!bool);
        cbStopBits.setDisable(!bool);
        cbParity.setDisable(!bool);

        btnDisconnect.setDisable(bool);
        btnResetModem.setDisable(bool);
        btnSend.setDisable(bool);
    }

    @FXML
    public void refreshPort() {
        fillComboBoxPorts();
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

    private void setDefaultParam() {
        cbBaudRate.setValue(57600);
        cbDataBits.setValue(8);
        cbStopBits.setValue(1);
        cbParity.setValue(Parity.NONE);
    }
}
