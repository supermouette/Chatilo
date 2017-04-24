package examples;
import java.awt.EventQueue;

import javax.swing.JFrame;

import examples.widgets.ExampleFrame;
import examples.widgets.ListExampleFrame;


/**
 * Programme principal lançant une {@link ExampleFrame}
 * @author davidroussel
 *
 */
public class RunListFrame
{
	/**
	 * Programme principal
	 * @param args
	 */
	public static void main(String[] args)
	{
		if (System.getProperty("os.name").startsWith("Mac OS"))
		{
			// Met en place le menu en haut de l'écran plutôt que dans l'application
			System.setProperty("apple.laf.useScreenMenuBar", "true");
	        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Name");
		}

		// Insertion de la frame dans la file des évènements GUI
		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					JFrame frame = new ListExampleFrame();
					frame.pack();
					frame.setVisible(true);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}
}
