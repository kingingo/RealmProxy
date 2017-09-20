package realmproxy.player;

import java.io.IOException;

import realmbase.Parameter;
import realmbase.RealmBase;
import realmbase.event.EventHandler;
import realmbase.event.EventListener;
import realmbase.event.EventManager;
import realmbase.event.events.PacketReceiveEvent;
import realmbase.packets.Packet;
import realmbase.packets.client.HelloPacket;
import realmbase.packets.server.Create_SuccessPacket;
import realmbase.packets.server.FailurePacket;
import realmbase.packets.server.ReconnectPacket;
import realmbase.xml.GetXml;
import realmproxy.RealmProxy;

public class ConnectListener implements EventListener{
	
	public ConnectListener() {
		EventManager.register(this);
	}
	
	@EventHandler
	public void onReceive(PacketReceiveEvent ev) {
		Packet packet = ev.getPacket();
		Player client = (Player)ev.getClient();
		if(packet.getId() == GetXml.packetMapName.get("RECONNECT")){
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
		}else if(packet.getId() == GetXml.packetMapName.get("HELLO")){
			HelloPacket hpacket = (HelloPacket)packet;
			RealmBase.println("Receive HelloPacket initialize connection!");
			RealmBase.println("Detailes: "+hpacket.toString());
			client.connect(hpacket, hpacket.getGameId());
			ev.setCancelled(true);
		}
	}
}
