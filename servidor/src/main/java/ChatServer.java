import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatServer implements ChatServerInterface {

	private final Map<Integer, String> usuarios = new HashMap<>(); // userId, ip
	private final Map<String, String> nicknames = new HashMap<>(); // ip, nickname
	private final Map<String, Integer> canaisDisponiveis = new HashMap<>(); // nome do canal, userId
	private final Map<String, List<Integer>> usuariosEmCanal = new HashMap<>(); // nome do canal, usuarios nela
	private int clientesSequence = 0;

	/* Registra o usuário no servidor de chat. Um ID é retornado */
	public int register(String ipaddress, String nickname) throws RemoteException {
		if (nicknames.containsKey(nickname)) {
			System.out.println("Usuário não existe. Registrando no sistema.");
			usuarios.put(clientesSequence, nickname);
			nicknames.put(ipaddress, nickname);
			clientesSequence++;
			return clientesSequence;
		} else {
			System.out.println("Usuário já registrado!");
			return 0;
		}
	}

	/* Solicita alteração do apelido do usuário. */
	public int nick(int id, String nickname) throws RemoteException {
		if (usuarios.containsKey(id)) {
			System.out.println(String.format("Usuário com id %s está solicitando a troca de nickname.", id));
			nicknames.replace(usuarios.get(id), nickname);
			return 1;
		} else {
			System.out.println(String.format("Usuário %s não existe!", id));
			return 0;
		}
	}

	/* Solicita a lista de canais disponíveis no servidor */
	public String[] list(int id) throws RemoteException {
		return new String[0];
	}

	/* Solicita a criação de um novo canal no servidor. O usuário que criar o canal será o administrador do mesmo.
	Apenas o administrador pode realizar operações do tipo remove e kick. O apelido do administrador do canal deve
	sempre ser impresso com um * na frente. */
	public int create(int id, String channel) throws RemoteException {
		if (!canaisDisponiveis.containsKey(channel)) {
			System.out.println(String.format("Criando canal %s com o usuário %s de administrador", channel, id));
			canaisDisponiveis.put(channel, id);
			return 1;
		} else {
			return 0;
		}
	}

	/* Solicita a remoção de um canal. Todos os usuários que fazem parte do canal devem ser informados.
	Apenas o administrador do canal pode realizar essa operação. */
	public int remove(int id, String channel) throws RemoteException {
		return 0;
	}

	/* Solicita a participação em um canal. A partir desse momento, o usuário deve passar a receber
	todas as mensagens enviadas ao canal. */
	public int join(int id, String channel) throws RemoteException {
		if (canaisDisponiveis.containsKey(channel)) {
			System.out.println(String.format("Adicionando usuário %s no canal %s", id, channel));
			List<Integer> usuariosAtuais = usuariosEmCanal.get(channel);
			usuariosAtuais.add(id);
			usuariosEmCanal.replace(channel, usuariosAtuais);
		} else {
			System.out.println(String.format("Canal %s não existe!", channel));
		}
		return 1;
	}

	/* Solicita a saída do canal atual. A partir desse momento, o usuário não deve receber mensagens enviadas ao canal */
	public int part(int id, String channel) throws RemoteException {
		if (usuariosEmCanal.get(channel).contains(id)) {
			List<Integer> usuariosAtuais = usuariosEmCanal.get(channel);
			usuariosAtuais.remove(id);
			usuariosEmCanal.replace(channel, usuariosAtuais);
			return 1;
		} else {
			System.out.println(String.format("Usuário %s não está presente no canal %s!", id, channel));
			return 0;
		}
	}

	/* Solicita a lista de usuários que fazem parte do canal atual. Necessário ter executado um comando join anteriormente */
	public String[] names(int id, String channel) throws RemoteException {
		if (usuariosEmCanal.get(channel).contains(id)) {
			System.out.println(String.format("Usuário %s está no canal %s, listando os nomes dos usuários.", id, channel));
			return (String[])usuariosEmCanal.get(channel).toArray();
		} else {
			return new String[]{};
		}
	}

	/* Solicita a remoção de um usuário de um canal. Somente o administrador do canal pode realizar essa operação. */
	public int kick(int id, String channel, String nickname) throws RemoteException {
		if (canaisDisponiveis.get(channel) == id) {
			System.out.println(String.format("Usuário %s é administrador do canal %s. Executando kick.", id, channel));
			int userId = encontraUserId(nickname);
			List<Integer> usuariosAtuais = usuariosEmCanal.get(channel);
			usuariosAtuais.remove(userId);
			return 1;
		} else {
			System.out.println(String.format("Usuário %s não é administrador do canal!", id));
			return 0;
		}
	}

	/* Envia uma mensagem privada para um usuário. Somente o usuário especificado pelo nickname deve receber a mensagem. */
	public int msg(int id, String channel, String nickname, String message) throws RemoteException {
		return 0;
	}

	/* Envia uma mensagem ao canal atual. Necessário ter executado um comando join anteriormente. */
	public int message(int id, String channel, String message) throws RemoteException {
		return 0;
	}

	/* Desconecta do serviço de chat. Se o usuário estiver participando de algum canal,
	ele deve ser removido desse canal antes de fechar o programa. */
	public int quit(int id) throws RemoteException {
		usuariosEmCanal.forEach((channel, usuariosNoCanal) -> {
			if (usuariosNoCanal.contains(id)) {
				List<Integer> usuariosAtuais = usuariosEmCanal.get(channel);
				usuariosAtuais.remove(id);
				usuariosEmCanal.replace(channel, usuariosAtuais);
			}
		});
		return 1;
	}

	private int encontraUserId(String nickname) {
		System.out.println(String.format("Efetuando busca do id para o nickname %s", nickname));
		int[] id = new int[1];
		usuarios.forEach((userId, ip) -> {
			if (usuarios.get(userId).equals(nickname)) {
				System.out.println(String.format("Encontrado o id do nickname %s", nickname));
				id[0] = userId;
			}
		});
		return id[0];
	}
}
