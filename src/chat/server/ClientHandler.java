package chat.server;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.util.Vector;
import java.util.logging.Logger;

import chat.Vocabulary;
import logger.LoggerFactory;
import models.Message;

/**
 * Classe utilisée pour traiter chacune des connections des clients dans un
 * nouveau thread
 *
 * @author davidroussel
 */
public class ClientHandler implements Runnable
{
	/**
	 * le ChatServer qui a lancé ce thread
	 *
	 * @uml.property name="parent"
	 * @uml.associationEnd aggregation="shared"
	 */
	private ChatServer parent;

	/**
	 * Le client principal de ce handler
	 *
	 * @uml.property name="mainClient"
	 * @uml.associationEnd aggregation="shared"
	 */
	private InputClient mainClient;

	/**
	 * Les autres clients reliés au serveur.
	 *
	 * @uml.property name="allClients"
	 * @uml.associationEnd multiplicity="(1 -1)" ordering="true"
	 *                     aggregation="shared"
	 *                     inverse="clientHandler:chat.server.InputOutputClient"
	 */
	private Vector<InputOutputClient> allClients;

	/**
	 * Compteur d'instances du nombre de threads créés pour traiter les
	 * connections
	 *
	 * @uml.property name="nbThreads"
	 */
	private static int nbThreads = 0;

	/**
	 * Logger pour l'affichage des messages de debug
	 */
	private Logger logger;

	/**
	 * Constructeur d'un handler de client
	 *
	 * @param parent le {@link ChatServer} qui a lancé ce Runnable
	 * @param mainClient le client principal qu'il faut écouter
	 * @param allClients les autres clients à qui il faut redistribuer ce
	 *            qu'envoie le client principal
	 */
	public ClientHandler(ChatServer parent,
	                     InputClient mainClient,
	                     Vector<InputOutputClient> allClients,
	                     Logger parentLogger)
	{
		this.parent = parent;
		this.mainClient = mainClient;
		this.allClients = allClients;
		nbThreads++;
		logger = LoggerFactory.getParentLogger(getClass(),
		                                       parentLogger,
		                                       parentLogger.getLevel());
	}

	/**
	 * Accesseur en lecture du nombre de ClientHandler en activité
	 *
	 * @return the nbThreads
	 * @uml.property name="nbThreads"
	 */
	public static int getNbThreads()
	{
		return nbThreads;
	}

	/**
	 * Exécution d'un handler de client. Consiste à lire une ligne du client
	 * jusqu'à ce que l'on reçoive la commande bye, ou qu'une IOException
	 * intervienne si le flux est coupé
	 *
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		boolean loggedOut = false;
		boolean killed = false;
		String clientInput = null;


		try
		{
			/*
			 * Attente d'une ligne de texte de la part d'un client (appel
			 * bloquant)
			 */
			while (!loggedOut && !killed &&
			       ((clientInput = mainClient.getIn().readLine()) != null))
			{
				// Affiche ce qui est reçu par le serveur dans la console
				System.out.println(mainClient.getName() + " > " + clientInput);

				// on vérifie que ce client n'a pas été banni par un super utilisateur
				if (mainClient.isBanned())
				{
					logger.info(mainClient.getName() + " is banned");
					loggedOut = true;
					break;
				}

				// On vérifie qu'il ne s'agit pas d'un message de contrôle (kick ou bye)
				boolean controlMessage = false;
				for (String command : Vocabulary.commands)
				{
					if (clientInput.toLowerCase().startsWith(command))
					{
						controlMessage = true;
						break;
					}
				}

				StringBuffer messageContent = new StringBuffer();

				if (controlMessage)
				{
					// Le client veut nous quitter
					if (clientInput.toLowerCase().equals(Vocabulary.byeCmd))
					{
						messageContent.append(mainClient.getName() +
						                      " logged out");
						loggedOut = true;
					}
					// on vérifie si un kill est demandé par le client
					else if (clientInput.toLowerCase().startsWith(Vocabulary.killCmd))
					{
						// on vérifie que le client est super-utilisateur
						// (1er de tous les clients)
						if (allClients.get(0) == mainClient)
						{
							killed = true;
							parent.setListening(false);
							break;
						}
					}
					// on vérifie si un kick est demandé par le client
					else if (clientInput.toLowerCase().startsWith(Vocabulary.kickCmd))
					{
						messageContent.append(Vocabulary.kickCmd);
						// On bloque l'accès à allClients tant que l'on traite
						// la commande du mainClient
						synchronized (allClients)
						{
							// on vérifie que le client est super-utilisateur
							// (1er de tous les clients)
							if (allClients.get(0) == mainClient)
							{
								// on recherche le nom du client à kicker
								String kickedName = null;
								try
								{
									/*
									 * On recherche le nom du client à kicker
									 * dans kick clientToKill
									 */
									kickedName = clientInput.substring(
										Vocabulary.kickCmd.length() + 1);
								}
								catch (IndexOutOfBoundsException iob)
								{
									logger.warning("ClientHandler: Error retreiving client name to kick");
								}
								if (kickedName != null)
								{
									messageContent.append(" " + kickedName);
									InputOutputClient kickedClient =
										parent.searchClientByName(kickedName);
									if (kickedClient != null)
									{
										kickedClient.setBanned(true);
										logger.info("Clienthandler["
											+ mainClient.getName() + "] client "
											+ kickedName + " banned");
										messageContent.append(" [request granted by server]");
									}
									else
									{
										messageContent.append(" [client "
											+ kickedName + " does not exist]");
									}
								}
								else
								{
									messageContent.append(" [no client name to kick]");
								}
							}
							else
							{
								int cmdL = Vocabulary.kickCmd.length();
								messageContent.append(clientInput.substring(cmdL, (clientInput.length())));
								messageContent.append(" [request denied by server]");
							}
							messageContent.append(" by " + mainClient.getName());
						}
					}
				}
				else
				{
					// Il s'agit d'un message ordinaire
					messageContent.append(clientInput);
				}

				/*
				 * Création du message à diffuser
				 */
				Message message = null;
				if (controlMessage)
				{
					message = new Message(messageContent.toString());
				}
				else
				{
					message = new Message(messageContent.toString(),
					                      mainClient.getName());
				}

				/*
				 * Diffusion du message à tous les clients.
				 * allClients est un Vector qui est atomique donc a
				 * priori on a pas besoin du "synchronized (allClients)",
				 * Néanmoins ce synchronized permet de bloquer l'accès à
				 * l'ensemble des autres clients quand on diffuse le message de
				 * notre mainClient à tous les clients. Sans quoi on pourrait
				 * diffuser le message à un client, puis se faire interrompre
				 * par un autre client, puis diffuser le message à un autre
				 * client, etc. A vérifier ...
				 */
				synchronized (allClients)
				{
					for (InputOutputClient c : allClients)
					{
						if (c.isReady())
						{
							// récupération du flux de sortie et envoi du message
							ObjectOutputStream out = c.getOut();
							out.writeObject(message);
						}
						else
						{
							logger.warning("ClientHandler["
									+ mainClient.getName() + "]Client "
									+ c.getName() + " not ready");
						}
					}
				}
			}
		}
		catch (InvalidClassException ice)
		{
			logger.severe("ClientHandler["
				+ mainClient.getName() + "]: write to client invalid class " +
				ice.getLocalizedMessage());
		}
		catch (NotSerializableException nse)
		{
			logger.severe(
				"ClientHandler[" + mainClient.getName()
					+ "]: write to not serializable exception "
					+ nse.getLocalizedMessage());
		}
		catch (IOException e)
		{
			logger.severe("ClientHandler[" + mainClient.getName()
					+ "]: received or write failed, Closing client " + this);
		}

		// remove current client from allClients (should be atomic)
		synchronized (allClients)
		{
			allClients.remove(mainClient);
		}
		// cleanup current client
		mainClient.cleanup();
		synchronized (parent)
		{
			// décrémentation du nombre de threads des clients
			nbThreads--;
			// Nettoyage du ChatServer parent (qui pourra evt s'arrêter s'il n'y a
			// plus de clients)
			parent.cleanup();
		}
	}

}
