package examples;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.Predicate;

import models.Message;
import models.Message.MessageOrder;

/**
 * Test du flux trié et filtré des messages
 * @author davidroussel
 */
public class TestMessageStream
{
	private static void randomWait(int max)
	{
		Random rand = new Random(Calendar.getInstance().getTimeInMillis());
		try
		{
			Thread.sleep(rand.nextInt(max));
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Programme principal
	 * @param args arguments [non utilisé]
	 */
	public static void main(String[] args)
	{
		Vector<Message> messages = new Vector<Message>();
		int delay = 5000;

		Date date = Calendar.getInstance().getTime();
		messages.add(new Message("Message de T", "Ténéphore"));
		randomWait(delay);
		messages.add(new Message("Hello", "Zébulon"));
		randomWait(delay);
		messages.add(new Message("ZBulon's in the place", "Zébulon"));
		randomWait(delay);
		messages.add(new Message(date, "ZBulon antidaté", "Zébulon"));
		randomWait(delay);
		messages.add(new Message("Message de contrôle")); // sans auteur

		Consumer<Message> messagePrinter = (Message m) -> System.out.println(m);

		// Flux ordinaire des messages
		System.out.println("Flux entier des messages non triés : ");
		messages.stream().forEach(messagePrinter);

		// Flux entier des messsages triés par date
		System.out.println("Flux entier des messages triés par date : ");
		messages.stream().sorted().forEach(messagePrinter);

		Message.removeOrder(MessageOrder.DATE);
		Message.addOrder(MessageOrder.AUTHOR);

		System.out.println("Flux entier des messages triés par auteur : ");
		messages.stream().sorted().forEach(messagePrinter);

		Message.addOrder(MessageOrder.CONTENT);
		System.out.println("Flux entier des messages triés par auteur et par contenu: ");
		messages.stream().sorted().forEach(messagePrinter);

		Message.addOrder(MessageOrder.DATE);
		System.out.println("Flux entier des messages triés par auteur et par contenu et par date: ");
		messages.stream().sorted().forEach(messagePrinter);

		Predicate<Message> zebulonFilter = (Message m) ->
		{
			if (m != null)
			{
				if (m.hasAuthor())
				{
					if (m.getAuthor().equals("Zébulon"))
					{
						return true;
					}
				}
			}
			return false;
		};

		// Flux filtré (pour Zébulon) des messages triés
		System.out.println("Flux filtré (Zébulon) des messages triés par auteur et par contenu : ");
		messages.stream().sorted().filter(zebulonFilter).forEach(messagePrinter);
		Message.removeOrder(MessageOrder.CONTENT);
		Message.removeOrder(MessageOrder.AUTHOR);
		Message.clearOrders();
		Message.addOrder(MessageOrder.DATE);

		System.out.println("Flux filtré (Zébulon) des messages re-triés par date: ");
		messages.stream().filter(zebulonFilter).sorted().forEach(messagePrinter);
	}

}
