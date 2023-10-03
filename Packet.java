import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.*;

class Packet {
  private static int HEADER_SIZE = 1;
  private static int TOTAL_SIZE = 1024;
  private static int DATA_SIZE = TOTAL_SIZE - HEADER_SIZE;
  public boolean ack = false;
  public byte sequenceNumber;
  public long sentTime;
  private byte[] data;
  private short checkSum;
  public short packetNum;

  public Packet() {
    data = new byte[DATA_SIZE];
    checkSum = 0;
    packetNum = 0;
    sequenceNumber = 0;
  }

  public static Packet createPacket(DatagramPacket packet, byte sequenceNum){
    Packet newPacket = new Packet();
    ByteBuffer bytebuffer = ByteBuffer.wrap(packet.getData());
    byte[] data = packet.getData();
    byte[] rest;


    newPacket.packetNum = (short) (sequenceNum - 1);
    newPacket.sequenceNumber = sequenceNum;
    newPacket.checkSum = checksum(data);

    rest = new byte[data.length - bytebuffer.position()];
    System.arraycopy(data,bytebuffer.position(), rest, 0, rest.length);
    newPacket.data = rest;
    return newPacket;
  }

  public static ArrayList<Packet> segment(byte[] fileData){
    ArrayList<Packet> packetList = new ArrayList<Packet>();
    int byteNum = 0;
    short packetNum = 0;
    byte sequenceNum = 1;

    if (fileData.length == 0) {
      throw new IllegalArgumentException("File has no contents");
    }

    while(byteNum < fileData.length){
      Packet currPacket = new Packet();
      byte[] temp = new byte[DATA_SIZE];
      byte[] data = new byte[TOTAL_SIZE];
      int dataSize = DATA_SIZE;

      if(fileData.length - byteNum < TOTAL_SIZE){
        dataSize = fileData.length - byteNum;
      }

      for (int i = 0; i < dataSize; i++){
        temp[i] = fileData[byteNum];
        byteNum++;
      }
      data[0] = sequenceNum;
      for (int i = 1; i < TOTAL_SIZE; i++){
        data[i] = temp[i-1];
      }
      currPacket.data = data;
      currPacket.packetNum = packetNum;
      currPacket.checkSum = checksum(temp);
      packetList.add(currPacket);
      packetNum++;
      sequenceNum++;
    }
    return packetList;
  }

  public static byte[] reassemble(ArrayList<Packet> packetListIn){
    int size = 0;
    int byteIdx = 0;
    int arrSize = packetListIn.size();

    ArrayList<Packet> packetList = new ArrayList<Packet>();
    for(int i = 0; i < arrSize; i++){
      packetList.add(null);
    }

    for(Packet packet : packetListIn){
      packetList.set(packet.packetNum, packet);
      size += packet.data.length;
    }

    byte[] data = new byte[size - packetList.size()];

    for (int i = 0; i < packetList.size(); i++){
      byte[] currData = packetList.get(i).data;
      int packetNum = packetList.get(i).packetNum;
      if (packetNum == i){
        for (int j = 0; j < currData.length-1; j++){
          data[byteIdx++] = currData[j+1];
        }
      }
    }
    return data;
  }

  public static short checksum(byte[] arr) {
    short checksum;
    long sum = 0;
    int length = arr.length;
    int count = 0;

    while (length > 1) {
      sum += (arr[count] << 8) | ((arr[count+1]) & 0x00FF);
      if ((sum & 0xFFFF0000) > 0) {
        sum = ((sum & 0xFFFF) + 1);
      }
      count += 2;
      length -= 2;
    }
    if (length > 0){
      sum += (arr[count] << 8 & 0xFF00);
      if ((sum & 0xFFFF0000) > 0) {
        sum = ((sum & 0xFFFF) + 1);
      }
    }
    checksum = (short) (~sum & 0xFFFF);
    return checksum;
  }

  public static byte[] getData(Packet packet){
    return packet.data;
  }

  public static short getCheckSum(Packet packet) {
    return packet.checkSum;
  }

  public static short getPacketNum(Packet packet){
    return packet.packetNum;
  }

  public void ack(Packet packet){
    packet.ack = true;
  }
}
