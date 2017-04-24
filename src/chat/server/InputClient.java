package chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Logger;

import logger.LoggerFactory;

/**
 * Classe stockant les caractéristiques d'un client traité par un
 * {@link ClientHandler}. Celui ci est caractérisé par
 * <ul>
 * <li>{@link #clientSocket} : {@link Socket} du client</li>
 * <li>{@link #name} : nom du client</li>
 * <li>{@link #inBR} : {@link BufferedReader} créé à partir d'un
 * {@link InputStreamReader} sur l'{@link InputStream} de la {@link Socket}
 * et permettant de lire le texte en provenance du client</li>
 * <li>{@link #ready} indique que l'{@link BufferedReader} a été créé et que
 * l'on est prêt à lire les lignes en provenance du client</li>
 * <li>{@link #banned} indique le statut de bannissement</li>
 * </ul>
 *
 * @author davidroussel
 */
public class InputClient
{
	/**
	 * La socket du client
	 */
	protected Socket clientSocket;

	/**
	 * Le nom du client
	 *
	 * @uml.property name="name"
	 */
	protected String name;

	/**
	 * le flux d'entrée du client (celui sur lequel on lit ce qui vient du
	 * client)
	 */
	protected BufferedReader inBR;

	/**
	 * Un Main client est "ready" lorsque sa clientSocket est non nulle et que
	 * l'on a réussi à obtenir son input stream
	 *
	 * @uml.property name="ready"
	 */
	protected boolean ready;

	/**
	 * Etat de bannissement du client. Idée : le premier utilisateur du serveur
	 * est considéré comme le super-user (un MainClient). En conséquence il a
	 * le privilège de pouvoir kicker les autres clients.
	 *
	 * @uml.property name="banned"
	 */
	protected boolean banned;

	/**
	 * logger pour afficher les messages de debug
	 */
	protected Logger logger;

	/**
	 * Constructeur d'un MainClient
	 * @param socket the client's socket
	 * @param name the client's name
	 * @param parentLogger logger parent pour l'affichage des messages de debug
	 */
	public InputClient(Socket socket, String name, Logger parentLogger)
	{
		clientSocket = socket;
		this.name = name;
		inBR = null;
		ready = false;

		logger = LoggerFactory.getParentLogger(getClass(),
		                                       parentLogger,
		                                       parentLogger.getLevel());

		if (socket != null)
		{
			logger.info("InputClient: Creating Input Stream ... ");
			try
			{
				inBR = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				ready = true;
			}
			catch (IOException e)
			{
				logger.severe("InputClient: unable to get client socket input stream");
				logger.severe(e.getLocalizedMessage());
			}
		}
	}

	/**
	 * Accesseur en lecture du nom du client
	 *
	 * @return the name
	 * @uml.property name="name"
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Accesseur en lecture du flux d'entrée du client
	 *
	 * @return the input {@link BufferedReader}
	 */
	public BufferedReader getIn()
	{
		return inBR;
	}

	/**
	 * Accesseur en lecture de l'état du client
	 *
	 * @return the ready
	 * @uml.property name="ready"
	 */
	public boolean isReady()
	{
		return ready;
	}

	/**
	 * Accesseur en lecture de l'état de banissement
	 *
	 * @return l'état de banissement
	 * @uml.property name="banned"
	 */
	public boolean isBanned()
	{
		return banned;
	}

	/**
	 * Accesseur en écriture de l'état de banissement
	 *
	 * @param l'état de banissement à mettre en place
	 * @uml.property name="banned"
	 */
	public void setBanned(boolean banned)
	{
		this.banned = banned;
	}

	/**
	 * Nettoyage d'un client principal : fermeture du flux d'entrée et fermeture
	 * de sa socket.
	 */
	public void cleanup()
	{
		ready = false;
		logger.info("MainClient::cleanup: closing input stream ... ");
		try
		{
			inBR.close();
		}
		catch (IOException e)
		{
			logger.severe("MainClient::cleanup: unable to close input stream");
			logger.severe(e.getLocalizedMessage());
		}

		logger.info("MainClient::cleanup: closing client socket ... ");
		try
		{
			clientSocket.close();
		}
		catch (IOException e)
		{
			logger.severe("MainClient::cleanup: unable to close client socket");
			logger.severe(e.getLocalizedMessage());
		}
	}
}
