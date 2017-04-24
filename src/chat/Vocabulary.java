package chat;
/**
 * Interface contenant le vocabulaire spécial utilisé dans le serveur de chat
 * @author davidroussel
 */
public interface Vocabulary
{
	/**
	 * Mot clé utilisé par un client pour se déloguer du serveur
	 */
	public final static String byeCmd="bye";

	/**
	 * Mot clé utilisé par un super user pour terminer le serveur
	 */
	public final static String killCmd="kill";

	/**
	 * Mot clé spécial utilisé par un super user pour déloguer de force un
	 * client : kick <username>
	 */
	public final static String kickCmd="kick";

	/**
	 * Sauts de ligne du système d'exploitation (utilisé dans le texte)
	 */
	public final static String newLine = System.getProperty("line.separator");

	/**
	 * Un tableau contenant l'ensemble des commandes du serveur afin de pouvoir
	 * le parcourir
	 */
	public final static String[] commands = {byeCmd, kickCmd, killCmd};

}
