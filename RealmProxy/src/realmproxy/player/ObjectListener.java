package realmproxy.player;

import java.util.HashMap;

import realmbase.Client;
import realmbase.GetXml;
import realmbase.RealmBase;
import realmbase.data.Entity;
import realmbase.data.Status;
import realmbase.data.Type;
import realmbase.listener.PacketListener;
import realmbase.listener.PacketManager;
import realmbase.packets.Packet;
import realmbase.packets.server.New_TickPacket;
import realmbase.packets.server.QuestObjIdPacket;
import realmbase.packets.server.UpdatePacket;

public class ObjectListener implements PacketListener{

	private HashMap<Integer,Entity> players = new HashMap<Integer, Entity>();
	private HashMap<Integer,Entity> quests = new HashMap<Integer, Entity>();
	private HashMap<Integer,Entity> portals = new HashMap<Integer, Entity>();
	
	public ObjectListener() {
		PacketManager.addListener(this);
	}
	
	@Override
	public boolean onReceive(Client c, Packet packet, Type from) {
		Player client = (Player)c;
		if(packet.getId() == GetXml.getPacketMapName().get("UPDATE")){
			UpdatePacket upacket = (UpdatePacket)packet;
			
			for(int i = 0; i < upacket.getNewObjs().length ; i++){
				Entity e = upacket.getNewObjs()[i];
				
				if(GetXml.getPlayersMap().containsKey(Integer.valueOf(e.objectType))){
					players.put(e.status.objectId, e);
				}else if(GetXml.getPortalsMap().containsKey(Integer.valueOf(e.objectType))){
					portals.put(e.status.objectId, e);
					RealmBase.println("Portal: "+GetXml.getPortalsMap().get(Integer.valueOf(e.objectType)));
					RealmBase.println("X: "+e.status.pos.x);
					RealmBase.println("Y: "+e.status.pos.y);
				}else if(GetXml.getQuestsMap().containsKey(Integer.valueOf(e.objectType))){
					quests.put(e.status.objectId, e);
					RealmBase.println("QuestType: "+GetXml.getQuestsMap().get(Integer.valueOf(e.objectType)));
					RealmBase.println("X: "+e.status.pos.x);
					RealmBase.println("Y: "+e.status.pos.y);
				}
			}
			
			for(int i = 0; i < upacket.getDrops().length ; i++){
				int objectId = upacket.getDrops()[i];
				
				players.remove(objectId);
				portals.remove(objectId);
				quests.remove(objectId);
			}
		}else if(packet.getId() == GetXml.getPacketMapName().get("NEW_TICK")){
			New_TickPacket tpacket = (New_TickPacket)packet;
			
			for(int i = 0; i < tpacket.getStatuses().length ; i++){
				Status e = tpacket.getStatuses()[i];
				
				for(Integer objectId : players.keySet()){
					if(objectId == e.objectId){
						players.get(objectId).status.pos.x=e.pos.x;
						players.get(objectId).status.pos.y=e.pos.y;
						break;
					}
				}
			}
		}else if(packet.getId() == GetXml.getPacketMapName().get("QUESTOBJID")){
			QuestObjIdPacket qpacket = (QuestObjIdPacket)packet;
			
			if(quests.containsKey(qpacket.getObjectId())){
				Entity e = quests.get(qpacket.getObjectId());
				RealmBase.println("NEW QUEST -> "+GetXml.getQuestsMap().get(Integer.valueOf(e.objectType)));
				RealmBase.println("X: "+e.status.pos.x);
				RealmBase.println("Y: "+e.status.pos.y);
			}
		}
		return false;
	}

	@Override
	public boolean onSend(Client client, Packet packet, Type to) {
		return false;
	}

}
