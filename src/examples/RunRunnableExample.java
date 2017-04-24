package examples;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Exemple de classe implémentant un Runnable et lancé dans un Thread
 *
 * @author davidroussel
 */
public class RunRunnableExample
{
	/**
	 * Classe interne représentant un simple compteur à exécuter dans un thread.
	 * Le compteur compte de 0 à une valeur max. Lorsque le compteur atteint la
	 * valeur max le compteur s'arrête.
	 * @author davidroussel
	 */
	protected static class Counter implements Runnable
	{
		/**
		 * Nombre de compteurs instanciés
		 */
		private static int CounterNumber = 0;

		/**
		 * Le numéro de compteur
		 */
		private int number;
		/**
		 * Le compteur proprement dit
		 */
		private int count;

		/**
		 * La valeur max du compteur
		 */
		private int max;

		/**
		 * Constructeur valué du compteur
		 * @param max la valeur max du compteur à laquelle il s'arrête
		 */
		public Counter(int max)
		{
			number = ++CounterNumber;
			count = 0;
			this.max = max;
		}

		/**
		 * Nettoyage lors de la destruction
		 * @see java.lang.Object#finalize()
		 */
		@Override
		protected void finalize() throws Throwable
		{
			CounterNumber--;
		}

		/**
		 * Boucle d'éxécution principale du compteur : Tant que le compteur n'a
		 * pas atteint la valeur max le compteur incrémente son compteur de 1,
		 * affiche la valeur courante du compteur puis on demande au thread
		 * dans lequel il tourne de passer la main à un autre thread (en
		 * espérant que ceux ci nous repassent la main un jour afin que l'on
		 * puisse continuer à compter).
		 */
		@Override
		public void run()
		{
			while (count < max)
			{
				count++;

				System.out.println(this); // utilisation du toString

				// passe la main à d'autres threads (si besoin)
				Thread.yield();
			}
		}

		/**
		 * Représentation sous forme de chaine de caractères
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return new String("Counter #" + number + " = " + count);
		}
	}

	/**
	 * Collection de compteurs Runnable à lancer
	 */
	protected Collection<Counter> counters;

	/**
	 * Collection de threads dans lesquels on va vaire tourner les Counter.
	 */
	protected Collection<Thread> threads;

	/**
	 * Constructeur d'un RunnableExample.
	 * Crée un certain nombre de compteur (Runnable), puis crée le même nombre
	 * de threads dans lesquels on place ces compteurs
	 */
	public RunRunnableExample(int nbCounters)
	{
		counters = new ArrayList<Counter>(nbCounters);
		threads = new ArrayList<Thread>(nbCounters);

		for (int i = 0; i < nbCounters; i++)
		{
			Counter c = new Counter(10);
			counters.add(c);

			Thread t = new Thread(c);
			threads.add(t);
		}
	}

	/**
	 * Lancement de tous les threads (contenant les compteurs)
	 */
	public void launch()
	{
		for (Thread t : threads)
		{
			t.start();
		}
	}

	/**
	 * attente de la fin de tous les threads pour terminer le thread principal
	 */
	public void terminate()
	{
		for (Thread t : threads)
		{
			try
			{
				t.join();
			}
			catch (InterruptedException e)
			{
				System.err.println("Thread" + t + " join interrupted");
				e.printStackTrace();
			}
		}

		System.out.println("All threads terminated");
	}

	/**
	 * Programme principal.
	 * Lancement de plusieurs Counters
	 *
	 * @param args arguments du programme pour y lire le nombre de compteurs à
	 * lancer
	 */
	public static void main(String[] args)
	{
		int nbCounters = 3;
		// on lit le nombre de counters dans le premier argument du programme
		if (args.length > 0)
		{
			int value;
			try
			{
				value = Integer.parseInt(args[0]);
				if (value > 0)
				{
					nbCounters = value;
				}
			}
			catch (NumberFormatException nfe)
			{
				System.err.println("Error reading number of counters");
			}
		}

		RunRunnableExample runner = new RunRunnableExample(nbCounters);

		runner.launch();

		System.out.println("All threads launched");

		runner.terminate();
	}
}
