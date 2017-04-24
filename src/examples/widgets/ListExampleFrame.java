package examples.widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Stack;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Exemple de fenètre contenant une liste d'éléments
 *
 * @author davidroussel
 */
public class ListExampleFrame extends JFrame
{
	/**
	 * Chaîne de caractère pour passer à la ligne
	 */
	private static String newline = System.getProperty("line.separator");

	/**
	 * Liste des éléments à afficher dans la JList.
	 * Les ajouts et retraits effectués dans cette ListModel seront alors
	 * automatiquement transmis au JList contenant ce ListModel
	 */
	private DefaultListModel<String> elements = new DefaultListModel<String>();

	/**
	 * Le modèle de sélection de la JList.
	 * Conserve les indices des éléments sélectionnés de {@link #elements} dans
	 * la JList qui affiche ces éléments.
	 */
	private ListSelectionModel selectionModel = null;

	/**
	 * La text area où afficher les messages
	 */
	private JTextArea output = null;

	/**
	 * Action à réaliser lorsque l'on souhaite supprimer les éléments
	 * sélectionnnés de la liste
	 */
	private final Action removeAction = new RemoveItemAction();

	/**
	 * Action à réaliser lorsque l'on souhaite déselctionner tous les élements de la liste
	 */
	private final Action clearSelectionAction = new ClearSelectionAction();

	/**
	 * Action à réaliser lorsque l'on souhaite ajouter un élément à la liste
	 */
	private final Action addAction = new AddAction();

	/**
	 * @throws HeadlessException
	 */
	public ListExampleFrame() throws HeadlessException
	{
		super(); // déjà implicite
		elements.addElement("Ténéphore");
		elements.addElement("Zébulon");
		elements.addElement("Zéphirine");
		elements.addElement("Uriel");
		elements.addElement("Philomène");

		setPreferredSize(new Dimension(200, 100));
		getContentPane().setLayout(new BorderLayout(0, 0));

		JScrollPane textScrollPane = new JScrollPane();
		getContentPane().add(textScrollPane, BorderLayout.CENTER);

		output = new JTextArea();
		textScrollPane.setViewportView(output);

		JPanel leftPanel = new JPanel();
		leftPanel.setPreferredSize(new Dimension(200, 10));
		getContentPane().add(leftPanel, BorderLayout.WEST);
		leftPanel.setLayout(new BorderLayout(0, 0));

		JButton btnClearSelection = new JButton("Clear Selection");
		btnClearSelection.setAction(clearSelectionAction);
		leftPanel.add(btnClearSelection, BorderLayout.NORTH);

		JScrollPane listScrollPane = new JScrollPane();
		leftPanel.add(listScrollPane, BorderLayout.CENTER);

		JList<String> list = new JList<String>(elements);
		listScrollPane.setViewportView(list);
		list.setName("Elements");
		list.setBorder(UIManager.getBorder("EditorPane.border"));
		list.setSelectedIndex(0);
		list.setCellRenderer(new ColorTextRenderer());

		JPopupMenu popupMenu = new JPopupMenu();
		addPopup(list, popupMenu);

		JMenuItem mntmAdd = new JMenuItem(addAction);
		mntmAdd.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.META_MASK));
		popupMenu.add(mntmAdd);

		JMenuItem mntmRemove = new JMenuItem(removeAction);
		popupMenu.add(mntmRemove);

		JSeparator separator = new JSeparator();
		popupMenu.add(separator);

		JMenuItem mntmClearSelection = new JMenuItem(clearSelectionAction);
		popupMenu.add(mntmClearSelection);

		selectionModel = list.getSelectionModel();
		selectionModel.addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();

				int firstIndex = e.getFirstIndex();
				int lastIndex = e.getLastIndex();
				boolean isAdjusting = e.getValueIsAdjusting();
				/*
				 * isAdjusting remains true while events like drag n drop are
				 * still processed and becomes false afterwards.
				 */
				if (!isAdjusting)
				{
					output.append("Event for indexes " + firstIndex + " - "
						+ lastIndex + "; selected indexes:");

					if (lsm.isSelectionEmpty())
					{
						removeAction.setEnabled(false);
						clearSelectionAction.setEnabled(false);
						output.append(" <none>");
					}
					else
					{
						removeAction.setEnabled(true);
						clearSelectionAction.setEnabled(true);
						// Find out which indexes are selected.
						int minIndex = lsm.getMinSelectionIndex();
						int maxIndex = lsm.getMaxSelectionIndex();
						for (int i = minIndex; i <= maxIndex; i++)
						{
							if (lsm.isSelectedIndex(i))
							{
								output.append(" " + i);
							}
						}
					}
					output.append(newline);
				}
				else
				{
					// Still adjusting ...
					output.append("Processing ..." + newline);
				}
			}
		});
	}

	/**
	 * Color Text renderer for drawing list's elements in colored text
	 * @author davidroussel
	 */
	public static class ColorTextRenderer extends JLabel
		implements ListCellRenderer<String>
	{
		private Color color = null;

		/**
		 * Customized rendering for a ListCell with a color obtained from
		 * the hashCode of the string to display
		 * @see
		 * javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing
		 * .JList, java.lang.Object, int, boolean, boolean)
		 */
		@Override
		public Component getListCellRendererComponent(
			JList<? extends String> list, String value, int index,
			boolean isSelected, boolean cellHasFocus)
		{
			color = list.getForeground();
			if (value != null)
			{
				if (value.length() > 0)
				{
					color = new Color(value.hashCode()).darker();
				}
			}
			setText(value);
			if (isSelected)
			{
				setBackground(color);
				setForeground(list.getSelectionForeground());
			}
			else
			{
				setBackground(list.getBackground());
				setForeground(color);
			}
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setOpaque(true);
			return this;
		}
	}

	/**
	 * Adds a popup menu to a component
	 * @param component the parent component of the popup menu
	 * @param popup the popup menu to add
	 */
	private static void addPopup(Component component, final JPopupMenu popup)
	{
		component.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					showMenu(e);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e)
			{
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	private class RemoveItemAction extends AbstractAction
	{
		public RemoveItemAction()
		{
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.META_MASK));
			putValue(SMALL_ICON, new ImageIcon(ListExampleFrame.class.getResource("/examples/icons/remove_user-16.png")));
			putValue(LARGE_ICON_KEY, new ImageIcon(ListExampleFrame.class.getResource("/examples/icons/remove_user-32.png")));
			putValue(NAME, "Remove");
			putValue(SHORT_DESCRIPTION, "Removes item from list");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			output.append("Remove action triggered for indexes : ");
			int minIndex = selectionModel.getMinSelectionIndex();
			int maxIndex = selectionModel.getMaxSelectionIndex();
			Stack<Integer> toRemove = new Stack<Integer>();
			for (int i = minIndex; i <= maxIndex; i++)
			{
				if (selectionModel.isSelectedIndex(i))
				{
					output.append(" " + i);
					toRemove.push(new Integer(i));
				}
			}
			output.append(newline);
			while (!toRemove.isEmpty())
			{
				int index = toRemove.pop().intValue();
				output.append("removing element: "
					+ elements.getElementAt(index) + newline);
				elements.remove(index);
			}
		}
	}

	private class ClearSelectionAction extends AbstractAction
	{
		public ClearSelectionAction()
		{
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.META_MASK));
			putValue(LARGE_ICON_KEY, new ImageIcon(ListExampleFrame.class.getResource("/examples/icons/delete_sign-32.png")));
			putValue(SMALL_ICON, new ImageIcon(ListExampleFrame.class.getResource("/examples/icons/delete_sign-16.png")));
			putValue(NAME, "Clear selection");
			putValue(SHORT_DESCRIPTION, "Unselect selected items");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			output.append("Clear selection action triggered" + newline);
			selectionModel.clearSelection();
		}
	}

	private class AddAction extends AbstractAction
	{
		public AddAction()
		{
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.META_MASK));
			putValue(SMALL_ICON, new ImageIcon(ListExampleFrame.class.getResource("/examples/icons/add_user-16.png")));
			putValue(LARGE_ICON_KEY, new ImageIcon(ListExampleFrame.class.getResource("/examples/icons/add_user-32.png")));
			putValue(NAME, "Add...");
			putValue(SHORT_DESCRIPTION, "Add item");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			output.append("Add action triggered" + newline);
			String inputValue = JOptionPane.showInputDialog("New item name");
			if (inputValue != null)
			{
				if (inputValue.length() > 0)
				{
					elements.addElement(inputValue);
				}
			}
		}
	}
}
