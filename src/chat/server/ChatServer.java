package chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Vector;
import java.util.logging.Logger;

import chat.Failure;
import logger.LoggerFactory;

/**
 * Classe du serveur de chat Chaque message de chaque client doit être renvoyé à
 * tous autres clients
 *
 * @author davidroussel
 */
public class ChatServer implements Runnable
{
	/**
	 * La socket serveur
	 */
	private ServerSocket serverSocket;

	/**
	 * Le port par défaut utilisé
	 */
	public final static int DEFAULTPORT = 1394;

	/**
	 * Temps d'attente (en ms) par défaut d'une connection d'un client. Au bout
	 * de ce temps une {@link SocketTimeoutException} est générée et on peut
	 * choisir de recommencer à attendre (s'il reste des clients) ou bien
	 * arrêter le serveur (s'il n'y a plus de clients)
	 */
	public final static int DEFAULTTIMEOUT = 1000;

	/**
	 * La liste des différents clients. Un client est constitué :
	 * <ul>
	 * <li>d'une {@link Socket}</li>
	 * <li>d'un nom : {@link String}</li>
	 * <li>d'un flux d'entrée : {@link BufferedReader}</li>
	 * <li>d'un flux de sortie {@link PrintWriter}</li>
	 * </ul>
	 * Cette liste devra être accédée de manière synchrone par les différents
	 * threads traitant les différents clients.
	 *
	 * @uml.property name="clients"
	 * @uml.associationEnd multiplicity="(0 -1)" ordering="true"
	 *                     aggregation="composite"
	 *                     inverse="chatServer:chat.server.InputOutputClient"
	 */
	private Vector<InputOutputClient> clients;

	/**
	 * Liste des handlers de chaque client
	 * @uml.property name="handlers"
	 * @uml.associationEnd multiplicity="(0 -1)" ordering="true"
	 *                     aggregation="composite"
	 *                     inverse="chatServer:chat.server.ClientHandler"
	 */
	private Vector<ClientHandler> handlers;

	/**
	 * logger pour afficher les messages d'erreur
	 */
	private Logger logger;

	/**
	 * Etat d'écoute du serveur. Cet état est vrai au départ et passe à false
	 * lorsque le dernier client se déconnecte.
	 */
	private boolean listening;

	/**
	 * Termine le serveur lorsque le dernier client se délogue
	 */
	private final boolean quitOnLastClient;

	/**
	 * Constructeur valué d'un serveur de chat. Celui ci initialise la
	 * {@link ServerSocket},
	 *
	 * @param port le port sur lequel on écoute les requètes
	 * @param verbose affiche les messages de débug ou pas
	 * @param timeout temps d'attente de connection d'un client
	 * @param quitOnLastClient quitte le serveur lorsque le dernier client
	 * se délogue
	 * @param parentLogger logger parent pour l'affichage des messages de
	 * debug
	 * @throws IOException Si une erreur intervient lors de la création de la
	 *             {@link ServerSocket}
	 */
	public ChatServer(int port,
	                  int timeout,
	                  boolean quitOnLastClient,
	                  Logger parentLogger)
	    throws IOException
	{
		this.quitOnLastClient = quitOnLastClient;
		logger = LoggerFactory.getParentLogger(getClass(),
		                                       parentLogger,
		                                       parentLogger.getLevel());

		logger.info("ChatServer::ChatServer(port = " + port + ", timeout = "
		    + timeout + ", quit = " + (quitOnLastClient ? "true" : "false")
		    + ")");

		serverSocket = new ServerSocket(port);
		if (serverSocket != null)
		{
			serverSocket.setSoTimeout(timeout);
		}

		clients = new Vector<InputOutputClient>();
		handlers = new Vector<ClientHandler>();
	}

	/**
	 * Constructeur valué d'un serveur de chat. Celui ci initialise la
	 * {@link ServerSocket},
	 *
	 * @param port le port sur lequel on écoute les requètes
	 * @param verbose affiche les messages de débug ou pas
	 * @param parentLogger logger parent pour l'affichage de messages de debug
	 * @throws IOException Si une erreur intervient lors de la création de la
	 *             {@link ServerSocket}
	 */
	public ChatServer(int port, Logger parentLogger) throws IOException
	{
		this(port, DEFAULTTIMEOUT, true, parentLogger);
	}

	/**
	 * Constructeur par défaut d'un serveur de chat. Celui ci initialise la
	 * @param parentLogger logger parent pour l'affichage des messages de
	 * debug
	 * {@link ServerSocket}, Le port utilisé par défaut est défini par
	 * {@link #DEFAULTPORT}
	 *
	 * @throws IOException Si une erreur intervient lors de la création de la
	 *             {@link ServerSocket}
	 * @see #DEFAULTPORT
	 */
	public ChatServer(Logger parentLogger) throws IOException
	{
		this(DEFAULTPORT, parentLogger);
	}

	/**
	 * Accesseur en lecture du {@link #quitOnLastClient}
	 * @return la valeu du {@link #quitOnLastClient}
	 */
	public boolean isQuitOnLastClient()
	{
		return quitOnLastClient;
	}

	/**
	 * Change l'état d'écoute du serveur
	 * @param value la nouvelle valeur
	 */
	public synchronized void setListening(boolean value)
	{
		listening = value;
	}

	/**
	 * Exécution du serveur de chat : - On attend la connection d'un client -
	 * Lorsque celle ci se produit le client est traité dans un nouveau thread -
	 * Lorsqu'un client envoie un message au serveur, celui ci le rediffuse à
	 * l'ensemble des autres clients
	 *
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		Vector<Thread> handlerThreads = new Vector<Thread>();
		listening = true;

		while (listening)
		{
			Socket clientSocket = null;
			String clientName = null;

			// acceptation de la socket du client
			try
			{
				// on attends ici une connection d'un nouveau client
				clientSocket = serverSocket.accept(); // --> IOException
				logger.fine("ChatServer: client connection accepted");

			}
			catch (SocketTimeoutException ste)
			{
				// on re-attends
				logger.info("Socket timeout, rewaiting ...");
				continue;
			}
			catch (IOException e)
			{

				logger.severe(Failure.SERVER_CONNECTION.toString()
				    + ": " + e.getLocalizedMessage());
				System.exit(Failure.SERVER_CONNECTION.toInteger());
			}

			if (clientSocket != null)
			{
				// récupération du nom du client
				BufferedReader reader = null;
				logger.info("ChatServer: Creatingc client input stream to get client's name ... ");
				try
				{
					reader = new BufferedReader(new InputStreamReader(
							clientSocket.getInputStream()));
				}
				catch (IOException e1)
				{
					logger.severe("ChatServer: " + Failure.CLIENT_INPUT_STREAM);
					logger.severe(e1.getLocalizedMessage());
					System.exit(Failure.CLIENT_INPUT_STREAM.toInteger());
				}
				if (reader != null)
				{
					logger.info("ChatServer: reading client's name: ");
					try
					{
						// Lecture du nom du client
						clientName = reader.readLine();
						logger.info("ChatServer: client name " + clientName);
					}
					catch (IOException e)
					{
						logger.severe("ChatServer: "+ Failure.NO_NAME_CLIENT);
						logger.severe(e.getLocalizedMessage());
						System.exit(Failure.NO_NAME_CLIENT.toInteger());
					}

					/*
					 * On ne doit PAS fermer le client input stream car cela
					 * revient à fermer la socket
					 */
				}

				// Avant d'enregister cette connection dans l'ensemble des
				// clients il faut vérifier qu'aucun client ne porte le même
				// nom
				if (searchClientByName(clientName) == null)
				{
					// Création d'un nouveau client
					InputOutputClient newClient =
							new InputOutputClient(clientSocket,
							                      clientName,
							                      logger);

					// Ajout du nouveau client à la liste des clients.
					synchronized (clients)
					{
						clients.add(newClient);
					}

					// Création et lancement d'un handler pour ce client
					ClientHandler handler = new ClientHandler(this,
					                                          newClient,
					                                          clients,
					                                          logger);
					handlers.add(handler);
					Thread handlerThread = new Thread(handler);
					handlerThread.start();
					handlerThreads.add(handlerThread);
				}
				else // un client avec ce nom existe déjà
				{
					// on notifie au client qu'il est refusé
					try
					{
						PrintWriter out = new PrintWriter(
								clientSocket.getOutputStream(), true);
						out.println("server > Sorry another client already use the name "
								+ clientName);
						out.println("Hit ^D to close your client and try another name");
						out.close();
					}
					catch (IOException e)
					{
						logger.severe("ChatServer: " + Failure.CLIENT_OUTPUT_STREAM);
						logger.severe(e.getLocalizedMessage());
					}
				}

				/*
				 * Lorsqu'un ClientHandler se termine il lance la méthode
				 * cleanup qui lorqu'il n'y a plus aucun thread modifie la
				 * valeur de "listening" à false
				 */
			}
		} // while listening

		// attente de la fin de tous les threads de ClientHandler
		for (Thread t : handlerThreads)
		{
			try
			{
				t.join();
			}
			catch (InterruptedException e)
			{
				logger.severe("ChatServer::run: Client handlers join interrupted");
				logger.severe(e.getLocalizedMessage());
			}
		}

		logger.info("ChatServer::run: all client handlers terminated");


		handlerThreads.clear();
		handlers.clear();
		clients.clear();

		// Fermeture de la socket du serveur
		logger.info("ChatServer::run: Closing server socket ... ");
		try
		{
			serverSocket.close();
		}
		catch (IOException e)
		{
			logger.severe("Close serversocket Failed !");
			logger.severe(e.getLocalizedMessage());
		}

	}

	/**
	 * Méthode invoquée par les {@link ClientHandler} à la fin de leur exécution
	 * pour éventuellement arrêter le serveur lorsqu'il n'y a plus de clients
	 */
	protected synchronized void cleanup()
	{
		// s'il ne reste plus de threads on arrête la boucle
		int nbThreads = ClientHandler.getNbThreads();
		if (nbThreads <= 0)
		{
			if (quitOnLastClient)
			{
				listening = false;
				logger.info("ChatServer::run: no more threads.");
			}
		}
		else
		{
			logger.info("ChatServer::run: still " + nbThreads +
					" threads remaining ...");
		}
	}

	/**
	 * Recherche parmis les clients déjà enregistrés un client portant le même
	 * nom que l'argument
	 *
	 * @param clientName le nom du client à rechercher parmis les clients déjà
	 *            enregistrés
	 * @return le client recherché s'il existe ou bien null s'il n'existe pas
	 */
	protected InputOutputClient searchClientByName(String clientName)
	{
		/*
		 * La consultation de la liste des clients à la recherche d'un nom doit
		 * être atomique afin qu'aucun autre thread ne puisse modifier cette
		 * liste pendant qu'on la consulte : d'où le "synchronized"
		 */
		synchronized (clients)
		{
			for (InputOutputClient c : clients)
			{
				if (c.getName().equals(clientName))
				{
					return c;
				}
			}
		}

		return null;
	}
}
