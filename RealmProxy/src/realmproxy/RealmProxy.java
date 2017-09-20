package realmproxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import realmbase.Parameter;
import realmbase.RealmBase;
import realmproxy.net.ListenSocket;
import realmproxy.player.ConnectListener;
import realmproxy.player.MessageListener;
import realmproxy.player.Player;

public class RealmProxy {

	private static ListenSocket listenSocket;
	public static String localHost = "localhost";
	public static int localPort = 2050;

	private static final Map<Integer, InetSocketAddress> gameIdSocketAddressMap = new Hashtable<Integer, InetSocketAddress>();
	
	public static InetSocketAddress getSocketAddress(int gameId) {
		InetSocketAddress socketAddress = gameIdSocketAddressMap.get(gameId);
		if (socketAddress == null) {
			return Parameter.remoteHost;
		}
		return socketAddress;
	}
	
	public static void setSocketAddress(int gameId, String host, int port) {
		InetSocketAddress socketAddress = new InetSocketAddress(host, port);
		gameIdSocketAddressMap.put(gameId, socketAddress);
	}
	
	public static void init(){
		RealmBase.init();
		new ConnectListener();
		new MessageListener();
	}
	
	public static void main(String[] args) {
		init();
		RealmBase.println("startet...");
		
		listenSocket = new ListenSocket(localHost,localPort) {
			
			@Override
			public void socketAccepted(Socket socket) {
				try{
					new Player(socket);
					RealmBase.println("Verbunden "+socket);
				}catch(Exception e){
					e.printStackTrace();
					try {
						socket.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		};
		
		RealmBase.println("ListenSocket laeuft!");
		if (listenSocket.start()) {
			while (!listenSocket.isClosed()) {
				while (!Player.getNewPlayers().isEmpty()) {
					Player client = Player.getNewPlayers().remove(0);
					Player.getPlayers().add(client);
				}
				int cores = Runtime.getRuntime().availableProcessors();
				Thread[] threads = new Thread[cores];
				int core = 0;
				Iterator<Player> i = Player.getPlayers().iterator();
				while (i.hasNext()) {
					final Player client = i.next();
					if (client == null 
							|| client.getLocalSocket()==null 
							|| (client.getLocalSocket()!=null&&client.getLocalSocket().isClosed()) 
							|| (client.getRemoteSocket()!=null&&client.getRemoteSocket().isClosed())) {
						if(client != null)client.kick();
						i.remove();
						continue;
					}
					if (threads[core] != null) {
						try {
							threads[core].join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					(threads[core] = new Thread(new Runnable() {
						
						@Override
						public void run() {
							client.process();
						}
						
					})).start();
					core = (core + 1) % cores;
				}
				for (Thread thread: threads) {
					if (thread == null) {
						continue;
					}
					try {
						thread.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				Thread.yield();
			}
			
			Iterator<Player> i = Player.getPlayers().iterator();
			while (i.hasNext()) {
				Player user = i.next();
				user.kick();
			}
		} else {
			RealmBase.println("Der Listener hat probleme! Sei sicher ob vlt nicht schon das Programm laeuft");
		}
	}
	
}
