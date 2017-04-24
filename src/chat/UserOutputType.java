package chat;

/**
 * Les différents types de de données attendues dans le flux de sortie
 * vers le client pour afficher les message en provenance du serveur.
 */
public enum UserOutputType
{
	/**
	 * Le client attends des données sous forme texte
	 */
	TEXT,
	/**
	 * Le client attends des données sous forme d'objets (en l'occurrence
	 * des Message ou des UserMessage)
	 */
	OBJECT;

	/**
	 * Affichage sous forme de texte des erreurs possibles
	 */
	@Override
	public String toString()
	{
		switch (this)
		{
			case TEXT:
				return new String("Text output type");
			case OBJECT:
				return new String("Object output type");
		}
		throw new AssertionError("UserOutputType: unknown type: " + this);
	}

	/**
	 * Conversion en entier du type sortie vers l'utilisateur
	 *
	 * @return le numéro correspondant au type de sortie vers l'utilisateur
	 * <ul>
	 * 	<li>TEXT = 1</li>
	 * 	<li>OBJECT = 2</li>
	 * </ul>
	 */
	public int toInteger()
	{
		return ordinal() + 1;
	}

	public static UserOutputType fromInteger(int value)
	{
		int controlValue;
		if (value < 1)
		{
			controlValue = 1;
		}
		else if (value > 2)
		{
			controlValue = 2;
		}
		else
		{
			controlValue = value;
		}
		switch (controlValue)
		{
			default:
			case 1:
				return TEXT;
			case 2:
				return OBJECT;
		}
	}

}
