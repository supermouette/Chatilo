package widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import logger.LoggerFactory;

public abstract class AbstractClientFrame extends JFrame implements Runnable
{
	/**
	 * Etat d'exécution du run pour écouter les messages en provenance du
	 * serveur
	 */
	protected Boolean commonRun;

	/**
	 * Flux d'entrée pour lire les messages du serveur
	 */
	protected final PipedInputStream inPipe;

	/**
	 * Ecrivain vers le flux de sortie Ecrit le contenu du {@link #txtFieldSend}
	 * dans le {@link #outPipe}
	 */
	protected final PrintWriter outPW;

	/**
	 * Flux de sortie pour envoyer le contenu du message
	 */
	protected final PipedOutputStream outPipe;

	/**
	 * Logger pour afficher les messages ou les rediriger dans un fichier de log
	 */
	protected Logger logger;

	/**
	 * Le document sous-jacent d'un {@link JTextPane} dans lequel on écrira
	 * les messages
	 */
	protected StyledDocument document;

	/**
	 * Le style du document {@link #document}
	 */
	protected Style documentStyle;

	/**
	 * La couleur par défaut du texte {@link #documentStyle}
	 */
	protected Color defaultColor;

	/**
	 * Map associant une couleur à un nom afin que l'on n'ai pas à générer
	 * une couleur à chaque fois que l'on a besoin d'une couleur pour un nom.
	 * Cette map est mise à jour dans {@link #getColorFromName(String)}
	 */
	protected Map<String, Color> colorMap;

	/**
	 * Constructeur [protégé] de la fenêtre de chat abstraite
	 * @param name le nom de l'utilisateur
	 * @param host l'hôte sur lequel on est connecté
	 * @param commonRun état d'exécution des autres threads du client
	 * @param parentLogger le logger parent pour les messages
	 * @throws HeadlessException
	 */
	protected AbstractClientFrame(String name,
	                              String host,
	                              Boolean commonRun,
	                              Logger parentLogger)
		throws HeadlessException
	{
		// --------------------------------------------------------------------
		// Logger
		//---------------------------------------------------------------------
		logger = LoggerFactory.getParentLogger(getClass(),
		                                       parentLogger,
		                                       (parentLogger == null ?
		                                    	Level.WARNING :
		                                        parentLogger.getLevel()));

		// --------------------------------------------------------------------
		// Common run avec d'autres threads
		//---------------------------------------------------------------------
		if (commonRun != null)
		{
			this.commonRun = commonRun;
		}
		else
		{
			this.commonRun = Boolean.TRUE;
		}

		// --------------------------------------------------------------------
		// Flux d'IO
		//---------------------------------------------------------------------
		inPipe = new PipedInputStream();
		logger.info("AbstractClientFrame : PipedInputStream Created");

		outPipe = new PipedOutputStream();
		logger.info("AbstractClientFrame : PipedOutputStream Created");
		outPW = new PrintWriter(outPipe, true);
		if (outPW.checkError())
		{
			logger.warning("ClientFrame: Output PrintWriter has errors");
		}
		else
		{
			logger.info("AbstractClientFrame : Printwriter to PipedOutputStream Created");
		}

		// --------------------------------------------------------------------
		// Window setup
		//---------------------------------------------------------------------
		if (name != null)
		{
			setTitle(name);
		}

		setPreferredSize(new Dimension(400, 200));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		document = null;
		documentStyle = null;
		defaultColor = Color.BLACK;
		colorMap = new TreeMap<String, Color>();
	}

	/**
	 * Envoi d'un message. Envoi d'un message dans le {@link #outPipe} (si celui
	 * ci est non null) en utilisant le {@link #outPW}
	 * @param le message à envoyer
	 */
	protected void sendMessage(String message)
	{
		logger.info("ClientFrame::sendMessage writing out: "
		    + (message == null ? "NULL" : message));
		/*
		 * DONE envoi du message dans le outPW et vérification du statut
		 * d'erreur du #outPW (si c'est le cas on ajoute un warning au logger).
		 */
		if (message != null)
		{
			outPW.println(message);
			if (outPW.checkError())
			{
				logger.warning("ClientFrame::sendMessage: error writing");
			}
		}
	}

	/**
	 * Couleur d'un texte d'après le contenu du texte.
	 * @param name le texte
	 * @return un couleur aléatoire initialisée avec le hashCode du texte ou
	 * bien null si name est vide ou null
	 */
	protected Color getColorFromName(String name)
	{
		/*
		 * DONE renvoyer une couleur (pas trop claire) d'après le nom
		 * fourni en argument. Calcule une couleur en utilisant le hashCode du
		 * texte pour initialiser un Random, le nextInt de ce Random nous
		 * fournira alors un entier utilisé pour créer une Color. On pourra
		 * éventuellement utiliser la méthode darker() sur cette couleur pour
		 * éviter les couleurs trop claires qui se voient mal sur fond blanc.
		 */
		if (name != null)
		{
			if (name.length() > 0)
			{
				if (!colorMap.containsKey(name))
				{
					Random rand = new Random(name.hashCode());
					colorMap.put(name, new Color(rand.nextInt()).darker());
					// colorMap.put(name, name.hashCode()).darker();
					logger.info("Adding \"" + name + "\" to colorMap");
				}

				return colorMap.get(name);
			}
		}

		return null;
	}

	/**
	 * Accesseur en lecture de l' {@link #inPipe} pour y connecter un
	 * {@link PipedOutputStream}
	 * @return l'inPipe sur lequel on lit
	 */
	public PipedInputStream getInPipe()
	{
		return inPipe;
	}

	/**
	 * Accesseur en lecture de l' {@link #outPipe} pour y connecter un
	 * {@link PipedInputStream}
	 * @return l'outPipe sur lequel on écrit
	 */
	public PipedOutputStream getOutPipe()
	{
		return outPipe;
	}

	/**
	 * Fermeture de la fenètre et des flux à la fin de l'exécution
	 */
	public void cleanup()
	{
		logger.info("ClientFrame::cleanup: closing window ... ");
		dispose();

		logger.info("ClientFrame::cleanup: closing output print writer ... ");
		outPW.close();

		logger.info("ClientFrame::cleanup: closing output stream ... ");
		try
		{
			outPipe.close();
		}
		catch (IOException e)
		{
			logger.warning("ClientFrame::cleanup: failed to close output stream"
				+ e.getLocalizedMessage());
		}

		logger.info("ClientFrame::cleanup: closing input stream ... ");
		try
		{
			inPipe.close();
		}
		catch (IOException e)
		{
			logger.warning("ClientFrame::cleanup: failed to close input stream"
				+ e.getLocalizedMessage());
		}
	}
}
