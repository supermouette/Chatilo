import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import chat.Failure;
import logger.LoggerFactory;

/**
 * Classe abstraite de base pour lancer un client ou un serveur de chat
 * @author davidroussel
 */
public abstract class AbstractRunChat
{
	/**
	 * Port à utiliser pour les connnections entre clients et serveur
	 */
	protected int port;

	/**
	 * numero de port de communication par défaut
	 */
	public static final int DEFAULTPORT = 1394;

	/**
	 * Etat de verbose. Si true les messages de debug seront
	 * affichés. Si false les messages de debug ne seront pas affichés
	 */
	protected boolean verbose;

	/**
	 * Le logger utilisé pour afficher (ou pas) les messages d'infos et
	 * d'erreurs.
	 */
	protected Logger logger;

	/**
	 * Constructeur d'un client ou d'un serveur de chat d'après les arguments
	 * fournis au programme principal
	 * @param args les arguments fournis au programme principal en vue de
	 * mettre en place certaines options particulière à un client ou un serveur
	 * Recherche des valeurs pour {@link #port} et {@link #verbose} dans les
	 * chaînes de caractères fournis en arguments
	 */
	protected AbstractRunChat(String[] args)
	{
		setAttributes(args);
	}

	/**
	 * Mise en place des valeurs des attributs et parsing des arguments
	 * @param args les arguments fournis au programme principal en vue de
	 * mettre en place certaines options particulière à un client ou un serveur
	 * Recherche des valeurs pour {@link #port} et {@link #verbose} dans les
	 * chaînes de caractères fournis en arguments
	 */
	protected void setAttributes(String[] args)
	{
		/*
		 * On met d'abord les attributs locaux à leur valeur par défaut
		 */
		port = DEFAULTPORT;
		verbose = false;

		/*
		 * parsing des arguments
		 * 	-v | --verbose : si verbose affichage des messages dans la console
		 * 		sinon affichage des messages dans un fichier de log portant
		 * 		le nom de la classe qui l'instancie.log
		 * 	-p | --port : port à utiliser pour la serverSocket
		 */
		for (int i=0; i < args.length; i++)
		{
			if (args[i].startsWith("-")) // option argument
			{
				if (args[i].equals("--verbose") || args[i].equals("-v"))
				{
					System.out.println("Setting verbose on");
					verbose = true;
				}
				if (args[i].equals("--port") || args[i].equals("-p"))
				{
					System.out.print("Setting port to: ");
					if (i < (args.length - 1))
					{
						// recherche du numéro de port dans le prochain argument
						Integer portInteger = readInt(args[++i]);
						if (portInteger != null)
						{
							int readPort = portInteger.intValue();
							if (readPort >= 1024)
							{
								port = readPort;
							}
							else
							{
								System.err.println(Failure.INVALID_PORT);
								System.exit(Failure.INVALID_PORT.toInteger());
							}
						}
						System.out.println(port);
					}
					else
					{
						System.out.println("nothing, invalid value");
					}
				}
			}
		}

		/*
		 * Création du logger
		 */
		logger = null;
		Class<?> runningClass = getClass();
		String logFilename =
		    (verbose ? null : runningClass.getSimpleName() + ".log");
		Logger parent = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		Level level = (verbose ? Level.ALL : Level.WARNING);
		try
		{
			logger = LoggerFactory.getLogger(runningClass,
			                                 verbose,
			                                 logFilename,
			                                 false,
			                                 parent,
			                                 level);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			System.exit(Failure.OTHER.toInteger());
		}
	}

	/**
	 * Une fois le client ou le serveur prêt, on lance son exécution
	 */
	protected abstract void launch();

	/**
	 * Lecture d'un entier à partir d'une chaîne de caractères
	 * @param s la chaine à lire
	 * @return l'entier parsé dans la chaine de caractère ou bien null
	 * s'il s'est produit une erreur de parsing
	 */
	protected Integer readInt(String s)
	{
		try
		{
			Integer value = new Integer(Integer.parseInt(s));
			return value;
		}
		catch (NumberFormatException e)
		{
			// System.err.println("readInt: " + s + " is not a number");
			logger.warning("readInt: " + s + " is not a number");
			return null;
		}
	}
}
