package chat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.logging.Logger;

import chat.Failure;
import logger.LoggerFactory;

/**
 * User Handler Classe s'occupant de récupérer ce que tape l'utilisateur et de
 * l'envoyer au serveur de chat
 *
 * @author davidroussel
 */
class UserHandler implements Runnable
{
	/**
	 * Lecteur du flux d'entrée depuis l'utilisateur
	 */
	private BufferedReader userInBR;

	/**
	 * Ecrivain vers le flux de sortie vers le serveur
	 */
	private PrintWriter serverOutPW;

	/**
	 * Etat d'exécution commun du UserHandler et du {@link ServerHandler}
	 */
	private Boolean commonRun;

	/**
	 * Logger utilisé pour afficher (ou pas) les messafes d'erreurs
	 */
	private Logger logger;

	/**
	 * Constructeur d'un UserHandler
	 *
	 * @param in Le flux d'entrée de l'utilisateur pour les entrées utilisateur
	 * @param out le flux de sortie vers le serveur
	 * @param commonRun l'état d'exécution commun du {@link UserHandler} et du
	 *            {@link ServerHandler}
	 * @param parentLogger le logger parent
	 */
	public UserHandler(InputStream in, OutputStream out, Boolean commonRun,
			Logger parentLogger)
	{
		logger = LoggerFactory.getParentLogger(getClass(), parentLogger,
				parentLogger.getLevel());

		/*
		 * Création du lecteur de flux d'entrée de l'utilisateur : userInBR sur
		 * l'InputStream in si celui ci est non null. Sinon on quitte avec la
		 * valeur Failure.USER_INPUT_STREAM
		 */
		if (in != null)
		{
			logger.info("UserHandler: creating user input buffered reader ... ");

			/*
			 * TODO Création du BufferedReader sur un InputStreamReader à partir
			 * du flux d'entrée en provenance de l'utilisateur
			 */
			// userInBR = TODO Complete ...
		}
		else
		{
			logger.severe("UserHandler: null input stream"
					+ Failure.USER_INPUT_STREAM);
			System.exit(Failure.USER_INPUT_STREAM.toInteger());
		}

		/*
		 * Création de l'écrivain vers le flux de sortie vers le serveur :
		 * serverOutPW sur l'OutputStream out si celui ci est non null. Sinon,
		 * on quitte avec la valeur Failure.CLIENT_OUTPUT_STREAM
		 */
		if (out != null)
		{
			logger.info("UserHandler: creating server output print writer ... ");

			/*
			 * TODO Création du PrintWriter sur le flux de sortie vers le
			 * serveur (en mode autoflush)
			 */
			// serverOutPW = TODO Complete ...
		}
		else
		{
			logger.severe("UserHandler: null output stream"
					+ Failure.CLIENT_OUTPUT_STREAM);
			System.exit(Failure.CLIENT_OUTPUT_STREAM.toInteger());
		}

		/*
		 * On vérifie que le commonRun passé en argument est non null avant de
		 * le copier dans notre commonRun. Sinon on quitte avec la valeur
		 * Failure.OTHER
		 */
		if (commonRun != null)
		{
			this.commonRun = commonRun;
		}
		else
		{
			logger.severe("ServerHandler: null common run " + Failure.OTHER);
			System.exit(Failure.OTHER.toInteger());
		}
	}

	/**
	 * Exéction d'un UserHandler. Écoute les entrées en provenance de
	 * l'utilisateur et les envoie dans le flux de sortie vers le serveur
	 *
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		String userInput = null;

		/*
		 * Boucle principale de lecture des messages en provenance de
		 * l'utilisateur. tantque commonRun est vrai on lit une ligne depuis le
		 * userInBR dans userInput Si cette ligne est non nulle, on l'envoie
		 * dans serverOutPW
		 */
		while (commonRun.booleanValue())
		{
			/*
			 * TODO Lecture d'une ligne en provenance de l'utilisateur grâce
			 * au userInBR. Si une IOException intervient - Ajout d'un
			 * severe au logger - On quitte la boucle
			 */
			// userInput = TODO Complete ...

			if (userInput != null)
			{
				/*
				 * TODO Envoi du texte au serveur grâce au serverOutPW et
				 * vérification de l'état d'erreur du serverOutPW avec ajout
				 * d'un warning au logger et break si c'est le cas.
				 */
				// TODO serverOutPW...

				/*
				 * TODO Si la commande Vocabulary.byeCmd a été tapée par
				 * l'utilisateur on quitte la boucle
				 */
			}
			else
			{
				logger.warning("UserHandler: null user input");
				break;
			}
		}

		if (commonRun.booleanValue())
		{
			logger.info("UserHandler: changing run state at the end ... ");

			synchronized (commonRun)
			{
				commonRun = Boolean.FALSE;
			}
		}
	}

	/**
	 * Fermeture des flux
	 */
	public void cleanup()
	{
		logger.info("UserHandler: closing user input stream reader ... ");
		/*
		 * fermeture du lecteur de flux d'entrée de l'utilisateur Si une
		 * IOException intervient : - Ajout d'un severe au logger
		 */
		try
		{
			userInBR.close();
		}
		catch (IOException e)
		{
			logger.severe("UserHandler: closing server input stream reader failed");
			logger.severe(e.getLocalizedMessage());
		}

		logger.info("UserHandler: closing server output print writer ... ");
		// fermeture de l'écrivain vers le flux de sortie vers le serveur
		serverOutPW.close();
	}
}
