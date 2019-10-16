import java.rmi.RemoteException;

public class ChatClient implements ChatClientInterface {

	/* Comandos disponíveis:
		- /nick <nickname>
		- /list list()
		- /create <channel>
		- /remove <channel>
		- /join <channel>
		- /part
		- /kick <channel> <nickname>
		- /msg <nickname> <message>
		- /quit */
	@Override
	public int message(String message) throws RemoteException {
		/* Utilizar uma regex para separar a string em grupos */
		return 0;
	}

	@Override
	public int kick() throws RemoteException {
		return 0;
	}
}
