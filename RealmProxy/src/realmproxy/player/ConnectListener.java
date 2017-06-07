package realmproxy.player;

import java.io.IOException;

import realmbase.Client;
import realmbase.GetXml;
import realmbase.Parameter;
import realmbase.RealmBase;
import realmbase.data.Type;
import realmbase.listener.PacketListener;
import realmbase.listener.PacketManager;
import realmbase.packets.Packet;
import realmbase.packets.client.HelloPacket;
import realmbase.packets.server.Create_SuccessPacket;
import realmbase.packets.server.FailurePacket;
import realmbase.packets.server.ReconnectPacket;
import realmproxy.RealmProxy;

public class ConnectListener implements PacketListener{
	
	public ConnectListener() {
		PacketManager.addListener(this);
	}
	
	@Override
	public boolean onReceive(Client c, Packet packet, Type from) {
		Player client = (Player)c;
		if(packet.getId() == GetXml.getPacketMapName().get("RECONNECT")){
			ReconnectPacket rpacket = (ReconnectPacket)packet;
			RealmBase.println("Detailes: "+rpacket.toString());
			
			String host = rpacket.getHost();
			int port = rpacket.getPort();
			
			if(port == -1){
				host = Parameter.remoteHost.getHostString();
				port = Parameter.remoteHost.getPort();
			}
			
			RealmProxy.setSocketAddress(rpacket.getGameId(), host, port); 
			rpacket.setHost(RealmProxy.localHost);
			rpacket.setPort(RealmProxy.localPort);
			try {
				client.getRemoteSocket().close();
				client.setRemoteSocket(null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else if(packet.getId() == GetXml.getPacketMapName().get("HELLO")){
			HelloPacket hpacket = (HelloPacket)packet;
			RealmBase.println("Receive HelloPacket initialize connection!");
			RealmBase.println("Detailes: "+hpacket.toString());
			client.connect(hpacket, hpacket.getGameId());
			return true;
		}else if(packet.getId() == GetXml.getPacketMapName().get("FAILURE")){
			FailurePacket fpacket = (FailurePacket)packet;
			RealmBase.println("FailurePacket-> "+fpacket.getErrorId()+" "+fpacket.getErrorDescription());
		}else if(packet.getId() == GetXml.getPacketMapName().get("CREATE_SUCCESS")){
			Create_SuccessPacket cpacket = (Create_SuccessPacket)packet;
			client.setClientId(cpacket.getObjectId());
		}
		
		return false;
	}

	@Override
	public boolean onSend(Client client, Packet packet, Type to) {
		
		return false;
	}
}
