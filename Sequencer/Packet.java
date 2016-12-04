package Sequencer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;

public class Packet implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private InetAddress senderAddress;
	private int senderPort;

	private ReplicaOperation replicaOperation;
	private OperationParameters operationParameters;
	private int sequencernumber;

	public Packet(InetAddress senderAdress, int senderPort, ReplicaOperation replicaOperation, OperationParameters operationParameters) {
		super();
		this.replicaOperation = replicaOperation;
		this.operationParameters = operationParameters;
		this.senderAddress = senderAdress;
		this.senderPort = senderPort;
	}
	
	public Packet(ReplicaOperation replicaOperation, OperationParameters operationParameters) {
		super();
		this.replicaOperation = replicaOperation;
		this.operationParameters = operationParameters;
	}

	public ReplicaOperation getReplicaOperation() {
		return replicaOperation;
	}

	public void setReplicaOperation(ReplicaOperation replicaOperation) {
		this.replicaOperation = replicaOperation;
	}

	public OperationParameters getOperationParameters() {
		return operationParameters;
	}

	public void setOperationParameters(OperationParameters operationParameters) {
		this.operationParameters = operationParameters;
	}

	public InetAddress getSenderAddress() {
		return senderAddress;
	}

	public void setSenderAddress(InetAddress senderAddress) {
		this.senderAddress = senderAddress;
	}

	public int getSenderPort() {
		return senderPort;
	}

	public void setSenderPort(int senderPort) {
		this.senderPort = senderPort;
	}


	public int getSequencernumber() {
		return sequencernumber;
	}

	public void setSequencernumber(int sequencernumber) {
		this.sequencernumber = sequencernumber;
	}
	
	
	
}
