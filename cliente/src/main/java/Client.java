import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Logger;

public class Client {

	private static final Logger logger = Logger.getLogger(Client.class.getName());
	private static String channelJoined = null;

	public static void main(String[] args) {
		try {
			if (args[0] == null) {
				logger.info("Argumentos inválidos!");
				System.exit(0);
			}
			String port = args[0];
			logger.info(String.format("Expondo o serviço na porta %s", args[0]));
			Registry registry = LocateRegistry.createRegistry(Integer.parseInt(port));
			logger.info("RMI Registry ready.");

			registry.rebind("ChatClient", new ChatClient());
			logger.info("ChatClient is ready.");

			ChatServerInterface chatServer = (ChatServerInterface) Naming.lookup("ChatServer");
			Integer userId = chatServer.register(port, port);

			while (true) {
				Scanner scanner = new Scanner(System.in);
				String command = scanner.nextLine();
				String[] arguments = command.split(" ");

				switch (arguments[0]) {
					case "/nick":
						chatServer.nick(userId, arguments[1]);
						break;
					case "/list":
						String[] channels = chatServer.list(userId);
						if (channels.length < 1) {
							logger.info("Nenhum canal disponível.");
						} else {
							Arrays.stream(channels).forEach(c -> logger.info(String.format("Canal %s está disponível", c)));
						}
						break;
					case "/create":
						chatServer.create(userId, arguments[1]);
						break;
					case "/remove":
						chatServer.remove(userId, arguments[1]);
						break;
					case "/join":
						int result = chatServer.join(userId, arguments[1]);
						if (result == 1) {
							channelJoined = arguments[1];
						}
						break;
					case "/part":
						int response = chatServer.part(userId, channelJoined);
						if (response == 1) {
							channelJoined = null;
						}
						break;
					case "/names":
						if (channelJoined == null) {
							logger.info("Você não está em nenhum canal!");
						} else {
							String[] names = chatServer.names(userId, channelJoined);
							if (names.length < 1) {
								logger.info("Nenhum usuário disponível.");
							}
							Arrays.stream(names).forEach(c -> logger.info(String.format("Usuário %s está disponível", c)));
						}
						break;
					case "/kick":
						if (channelJoined == null) {
							logger.info("Você não está em nenhum canal!");
						} else {
							chatServer.kick(userId, channelJoined, arguments[2]);
						}
						break;
					case "/quit":
						chatServer.quit(userId);
						break;
					case "/msg":
						if (channelJoined == null) {
							logger.info("Você não está em nenhum canal!");
						} else {
							String message = command
									.replace(args[0], "")
									.replace(args[1], "");
							chatServer.msg(userId, channelJoined, args[1], message);
						}
						break;
					default:
						chatServer.message(userId, channelJoined, command);
						break;
				}

			}

	} catch (Exception e) {
			logger.info("ChatClient falhou!");
			e.printStackTrace();
		}
	}
}

