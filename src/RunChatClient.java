import java.awt.EventQueue;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;

import chat.Failure;
import chat.UserOutputType;
import chat.client.ChatClient;
import widgets.AbstractClientFrame;
import widgets.ClientFrame;

/**
 * Lanceur d'un client de chat.
 *
 * @author davidroussel
 */
public class RunChatClient extends AbstractRunChat
{
	/**
	 * Hôte sur lequel se trouve le serveur de chat
	 */
	private String host;

	/**
	 * Nom d'utilisateur à utiliser pour se connecter au serveur. Si le nom
	 * n'est pas fournit
	 */
	private String name;

	/**
	 * Flux d'entrée sur lequel lire les messages tapés par l'utilisateur
	 */
	private InputStream userIn;

	/**
	 * Flux de sortie sur lequel envoyer les messages vers l'utilisateur
	 */
	private OutputStream userOut;

	/**
	 * Indique si le client à créer est un GUI ou pas
	 */
	private boolean gui;

	/**
	 * La version de l'interface graphique à lancer:
	 * <ul>
	 * 	<li>version 1 correspond à l'utilisation d'une ClientFrame</li>
	 * 	<li>version 2 correspond à l'utilisation d'une SuperClientFrame</li>
	 * </ul>
	 */
	private int guiVersion;

	/**
	 * Ensemble des threads des clients.
	 * Il faudra attendre la fin de ces threads pour terminer l'exécution
	 * principal.
	 */
	private Vector<Thread> threadPool;

	/**
	 * Constructeur d'un lanceur de client d'après les arguments du programme
	 * principal
	 *
	 * @param args les arguments du programme principal
	 */
	protected RunChatClient(String[] args)
	{
		super(args);

		/*
		 * Initialisation des flux d'I/O utilisateur à null
		 * ils dépendront du client à créer (console ou GUI)
		 */
		userIn = null;
		userOut = null;

		/*
		 * Initialisation du pool de thread des clients
		 */
		threadPool = new Vector<Thread>();
	}

	/**
	 * Mise en place des attributs du client de chat en fonction des arguments
	 * utilisés dans la ligne de commande
	 * @param args les arguments fournis au programme principal.
	 */
	@Override
	protected void setAttributes(String[] args)
	{
		/*
		 * parsing des arguments communs aux clients et serveur
		 * -v | --verbose
		 * -p | --port : port à utiliser pour la serverSocket
		 */
		super.setAttributes(args);

		/*
		 * On met d'abord les attributs locaux à leur valeur par défaut
		 */
		host = null;
		name = null;
		gui = false;

		/*
		 * parsing des arguments spécifique au client
		 * -h | --host : nom ou adresse IP du serveur
		 * -n | --name : nom d'utilisateur
		 * -g | --gui : pour lancer le client GUI
		 */
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("--host") || args[i].equals("-h"))
			{
				if (i < (args.length - 1))
				{
					// parse next arg for in port value
					host = args[++i];
					logger.fine("Setting host to " + host);
				}
				else
				{
					logger.warning("Setting host to: nothing, invalid value");
				}
			}
			else if (args[i].equals("--name") || args[i].equals("-n"))
			{
				if (i < (args.length - 1))
				{
					// parse next arg for in port value
					name = args[++i];
					logger.fine("Setting user name to: " + name);
				}
				else
				{
					logger.warning("Setting user name to: nothing, invalid value");
				}
			}
			if (args[i].equals("--gui") || args[i].equals("-g"))
			{
				gui = true;
				if (i < (args.length - 1))
				{
					// parse next arg for gui version
					try
					{
						guiVersion = Integer.parseInt(args[++i]);
						if (guiVersion < 1)
						{
							guiVersion = 1;
						}
						else if (guiVersion > 2)
						{
							guiVersion = 2;
						}
					}
					catch (NumberFormatException nfe)
					{
						logger.warning("Invalid gui number, revert to 1");
						guiVersion = 1;
					}
					logger.fine("Setting gui to " + guiVersion);
				}
				else
				{
					logger.warning("ReSetting gui version to 1, invalid value");
					guiVersion = 1;
				}
			}
		}

		if (host == null) // on va chercher local host
		{
			try
			{
				host = InetAddress.getLocalHost().getHostName();
			}
			catch (UnknownHostException e)
			{
				logger.severe(Failure.NO_LOCAL_HOST.toString());
				logger.severe(e.getLocalizedMessage());
				System.exit(Failure.NO_LOCAL_HOST.toInteger());
			}
		}

		if (name == null) // on va chercher le nom de l'utilisateur
		{
			try
			{
				// Try LOGNAME on unix type systems
				name = System.getenv("LOGNAME");
			}
			catch (NullPointerException npe)
			{
				logger.warning("no LOGNAME found, trying USERNAME");
				try
				{
					// Try USERNAME on other systems
					name = System.getenv("USERNAME");
				}
				catch (NullPointerException npe2)
				{
					logger.severe(Failure.NO_USER_NAME + " abort");
					System.exit(Failure.NO_USER_NAME.toInteger());
				}
			}
			catch (SecurityException se)
			{
				logger.severe(Failure.NO_ENV_ACCESS + " !");
				System.exit(Failure.NO_ENV_ACCESS.toInteger());
			}
		}
	}

	/**
	 * Lancement du ChatClient
	 */
	@Override
	protected void launch()
	{
		/*
		 * Create and Launch client
		 */
		logger.info("Creating client to " + host + " at port " + port
				+ " with verbose " + (verbose ? "on" : "off ... "));

		Boolean commonRun;

		if (gui)
		{
			if (System.getProperty("os.name").startsWith("Mac OS"))
			{
				// Met en place le menu en haut de l'écran plutôt que dans l'application
				System.setProperty("apple.laf.useScreenMenuBar", "true");
		        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Name");
			}

			/*
			 * On a besoin d'un commonRun entre la frame et les ServerHandler
			 * et UserHandler du client créé plus bas.
			 */
			commonRun = Boolean.TRUE;

			/*
			 * Création de la fenêtre de chat
			 * TODO à customizer lorsrque vous aurez créé la classe
			 * ClientFrame2
			 */
			final AbstractClientFrame frame = new ClientFrame(name, host, commonRun, logger);

			/*
			 * TODO Création du flux de sortie vers le GUI : userOut à partir du
			 * flux d'entrée de la frame (ClientFrame#getInPipe())
			 * 	- Creation d'un PipedOutputStream à connecter sur
			 * 	- le PipedInputStream de la frame
			 */
			try
			{
				// userOut = TODO Complete ...
				throw new IOException(); // TODO Remove when done
			}
			catch (IOException e)
			{
				logger.severe(Failure.USER_OUTPUT_STREAM
						+ " unable to get piped out stream");
				logger.severe(e.getLocalizedMessage());
				System.exit(Failure.USER_OUTPUT_STREAM.toInteger());
			}

			/*
			 * TODO Création du flux d'entrée depuis le GUI : userIn à partir du
			 * flux de sortie de la frame (ClientFrame#getOutPipe())
			 * 	- Création d'un PipedInputStream à connecter sur
			 * 	- le PipedOutputStream de la frame
			 */
			try
			{
				// userIn = TODO Complete ...
				throw new IOException(); // TODO Remove when done
			}
			catch (IOException e)
			{
				logger.severe(Failure.USER_INPUT_STREAM
						+ " unable to get user piped in stream");
				logger.severe(e.getLocalizedMessage());
				System.exit(Failure.USER_INPUT_STREAM.toInteger());
			}

			/*
			 * Insertion de la frame dans la file des évènements GUI
			 * grâce à un Runnable anonyme
			 */
			EventQueue.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						frame.pack();
						frame.setVisible(true);
					}
					catch (Exception e)
					{
						logger.severe("GUI Runnable::pack & setVisible" + e.getLocalizedMessage());
					}
				}
			});

			/*
			 * Création et lancement du thread de la frame
			 */
			Thread guiThread = new Thread(frame);
			threadPool.add(guiThread);
			guiThread.start();

		}
		else // client console
		{
			// lecture depuis la console
			userIn = System.in;
			// écriture vers la console
			userOut = System.out;
			// On a pas besoin d'un commonRun avec le client console
			commonRun = null;
		}

		/*
		 * Lancement du ChatClient
		 */
		UserOutputType outType = UserOutputType.fromInteger(guiVersion);
		ChatClient client = new ChatClient(host,		// hôte du serveur
		                                   port,		// port tcp
		                                   name,		// nom d'utilisateur
		                                   userIn,		// entrées utilisateur
		                                   userOut,		// sorties utilisateur
		                                   outType,		// Type sortie utilisateur
		                                   commonRun,	// commonRun avec le GUI
		                                   logger);		// parent logger
		if (client.isReady())
		{
			Thread clientThread = new Thread(client);
			threadPool.add(clientThread);

			clientThread.start();

			logger.fine("client launched");

			// attente de l'ensemble des threads du threadPool pour terminer
			for (Thread t : threadPool)
			{
				try
				{
					t.join();
					logger.fine("client thread end");
				}
				catch (InterruptedException e)
				{
					logger.severe("join interrupted" + e.getLocalizedMessage());
				}
			}
		}
		else
		{
			logger.severe(Failure.CLIENT_NOT_READY + " abort ...");
			System.exit(Failure.CLIENT_NOT_READY.toInteger());
		}
	}

	/**
	 * Programme principal de lancement d'un client de chat
	 * @param args argument du programme
	 * <ul>
	 * <li>--host <host address> : set host to connect to</li>
	 * <li>--port <port number> : set host connection port</li>
	 * <li>--name <user name> : user name to use to connect</li>
	 * <li>--verbose : set verbose on</li>
	 * <li>--gui <1 or 2>: use graphical interface rather than console interface
	 * </li>
	 * </ul>
	 */
	public static void main(String[] args)
	{

		RunChatClient client = new RunChatClient(args);

		client.launch();
	}
}
