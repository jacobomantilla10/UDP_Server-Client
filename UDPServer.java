import java.io.*;
import java.net.*;
import java.util.*;

class UDPServer {
  public static void main(String args[]) throws Exception {

    DatagramSocket serverSocket = new DatagramSocket(3000);

    byte[] receiveData = new byte[1024];

    System.out.println("********** Listening... **********");

    while(true) {
      //create space for received datagram
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
      //receive Datagram
      try {
      serverSocket.receive(receivePacket);

      String request = new String(receivePacket.getData());

      String fileName = request.split(" ")[1];
      String returnData = getFileData(fileName);
      //Get IP address and port number of sender
      InetAddress IPAddress = receivePacket.getAddress();
      int port = receivePacket.getPort();
      int packetNum = 1;

      //part where data actually gets sent
      System.out.println("\n********** Sending Packets **********");
      String dataWithHeader = "HTTP/1.0 200 " + fileName + " Follows\r\n"
                            + "Content-Type: text/plain\r\n"
                            + "Content-Length: " + returnData.length() +  "\r\n"
                            + "\r\n" +  returnData;
      byte[] sendData = returnData.getBytes();
      ArrayList<Packet> packetList = Packet.segment(dataWithHeader.getBytes());


      SelectiveRepeat.sendData(packetList, IPAddress, port, serverSocket);

      byte[] nullByte = "\0".getBytes();
      DatagramPacket nullPacket = new DatagramPacket(nullByte, nullByte.length, IPAddress, port);
      serverSocket.send(nullPacket);
      System.out.println("********** Finished **********\n");
      System.out.println("********** Listening... **********");
      } catch (Exception e) {
      }
    }
  }

  private static String getFileData(String fileName) {
    String fileData = "";
    try {
      File requestFile = new File(fileName);
      Scanner myReader = new Scanner(requestFile);
      while (myReader.hasNextLine()) {
        fileData += myReader.nextLine();
      }
      myReader.close();
    } catch (FileNotFoundException e) {
      fileData = "File not found in server";
    }
    return fileData;
  }
}
