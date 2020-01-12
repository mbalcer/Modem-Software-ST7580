package controller;

import com.fazecast.jSerialComm.SerialPort;
import configuration.Configuration;
import configuration.LoadConfiguration;
import configuration.SaveConfiguration;
import frame.Frame;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import port.DisplayData;
import port.PortCOM;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
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
    private Button btnLoadConf;

    @FXML
    private Button btnSaveConf;

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
    private int byteModulation;
    private Mode mode;

    private Logger log = LoggerFactory.getLogger(MainController.class);

    public void initialize() {
        initToggleGroup(groupDisplayReceivedData, rbReceiveHEX, rbReceiveASCII);
        initToggleGroup(groupSendData, rbSendASCII, rbSendHEX);
        initToggleGroup(groupMode, rbDl, rbPhy);
        initToggleGroup(groupModulation, rbBPSK, rbQPSK, rb8PSK);
        mode = Mode.DL;
        sendData = DisplayData.ASCII;
        modulation = Modulation.BPSK;
        byteModulation = 4;
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
        Frame resetFrame = new Frame(0x3C);
        connectedPort.send(resetFrame.getBytes());
        log.info("Reset...");
        rbDl.setSelected(true);
        mode = Mode.DL;
    }

    @FXML
    public void send() {
        String message = textToSend.getText();
        if (sendData == DisplayData.ASCII) {
            connectedPort.send(makeFrameBeforeSend(message.getBytes()));
            info.setText("You have successfully sent the text in ASCII");
            log.info("Wiadomość została wysłana poprawnie (ASCII)");
            info.setTextFill(Paint.valueOf("GREEN"));
        } else if (sendData == DisplayData.HEX) {
            message = message.replaceAll("\\s+", "");
            try {
                byte[] bytes = DatatypeConverter.parseHexBinary(message);
                connectedPort.send(makeFrameBeforeSend(bytes));
                info.setText("You have successfully sent the text in HEX");
                log.info("Wiadomość została wysłana poprawnie (HEX)");
                info.setTextFill(Paint.valueOf("GREEN"));
            } catch (IllegalArgumentException e) {
                info.setText("You are trying to send incorrect char in HEX");
                log.error("Wiadomość nie została wysłana (HEX)");
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
            info.setText("You are connected to the port " + port.getPort().getSystemPortName());
            info.setTextFill(Paint.valueOf("GREEN"));
            log.info("Połączyłeś się z portem " + port.getPort().getSystemPortName());
            disableButtons(false);
        } else {
            info.setText("Unable to connect to port " + port.getPort().getSystemPortName());
            info.setTextFill(Paint.valueOf("RED"));
            log.error("Nie można się połączyć z portem " + port.getPort().getSystemPortName());
        }
    }

    private void checkClosePort(PortCOM port) {
        if (port.getPort().isOpen()) {
            info.setText("Cannot disconnect from port " + port.getPort().getSystemPortName());
            info.setTextFill(Paint.valueOf("RED"));
            log.error("Nie można zamknąć portu " + port.getPort().getSystemPortName());
        } else {
            info.setText("Disconnected from the port " + port.getPort().getSystemPortName());
            info.setTextFill(Paint.valueOf("GREEN"));
            log.info("Poprawnie zamknięto port " + port.getPort().getSystemPortName());
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
        btnLoadConf.setDisable(bool);
        btnSaveConf.setDisable(bool);
    }

    @FXML
    public void refreshPort() {
        fillComboBoxPorts();
        log.info("Lista portów została odświeżona");
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
        mode = Mode.PHY;
        setMode();
    }

    @FXML
    public void setDl() {
        mode = Mode.DL;
        setMode();
    }

    private void setMode() {
        int modeByte = (mode.equals(Mode.PHY)) ? 0x10 : 0x11;
        Frame modeFrame = new Frame(0x08, 0x00, modeByte);
        connectedPort.send(modeFrame.getBytes());
        info.setText("Changed mode on "+mode.toString());
        info.setTextFill(Paint.valueOf("GREEN"));
        log.info("Zmieniono tryb na "+mode.toString());
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

        byteModulation = 4 + (fec << 6) + (modulationValue << 4);
        log.info("Ustawiono modulację na " + modulation.toString() + ((fec==1)?" coded":""));
    }

    private byte[] makeFrameBeforeSend(byte[] data) {
        int modeValue = rbPhy.isSelected() ? 0x24 : 0x50;

        int[] dataInt = new int[data.length + 1];
        dataInt[0] = byteModulation;
        for (int i = 1; i < dataInt.length; i++) {
            dataInt[i] = data[i - 1] & 0xff;
        }

        Frame frame = new Frame(modeValue, dataInt);
        return frame.getBytes();
    }

    public void loadConfig() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File file = fileChooser.showOpenDialog(new Stage());
        LoadConfiguration loadConfiguration = new LoadConfiguration(file);
        Configuration config = loadConfiguration.getConfiguration();

        switch (config.getMode()) {
            case DL: setDl(); rbDl.setSelected(true); break;
            case PHY: setPhy(); rbPhy.setSelected(true); break;
        }
        cbModulationCoded.setSelected(config.getCoded());
        rb8PSK.setDisable(cbModulationCoded.isSelected());
        switch (config.getModulation()) {
            case BPSK: setBPSK(); rbBPSK.setSelected(true); break;
            case QPSK: setQPSK(); rbQPSK.setSelected(true); break;
            case PSK8: set8PSK(); rb8PSK.setSelected(true); break;
        }
    }

    public void saveConfig() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("properties", "*.properties"));
        File file = fileChooser.showSaveDialog(new Stage());
        if (file != null) {
            Configuration config = new Configuration(mode, modulation, false);
            SaveConfiguration saveConfiguration = new SaveConfiguration(file);
            saveConfiguration.saveConfiguration(config);
        }
    }


}
