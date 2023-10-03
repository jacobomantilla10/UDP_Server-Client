import java.io.*;
import java.net.*;
import java.util.*;

class UDPClient {
  public static void main (String[] args) throws Exception{

    //Sending data
    BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
    DatagramSocket clientSocket = new DatagramSocket();
    InetAddress IPAddress = InetAddress.getByName("localhost");

    byte[] sendData = new byte[1024];
    String sentence = "GET serverFile.html HTTP/1.0";

    sendData = sentence.getBytes();

    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 3000);
    clientSocket.send(sendPacket);

    //Receiving data
    byte[] receiveData = new byte[1024];
    ArrayList<Packet> packetList = new ArrayList<Packet>();

    boolean isFinished = false;
    short packetNum = 0;

    System.out.println("********** Receiving Data **********");

    while (!isFinished) {
      DatagramPacket receiveDatagramPacket = new DatagramPacket(receiveData, receiveData.length);
      clientSocket.receive(receiveDatagramPacket);
      Packet receivePacket = Packet.createPacket(receiveDatagramPacket, receiveDatagramPacket.getData()[0]);
      packetNum++;
      if (Packet.getData(receivePacket)[0] == '\0' && packetNum != 0){
        isFinished = true;
      } else {
        double probability = 0.0;
        double lossProbability = 0.0;
        if (args.length != 0){
          probability = Double.parseDouble(args[0]);
          if (args.length != 1){
            lossProbability = Double.parseDouble(args[0]);
          }
        }
        Packet gremlinPacket = gremlin(probability, lossProbability, receivePacket);
        if (gremlinPacket != null && !errorDetection(gremlinPacket)){
          System.out.println("Received packet" + receivePacket.packetNum);
          packetList.add(receivePacket);
          ack(receivePacket, IPAddress, 3000, clientSocket);
        } else if (gremlinPacket != null) {
          System.out.println("Received packet" + receivePacket.packetNum);
          nack(receivePacket, IPAddress, 3000, clientSocket);
        }
      }
    }
    System.out.println("********** Finished **********");
    clientSocket.close();

    byte[] outputData = Packet.reassemble(packetList);
    String output = new String(outputData);
    System.out.println("\n********** File Contents: **********");
    System.out.println(output);
    System.out.println("\n********** Finished **********");
  }


  private static boolean errorDetection(Packet packet){
      Short checksum = Packet.getCheckSum(packet);
      byte[] data = Packet.getData(packet);
      short clientChecksum = Packet.checksum(data);
      if (!checksum.equals(clientChecksum)) {
        Short packetNum = Packet.getPacketNum(packet);
        System.out.println("Error detected in packet " + (packetNum + 1));
        return true;
      }
      return false;
  }
  private static Packet gremlin(double probability, double lossProbability, Packet packet) {

    Random rand = new Random();
    double damageProb = rand.nextDouble();
    double damageAmtProb = rand.nextDouble();
    int damageAmt;

    if (damageAmtProb >= 0.0 && damageAmtProb <= 0.5) {
      damageAmt = 1;
    } else if (damageAmtProb >= 5.0 && damageAmtProb <= 0.8) {
      damageAmt = 2;
    } else {
      damageAmt = 3;
    }

    if (probability >= damageProb){
      for (int i = 0; i < damageAmt; i++){
        byte[] data = Packet.getData(packet);
        int damageIdx = rand.nextInt(data.length);
        data[damageIdx] = (byte) ~data[damageIdx];
      }
    }

    if (lossProbability >= damageProb){
      System.out.println("Lost packet: " + packet.packetNum);
      packet = null;
    }
    return packet;
  }

  private static void ack(Packet packet, InetAddress IPAddress, int port, DatagramSocket clientSocket) throws IOException{
    String ack = "ACK " + packet.packetNum;
    DatagramPacket sendData = new DatagramPacket(ack.getBytes(), ack.length(), IPAddress, port);
    clientSocket.send(sendData);
    System.out.println("ACKED packet: " + packet.packetNum);
  }

  private static void nack(Packet packet, InetAddress IPAddress, int port, DatagramSocket clientSocket) throws Exception{
    String nack = "NACK " + packet.packetNum;
    DatagramPacket sendData = new DatagramPacket(nack.getBytes(), nack.length(), IPAddress, port);
    clientSocket.send(sendData);
    System.out.println("NACKED packet: " + packet.packetNum);
  }
}
