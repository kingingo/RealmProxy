package realmproxy.player;

import realmbase.RealmBase;
import realmbase.data.Location;
import realmbase.event.EventHandler;
import realmbase.event.EventListener;
import realmbase.event.EventManager;
import realmbase.event.events.PacketReceiveEvent;
import realmbase.packets.Packet;
import realmbase.packets.client.EnemyHitPacket;
import realmbase.packets.client.MovePacket;
import realmbase.xml.GetXml;

public class MessageListener implements EventListener{
	
	public MessageListener() {
		EventManager.register(this);
	}
	
	Location last;
	
	@EventHandler
	public void onReceive(PacketReceiveEvent ev) {
		Packet packet = ev.getPacket();
		Player player = (Player) ev.getClient();
//		if(packet.getId() == GetXml.packetMapName.get("MOVE")){
//			MovePacket move = (MovePacket) ev.getPacket();
//			RealmBase.println("LOC: "+move.getNewPosition().x+"/"+move.getNewPosition().y);
//		}
	}
}
