package chat.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import chat.Failure;
import chat.UserOutputType;
import logger.LoggerFactory;

/**
 * Classe Principale d'un client de chat.
 * Instancie :
 * 	- la socket pour commnuniquer avec le serveur
 * 	- le UserHandler pour traiter les messages de l'utilisateur
 * 	- le ServerHandler pour traiter les messages du serveur
 * @author davidroussel
 */
public class ChatClient implements Runnable
{
	/**
	 * Nom d'utilisateur utilisé pour se connecter
	 */
	private String userName;

	/**
	 * Socket du client
	 */
	private Socket clientSocket;

	/**
	 * Flux d'entrée depuis le serveur
	 */
	private InputStream serverIn;

	/**
	 * Flux de sortie vers le serveur
	 */
	private OutputStream serverOut;

	/**
	 * Ecrivain vers le flux de sortie vers le serveur. Utilisé temporairement
	 * pour envoyer notre nom d'utilisateur au serveur
	 */
	private PrintWriter serverOutPW;

	/**
	 * Flux d'entrée depuis l'utilisateur
	 */
	private InputStream userIn;

	/**
	 * Flux de sortie vers l'utilisateur
	 */
	private OutputStream userOut;

	/**
	 * Handler des données en provenance du serveur
	 *
	 * @uml.property name="serverHandler"
	 * @uml.associationEnd multiplicity="(1 1)" aggregation="composite"
	 */
	private ServerHandler serverHandler = null;

	/**
	 * Handler des données en provenance de l'utilisateur
	 *
	 * @uml.property name="userHandler"
	 * @uml.associationEnd multiplicity="(1 1)" aggregation="composite"
	 */
	private UserHandler userHandler = null;

	/**
	 * Etat d'exécution commun du {@link #userHandler} et du
	 * {@link #serverHandler}, lorsque l'un des deux Runnable se termine, il met
	 * commonRun à faux ce qui force l'autre à se terminer.
	 */
	private Boolean commonRun;

	/**
	 * Etat du client. true si la socket ainsi que les différents flux
	 * d'entrée/sortie ont été créés
	 *
	 * @uml.property name="ready"
	 */
	private boolean ready;

	/**
	 * Le logger utilisé pour afficher les messages d'infos|erreurs|warnings
	 */
	private Logger logger;

	/**
	 * Constructeur d'un client de chat
	 *
	 * @param host l'adresse du serveur
	 * @param port le port à utiliser pour communiquer avec le serveur
	 * @param name le nom d'utilisateur utilisé
	 * @param in le flux d'entrée depuis l'utilisateur
	 * @param out le flux de sortie vers l'utilisateur
	 * @param outType le type de données attendues dans le flux de sortie vers
	 * le client (texte ou objets)
	 * @param l'état d'exécution commun avec un autre runnable. ou bien null
	 *            s'il n'y a pas d'autre runnable à synchroniser avec ceux
	 *            lancés dans le ChatClient
	 * @param verbose niveau de debug pour les messages
	 */
	public ChatClient(String host,
	                  int port,
	                  String name,
	                  InputStream in,
	                  OutputStream out,
	                  UserOutputType outType,
	                  Boolean commonRun,
	                  Logger parentLogger)
	{
		userName = name;
		ready = false;

		// Création du logger
		logger = LoggerFactory.getParentLogger(getClass(),
		                                       parentLogger,
		                                       parentLogger.getLevel());

		/*
		 * TODO Création de la socket vers host/port
		 */
		clientSocket = null;
		try
		{
			clientSocket = new Socket(host, port);
			logger.info("ChatClient: socket created");
		}
		catch (UnknownHostException e)
		{
			/*
			 * TODO Notez bien cette façon de faire, vous devrez la  reproduire
			 * par la suite
			 */
			logger.severe("ChatClient: " + Failure.UNKNOWN_HOST + ": " + host);
			logger.severe(e.getLocalizedMessage());
			System.exit(Failure.UNKNOWN_HOST.toInteger());
		}
		catch (IOException e)
		{
			logger.severe("ChatClient: " + Failure.CLIENT_CONNECTION
					+ " to: \"" + host + "\" at port \"" + port + "\"");
			logger.severe(e.getLocalizedMessage());
			System.exit(Failure.CLIENT_CONNECTION.toInteger());
		}

		/*
		 * TODO Obtention du flux de sortie vers le serveur (serverOut) à partir
		 * de la clientSocket.
		 * avec utilisation du logger pour afficher la progression ou les erreurs
		 * 	- logger.info("ChatClient: got client output stream to server"); si le serverOut est non null
		 * 	- logger.severe("ChatClient: null server out" + Failure.CLIENT_INPUT_STREAM); si le serverOut est null
		 * 	- logger.severe("ChatClient: " + Failure.CLIENT_OUTPUT_STREAM); si une IOException survient
		 * les "severe" doivent être suivi d'un System.exit(...) comme ci-dessus;
		 */
		serverOut = null;
		try
		{
			// TODO serverOut = ...
			if (serverOut != null)
			{
				logger.info("ChatClient: got client output stream to server");
			}
			else
			{
				logger.severe("ChatClient: null server out" + Failure.CLIENT_INPUT_STREAM);
				System.exit(Failure.CLIENT_OUTPUT_STREAM.toInteger());
			}
			throw new IOException(); // TODO Remove this line when serverOut is obtained
		}
		catch (IOException e)
		{
			logger.severe("ChatClient: " + Failure.CLIENT_OUTPUT_STREAM);
			logger.severe(e.getLocalizedMessage());
			System.exit(Failure.CLIENT_OUTPUT_STREAM.toInteger());
		}

		/*
		 * TODO Création PrintWriter temporaire sur le serverOut
		 * (avec autoFlush): serverOutPW
		 * et envoi de notre nom d'utilisateur au serveur (avec un println)
		 * afin qu'il puisse créer un thread dédié à notre traitement
		 * ajout d'un message d'info au logger pour la création du serverOutPW
		 * et d'un warning si celui ci a des erreurs après l'envoi du nom au
		 * serveur.
		 */
		if (serverOut != null)
		{
			// serverOutPW = // TODO Complete ...
			logger.info("ChatClient: sending name to server ... ");

			serverOutPW.println(userName);
			if (serverOutPW.checkError())
			{
				logger.warning("ChatClient: serverOutPw has errors");
			}
		}

		/*
		 * TODO Obtention du flux d'entrée depuis le serveur (serverIn) à partir
		 * de la clientSocket.
		 * Si une IOException
		 * 	- ajout d'un "severe" au logger avec Failure.CLIENT_INPUT_STREAM
		 * 	- System.exit(...);
		 */
		serverIn = null;
		try
		{
			// TODO serverIn = ...
			throw new IOException(); // TODO Remove this line when serverIn is obtained
		}
		catch (IOException e)
		{
			logger.severe("ChatClient: " + Failure.CLIENT_INPUT_STREAM);
			logger.severe(e.getLocalizedMessage());
			System.exit(Failure.CLIENT_INPUT_STREAM.toInteger());
		}

		// obtention des flux de l'utilisateur
		userIn = in;
		userOut = out;

		// Etat d'exécution commun
		if (commonRun == null)
		{
			this.commonRun = new Boolean(true);
		}
		else
		{
			this.commonRun = commonRun;
		}

		// Création du user handler
		userHandler = new UserHandler(userIn,
		                              serverOut,
		                              this.commonRun,
		                              logger);

		// création du server handler
		serverHandler = new ServerHandler(userName,
		                                  serverIn,
		                                  userOut,
		                                  outType,
		                                  this.commonRun,
		                                  logger);

		ready = true;
	}

	/**
	 * Accès en lecture de l'état du client
	 *
	 * @return the ready
	 * @uml.property name="ready"
	 */
	public boolean isReady()
	{
		return ready;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		/*
		 * Tant que ce que l'on lit depuis l'utilisateur n'est pas null (avec un
		 * ctrl-D par exemple), on envoie ce que l'on a lu au serveur et on
		 * attends que celui ci nous réponde pour afficher ce qu'il nous envoie.
		 * On a donc deux boucles d'attente : d'une part l'utilisateur, d'autre
		 * part le serveur. Chaque boucle est donc traitée dans son propre
		 * thread UserHandler traite les entrées de l'utilisateur ServerHandler
		 * traite les entrées du serveur et on attends la fin des deux threads
		 * pour terminer le client Les deux threads partagent une variable
		 * "commonRun" lorsque l'un des deux threads se termine il met cette
		 * vatiable à false. A chaque tour de boucle de chacun des threads ils
		 * consultent (de manière atomique) cette variable afin de savoir s'ils
		 * peuvent continuer
		 */

		Thread[] threads = new Thread[2];

		// Création du thread du UserHandler
		threads[0] = new Thread(userHandler);

		// Création du thread du ServerHandler
		threads[1] = new Thread(serverHandler);

		// Lancement des threads
		for (int i = 0; i < threads.length; i++)
		{
			threads[i].start();
		}

		// Attente de la fin des 2 threads
		for (int i = 0; i < threads.length; i++)
		{
			try
			{
				threads[i].join();
			}
			catch (InterruptedException e)
			{
				logger.warning("Join thread " + i + " interrupted");
			}
		}

		logger.info("ChatClient: All threads terminated");

		cleanup();
	}

	/**
	 * Nettoyage du client : fermeture des flux d'entrée/sortie et fermeture de
	 * la socket
	 */
	public void cleanup()
	{
		// Cleanup du #userHandler
		userHandler.cleanup();

		// Cleanup du #serverHandler
		serverHandler.cleanup();

		// fermeture du flux temporaire de sortie vers le serveur
		logger.info("ChatClient: closing server output stream ... ");
		serverOutPW.close();

		// fermeture de la socket
		logger.info("ChatClient: closing client socket ... ");
		try
		{
			clientSocket.close();
		}
		catch (IOException e)
		{
			logger.severe("ChatClient: closing client socket failed");
			logger.severe(e.getLocalizedMessage());
		}
	}
}
