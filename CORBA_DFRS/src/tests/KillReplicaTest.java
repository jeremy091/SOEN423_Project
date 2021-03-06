package tests;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import jeremy_replica.udp.UdpHelper;
import json.JSONReader;
import packet.Operation;
import packet.Packet;
import packet.ReplicaAliveOperation;
import packet.ReplicaAliveReply;
import packet.ReplicaRebootOperation;
import packet.ReplicaRebootReply;

public class KillReplicaTest {
	public static int BUFFER_SIZE = 50000;

	public static void main(String[] args) {
		// Initialize ports configuration
		JSONReader jsonReader = new JSONReader();
		
		String username = "Patrick";
		int port = jsonReader.getPortForKeys(username, "RM");
		testKillReplica(port);
	}
	
	private static void testReplicaCrash(int port) {
		// Build the action for the packet
		ReplicaAliveOperation replicaAliveOperation = new ReplicaAliveOperation();

		// Create a packet with the operation
		Packet packet = new Packet(Operation.REPLICA_CRASH, replicaAliveOperation);

		// Process the packet
		Packet result = processOperationPacket(packet, port);
		ReplicaAliveReply reply = (ReplicaAliveReply) result.getOperationParameters();
		System.out.println("Replica wasCrashed: " + reply.isAlive());
	}
	
	private static void testKillReplica(int port) {
		// Build the action for the packet
		ReplicaRebootOperation rebootReplicaOperation = new ReplicaRebootOperation();

		// Create a packet with the operation
		Packet packet = new Packet(Operation.REPLICA_KILL, rebootReplicaOperation);

		// Process the packet
		Packet result = processOperationPacket(packet, port);
		ReplicaRebootReply reply = (ReplicaRebootReply) result.getOperationParameters();
		System.out.println("Replica isKilled: " + reply.isRebooted());
	}

	private static void testRebootReplica(int port) {
		// Build the action for the packet
		ReplicaRebootOperation rebootReplicaOperation = new ReplicaRebootOperation();

		// Create a packet with the operation
		Packet packet = new Packet(Operation.REPLICA_REBOOT, rebootReplicaOperation);

		// Process the packet
		Packet result = processOperationPacket(packet, port);
		ReplicaRebootReply reply = (ReplicaRebootReply) result.getOperationParameters();
		System.out.println("Replica isRebooted: " + reply.isRebooted());
	}
	
	private static void testReplicaAlive(int port) {
		// Build the action for the packet
		ReplicaAliveOperation replicaAliveOperation = new ReplicaAliveOperation();

		// Create a packet with the operation
		Packet packet = new Packet(Operation.REPLICA_ALIVE, replicaAliveOperation);

		// Process the packet
		Packet result = processOperationPacket(packet, port);
		ReplicaAliveReply reply = (ReplicaAliveReply) result.getOperationParameters();
		System.out.println("Replica isAlive: " + reply.isAlive());
	}
	
	private static Packet processOperationPacket(Packet packet, int port) {
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
			byte[] message = UdpHelper.getByteArray(packet);
			InetAddress host = InetAddress.getByName("localhost");
			int serverPort = port;
			DatagramPacket requestPacket = new DatagramPacket(message, message.length, host, serverPort);
			socket.send(requestPacket);
			byte[] buffer = new byte[BUFFER_SIZE];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			socket.receive(reply);
			Packet result = (Packet) UdpHelper.getObjectFromByteArray(reply.getData());
			return result;
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			socket.close();
		}
		return null;
	}
}