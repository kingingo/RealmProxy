package realmproxy.player;

import realmbase.Client;
import realmbase.GetXml;
import realmbase.RealmBase;
import realmbase.data.Type;
import realmbase.listener.PacketListener;
import realmbase.listener.PacketManager;
import realmbase.packets.Packet;

public class MessageListener implements PacketListener{
	
	public MessageListener() {
		PacketManager.addListener(this);
	}
	
	@Override
	public boolean onReceive(Client c, Packet packet, Type from) {
		Player client = (Player)c;
		if(packet.getId() == GetXml.getPacketMapName().get("GOTO")){
			RealmBase.println("D: "+packet.toString());
		}else if(packet.getId() == GetXml.getPacketMapName().get("GOTOACK")){
			RealmBase.println("T: "+(System.currentTimeMillis()));
			RealmBase.println("T: "+(System.currentTimeMillis()-c.getConnectTime()));
			RealmBase.println("D: "+packet.toString());
		}
		
		return false;
	}

	@Override
	public boolean onSend(Client client, Packet packet, Type to) {
		return false;
	}

}
