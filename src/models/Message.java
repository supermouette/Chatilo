package models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

/**
 * Classe contenant un message envoyé par le serveur.
 * Un message d'un utilisateur est caractérisé par :
 * <ul>
 * 	<li>la date d'arrivée du message</li>
 * 	<li>le contenu du message></li>
 * 	<li>(eventuellement) un author</li>
 * </ul>
 * Les message peuvent être comparés entre eux pour obtenir l'ordre des messages
 * avec la méthode compareTo(Message m). Les critère d'ordre des messages
 * peuvent être customizés.
 * @author davidroussel
 */
public class Message implements Serializable, Comparable<Message>
{
	/**
	 * Les différents ordres de comparaison possibles pour un message
	 */
	public enum MessageOrder
	{
		/**
		 * Comparaison suivant l'ordre alphabétique de l'auteur
		 */
		AUTHOR,
		/**
		 * Comparaison suivant la date du message
		 */
		DATE,
		/**
		 * Comparaison suivant l'ordre alphabétique du contenu du message
		 */
		CONTENT;

		/**
		 * Affichage d'un critère d'ordre
		 * @return une chaine de caractère représentant un critère d'ordre
		 */
		@Override
		public String toString()
		{
			switch (this)
			{
				case AUTHOR:
					return new String("Author");
				case DATE:
					return new String("Date");
				case CONTENT:
					return new String("Content");
			}
			throw new AssertionError("MessageOrder: unknown order: " + this);
		}
	}

	/**
	 * Ensemble des critères de tri [Initialisé à vide]
	 * Les critères de tri peuvent contenir une et une seule instance
	 * des différents éléments de {@link MessageOrder} dans n'importe quel
	 * ordre.
	 */
	protected static Vector<MessageOrder> orders = new Vector<MessageOrder>();

	/**
	 * La date d'arrivée du message
	 */
	private Date date;

	/**
	 * Le contenu du message
	 */
	private String content;

	/**
	 * L'auteur du message (optionnel).
	 * Un message du serveur peut éventuellement ne pas avoir d'auteur
	 */
	private String author;

	/**
	 * Formatteur pour l'affichage de la date des messages
	 */
	protected static SimpleDateFormat dateFormat =
	    new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	/**
	 * Constructeur valué d'un message
	 * @param date la date d'arrivée du message
	 * @param content le contenu du message
	 * @param author l'auteur du message
	 */
	public Message(Date date, String content, String author)
	{
		// date ne doit pas être null
		this.date = (date != null ? date : Calendar.getInstance().getTime());
		// content ne doit pas être null
		this.content = (content != null ? content : new String());
		this.author = author;
	}

	/**
	 * Constructeur valué d'un message
	 * @param date la date d'arrivée du message
	 * @param content le contenu du message
	 */
	public Message(Date date, String content)
	{
		this(date, content, null);
	}

	/**
	 * Constructeur valué d'un message.
	 * La date d'arrivée est implicitement initialisée à "maintenant" en
	 * utilisant le calendrier
	 * @param content le contenu du message
	 * @param author l'auteur du message
	 * @see Calendar#getInstance()
	 * @see Calendar#getTime()
	 */
	public Message(String content, String author)
	{
		this(null, content, author);
	}

	/**
	 * Constructeur valué d'un message.
	 * La date d'arrivée est implicitement initialisée à "maintenant" en
	 * utilisant le calendrier
	 * @param content le contenu du message
	 * @see Calendar#getInstance()
	 * @see Calendar#getTime()
	 */
	public Message(String content)
	{
		this(content, null);
	}

	/**
	 * Accesseur en lecture de la date du message
	 * @return la date du message
	 */
	public Date getDate()
	{
		return date;
	}

	/**
	 * Accesseur en lecture de la chaîne formattée de la date du message
	 * @return la chaîne formattée de la date du message
	 */
	public String getFormattedDate()
	{
		return dateFormat.format(date);
	}

	/**
	 * Accesseur en lecture du contenu du message
	 * @return le contenu du message
	 */
	public String getContent()
	{
		return content;
	}

	/**
	 * Accesseur en lecture de l'auteur du message
	 * @return l'auteur du message ou bien null s'il s'agit d'un
	 * message direct du serveur
	 */
	public String getAuthor()
	{
		return author;
	}

	/**
	 * Indique si un message à un auteur (ce qui n'est le cas que pour les
	 * messages envoyés par les utilisateurs au serveur, les messages de
	 * contrôle diffusés par le serveur n'ont pas d'auteurs.)
	 * @return true si le message a un auteur, false autrement
	 */
	public boolean hasAuthor()
	{
		return author != null;
	}

	/**
	 * Accesseur en lecture du formatteur de date des messages
	 * @return le formateur de date des messages
	 */
	public static SimpleDateFormat getDateFormat()
	{
		return dateFormat;
	}

	/**
	 * @return le hashcode du message basé sur le hashcode de sa date, de son
	 * auteur et de son contenu (evt utilisé dans un hashset de messages)
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int hash = date.hashCode();
		hash = (prime * hash) + content.hashCode();
		if (author != null)
		{
			hash = (prime * hash) + author.hashCode();
		}
		return hash;
	}

	/**
	 * Comparaison binaire avec un autre objet
	 * @param obj l'autre objet à comparer
	 * @return true si l'autre objet est un message avec les mêmes attributs
	 * @note on peut utiliser la comparaison 3-way pour effectivement comparer
	 * deux messages;
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}

		if (obj == this)
		{
			return true;
		}

		if (obj instanceof Message)
		{
			Message m = (Message) obj;

			if (date.equals(m.date))
			{
				if (content.equals(m.content))
				{
					if (author != null)
					{
						return author.equals(m.author);
					}
					else
					{
						return m.author == null;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Affichage du message sous forme de chaîne de caractères
	 * @return une chaîne de caractère représentant le message sous la forme
	 * [yyyy/mm/dd HH:MM:SS] author > message content
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer("[");

		sb.append(dateFormat.format(date));
		sb.append("] ");
		if (author != null)
		{
			sb.append(author);
			sb.append(" > ");
		}
		sb.append(content);

		return sb.toString();
	}

	/**
	 * Affichage des critères d'ordre utilisés lors de la comparaison de
	 * messages
	 * @return une chaîne de caractères contenant les différents critères
	 * d'ordre des messages
	 */
	public static String toStringOrder()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for (Iterator<MessageOrder> it = orders.iterator(); it.hasNext(); )
		{
			sb.append(it.next().toString());
			if (it.hasNext())
			{
				sb.append(", ");
			}
		}
		sb.append("}");

		return sb.toString();
	}

	/**
	 * Comparaison (3 way : -1, 0, 1) de deux messages en utilisant les
	 * critères de comparaison mis en place dans {@link #orders}
	 * @param m l'autre message à comparer
	 * @return -1 si le message courant est considéré comme inférieur au message
	 * m suivant les critères présents dans {@link #orders}, 0 s'ils sont
	 * considérés comme égaux et 1 si le message courant est considéré comme
	 * supérieur au message m, toujours suivant les critères mis en place dans
	 * {@link #orders}.
	 */
	@Override
	public int compareTo(Message m)
	{
		int compare = 0;
		if (orders.isEmpty())
		{
			// l'ordre par défaut est la date du message
			compare = date.compareTo(m.date);
		}
		else
		{
			for (Iterator<MessageOrder> it = orders.iterator(); it.hasNext();)
			{
				MessageOrder criterium = it.next();
				switch (criterium)
				{
					case AUTHOR:
						if (author != null)
						{
							if(m.author != null)
							{
								compare = author.compareTo(m.author);
							}
							else
							{
								/*
								 * Un message avec auteur sera considéré comme
								 * supérieur à un message sans auteur
								 */
								compare = 1;
							}
						}
						else // author == null
						{
							if (m.author != null)
							{
								/*
								 * un message sans auteur sera considéré comme
								 * inférieur à un message avec auteur
								 */
								compare = -1;
							}
							else
							{
								compare = 0;
							}
						}
						break;
					case DATE:
						compare = date.compareTo(m.date);
						break;
					case CONTENT:
						compare = content.compareTo(m.content);
					default:
						break;
				}
				// Si le critère courant permet de différentier les messages
				// on renvoie sa valeur tout de suite.
				if (compare != 0)
				{
					break;
				}
			}
			// On a terminé la boucle sans avoir renvoyé une valeur != 0,
			// tous les critères de comparaison ont été 0 (valeurs égales)
		}
		return compare;
	}

	/**
	 * Ajout d'un critère de tri aux critères de tri
	 * @param o le critère à ajouter
	 * @return true si le critère de tri n'était pas déjà présent dans
	 * l'ensemble et qu'il a pu être ajouté, false sinon.
	 */
	public static boolean addOrder(MessageOrder o)
	{
		if (o != null)
		{
			if (!orders.contains(o))
			{
				return orders.add(o);
			}
		}
		return false;
	}

	/**
	 * Retrait d'un critère de tri aux critères de tri
	 * @param o le critère de tri à retirer
	 * @return true si le crière de tri était présent dans l'ensemble des
	 * critères et qu'il a été retiré, false sinon.
	 */
	public static boolean removeOrder(MessageOrder o)
	{
		if (o != null)
		{
			return orders.remove(o);
		}
		return false;
	}

	/**
	 * Effacement de l'ensemble des critères de tri
	 */
	public static void clearOrders()
	{
		orders.clear();
	}
}
