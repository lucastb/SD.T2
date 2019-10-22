import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;

public class ChatClient extends UnicastRemoteObject implements ChatClientInterface {

	private static final Logger logger = Logger.getLogger(ChatClient.class.getName());

	protected ChatClient() throws RemoteException {
	}

	@Override
	public int message(String message) throws RemoteException {
		logger.info(message);
		return 1;
	}

	@Override
	public int kick() {
		System.exit(0);
		return 0;
	}
}

