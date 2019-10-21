import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.logging.Logger;

public class ChatClient extends UnicastRemoteObject implements ChatClientInterface {

	private static final Logger logger = Logger.getLogger(ChatClient.class.getName());
	private static String channelJoined = null;
	private static Integer userId = null;
	private ChatServerInterface chatServer = (ChatServerInterface) Naming.lookup("//localhost/ChatServer");

	protected ChatClient(String ipaddress) throws RemoteException, MalformedURLException, NotBoundException {
		userId = chatServer.register(ipaddress, ipaddress);
	}

	@Override
	public int message(String message) throws RemoteException {
		if (message.startsWith("/nick")) {
			String nickname = message.replace("/nick", "").trim();
			chatServer.nick(userId, nickname);
		} else if (message.startsWith("/list")) {
			String[] channels = chatServer.list(userId);
			Arrays.stream(channels).forEach(c -> logger.info(String.format("Canal %s está disponível", c))
			);
		} else if (message.startsWith("/create")) {
			String channelName = message.replace("/create", "").trim();
			chatServer.create(userId, channelName);
		} else if (message.startsWith("/remove")) {
			String channelName = message.replace("/remove", "").trim();
			chatServer.remove(userId, channelName);
		} else if (message.startsWith("/join")) {
			String channelName = message.replace("/join", "").trim();
			int result = chatServer.join(userId, channelName);
			if (result == 1) {
				channelJoined = channelName;
			}
		} else if (message.startsWith("/part")) {
			int result = chatServer.part(userId, channelJoined);
			if (result == 1) {
				channelJoined = null;
			}
		} else if (message.startsWith("/names")) {
			String[] names = chatServer.names(userId, channelJoined);
			if (names.length < 1) {
				logger.info("Nenhum usuário disponível.");
			}
			Arrays.stream(names).forEach(c -> logger.info(
					String.format("Usuário %s está disponível", c))
			);
		} else if (!message.startsWith("/")) {
			chatServer.message(userId, message, channelJoined);
		} else if (message.startsWith("/quit")) {
			int result = chatServer.quit(userId);
			if (result == 1) {
				userId = null;
			}
		}
		return 1;
	}

	@Override
	public int kick() {
		return 0;
	}
}
