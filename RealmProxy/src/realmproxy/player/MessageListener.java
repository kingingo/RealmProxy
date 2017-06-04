package realmproxy.player;

import realmbase.Client;
import realmbase.GetXml;
import realmbase.RealmBase;
import realmbase.data.Location;
import realmbase.data.Type;
import realmbase.listener.PacketListener;
import realmbase.listener.PacketManager;
import realmbase.packets.Packet;
import realmbase.packets.client.MovePacket;

public class MessageListener implements PacketListener{
	
	public MessageListener() {
		PacketManager.addListener(this);
	}
	
	Location last;
	
	@Override
	public boolean onReceive(Client c, Packet packet, Type from) {
		if(packet.getId() == GetXml.getPacketMapName().get("MOVE")){
			MovePacket mpacket = (MovePacket)packet;
			RealmBase.println("LOC: "+mpacket.getNewPosition().x+"/"+mpacket.getNewPosition().y);
		}
		
		return false;
	}

	@Override
	public boolean onSend(Client client, Packet packet, Type to) {
		return false;
	}

}
