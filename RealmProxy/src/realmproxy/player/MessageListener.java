package realmproxy.player;

import realmbase.Client;
import realmbase.RealmBase;
import realmbase.data.Location;
import realmbase.data.Type;
import realmbase.listener.PacketListener;
import realmbase.listener.PacketManager;
import realmbase.packets.Packet;
import realmbase.packets.client.EnemyHitPacket;
import realmbase.packets.client.PlayerShootPacket;
import realmbase.packets.server.ShootPacket;
import realmbase.packets.server.Show_EffectPacket;
import realmbase.xml.GetXml;

public class MessageListener implements PacketListener{
	
	public MessageListener() {
		PacketManager.addListener(this);
	}
	
	Location last;
	
	@Override
	public boolean onReceive(Client c, Packet packet, Type from) {
		if(packet.getId() == GetXml.packetMapName.get("ENEMYHIT")){
			EnemyHitPacket cpacket = (EnemyHitPacket)packet;
			RealmBase.println("C: "+cpacket.toString());
		}
		
		return false;
	}

	@Override
	public boolean onSend(Client client, Packet packet, Type to) {
		return false;
	}

}
