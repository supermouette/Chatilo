package logger;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Logger Factory
 * @author davidroussel
 */
public class LoggerFactory
{
	/**
	 * Factory simple pour un logger de console
	 * @param client la classe cliente du logger. utilisée pour donner un nom au
	 * logger
	 * @param le niveau de log
	 * @return un logger simple utilisant la console
	 * @throws IOException
	 */
	public static <E> Logger getConsoleLogger(Class<E> client, Level level)
	{
		Logger logger = null;
		try
		{
			logger = getLogger(client, true, null, false, null, level);

		}
		catch (IOException e)
		{
			System.err.println("getConsoleLogger: impossible file IO error");
			e.printStackTrace();
			System.exit(e.hashCode());
		}

		return logger;
	}

	/**
	 * Factory pour obtenir un logger ayant un parent spécifique
	 * @param client la classe cliente du logger. utilisée pour donner un nom au
	 * logger
	 * @param parentLogger le logger parent
	 * @param level le niveau de log
	 * @return un logger ayant pour parent le parentLogger
	 */
	public static <E> Logger getParentLogger(Class<E> client,
	                                         Logger parentLogger,
	                                         Level level)
	{
		Logger logger = null;
		try
		{
			logger = getLogger(client, true, null, false, parentLogger, level);
		}
		catch (IOException e)
		{
			System.err.println("getParentLogger: impossible file IO error");
			e.printStackTrace();
			System.exit(e.hashCode());
		}

		return logger;
	}

	/**
	 * Factory pour obtenir un logger dans un fichier de log
	 * @param client la classe cliente du logger. utilisée pour donner un nom au
	 * logger
	 * @param fileName nom du fichier de log
	 * @param xmlFormat formattage du fichier de log en XML
	 * @param level le niveau de log
	 * @return un nouveau logger vers un fichier de log
	 * @throws IOException si l'on arrive pas à ouvrir le fichier de log
	 */
	public static <E> Logger getFileLogger(Class<E> client,
	                                       String fileName,
	                                       boolean xmlFormat,
	                                       Level level)
	    throws IOException
	{
		return getLogger(client, false, fileName, xmlFormat, null, level);
	}

	/**
	 * Factory générale pour obtenir un logger
	 * @param client la classe cliente du logger. utilisée pour donner un nom au
	 * logger
	 * @param verbose affichage des logs dans la console
	 * @param logFileName fichier de log (pas de fichier de log si null)
	 * @param xmlFormat formattage du fichier de log en XML
	 * @param parentLogger parent logger. Si le parent logger est non null
	 * l'argument verbose n'est pas pris en compte
	 * @param level le niveau de log
	 * @return un nouveau logger si les paramètres le permettent ou bien null si
	 * ce n'est pas le cas
	 * @throws IOException si l'on arrive pas à ouvrir le fichier de log
	 */
	public static <E> Logger getLogger(Class<E> client,
	                                   boolean verbose,
	                                   String logFileName,
	                                   boolean xmlFormat,
	                                   Logger parentLogger,
	                                   Level level)
	    throws IOException
	{
		Logger logger = null;

		if (verbose || (logFileName != null) || (parentLogger != null))
		{
			if (client != null)
			{
				String canonicalName = client.getCanonicalName();
				logger = Logger.getLogger(canonicalName);

				if (parentLogger != null)
				{
					logger.setParent(parentLogger);
				}
				else
				{
					if (!verbose)
					{
						/*
						 * On ne veut pas que les messages de log aillent dans
						 * la console.
						 */
						logger.setUseParentHandlers(false);
					}
				}

				if (logFileName != null)
				{
					String filename = logFileName;
					if (xmlFormat)
					{
						if (!logFileName.contains(new String("xml")))
						{
							filename = logFileName + ".xml";
						}
					}

					// Ajout d'un fileHandler au logger
					try
					{
						Handler handler = new FileHandler(filename);
						if (!xmlFormat)
						{
							// par défaut le formattage fichier sera en XML
							// il faut donc remettre en place un formatteur
							// simple
							handler.setFormatter(new SimpleFormatter());
						}

						// Ajout de ce filehandler au logger
						logger.addHandler(handler);
						logger.info("log file created");
					}
					catch (IllegalArgumentException e)
					{
						String message = "Empty log file name";
						logger.severe(message);
						logger.severe(e.getLocalizedMessage());
						throw e;
					}
					catch (SecurityException e)
					{
						String message =
						    "Do not have privileges to open log file "
						        + logFileName;
						logger.warning(message);
						logger.warning(e.getLocalizedMessage());
					}
					catch (IOException e)
					{
						String message = "Error opening file " + logFileName;
						logger.severe(message);
						logger.severe(e.getLocalizedMessage());
						throw e;
					}
				}
			}
			else
			{
				if (parentLogger != null)
				{
					logger = parentLogger;
				}
			}
		}

		if (logger != null)
		{
			logger.info("Logger ready");
			logger.setLevel(level);
		}

		return logger;
	}
}
