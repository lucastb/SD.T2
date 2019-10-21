import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ChatServer extends UnicastRemoteObject implements ChatServerInterface {

	private static final Logger logger = Logger.getLogger(ChatServer.class.getName());

	public ChatServer() throws RemoteException {
	}

	private final Map<Integer, String> usuarios = new HashMap<>(); // userId, ip
	private final Map<String, String> nicknames = new HashMap<>(); // ip, nickname
	private final Map<String, Integer> canaisDisponiveis = new HashMap<>(); // nome do canal, userId
	private final Map<String, List<Integer>> usuariosEmCanal = new HashMap<>(); // nome do canal, usuarios nela
	private int clientesSequence = 0;

	/* Registra o usuário no servidor de chat. Um ID é retornado */
	public int register(String ipaddress, String nickname) throws RemoteException {
		if (!nicknames.containsKey(nickname)) {
			logger.info("Usuário não existe. Registrando no sistema.");
			int idUsuario = clientesSequence;
			usuarios.put(idUsuario, nickname);
			nicknames.put(ipaddress, nickname);
			clientesSequence = clientesSequence++;
			logger.info(String.format("Usuário %s registrado no sistema. Ip => %s", idUsuario, ipaddress));
			return idUsuario;
		} else {
			logger.info("Usuário já registrado!");
			return 0;
		}
	}

	/* Solicita alteração do apelido do usuário. */
	public int nick(int id, String nickname) {
		String nicknameAtual = encontraNickname(id);
		if (nicknames.containsValue(nicknameAtual)) {
			logger.info(String.format("Usuário com id %s está solicitando a troca de nickname.", id));
			nicknames.replace(usuarios.get(id), nickname);
			return 1;
		} else {
			logger.info(String.format("Usuário %s não existe!", id));
			return 0;
		}
	}

	/* Solicita a lista de canais disponíveis no servidor */
	public String[] list(int id) throws RemoteException {
		return canaisDisponiveis.keySet().toArray(new String[0]);
	}

	/* Solicita a criação de um novo canal no servidor. O usuário que criar o canal será o administrador do mesmo.
	Apenas o administrador pode realizar operações do tipo remove e kick. O apelido do administrador do canal deve
	sempre ser impresso com um * na frente. */
	public int create(int id, String channel) throws RemoteException {
		if (!canaisDisponiveis.containsKey(channel)) {
			logger.info(String.format("Criando canal %s com o usuário %s de administrador", channel, id));
			canaisDisponiveis.put(channel, id);
			return 1;
		} else {
			return 0;
		}
	}

	/* Solicita a remoção de um canal. Todos os usuários que fazem parte do canal devem ser informados.
	Apenas o administrador do canal pode realizar essa operação. */
	public int remove(int id, String channel) throws RemoteException {
		if (canaisDisponiveis.get(channel).equals(id)) {
			logger.info(String.format("Removendo todos usuários do canal %s", channel));
			usuariosEmCanal.remove(channel);
			canaisDisponiveis.remove(channel);
			// mandar mensagem para todos os usuários
			return 1;
		} else {
			logger.info("Usuário %s não é administrador no canal.");
			return 0;
		}
	}

	/* Solicita a participação em um canal. A partir desse momento, o usuário deve passar a receber
	todas as mensagens enviadas ao canal. */
	public int join(int id, String channel) throws RemoteException {
		if (canaisDisponiveis.containsKey(channel)) {
			logger.info(String.format("Adicionando usuário %s no canal %s", id, channel));
			List<Integer> usuariosAtuais = usuariosEmCanal.get(channel);
			if (usuariosAtuais == null) {
				usuariosAtuais = List.of(id);
				usuariosEmCanal.put(channel, usuariosAtuais);
			} else {
				usuariosAtuais.add(id);
				usuariosEmCanal.replace(channel, usuariosAtuais);
			}
		} else {
			logger.info(String.format("Canal %s não existe!", channel));
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
			logger.info(String.format("Usuário %s não está presente no canal %s!", id, channel));
			return 0;
		}
	}

	/* Solicita a lista de usuários que fazem parte do canal atual. Necessário ter executado um comando join anteriormente */
	public String[] names(int id, String channel) throws RemoteException {
		if (usuariosEmCanal.get(channel).contains(id)) {
			logger.info(String.format("Usuário %s está no canal %s, listando os nomes dos usuários.", id, channel));
			return usuariosEmCanal.get(channel).stream().map(this::encontraNickname).toArray(String[]::new);
		} else {
			return new String[]{};
		}
	}

	/* Solicita a remoção de um usuário de um canal. Somente o administrador do canal pode realizar essa operação. */
	public int kick(int id, String channel, String nickname) throws RemoteException {
		if (canaisDisponiveis.get(channel) == id) {
			logger.info(String.format("Usuário %s é administrador do canal %s. Executando kick.", id, channel));
			int userId = encontraUserId(nickname);
			List<Integer> usuariosAtuais = usuariosEmCanal.get(channel);
			usuariosAtuais.remove(userId);
			return 1;
		} else {
			logger.info(String.format("Usuário %s não é administrador do canal!", id));
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
		logger.info(String.format("Efetuando busca do id para o nickname %s", nickname));
		int[] id = new int[1];
		usuarios.forEach((userId, ip) -> {
			if (usuarios.get(userId).equals(nickname)) {
				logger.info(String.format("Encontrado o id do nickname %s", nickname));
				id[0] = userId;
			}
		});
		return id[0];
	}

	private String encontraNickname(Integer userId) {
		String[] nickname = new String[1];
		usuarios.forEach((id, ip) -> {
			if (id.equals(userId)) {
				nickname[0] = nicknames.get(ip);
			}
		});
		return nickname[0];
	}
}
