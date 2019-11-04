package model;

public class Frame {
    private final Byte STX = 0x02;
    private final Byte STX_2 = 0x03;

    private FrameStatus frameStatus;
    private Integer len;
    private Byte commandCode;
    private Byte[] data;
    private Byte[] checkSum;
    private Integer counterData;

    public Frame() {
        frameStatus = FrameStatus.BEGIN;
        checkSum = new Byte[2];
    }

    public void processFrame(Byte receivedByte) {
        switch (frameStatus) {
            case BEGIN:
                if (receivedByte == STX || receivedByte == STX_2) {
                    frameStatus = FrameStatus.LEN;
                    counterData = 0;
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
                } else
                    frameStatus = FrameStatus.FIRST_FCS;
            break;
            case FIRST_FCS:
                checkSum[0] = receivedByte;
                frameStatus = FrameStatus.SECOND_FCS;
            break;
            case SECOND_FCS:
                checkSum[1] = receivedByte;
                frameStatus = FrameStatus.BEGIN;
            break;
        }
    }
}
