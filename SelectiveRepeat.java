import java.net.*;
import java.io.*;
import java.util.*;

class SelectiveRepeat {

  private static final long TIMEOUT_INTERVAL = 100;
  private static DatagramSocket serverSocket;
  private static InetAddress IPAddress;
  private static int port;

  static ArrayList<Packet> packetList;
  static Vector<Packet> window;

  public static void sendData(ArrayList<Packet> packetListIn, InetAddress IPAddressIn, int portIn, DatagramSocket serverSocketIn) throws IOException {
    //Set up global variables and socket timeout
    serverSocket = serverSocketIn;
    IPAddress = IPAddressIn;
    port = portIn;
    window = new Vector<Packet>();
    packetList = packetListIn;
    serverSocket.setSoTimeout(100);

    int numAcks = 0;
    int numPackets = packetList.size();

    while (packetList.size() > 0 || numAcks < numPackets) {
      if (window.size() < 8 && packetList.size() > 0) {
        //window still has space to send packet
        Packet packet = packetList.remove(0);
        packet.sentTime = new Date().getTime();
        sendPacket(packet, false);
        window.add(packet);
      } else if (window.size() > 0 && (window.get(0).ack == false) && (new Date().getTime() - window.get(0).sentTime < TIMEOUT_INTERVAL)){
        //window is full and no packets have timed out
        try{
          byte[] receiveAck = new byte[32];
          DatagramPacket ack = new DatagramPacket(receiveAck, receiveAck.length);
          serverSocket.receive(ack);

          String data = new String(ack.getData());
          int sequenceNumber = Integer.parseInt(data.split(" ")[1].trim());

          if (data.split(" ")[0].trim().equals("ACK")){

            System.out.println("Received ACK for " + (sequenceNumber));

            for (int i = 0; i < window.size(); i++){
              if (window.get(i).packetNum == (sequenceNumber)){
                window.get(i).ack = true;
                numAcks++;
                while(window.size() > 0 && window.get(0).ack == true){
                  window.remove((0));
                }
              }
            }
          } else {
              System.out.println("Received NACK!!! for " + (sequenceNumber));

              for (int i = 0; i < window.size(); i++){
                if (window.get(i).packetNum == (sequenceNumber)){

                  Packet packet = window.get(i);
                  window.get(i).sentTime = new Date().getTime();
                  sendPacket(packet, false);
                }
              }
            }
          } catch (Exception e) {
            //Do nothing, we timed out and need to retransmit the first packet
            }
          } else if (window.size() > 0) {
            //packet timed out/got lost
            Packet packet = window.get(0);
            window.get(0).sentTime = new Date().getTime();
            sendPacket(packet, true);
          }
        }
      }

      private static void sendPacket(Packet packet, boolean isLost) throws IOException {
        try{
          if (isLost) {
            System.out.println("Packet " + packet.packetNum + " got lost... resending");
          } else {
            System.out.println("Sending packet: " + packet.packetNum);
          }
          DatagramPacket sendPacket = new DatagramPacket(Packet.getData(packet), Packet.getData(packet).length, IPAddress, port);
          serverSocket.send(sendPacket);
        } catch (Exception e) {

        }
      }
    }
