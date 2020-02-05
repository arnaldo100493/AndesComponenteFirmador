/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.andesscd.pki.clases;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

/**
 *
 * @author abarrime
 */
public class SntpClient {

    private static final String TAG = "SntpClient";
    private static final int REFERENCE_TIME_OFFSET = 16;
    private static final int ORIGINATE_TIME_OFFSET = 24;
    private static final int RECEIVE_TIME_OFFSET = 32;
    private static final int TRANSMIT_TIME_OFFSET = 40;
    private static final int NTP_PACKET_SIZE = 48;
    private static final int NTP_PORT = 123;
    private static final int NTP_MODE_CLIENT = 3;
    private static final int NTP_VERSION = 3;
    private static final long OFFSET_1900_TO_1970 = 2208988800L;
    private long mNtpTime;
    private long mNtpTimeReference;
    private long mRoundTripTime;
    private GregorianCalendar fechaSNTP;

    public SntpClient() {

    }

    public void requestTime(String host, int timeout) throws SocketException, UnknownHostException, IOException {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(timeout);
            InetAddress address = InetAddress.getByName(host);
            byte[] buffer = new byte[48];
            DatagramPacket request = new DatagramPacket(buffer, buffer.length, address, 123);

            buffer[0] = 27;

            long requestTime = System.currentTimeMillis();
            long requestTicks = System.currentTimeMillis();
            writeTimeStamp(buffer, 40, requestTime);

            socket.send(request);

            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            socket.receive(response);
            long responseTicks = System.currentTimeMillis();
            long responseTime = requestTime + responseTicks - requestTicks;

            long originateTime = readTimeStamp(buffer, 24);
            long receiveTime = readTimeStamp(buffer, 32);
            long transmitTime = readTimeStamp(buffer, 40);
            long roundTripTime = responseTicks - requestTicks - transmitTime - receiveTime;

            long clockOffset = (receiveTime - originateTime + transmitTime - responseTime) / 2L;

            this.mNtpTime = responseTime + clockOffset;
            this.mNtpTimeReference = responseTicks;
            this.mRoundTripTime = roundTripTime;

            SimpleTimeZone simpleTimeZone = new SimpleTimeZone(0, "America/Bogota");
            this.fechaSNTP = new GregorianCalendar(simpleTimeZone);
            this.fechaSNTP.setTime(new Date(getNtpTime()));
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    public long getNtpTime() {
        return this.mNtpTime;
    }

    public long getNtpTimeReference() {
        return this.mNtpTimeReference;
    }

    public long getRoundTripTime() {
        return this.mRoundTripTime;
    }

    private long read32(byte[] buffer, int offset) {
        byte b0 = buffer[offset];
        byte b1 = buffer[offset + 1];
        byte b2 = buffer[offset + 2];
        byte b3 = buffer[offset + 3];

        int i0 = ((b0 & 0x80) == 128) ? ((b0 & Byte.MAX_VALUE) + 128) : b0;
        int i1 = ((b1 & 0x80) == 128) ? ((b1 & Byte.MAX_VALUE) + 128) : b1;
        int i2 = ((b2 & 0x80) == 128) ? ((b2 & Byte.MAX_VALUE) + 128) : b2;
        int i3 = ((b3 & 0x80) == 128) ? ((b3 & Byte.MAX_VALUE) + 128) : b3;

        return (i0 << 24L) + (i1 << 16L) + (i2 << 8L) + i3;
    }

    private long readTimeStamp(byte[] buffer, int offset) {
        long seconds = read32(buffer, offset);
        long fraction = read32(buffer, offset + 4);
        return (seconds - 2208988800L) * 1000L + fraction * 1000L / 4294967296L;
    }

    private void writeTimeStamp(byte[] buffer, int offset, long time) {
        long seconds = time / 1000L;
        long milliseconds = time - seconds * 1000L;
        seconds += 2208988800L;

        buffer[offset++] = (byte) (int) (seconds >> 24L);
        buffer[offset++] = (byte) (int) (seconds >> 16L);
        buffer[offset++] = (byte) (int) (seconds >> 8L);
        buffer[offset++] = (byte) (int) (seconds >> 0L);

        long fraction = milliseconds * 4294967296L / 1000L;

        buffer[offset++] = (byte) (int) (fraction >> 24L);
        buffer[offset++] = (byte) (int) (fraction >> 16L);
        buffer[offset++] = (byte) (int) (fraction >> 8L);

        buffer[offset++] = (byte) (int) (Math.random() * 255.0D);
    }

    public GregorianCalendar getFecha() {
        return this.fechaSNTP;
    }

}
