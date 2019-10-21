import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.logging.Logger;

public class Server {

	private static final Logger logger = Logger.getLogger(Server.class.getName());

	public static void main(String[] args) {
		try {
			LocateRegistry.createRegistry(1099);
			logger.info("RMI Registry ready.");
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		try {
			Naming.rebind("ChatServer", new ChatServer());
			logger.info("ChatServer is ready.");
		} catch (RemoteException | MalformedURLException e) {
			e.printStackTrace();
		}
	}

}
