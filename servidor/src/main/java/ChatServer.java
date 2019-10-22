import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.logging.Logger;

public class ChatServer extends UnicastRemoteObject implements ChatServerInterface {

	private static final Logger logger = Logger.getLogger(ChatServer.class.getName());

	public ChatServer() throws RemoteException {
	}

	private final Map<Integer, String> usuariosAtivos = new HashMap<>(); // porta, nickname
	private final Map<String, Integer> canaisDisponiveis = new HashMap<>(); // nome do canal, porta do admin
	private final Map<Integer, String> canalDoUsuario = new HashMap<>(); // porta, canal do usuário

	/* Registra o usuário no servidor de chat. Um ID é retornado */
	public int register(String porta, String nickname) throws RemoteException, NotBoundException {
		if (!usuariosAtivos.containsKey(Integer.parseInt(porta))) {
			logger.info("Usuário não existe. Registrando no sistema.");
			usuariosAtivos.put(Integer.valueOf(porta), nickname);
			logger.info(String.format("Usuário %s registrado no sistema. Porta => %s", nickname, porta));
			this.enviaMensagemParaCliente(String.format("Você acabou de ser registrado com o nickname %s", nickname), Integer.valueOf(porta));
			return Integer.parseInt(porta);
		} else {
			logger.info("Usuário já registrado!");
			return 0;
		}
	}

	/* Solicita alteração do apelido do usuário. */
	public int nick(int porta, String nickname) throws RemoteException, NotBoundException {
		String nicknameAtual = this.usuariosAtivos.get(porta);
		if (usuariosAtivos.containsValue(nicknameAtual)) {
			logger.info(String.format("Usuário com id %s está solicitando a troca de nickname.", porta));
			usuariosAtivos.replace(porta, nickname);
			this.enviaMensagemParaCliente(String.format("Você acabou de alterar o nickname para %s", nickname), Integer.valueOf(porta));
			return 1;
		} else {
			logger.info(String.format("Usuário %s não existe!", porta));
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
	public int create(int porta, String canal) throws RemoteException, NotBoundException {
		if (!canaisDisponiveis.containsKey(canal) && canal.charAt(0) == '#') {
			logger.info(String.format("Criando canal %s com o usuário %s de administrador", canal, usuariosAtivos.get(porta)));
			canaisDisponiveis.put(canal, porta);
			this.enviaMensagemParaCliente(String.format("Você criou o canal %s", canal), porta);
			return 1;
		} else {
			return 0;
		}
	}

	/* Solicita a remoção de um canal. Todos os usuários que fazem parte do canal devem ser informados.
	Apenas o administrador do canal pode realizar essa operação. */
	public int remove(int id, String channel) throws RemoteException, NotBoundException {
		if (canaisDisponiveis.get(channel).equals(id)) {
			logger.info(String.format("Removendo todos usuários do canal %s", channel));
			canalDoUsuario.forEach((porta, canal) -> {
				if (canal.equalsIgnoreCase(channel)) {
					canalDoUsuario.remove(porta);
					try {
						this.enviaMensagemParaCliente(String.format("Você foi retirado do canal %s", channel), porta);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			this.enviaMensagemParaCliente(String.format("Você removeu o canal %s", channel), id);
			canaisDisponiveis.remove(channel);
			return 1;
		} else {
			this.enviaMensagemParaCliente(String.format("Você removeu o canal %s", channel), id);
			logger.info(String.format("Usuário %s não é administrador no canal.", id));
			return 0;
		}
	}

	/* Solicita a participação em um canal. A partir desse momento, o usuário deve passar a receber
	todas as mensagens enviadas ao canal. */
	public int join(int id, String channel) throws RemoteException, NotBoundException {
		if (canaisDisponiveis.containsKey(channel)) {
			if (!canalDoUsuario.containsKey(id)) {
				logger.info(String.format("Adicionando usuário %s no canal %s", id, channel));
				canalDoUsuario.put(id, channel);
				this.enviaMensagemParaCliente(String.format("Você foi inserido no canal %s", channel), id);
			} else {
				this.enviaMensagemParaCliente(String.format("Usuário %s já está em outro canal", usuariosAtivos.get(id)), id);
				logger.info(String.format("Usuário %s já está em outro canal", usuariosAtivos.get(id)));
			}
		} else {
			logger.info(String.format("Canal %s não existe!", channel));
			this.enviaMensagemParaCliente(String.format("Canal %s não existe!", channel), id);
		}
		return 1;
	}

	/* Solicita a saída do canal atual. A partir desse momento, o usuário não deve receber mensagens enviadas ao canal */
	public int part(int id, String channel) throws RemoteException, NotBoundException {
		if (canalDoUsuario.get(id).equalsIgnoreCase(channel)) {
			canalDoUsuario.remove(id);
			this.enviaMensagemParaCliente(String.format("Você foi removido do canal %s", channel), id);
			return 1;

		} else {
			logger.info(String.format("Usuário %s não está presente no canal %s!", id, channel));
			this.enviaMensagemParaCliente(String.format("Usuário %s não está presente no canal %s!", id, channel), id);
			return 0;
		}
	}

	/* Solicita a lista de usuários que fazem parte do canal atual. Necessário ter executado um comando join anteriormente */
	public String[] names(int id, String channel) {
		if (canalDoUsuario.get(id).equalsIgnoreCase(channel)) {
			Set<String> usuarios = new HashSet<>();
			canalDoUsuario.forEach((porta, canal) -> {
				if (canal.equalsIgnoreCase(channel)) {
					usuarios.add(usuariosAtivos.get(porta));
				}
			});
			return usuarios.toArray(String[]::new);
		} else {
			logger.info(String.format("Usuário %s não faz parte do canal %s", usuariosAtivos.get(id), channel));
			return new String[0];
		}
	}

	/* Solicita a remoção de um usuário de um canal. Somente o administrador do canal pode realizar essa operação. */
	public int kick(int id, String channel, String nickname) throws RemoteException, NotBoundException {
		if (canaisDisponiveis.get(channel).equals(id)) {
			logger.info(String.format("Usuário %s é administrador do canal %s. Executando kick.", id, channel));
			canalDoUsuario.forEach((porta, canal) -> {
				if (usuariosAtivos.get(porta).equalsIgnoreCase(nickname)) {
					canalDoUsuario.remove(porta);
					try {
						this.enviaMensagemParaCliente(String.format("Usuário %s removido do canal %s.", id, channel), id);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			return 1;
		}
		else {
			logger.info(String.format("Usuário %s não é administrador do canal!", id));
			this.enviaMensagemParaCliente(String.format("Usuário %s não é administrador do canal!", id), id);
			return 0;
		}
	}

	/* Envia uma mensagem privada para um usuário. Somente o usuário especificado pelo nickname deve receber a mensagem. */
	public int msg(int id, String channel, String nickname, String message) throws RemoteException, NotBoundException {
		int portaDoNickname = getPortaDoNickname(nickname);
		if (canalDoUsuario.get(id).equalsIgnoreCase(channel) && canalDoUsuario.get(portaDoNickname).equalsIgnoreCase(channel)) {
			logger.info(String.format("Enviando mensagem do usuário %s para o usuário %s", usuariosAtivos.get(id), nickname));
			this.enviaMensagemParaCliente(message, portaDoNickname);
			return 1;
		} else {
			logger.info(String.format("Não foi possível enviar a mensagem para o usuário %s", nickname));
			return 0;
		}
	}

	/* Envia uma mensagem ao canal atual. Necessário ter executado um comando join anteriormente. */
	public int message(int id, String channel, String message) {
		if (canalDoUsuario.get(id).equalsIgnoreCase(channel)) {
			canalDoUsuario.forEach((porta, canal) -> {
				if (canal.equalsIgnoreCase(channel) && !porta.equals(id)) {
					try {
						if (canaisDisponiveis.get(channel).equals(id)) {
							this.enviaMensagemParaCliente("*" + "<" + usuariosAtivos.get(id) + "> " + message, porta);
						} else {
							this.enviaMensagemParaCliente("<" + usuariosAtivos.get(id) + "> " + message, porta);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			return 1;
		} else {
			logger.info(String.format("Usuário %s não está no canal %s", usuariosAtivos.get(id), channel));
			return 0;
		}
	}

	private Integer getPortaDoNickname(String nickname) {
		Integer port = null;
		for (Integer porta: usuariosAtivos.keySet()) {
			if (usuariosAtivos.get(porta).equalsIgnoreCase(nickname)) {
				port = porta;
			}
		}
		return port;
	}

	/* Desconecta do serviço de chat. Se o usuário estiver participando de algum canal,
	ele deve ser removido desse canal antes de fechar o programa. */
	public int quit(int id) throws RemoteException, NotBoundException {
		canalDoUsuario.remove(id);
		this.quitaCliente(id);
		return 1;
	}

	private void enviaMensagemParaCliente(String mensagem, Integer porta) throws RemoteException, NotBoundException {
		Registry registry = LocateRegistry.getRegistry(porta);
		ChatClientInterface chatClient = (ChatClientInterface) registry.lookup("ChatClient");
		chatClient.message(mensagem);
	}

	private void quitaCliente(Integer porta) throws RemoteException, NotBoundException {
		Registry registry = LocateRegistry.getRegistry(porta);
		ChatClientInterface chatClient = (ChatClientInterface) registry.lookup("ChatClient");
		chatClient.kick();
	}

}
