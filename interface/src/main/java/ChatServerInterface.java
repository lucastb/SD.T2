import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatServerInterface extends Remote {
	public int register(String ipaddress, String nickname) throws RemoteException, NotBoundException;

	public int nick(int id, String nickname) throws RemoteException, NotBoundException;

	public String[] list(int id) throws RemoteException;

	public int create(int id, String channel) throws RemoteException, NotBoundException;

	public int remove(int id, String channel) throws RemoteException, NotBoundException;

	public int join(int id, String channel) throws RemoteException, NotBoundException;

	public int part(int id, String channel) throws RemoteException, NotBoundException;

	public String[] names(int id, String channel) throws RemoteException;

	public int kick(int id, String channel, String nickname) throws RemoteException, NotBoundException;

	public int msg(int id, String channel, String nickname, String message) throws RemoteException, NotBoundException;

	public int message(int id, String channel, String message) throws RemoteException;

	public int quit(int id) throws RemoteException, NotBoundException;
}
