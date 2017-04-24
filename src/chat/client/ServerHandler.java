package chat.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.logging.Logger;

import chat.Failure;
import chat.UserOutputType;
import logger.LoggerFactory;
import models.Message;

/**
 * Server Handler. Classe s'occupant de lire le flux de messages en provenance
 * du serveur et de le transmettre sur le flux de sortie du client.
 * Un client peut accepter soit
 * 	- du texte uniquement (c'est le cas du client console et du 1er client GUI)
 * 	- des messages (comme ceux envoyés par le serveur) à travers un ObjectStream
 *
 * @author davidroussel
 */
class ServerHandler implements Runnable
{
	/**
	 * Flux d'entrée objet en provenance du serveur
	 */
	private ObjectInputStream serverInOS;

	/**
	 * Le type de flux à utiliser pour envoyer les message au client.
	 * Si le type de flux est {@link TEX}
	 */
	private UserOutputType userOutType;

	/**
	 * Ecrivain vers le flux de sortie texte vers l'utilisateur
	 */
	private PrintWriter userOutPW;

	/**
	 * Flux de sortie objet vers l'utilisateur
	 */
	private ObjectOutputStream userOutOS;

	/**
	 * Etat d'exécution commun du ServerHandler et du {@link UserHandler}
	 */
	private Boolean commonRun;

	/**
	 * Logger utilisé pour afficher (ou pas) les messages d'erreurs
	 */
	private Logger logger;

	/**
	 * Constructeur d'un ServerHandler
	 * @param name notre nom d'utilisateur sur le serveur
	 * @param in le flux d'entrée en provenance du serveur
	 * @param out le flux de sortie vers l'utilisateur
	 * @param commonRun l'état d'exécution commun du {@link ServerHandler} et du
	 *            {@link UserHandler}
	 * @param parentLogger logger parent pour affichage des messages de debug
	 */
	public ServerHandler(String name,
	                     InputStream in,
	                     OutputStream out,
	                     UserOutputType outType,
	                     Boolean commonRun,
	                     Logger parentLogger)
	{
		logger = LoggerFactory.getParentLogger(getClass(),
		                                       parentLogger,
		                                       parentLogger.getLevel());
		/*
		 * On vérifie que l'InputStream est non null et on crée notre serverInOS
		 * sur cet InputStream Sinon on quitte avec la valeur
		 * Failure.CLIENT_INPUT_STREAM
		 */
		if (in != null)
		{
			logger.info("ServerHandler: creating server input reader ... ");
			/*
			 * TODO Création du ObjectInputStream à partir du flux d'entrée
			 * en provenance du serveur, si une IOException survient,
			 * on quitte avec la valeur Failure.CLIENT_INPUT_STREAM
			 */
			serverInOS = null;
		}
		else
		{
			logger.severe("ServerHandler: " + Failure.CLIENT_INPUT_STREAM);
			System.exit(Failure.CLIENT_INPUT_STREAM.toInteger());
		}

		/*
		 * On vérifie que l'OutputStream est non null et on crée notre userOutPW
		 * ou bien notre userOutOS sur cet OutputStream. Sinon on quitte avec
		 * la valeur Failure.USER_OUTPUT_STREAM
		 */
		if (out != null)
		{
			logger.info("ServerHandler: creating user output ... ");
			/*
			 * TODO En fonction du outType, création d'un PrintWriter sur le
			 * flux de sortie vers l'utilisateur, ou bien d'un ObjectOutputStream
			 */
			userOutType = outType;
			switch (userOutType)
			{
				case OBJECT:
					userOutPW = null;
					// userOutOS = TODO Complete ...
					break;
				case TEXT:
				default:
					userOutOS = null;
					// userOutPW = TODO Complete ...
					break;
			}
		}
		else
		{
			logger.severe("ServerHandler: " + Failure.USER_OUTPUT_STREAM);
			System.exit(Failure.USER_OUTPUT_STREAM.toInteger());
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
	 * Exécution d'un ServerHandler. Écoute les entrées en provenance du serveur
	 * et les envoient sur la sortie vers l'utilisateur
	 *
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		/*
		 * Boucle principale de lecture des messages en provenance du serveur:
		 * tantque commonRun est vrai on lit une ligne depuis le serverInBR dans
		 * serverInput Si cette ligne est non nulle, on l'envoie dans le
		 * userOutPW Toute erreur ou exception dans cette boucle nous fait
		 * quitter cette boucle A la fin de la boucle on passe le commonRun à
		 * false de manière synchronisée (atomique) afin que le UserHandler
		 * s'arrête aussi.
		 */
		while (commonRun.booleanValue())
		{
			/*
			 * TODO lecture d'un message du serveur avec le serverInOS
			 * Si une Exception intervient
			 * 	- Ajout d'un warning au logger
			 * 	- on quitte la boucle while (commonRun...
			 */
			Message message = null;

			if ((message != null))
			{
				/*
				 * TODO Affichage du message vers l'utilisateur avec
				 * 	- le userOutPW si le client attends du texte
				 * 	- le userOutOS si le client attends des objet (des Message)
				 * vérification de l'état d'erreur du userOutPW
				 * avec ajout d'un warning au logger si c'est le cas
				 */
				boolean error = false;
				switch (userOutType)
				{
					case OBJECT:
						// TODO userOutOS...
						error = true;
						break; // Break this switch
					case TEXT:
					default:
						// TODO userOutPW...
						error = true;
						break;
				}
				if (error)
				{
					break; // break this loop
				}
			}
			else
			{
				logger.warning("ServerHandler: null input read");
				break;
			}
		}

		if (commonRun.booleanValue())
		{
			logger.info("ServerHandler: changing run state at the end ... ");

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
		logger.info("ServerHandler: closing server input stream reader ... ");
		/*
		 * fermeture du lecteur de flux d'entrée du serveur Si une
		 * IOException intervient ajout d'un severe au logger
		 */
		try
		{
			serverInOS.close();
		}
		catch (IOException e)
		{
			logger.severe("ServerHandler: closing server input stream reader failed: " +
			              e.getLocalizedMessage());
		}

		logger.info("ServerHandler: closing user output print writer ... ");

		/*
		 * fermeture des flux de sortie vers l'utilisateur (si != null)
		 * Si une exception intervient, ajout d'un severe au logger
		 */
		if (userOutPW != null)
		{
			userOutPW.close();

			if (userOutPW.checkError())
			{
				logger.severe("ServerHandler: closed user text output has errors: ");
			}
		}

		if (userOutOS != null)
		{
			try
			{
				userOutOS.close();
			}
			catch (IOException e)
			{
				logger.severe("ServerHandler: closing user object output stream failed: "
						+ e.getLocalizedMessage());
			}
		}
	}
}
