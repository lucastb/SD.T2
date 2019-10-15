import java.rmi.RemoteException;

public class ChatServerImpl implements ChatServer {

	public int register(String ipaddress, String nickname) throws RemoteException {
		return 0;
	}

	public int nick(int id, String nickname) throws RemoteException {
		return 0;
	}

	public String[] list(int id) throws RemoteException {
		return new String[0];
	}

	public int create(int id, String channel) throws RemoteException {
		return 0;
	}

	public int remove(int id, String channel) throws RemoteException {
		return 0;
	}

	public int join(int id, String channel) throws RemoteException {
		return 0;
	}

	public int part(int id, String channel) throws RemoteException {
		return 0;
	}

	public String[] names(int id, String channel) throws RemoteException {
		return new String[0];
	}

	public int kick(int id, String channel, String nickname) throws RemoteException {
		return 0;
	}

	public int msg(int id, String channel, String nickname, String message) throws RemoteException {
		return 0;
	}

	public int message(int id, String channel, String message) throws RemoteException {
		return 0;
	}

	public int quit(int id) throws RemoteException {
		return 0;
	}
}
