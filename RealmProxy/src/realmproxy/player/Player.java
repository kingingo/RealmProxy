package realmproxy.player;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Vector;

import lombok.Getter;
import lombok.Setter;
import realmbase.Client;
import realmbase.GetXml;
import realmbase.Parameter;
import realmbase.RealmBase;
import realmbase.data.Callback;
import realmbase.data.Type;
import realmbase.encryption.RC4;
import realmbase.listener.PacketManager;
import realmbase.packets.Packet;
import realmbase.packets.client.HelloPacket;
import realmproxy.RealmProxy;

public class Player extends Client{
	@Getter
	private static final List<Player> Players = new Vector<>();
	@Getter
	private static final List<Player> newPlayers = new Vector<Player>();
	
	private final byte[] localBuffer = new byte[bufferLength];
	private int localBufferIndex = 0;
	@Getter
	private Socket localSocket;
	private long localNoDataTime = System.currentTimeMillis();
	private RC4 localRecvRC4;
	private RC4 localSendRC4;
	
	public Player(Socket localSocket){
		if(localSocket == null){
			throw new NullPointerException("Socket darf nicht null sein!");
		}
		
		this.localSocket=localSocket;
		this.localRecvRC4 = new RC4(Parameter.cipherOut);
		this.localSendRC4 = new RC4(Parameter.cipherIn);
		getNewPlayers().add(this);
	}
	
	public boolean connect(HelloPacket packet, int gameId) {
		final InetSocketAddress socketAddress = RealmProxy.getSocketAddress(gameId);

		return connect(socketAddress, new Callback<Client>() {
			
			@Override
			public void call(Client client, Throwable exception) {
				if(exception != null){
					RealmBase.println("Kick!?");
					kick();
				}else{
					RealmBase.println("Send HelloPacket");
					client.sendPacketToServer(packet);
				}
			}
		});
	}
	
	public void sendPacketToClient(Packet packet){
		if(this.localSocket!=null && this.localSocket.isConnected()){
			boolean cancel = PacketManager.send(this, packet, Type.CLIENT);
				
			if(!cancel){
				byte[] packetBytes = packet.toByteArray();
				try {
					this.localSendRC4.cipher(packetBytes);
					int packetLength = packetBytes.length + 5;
						
					DataOutputStream out = new DataOutputStream(this.localSocket.getOutputStream());
					out.writeInt(packetLength);
					out.writeByte(packet.getId());
					out.write(packetBytes);
				}catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void sendPacketToClient(int packetId, byte[] packetBytes){
		sendPacketToClient(Packet.create(packetId, packetBytes));
	}
	
	public void process() {
		try {
			if (this.remoteSocket != null) {
				try {
					InputStream in = this.remoteSocket.getInputStream();
					if (in.available() > 0) {
						int bytesRead = this.remoteSocket.getInputStream().read(this.remoteBuffer, this.remoteBufferIndex, this.remoteBuffer.length - this.remoteBufferIndex);
						if (bytesRead == -1) {
							throw new SocketException("end of stream");
						} else if (bytesRead > 0) {
							this.remoteBufferIndex += bytesRead;
							while (this.remoteBufferIndex >= 5) {
								int packetLength = ((ByteBuffer) ByteBuffer.allocate(4).put(this.remoteBuffer[0]).put(this.remoteBuffer[1]).put(this.remoteBuffer[2]).put(this.remoteBuffer[3]).rewind()).getInt();
								if (this.remoteBufferIndex < packetLength) {
									break;
								}
								byte packetId = this.remoteBuffer[4];
								byte[] packetBytes = new byte[packetLength - 5];
								System.arraycopy(this.remoteBuffer, 5, packetBytes, 0, packetLength - 5);
								if (this.remoteBufferIndex > packetLength) {
									System.arraycopy(this.remoteBuffer, packetLength, this.remoteBuffer, 0, this.remoteBufferIndex - packetLength);
								}
								this.remoteBufferIndex -= packetLength;
								this.remoteRecvRC4.cipher(packetBytes);
								if(packetId!=74&&packetId!=33&&packetId!=18&&packetId!=101&&packetId!=1&&packetId!=35&&packetId!=52&&packetId!=102&&packetId!=69)
									RealmBase.println("Server -> Client: Id:"+(GetXml.getPacketMap().containsKey(String.valueOf(packetId)) ? GetXml.getPacketMap().get(String.valueOf(packetId)) : packetId)+" Length: "+packetBytes.length);
								
								Packet packet = Packet.create(packetId, packetBytes);
								if(!PacketManager.receive(this, packet, Type.SERVER))sendPacketToClient(packet);
							}
						}
						this.remoteNoDataTime = System.currentTimeMillis();
					} else if (System.currentTimeMillis() - this.remoteNoDataTime >= 10000) {
						throw new SocketException("remote data timeout");
					}
				} catch (Exception e) {
					if (!(e instanceof SocketException)) {
						e.printStackTrace();
					}
					this.disconnect();
				}
			}
			
			InputStream in = this.localSocket.getInputStream();
			if (in.available() > 0) {
				int bytesRead = in.read(this.localBuffer, this.localBufferIndex, this.localBuffer.length - this.localBufferIndex);
				if (bytesRead == -1) {
					throw new SocketException("eof");
				} else if (bytesRead > 0) {
					this.localBufferIndex += bytesRead;
					while (this.localBufferIndex >= 5) {
						int packetLength = ((ByteBuffer) ByteBuffer.allocate(4).put(this.localBuffer[0]).put(this.localBuffer[1]).put(this.localBuffer[2]).put(this.localBuffer[3]).rewind()).getInt();
						if (this.localBufferIndex < packetLength) {
							break;
						}
						byte packetId = this.localBuffer[4];
						byte[] packetBytes = new byte[packetLength - 5];
						System.arraycopy(this.localBuffer, 5, packetBytes, 0, packetLength - 5);
						if (this.localBufferIndex > packetLength) {
							System.arraycopy(this.localBuffer, packetLength, this.localBuffer, 0, this.localBufferIndex - packetLength);
						}
						this.localBufferIndex -= packetLength;
						this.localRecvRC4.cipher(packetBytes);
						if(packetId!=74&&packetId!=33&&packetId!=18&&packetId!=101&&packetId!=1&&packetId!=35&&packetId!=52&&packetId!=102&&packetId!=69)
							RealmBase.println("Client -> Server: Id:"+(GetXml.getPacketMap().containsKey(String.valueOf(packetId)) ? GetXml.getPacketMap().get(String.valueOf(packetId)) : packetId)+" Length: "+packetBytes.length);
						
						Packet packet = Packet.create(packetId, packetBytes);
						if(!PacketManager.receive(this, packet, Type.CLIENT))sendPacketToServer(packet);
					}
				}
				this.localNoDataTime = System.currentTimeMillis();
			} else if (System.currentTimeMillis() - this.localNoDataTime >= 10000) {
				throw new SocketException("local data timeout");
			}
		} catch (Exception e) {
			if (!(e instanceof SocketException)) {
				e.printStackTrace();
			}
			this.kick();
		}
	}
	
	public void kick() {
		this.disconnect();
		try {
			this.localSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
