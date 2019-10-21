import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;
import java.util.logging.Logger;

public class Client {

	private static final Logger logger = Logger.getLogger(Client.class.getName());

	public static void main(String[] args) {
		try {
			ChatClient implementation = new ChatClient(args[0]);
			LocateRegistry.createRegistry(1100);
			logger.info("RMI Registry ready.");

			Naming.rebind("ChatClient", implementation);
			logger.info("ChatClient is ready.");

			String ipaddress = args[0];
			ChatServerInterface chatServer = (ChatServerInterface) Naming.lookup("//localhost/ChatServer");
			Integer userId = chatServer.register(ipaddress, ipaddress);
			logger.info(String.format("Usuário %s registrado", userId));

			while (true) {
				Scanner scanner = new Scanner(System.in);
				String message = scanner.nextLine();
				implementation.message(message);
			}

		} catch (Exception e) {
			logger.info("ChatClient falhou!");
			e.printStackTrace();
		}
	}

}
