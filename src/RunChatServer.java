import java.io.IOException;
import java.net.SocketException;

import chat.Failure;
import chat.server.ChatServer;

/**
 * Classe/programme qui lance un serveur de chat
 * @author davidroussel
 */
public class RunChatServer extends AbstractRunChat
{
	/**
	 * Time out de la server socket avant qu'elle ne recommence à attendre
	 * des connections des éventuels clients
	 */
	private int timeout;

	/**
	 * Flag permettant (ou pas) de quitter le serveur lorsque le dernier
	 * client se délogue
	 */
	private boolean quitOnLastclient;

	/**
	 * Default time out to wait for client connection : 5 seconds
	 */
	public static final int DEFAULTTIMEOUT = 5000;

	/**
	 * Constructeur d'un lanceur de serveur d'après les arguments du programme
	 * principal
	 * @param args les arguments du programme principal
	 */
	protected RunChatServer(String[] args)
	{
		super(args);
	}

	/**
	 * Mise en place des attributs du serveur de chat en fonction des arguments
	 * utilisés dans la ligne de commande
	 * @param args les arguments fournis au programme principal.
	 */
	@Override
	protected void setAttributes(String[] args)
	{
		/*
		 * On met d'abord les attributs locaux à leur valeur par défaut
		 */
		timeout = DEFAULTTIMEOUT;
		quitOnLastclient = true;

		/*
		 * parsing des arguments communs aux clients et serveur
		 * 	-v | --verbose
		 * 	-p | --port : port à utiliser pour la serverSocket
		 */
		super.setAttributes(args);

		/*
		 * parsing des arguments spécifique au  serveur
		 * 	-t | --timeout : timeout d'attente de la server socket
		 */
		for (int i=0; i < args.length; i++)
		{
			if (args[i].equals("--timeout") || args[i].equals("-t"))
			{
				if (i < (args.length - 1))
				{
					// parse next arg for in port value
					Integer timeInteger = readInt(args[++i]);
					if (timeInteger != null)
					{
						timeout = timeInteger.intValue();
					}
					logger.info("Setting timeout to " + timeout);
				}
				else
				{
					logger.warning("invalid timeout value");
				}
			}
			if (args[i].equals("--quit") || args[i].equals("-q"))
			{
				quitOnLastclient = true;
				logger.info("Setting quit on last client to true");
			}
			if (args[i].equals("--noquit") || args[i].equals("-n"))
			{
				quitOnLastclient = false;
				logger.info("Setting quit on last client to false");
			}
		}
	}

	/**
	 * Lancement du serveur de chat
	 */
	@Override
	protected void launch()
	{
		/*
		 * Create and Launch server on local ip adress with port number and verbose
		 * status
		 */
		logger.info("Creating server on port " + port + " with timeout "
				+ timeout + " ms and verbose " + (verbose ? "on" : "off"));

		ChatServer server = null;
		try
		{
			server = new ChatServer(port, timeout, quitOnLastclient, logger);
		}
		catch (SocketException se)
		{
			logger.severe(Failure.SET_SERVER_SOCKET_TIMEOUT + ", abort ...");
			logger.severe(se.getLocalizedMessage());
			System.exit(Failure.SET_SERVER_SOCKET_TIMEOUT.toInteger());
		}
		catch (IOException e)
		{
			logger.severe(Failure.CREATE_SERVER_SOCKET + ", abort ...");
			e.printStackTrace();
			System.exit(Failure.CREATE_SERVER_SOCKET.toInteger());
		}

		// Wait for serverThread to stop
		Thread serverThread = null;
		if (server != null)
		{
			serverThread = new Thread(server);
			serverThread.start();

			logger.info("Waiting for server to terminate ... ");
			try
			{
				serverThread.join();
				logger.fine("Server terminated, program end.");
			}
			catch (InterruptedException e)
			{
				logger.severe("Server Thread Join interrupted");
				logger.severe(e.getLocalizedMessage());
			}
		}
	}

	/**
	 * Programme principal
	 * @param args les arguments
	 * <ul>
	 * 	<li>--port <port number> : set host connection port</li>
	 * 	<li>--verbose : set verbose on</li>
	 * 	<li>--timeout <timeout in ms> : server socket waiting time out</li>
	 * </ul>
	 */
	public static void main(String[] args)
	{
		RunChatServer server = new RunChatServer(args);

		server.launch();
	}
}
