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

    public Frame() {
        frameStatus = FrameStatus.BEGIN;
        checkSum = new Byte[2];
        correctFrame = false;
    }

    public void processFrame(Byte receivedByte) {
        System.out.println(frameStatus.toString() + " " + receivedByte);
        switch (frameStatus) {
            case BEGIN:
                if (receivedByte == STX || receivedByte == STX_2) {
                    frameStatus = FrameStatus.LEN;
                    counterData = 0;
                    correctFrame = false;
                    begin = receivedByte;
                }
            break;
            case LEN:
                len = receivedByte.intValue();
                data = new Byte[len];
                frameStatus = FrameStatus.COMMAND;
            break;
            case COMMAND:
                commandCode = receivedByte;
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

    public List<Byte> getFrame() {
        List<Byte> frame = new ArrayList<>();
        frame.add(begin);
        frame.add(len.byteValue());
        frame.add(commandCode);
        frame.addAll(Arrays.asList(data));
        frame.addAll(Arrays.asList(checkSum));

        return frame;
    }

    public void checkCorrectFrame() {
        Integer lenFrame = len + 2;
        Integer lenFromCheckSum = checkSum[0].intValue() + checkSum[1].intValue();

        if (lenFrame == lenFromCheckSum)
            correctFrame = true;
        else {
            correctFrame = false;
            System.out.println("Niepoprawna suma kontrolna " + lenFrame + " " + lenFromCheckSum);
        }
    }
}
