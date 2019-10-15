import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {

	public static void main(String[] args) {
		ChatServerImpl serverImplementation = new ChatServerImpl();

		try {
			ChatServerInterface stub = (ChatServerInterface) UnicastRemoteObject.exportObject(serverImplementation, 0);
			Registry registry = LocateRegistry.getRegistry();
			registry.rebind("ChatServer", stub);
		} catch (RemoteException ex) {
			ex.printStackTrace();
		}
	}

}
