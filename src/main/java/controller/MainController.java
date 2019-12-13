package controller;

import com.fazecast.jSerialComm.SerialPort;
import frame.Frame;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Paint;
import port.DisplayData;
import port.PortCOM;

import javax.xml.bind.DatatypeConverter;
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
    private RadioButton rbSendASCII;

    @FXML
    private RadioButton rbSendHEX;

    @FXML
    private RadioButton rbPhy;

    @FXML
    private RadioButton rbDl;

    @FXML
    private RadioButton rbQPSK;

    @FXML
    private RadioButton rbBPSK;

    @FXML
    private RadioButton rb8PSK;

    @FXML
    private CheckBox cbModulationCoded;

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
    private ToggleGroup groupSendData;
    private ToggleGroup groupMode;
    private ToggleGroup groupModulation;
    private DisplayData sendData;
    private Modulation modulation;

    public void initialize() {
        initToggleGroup(groupDisplayReceivedData, rbReceiveHEX, rbReceiveASCII);
        initToggleGroup(groupSendData, rbSendASCII, rbSendHEX);
        initToggleGroup(groupMode, rbDl, rbPhy);
        initToggleGroup(groupModulation, rbBPSK, rbQPSK, rb8PSK);
        sendData = DisplayData.ASCII;
        modulation = Modulation.BPSK;
        fillComboBoxPorts();
        fillComboBoxBaudRate();
        fillComboBoxDataBits();
        fillComboBoxStopBits();
        fillComboBoxParity();
        setDefaultParam();
        disableButtons(true);
    }

    private void initToggleGroup(ToggleGroup group, RadioButton... radioButtons) {
        group = new ToggleGroup();
        for (RadioButton r : radioButtons)
            r.setToggleGroup(group);
        radioButtons[0].setSelected(true);
    }

    private void fillComboBoxPorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        ObservableList<String> namePorts = FXCollections.observableArrayList();
        Arrays.stream(ports)
                .forEach(p -> namePorts.add(p.getSystemPortName()));
        choosePort.setItems(namePorts);
    }

    private void fillComboBoxBaudRate() {
        Integer[] baudRates = {600, 1200, 2400, 4800, 9600, 14400, 19200, 28800, 38400, 56000, 57600, 115200, 128000, 256000};
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

    private void setDefaultParam() {
        cbBaudRate.setValue(57600);
        cbDataBits.setValue(8);
        cbStopBits.setValue(1);
        cbParity.setValue(Parity.NONE);
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
//        final byte[] resetCode = {0x02, 0x00, 0x3C, 0x3C, 0x00};
        Frame resetFrame = new Frame((byte) 0x3C);
        connectedPort.send(resetFrame.getBytes());
        System.out.println("Reset...");
        rbDl.setSelected(true);
    }

    @FXML
    public void send() {
        String message = textToSend.getText();
        if (sendData == DisplayData.ASCII) {
            connectedPort.send(message.getBytes());
            info.setText("You have successfully sent the text in ASCII");
            info.setTextFill(Paint.valueOf("GREEN"));
        } else if (sendData == DisplayData.HEX) {
            message = message.replaceAll("\\s+","");
            try {
                byte[] bytes = DatatypeConverter.parseHexBinary(message);
                connectedPort.send(bytes);
                info.setText("You have successfully sent the text in HEX");
                info.setTextFill(Paint.valueOf("GREEN"));
            } catch (IllegalArgumentException e) {
                info.setText("You are trying to send incorrect char in HEX");
                info.setTextFill(Paint.valueOf("RED"));
            }
        }
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
        rbReceiveHEX.setDisable(bool);
        rbReceiveASCII.setDisable(bool);
        rbSendASCII.setDisable(bool);
        rbSendHEX.setDisable(bool);
        rbDl.setDisable(bool);
        rbPhy.setDisable(bool);
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

    @FXML
    public void setSendASCII() {
        sendData = DisplayData.ASCII;
    }

    @FXML
    public void setSendHEX() {
        sendData = DisplayData.HEX;
    }

    private void resetReceiveData() {
        receivedText.clear();
        connectedPort.closeListener();
        connectedPort.activeListener();
    }

    @FXML
    public void setPhy() {
        Frame phyFrame = new Frame((byte) 0x08, (byte) 0x00, (byte) 0x10);
        connectedPort.send(phyFrame.getBytes());
        info.setText("Changed mode on PHY");
        info.setTextFill(Paint.valueOf("GREEN"));
    }

    @FXML
    public void setDl() {
        Frame dlFrame = new Frame((byte) 0x08, (byte) 0x00, (byte) 0x11);
        connectedPort.send(dlFrame.getBytes());
        info.setText("Changed mode on DL");
        info.setTextFill(Paint.valueOf("GREEN"));
    }

    @FXML
    public void setBPSK() {
        modulation = Modulation.BPSK;
        cbModulationCoded.setDisable(false);
        setModulation();
    }

    @FXML
    public void setQPSK() {
        modulation = Modulation.QPSK;
        cbModulationCoded.setDisable(false);
        setModulation();
    }

    @FXML
    public void set8PSK() {
        modulation = Modulation.PSK8;
        cbModulationCoded.setDisable(true);
        setModulation();
    }

    @FXML
    public void setModulationCoded() {
        rb8PSK.setDisable(cbModulationCoded.isSelected());
        setModulation();
    }

    private void setModulation() {
        int modulationValue = modulation.ordinal();
        int fec = cbModulationCoded.isSelected() ? 1 : 0;
        int modeValue = rbPhy.isSelected() ? 0x24 : 0x50;

        int b = 4 + (fec << 6) + (modulationValue << 4);

        Frame modulationFrame = new Frame((byte) modeValue, (byte) b);
        connectedPort.send(modulationFrame.getBytes());
    }
}
