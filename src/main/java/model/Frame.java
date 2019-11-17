package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Frame {
    private final Byte STX = 0x02;
    private final Byte STX_2 = 0x03;

    private FrameStatus frameStatus;
    private Byte begin;
    private Integer len;
    private Byte commandCode;
    private Byte[] data;
    private Byte[] checkSum;
    private Integer counterData;
    private Boolean correctFrame;
    private Boolean sendAck;

    public Frame() {
        frameStatus = FrameStatus.BEGIN;
        checkSum = new Byte[2];
        correctFrame = false;
    }

    public Frame(byte cc, byte... data) {
        this();
        this.begin = STX;
        this.len = data.length;
        this.commandCode = cc;
        this.data = new Byte[data.length];
        for (int i = 0; i < data.length; i++) {
            this.data[i] = data[i];
        }

        Integer fcs = this.len + this.commandCode.intValue();

        for (Byte d:data) {
            fcs += d.intValue();
        }

        if (fcs > 0xff) {
            checkSum[0] = Byte.valueOf((byte) 0xFF);
            checkSum[1] = Byte.valueOf((byte) (fcs.byteValue() - 0xff));
        } else {
            checkSum[0] = Byte.valueOf(Byte.valueOf(fcs.byteValue()));
            checkSum[1] = Byte.valueOf(Byte.valueOf((byte) 0x00));
        }
    }

    public void processFrame(Byte receivedByte) {
        System.out.println(frameStatus.toString() + " " + String.format("0x%02x", receivedByte));
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
                }
            break;
            case LEN:
                len = receivedByte.intValue();
                data = new Byte[len];
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
        }
    }

    public Boolean isCorrectFrame() {
        return correctFrame;
    }

    public Boolean isSendAck() {
        return sendAck;
    }

    public List<Byte> getFrame() {
        List<Byte> frame = new ArrayList<>();
        frame.add(begin);
        frame.add(len.byteValue());
        frame.add(commandCode);
        frame.addAll(Arrays.asList(data));
        frame.addAll(Arrays.asList(checkSum));

        return frame;
    }

    public byte[] getBytes() {
        List<Byte> frame = getFrame();
        byte[] bytes = new byte[frame.size()];
        for(int i=0; i<frame.size(); i++)
            bytes[i] = frame.get(i).byteValue();

        return bytes;
    }

    public void checkCorrectFrame() {
        Integer lenFrame = len + commandCode.intValue();
        for (Byte d:data) {
            lenFrame += d.intValue();
        }
        Integer lenFromCheckSum = checkSum[0].intValue() + checkSum[1].intValue();

        if (lenFrame == lenFromCheckSum)
            correctFrame = true;
        else {
            correctFrame = false;
            System.out.println("Niepoprawna suma kontrolna " + lenFrame + " " + lenFromCheckSum);
        }
    }
    public void clearFrame() {
        counterData = 0;
        correctFrame = false;
    }
}
