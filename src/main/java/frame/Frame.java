package frame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Frame {
    private final Integer STX = 0x02;
    private final Integer STX_2 = 0x03;

    private FrameStatus frameStatus;
    private Integer begin;
    private Integer len;
    private Integer commandCode;
    private Integer status;
    private Integer[] data;
    private Integer[] checkSum;
    private Integer counterData;
    private Boolean correctFrame;
    private Boolean sendAck;
    private Integer receiveAck;

    private Logger log = LoggerFactory.getLogger(Frame.class);

    public Frame() {
        frameStatus = FrameStatus.BEGIN;
        checkSum = new Integer[2];
        correctFrame = false;
    }

    public Frame(int cc, int... data) {
        this();
        this.begin = STX;
        this.len = data.length;
        this.commandCode = cc;
        this.data = new Integer[data.length];
        for (int i = 0; i < data.length; i++) {
            this.data[i] = data[i];
        }

        Integer fcs = this.len + this.commandCode.intValue();

        for (Integer d : data) {
            fcs += d.intValue();
        }

        checkSum[0] = fcs & 0xff;
        checkSum[1] = (fcs & 0xff00) >> 8;
    }

    public void processFrame(Integer receivedByte) {
        log.info(frameStatus.toString() + " " + String.format("0x%02x", receivedByte));
        switch (frameStatus) {
            case BEGIN:
                clearFrame();
                if (receivedByte == STX || receivedByte == STX_2) {
                    frameStatus = FrameStatus.LEN;
                    begin = receivedByte;
                    if (receivedByte == STX)
                        sendAck = true;
                    else
                        sendAck = false;
                } else if (receivedByte == 0x3F) {
                    frameStatus = FrameStatus.STATUS;
                    begin = receivedByte;
                } else if (receivedByte == 0x06 || receivedByte == 0x15) {
                    receiveAck = receivedByte;
                }
                break;
            case LEN:
                len = receivedByte.intValue();
                data = new Integer[len];
                frameStatus = FrameStatus.COMMAND;
                break;
            case COMMAND:
                commandCode = receivedByte;
                if (len == 0)
                    frameStatus = FrameStatus.FIRST_FCS;
                else
                    frameStatus = FrameStatus.DATA;
                break;
            case DATA:
                if (counterData < len) {
                    data[counterData] = receivedByte;
                    counterData++;
                    if (counterData == len)
                        frameStatus = FrameStatus.FIRST_FCS;
                }
                break;
            case FIRST_FCS:
                checkSum[0] = receivedByte;
                frameStatus = FrameStatus.SECOND_FCS;
                break;
            case SECOND_FCS:
                checkSum[1] = receivedByte;
                checkCorrectFrame();
                frameStatus = FrameStatus.BEGIN;
                break;
            case STATUS:
                status = receivedByte;
                frameStatus = FrameStatus.BEGIN;
                break;
        }
    }

    public Boolean isCorrectFrame() {
        return correctFrame;
    }

    public Boolean isSendAck() {
        return sendAck;
    }

    public List<Integer> getFrame() {
        List<Integer> frame = new ArrayList<>();
        frame.add(begin);
        frame.add(len);
        frame.add(commandCode);
        frame.addAll(Arrays.asList(data));
        frame.addAll(Arrays.asList(checkSum));

        return frame;
    }

    public byte[] getBytes() {
        List<Integer> frame = getFrame();
        byte[] bytes = new byte[frame.size()];
        for (int i = 0; i < frame.size(); i++)
            bytes[i] = frame.get(i).byteValue();

        return bytes;
    }

    public void checkCorrectFrame() {
        Integer lenFrame = len + commandCode.intValue();
        for (Integer d : data) {
            lenFrame += d.intValue();
        }
        Integer lenFromCheckSum = checkSum[1].intValue() << 8 | checkSum[0].intValue();

        if (lenFrame.equals(lenFromCheckSum)) {
            correctFrame = true;
            log.info("Poprawna ramka");
        } else {
            correctFrame = false;
            log.error("Niepoprawna suma kontrolna " + lenFrame + " != " + lenFromCheckSum);
        }
    }

    public void clearFrame() {
        counterData = 0;
        correctFrame = false;
        status = null;
    }

    public Integer getStatus() {
        return status;
    }

    public Integer getReceiveAck() {
        return receiveAck;
    }

    public void setReceiveAck(Integer receiveAck) {
        this.receiveAck = receiveAck;
    }
}
