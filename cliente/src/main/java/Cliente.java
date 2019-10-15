import lombok.extern.log4j.Log4j2;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

@Log4j2
public class Cliente {

	public static void main(String[] args) {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {
			Registry registry = LocateRegistry.getRegistry("localhost");
			ChatServerInterface chatInterface = (ChatServerInterface) registry.lookup("ChatServer");


		} catch (RemoteException e) {

		} catch (Exception e) {

		}
	}

}
