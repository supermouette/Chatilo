package chat.server;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Logger;

import chat.Failure;


/**
 * Classe stockant les caractéristiques d'un client :
 * voir {@link InputClient}.
 * Un client "normal" ajoute aussi le flux de sortie sur lequel on écrit les
 * messages vers le client
 * <ul>
 * 	<li>out : {@link ObjectOutputStream}</li>
 * </ul>
 * @author davidroussel
 *
 */
public class InputOutputClient extends InputClient
{
	/**
	 * Le flux de sortie vers le client (celui sur lequel on écrit au client)
	 */
	private ObjectOutputStream outOS;

	/**
	 * Constructeur d'un client
	 * @param socket la socket du client
	 * @param name le nom du client
	 * @param verbose niveau de debug pour les messages
	 * @param parentLogger logger parent pour l'affichage des messages
	 */
	public InputOutputClient(Socket socket, String name, Logger parentLogger)
	{
		super(socket, name, parentLogger);
		if (ready)
		{
			outOS = null;
			ready = false;

			if (clientSocket != null)
			{
				logger.info("Client: Creating Output Stream ... ");
				try
				{
					outOS = new ObjectOutputStream(clientSocket.getOutputStream());
					ready = true;
				}
				catch (IOException e)
				{
					logger.severe("Client: unable to get client output stream");
					logger.severe(e.getLocalizedMessage());
				}
			}
		}
		else
		{
			logger.severe("Client: " + Failure.CLIENT_NOT_READY + ", abort...");
			System.exit(Failure.CLIENT_NOT_READY.toInteger());
		}
	}

	/**
	 * Accesseur en lecture du flux de sortie d'un client
	 * @return the out
	 */
	public ObjectOutputStream getOut()
	{
		return outOS;
	}

	/**
	 * Nettoyage d'un client : fermeture du flux de sortie et super.cleanup()
	 */
	@Override
	public void cleanup()
	{
		logger.info("Client::cleanup: closing output stream ... ");
		try
		{
			outOS.close();
		}
		catch (IOException e)
		{
			logger.severe("Client: unable to close client output stream");
			logger.severe(e.getLocalizedMessage());
		}
		super.cleanup();
	}
}
