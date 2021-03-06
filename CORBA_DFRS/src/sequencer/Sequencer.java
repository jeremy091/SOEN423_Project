package sequencer;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import global.Constants;
import json.JSONReader;
import packet.MulticastPacket;
import packet.Packet;
import udp.UdpHelper;

public class Sequencer implements Runnable{
	private final int BUFFER_SIZE = 50000;
	private final int THREAD_POOL_SIZE = Integer.MAX_VALUE;
	private final ExecutorService threadPool;
	private int sequencerport;
	private int replicaManagerPort;
	private int sequencernumber = 1;
	private ArrayList<Packet> sequencerlog = new ArrayList<Packet>();
	private final int groupportnumber = 9876;
	Thread packetDispatcherThread;
	
	public static void main(String[] args){
		Sequencer sequencer = new Sequencer("");
		new Thread(sequencer).start();
		System.out.println("Sequencer initialized.");
	}

	public Sequencer(String message) {
		super();
		this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
		JSONReader jsonReader = new JSONReader();
		sequencerport = jsonReader.getSequencerPort();
		// TODO : Get from configuration. This port is for RM to get logs.
		replicaManagerPort = 50000;
		this.packetDispatcherThread = initPacketDispatcher();
		this.packetDispatcherThread.start();
	}

	public ArrayList<Packet> getSequencerLog() {
		return sequencerlog;
	}
	
	public int getSequencernumber() {
		return sequencernumber;
	}

	public synchronized void setSequencernumber(int sequencernumber) {
		this.sequencernumber = sequencernumber;
	}
	
	@Override
	public void run() {
		UDPServer();
	}

	public void multicastToGroup(Packet packet) {

		MulticastSocket aSocket = null;
		try {
			// piggybacks sequencer number to packet
			packet.setSequencernumber(this.sequencernumber);

			// creates MulticastSocket with InetAddress and ServerPort
			aSocket = new MulticastSocket();
			InetAddress aGroup = InetAddress.getByName(Constants.MULTICAST_ADDRESS);

			// join group
			aSocket.joinGroup(aGroup);

			// converts packet to send to group
			byte[] m = UdpHelper.getByteArray(packet);
			DatagramPacket request = new DatagramPacket(m, UdpHelper.getByteArray(packet).length, aGroup,
					groupportnumber);

			synchronized (this) {
				// sends packet
				aSocket.send(request);
				// sequencer logs serialized packet
				this.logPacket(packet);

				// sequencer increments number
				this.setSequencernumber(this.getSequencernumber() + 1);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
	}

	public void logPacket(Packet packet) {
		sequencerlog.add(packet);
	}

	// sends the log of all operations to the replica manager once it has
	// received the retransmit request
	public void sendLog(int replicaManagerPort) {
		DatagramSocket aSocket = null;
		try {
			// create socket
			aSocket = new DatagramSocket();

			// convert sequencerlog Arraylist into a byte array using UdpHelper
			byte[] m = UdpHelper.getByteArray(sequencerlog);

			InetAddress aHost = InetAddress.getByName("localhost");
			DatagramPacket sendsequencerlogs = new DatagramPacket(m, UdpHelper.getByteArray(sequencerlog).length, aHost,
					replicaManagerPort);
			aSocket.send(sendsequencerlogs);
		} catch (Exception e) {

		}
	}

	public void UDPServer() {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(sequencerport);

			while (true) {
				// create packet and receive message
				byte[] buffer = new byte[1000];
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);

				// if request from front end was a packet it will multicast to
				// UDPParsers
				Packet frontendpacket;

				ByteArrayInputStream bis = new ByteArrayInputStream(request.getData());
				ObjectInput in = null;
				try {
					in = new ObjectInputStream(bis);
					frontendpacket = ((MulticastPacket) in.readObject()).getP();
					this.multicastToGroup(frontendpacket);

					// sends reply back to front end to say it received message
					String replytofrontend = "ACK";
					request.setData(replytofrontend.getBytes());

					DatagramPacket reply = new DatagramPacket(request.getData(), request.getLength(),
							request.getAddress(), request.getPort());
					aSocket.send(reply);

				} catch (ClassCastException e) {// if classcastexception occurs,
												// then the received message is
												// a string of the serverport

					// converts string of serverport into integer
					String receivedreplicatportnumber = (new String(request.getData()).trim());
					int replicaportnumber = Integer.parseInt(receivedreplicatportnumber);

					// calls method to send sequencerlogs
					this.sendLog(replicaportnumber);

					// sends reply back to front end to say it received message
					String replytofrontend = "ACK";
					request.setData(replytofrontend.getBytes());

					DatagramPacket reply = new DatagramPacket(request.getData(), request.getLength(),
							request.getAddress(), request.getPort());
					aSocket.send(reply);
				}

				finally {
					try {
						if (in != null) {
							in.close(); 
						}
					} catch (IOException ex) {
						// ignore close exception
					}
				}

			}

		} catch (Exception e) {

		}
	}
	
	private Thread initPacketDispatcher() {
		return new Thread(() -> {
			replicaManagerRequests();
		});
	}
	
	private void replicaManagerRequests() {
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket(replicaManagerPort);
			while (true) {
				byte[] buffer = new byte[BUFFER_SIZE];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);
				threadPool.execute(new SequencerPacketDispatcher(socket, packet, this));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (socket != null){
				socket.close();
			}
		}
	}
}
