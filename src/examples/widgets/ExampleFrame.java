package examples.widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * Exemple simple de fenêtre graphique
 * @author davidroussel
 */
public class ExampleFrame extends JFrame
{
	/**
	 * Chaîne de caractère pour passer à la ligne
	 */
	protected static String newline = System.getProperty("line.separator");

	/**
	 * Bouton "Red"
	 */
	private JButton redButton;

	/**
	 * Bouton "Blue"
	 */
	private JButton blueButton;

	/**
	 * Bouton "Clear"
	 */
	private JButton clearButton;

	/**
	 * Document dans lequel écrire (à extraire du JTextPane avec
	 * {@link JTextPane.getStyledDocument()})
	 */
	protected StyledDocument document;

	/**
	 * Style à appliquer lors de l'écriture dans le document
	 */
	protected Style style;

	/**
	 * Couleur par défaut lors de l'écriture dans le document
	 */
	protected Color defaultColor;

	/**
	 * Action à réaliser lorsque l'on cliquera sur le bouton "Red" ou lorsque
	 * l'on tapera "Crtl-R" dans le JTextPane
	 */
	private final Action redAction;

	/**
	 * Action à réaliser lorsque l'on cliquera sur le bouton "Blue" ou lorsque
	 * l'on tapera "Crtl-B" dans le JTextPane
	 */
	private final Action blueAction;

	/**
	 * Action à réaliser lorsque l'on cliquera sur le bouton "Clear" ou lorsque
	 * l'on tapera "Crtl-L" dans le JTextPane
	 */
	private final Action clearAction;

	/**
	 * Création d'une fenêtre graphique simple
	 * @throws HeadlessException
	 */
	public ExampleFrame() throws HeadlessException
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Red Blue Example");
		redAction = new RedAction();
		blueAction = new BlueAction();
		clearAction = new ClearAction();

		setPreferredSize(new Dimension(400, 200));

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu menuActions = new JMenu("Actions");
		menuBar.add(menuActions);

		JMenuItem menuItemRed = new JMenuItem(redAction);
		menuActions.add(menuItemRed);

		JMenuItem menuItemBlue = new JMenuItem(blueAction);
		menuActions.add(menuItemBlue);

		JSeparator separator = new JSeparator();
		menuActions.add(separator);

		JMenuItem menuItemClear = new JMenuItem(clearAction);
		menuActions.add(menuItemClear);

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		getContentPane().add(toolBar, BorderLayout.NORTH);

		redButton = new JButton(redAction);
		toolBar.add(redButton);

		blueButton = new JButton(blueAction);
		toolBar.add(blueButton);

		Component horizontalGlue = Box.createHorizontalGlue();
		toolBar.add(horizontalGlue);

		clearButton = new JButton(clearAction);
		toolBar.add(clearButton);

		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		JTextPane textPane = new JTextPane();

		document = textPane.getStyledDocument();
		style = textPane.addStyle("New Style", null);
		defaultColor = StyleConstants.getForeground(style);

		scrollPane.setViewportView(textPane);
	}

	/**
	 * Ajoute du texte avec une couleur spécifique à la fin du document
	 * @param text le texte à ajouter
	 * @param color la couleur dans laquelle ajouter le texte
	 */
	public void appendToDocument(String text, Color color)
	{
		StyleConstants.setForeground(style, color);

		try
		{
			document.insertString(document.getLength(), text
					+ newline, style);
		}
		catch (BadLocationException ex)
		{
			System.err.println("write at bad location");
			ex.printStackTrace();
		}

		StyleConstants.setForeground(style, defaultColor);
	}

	// ------------------------------------------------------------------------
	// Actions de l'application
	//	On utilise des actions lorsque celles ci doivent pouvoir être invoquées
	//	depuis divers élements de l'interface graphique: p.ex. menu ET bouton.
	//	Sinon un simple ActionListener sur un bouton par exemple suffirait.
	// ------------------------------------------------------------------------

	/**
	 * Action listener interne à la classe ExampleFrame pour executer les
	 * instructions requises lorsque l'on clique sur le bouton "blue"
	 */
	private class BlueAction extends AbstractAction
	{
		/**
		 * Constructeur de BlueAction: met en place le nom et la description de
		 * l'action ainsi que son raccourci clavier
		 */
		public BlueAction()
		{
			putValue(MNEMONIC_KEY, KeyEvent.VK_B);
			putValue(SMALL_ICON, new ImageIcon(ExampleFrame.class.getResource("/examples/icons/bg_blue-16.png")));
			putValue(LARGE_ICON_KEY, new ImageIcon(ExampleFrame.class.getResource("/examples/icons/bg_blue-32.png")));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.META_MASK));
			putValue(NAME, "Blue");
			putValue(SHORT_DESCRIPTION, "Prints \"Blue\" in blue in the document");
		}

		/**
		 * Action à réaliser lorsque le BlueAction est sollicité
		 * @param e l'action event associé
		 */
		@Override
		public void actionPerformed(ActionEvent e)
		{
			/*
			 * BlueAction étant une classe interne (non static) elle a
			 * donc accès aux membres de la classe ExampleFrame
			 * Change la couleur du texte en bleu et affiche un message
			 */
			appendToDocument("Blue", Color.BLUE);
		}
	}

	/**
	 * Listener lorsque le bouton #btnClear est activé.
	 * Efface le contenu du {@link #document}
	 */
	private class ClearAction extends AbstractAction
	{
		/**
		 * Constructeur de ClearAction: met en place le nom et la description de
		 * l'action ainsi que son raccourci clavier
		 */
		public ClearAction()
		{
			putValue(MNEMONIC_KEY, KeyEvent.VK_L);
			putValue(SMALL_ICON, new ImageIcon(ExampleFrame.class.getResource("/examples/icons/erase-16.png")));
			putValue(LARGE_ICON_KEY, new ImageIcon(ExampleFrame.class.getResource("/examples/icons/erase-32.png")));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.META_MASK));
			putValue(NAME, "Clear");
			putValue(SHORT_DESCRIPTION, "Clears the document");
		}

		/**
		 * Opérations à réaliser lorsque #clearAction est sollicitée
		 * @param e l'évènement à l'origine du déclenchement de l'action
		 */
		@Override
		public void actionPerformed(ActionEvent e)
		{
			try
			{
				document.remove(0, document.getLength());
			}
			catch (BadLocationException ex)
			{
				System.err.println("ClientFrame: clear doc: bad location");
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Action interne à la classe ExampleFrame pour executer les
	 * instructions requises lorsque l'on clique sur le bouton "red"
	 */
	private class RedAction extends AbstractAction
	{
		/**
		 * Constructeur de RedAction: met en place le nom et la description de
		 * l'action ainsi que son raccourci clavier
		 */
		public RedAction()
		{
			putValue(MNEMONIC_KEY, KeyEvent.VK_R);
			putValue(SMALL_ICON, new ImageIcon(ExampleFrame.class.getResource("/examples/icons/bg_red-16.png")));
			putValue(LARGE_ICON_KEY, new ImageIcon(ExampleFrame.class.getResource("/examples/icons/bg_red-32.png")));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.META_MASK));
			putValue(NAME, "Red");
			putValue(SHORT_DESCRIPTION, "Prints \"Red\" in red in the document");
		}

		/**
		 * Opérations à réaliser lorsque #redAction est sollicitée
		 * @param e l'évènement à l'origine du déclenchement de l'action
		 */
		@Override
		public void actionPerformed(ActionEvent e)
		{
			/*
			 * Change la couleur du texte en rouge et affiche "Red" dans le
			 * document
			 */
			appendToDocument("Red", Color.RED);
		}
	}
}
