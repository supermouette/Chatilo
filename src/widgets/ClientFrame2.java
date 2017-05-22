package widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Stack;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.StyleConstants;

import chat.Vocabulary;
import java.io.ObjectInputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static javax.swing.Action.ACCELERATOR_KEY;
import static javax.swing.Action.LARGE_ICON_KEY;
import static javax.swing.Action.NAME;
import static javax.swing.Action.SHORT_DESCRIPTION;
import static javax.swing.Action.SMALL_ICON;
import models.Message;

import widgets.ClientFrame2;
/**
 * Fenêtre d'affichae de la version GUI texte du client de chat.
 * @author davidroussel
 */
public class ClientFrame2 extends AbstractClientFrame
{
	/**
	 * Lecteur de flux d'entrée. Lit les données texte du {@link #inPipe} pour
	 * les afficher dans le {@link #document}
	 */
	private ObjectInputStream is;

	/**
	 * Le label indiquant sur quel serveur on est connecté
	 */
	protected final JLabel serverLabel;

	/**
	 * La zone du texte à envoyer
	 */
	protected final JTextField sendTextField;
	
	/**
	 * Chaîne de caractère pour passer à la ligne
	 */
	private static String newline = System.getProperty("line.separator");

	/**
	 * Actions à réaliser lorsque l'on veut effacer le contenu du document
	 */
	private final ClearAction clearAction;

	/**
	 * Actions à réaliser lorsque l'on veut envoyer un message au serveur
	 */
	private final SendAction sendAction;

	/**
	 * Actions à réaliser lorsque l'on veut envoyer un message au serveur
	 */
	protected final QuitAction quitAction;
	
	/**
	 * Actions à réaliser lorsque l'on veut filtrer les messages des utilisateurs selectionnés
	 */
	private final FilterMessages filterMessages;
	
	/**
	 * Actions à réaliser lorsque l'on veut kick un utilisateur
	 */
	private final KickSelected kickSelected;

	/**
	 * Référence à la fenêtre courante (à utiliser dans les classes internes)
	 */
	protected final JFrame thisRef;
	
	/**
	 * La text area où afficher les messages
	 */
	private JTextArea output = null;
	
	/**
	 * Liste des éléments à afficher dans la JList.
	 * Les ajouts et retraits effectués dans cette ListModel seront alors
	 * automatiquement transmis au JList contenant ce ListModel
	 */
	private DefaultListModel<String> users = new DefaultListModel<String>();
	
	/**
	 * Le modèle de sélection de la JList.
	 * Conserve les indices des éléments sélectionnés de {@link #users} dans
	 * la JList qui affiche ces éléments.
	 */
	private ListSelectionModel selectionModel = null;

	/**
	 * Action à réaliser lorsque l'on souhaite déselectionner tous les élements de la liste
	 */
	private final Action clearSelectionAction = new ClearSelectionAction("Clear selection");
	private final Action clearSelectionActionBtn = new ClearSelectionAction("");

    private ArrayList<Message> messages;
        
	/**
	 * Constructeur de la fenêtre
	 * @param name le nom de l'utilisateur
	 * @param host l'hôte sur lequel on est connecté
	 * @param commonRun état d'exécution des autres threads du client
	 * @param parentLogger le logger parent pour les messages
	 * @throws HeadlessException
	 */
	public ClientFrame2(String name,
	                   String host,
	                   Boolean commonRun,
	                   Logger parentLogger)
	    throws HeadlessException
	{
		super(name, host, commonRun, parentLogger);
		thisRef = this;

		// --------------------------------------------------------------------
		// Flux d'IO
		//---------------------------------------------------------------------
		/*
		 * Attention, la création du flux d'entrée doit (éventuellement) être
		 * reportée jusqu'au lancement du run dans la mesure où le inPipe
		 * peut ne pas encore être connecté à un PipedOutputStream
		 */

		// --------------------------------------------------------------------
		// Création des actions send, clear et quit
		// --------------------------------------------------------------------

		sendAction = new SendAction();
		clearAction = new ClearAction("Clear messages");
		quitAction = new QuitAction("Quit");
		filterMessages = new FilterMessages("Filter messages");
		kickSelected = new KickSelected("Kick selected");
		

		/*
		 * Ajout d'un listener pour fermer correctement l'application lorsque
		 * l'on ferme la fenêtre. WindowListener sur this
		 */
		addWindowListener(new FrameWindowListener());

		// --------------------------------------------------------------------
		// Widgets setup (handled by Window builder)
		// --------------------------------------------------------------------
		
		JPanel leftPanel = new JPanel();
		leftPanel.setPreferredSize(new Dimension(200, 10));
		getContentPane().add(leftPanel, BorderLayout.WEST);
		leftPanel.setLayout(new BorderLayout(0, 0));

		JScrollPane listScrollPane = new JScrollPane();
		leftPanel.add(listScrollPane, BorderLayout.CENTER);
		
		JScrollPane textScrollPane = new JScrollPane();
		getContentPane().add(textScrollPane, BorderLayout.CENTER);
		output = new JTextArea();
		textScrollPane.setViewportView(output);
		
		JList<String> list = new JList<String>(users);
		listScrollPane.setViewportView(list);
		list.setName("users");
		list.setBorder(UIManager.getBorder("EditorPane.border"));
		list.setSelectedIndex(0);
		list.setCellRenderer(new ColorTextRenderer());
		
		JPopupMenu popupMenu = new JPopupMenu();
		addPopup(list, popupMenu);

		JMenuItem mntmClear = new JMenuItem(clearSelectionActionBtn);
		popupMenu.add(mntmClear);

		JMenuItem mntmKick = new JMenuItem(kickSelected);
		popupMenu.add(mntmKick);

		JSeparator separator = new JSeparator();
		popupMenu.add(separator);

		JMenuItem mntmClearSelection = new JMenuItem(clearSelectionAction);
		popupMenu.add(mntmClearSelection);
		
		selectionModel = list.getSelectionModel();
		selectionModel.addListSelectionListener(new ListSelectionListener()
		{
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
						clearSelectionAction.setEnabled(false);
						output.append(" <none>");
					}
					else
					{
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
		
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		getContentPane().add(toolBar, BorderLayout.NORTH);

		JButton quitButton = new JButton(quitAction);
		quitButton.setHideActionText(true);
		toolBar.add(quitButton);
		
		JButton clearSelBtn = new JButton(clearSelectionAction);
		clearSelBtn.setHideActionText(true);
		toolBar.add(clearSelBtn);
		
		JButton kickSelBtn = new JButton(kickSelected);
		kickSelBtn.setHideActionText(true);
		toolBar.add(kickSelBtn);

		JButton clearBtn = new JButton(clearAction);
		clearBtn.setHideActionText(true);
		toolBar.add(clearBtn);
		
		JButton filterMessBtn = new JButton(filterMessages);
		filterMessBtn.setHideActionText(true);
		toolBar.add(filterMessBtn);

		Component toolBarSep = Box.createHorizontalGlue();
		toolBar.add(toolBarSep);

		serverLabel = new JLabel(host == null ? "" : host);
		toolBar.add(serverLabel);

		JPanel sendPanel = new JPanel();
		getContentPane().add(sendPanel, BorderLayout.SOUTH);
		sendPanel.setLayout(new BorderLayout(0, 0));
		sendTextField = new JTextField();
		sendTextField.setAction(sendAction);
		sendPanel.add(sendTextField);
		sendTextField.setColumns(10);

		JButton sendButton = new JButton(sendAction);
		sendPanel.add(sendButton, BorderLayout.EAST);

		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		JTextPane textPane = new JTextPane();
		textPane.setEditable(false);
		// autoscroll textPane to bottom
		DefaultCaret caret = (DefaultCaret) textPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		scrollPane.setViewportView(textPane);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu connectionsMenu = new JMenu("Connections");
		menuBar.add(connectionsMenu);

		JMenuItem quitConnectionsMenuItem = new JMenuItem(quitAction);
		connectionsMenu.add(quitConnectionsMenuItem);
		
		JMenu messageMenu = new JMenu("Messages");
		menuBar.add(messageMenu);
		
		JMenuItem clearMessagesMenuItem = new JMenuItem(clearAction);
		messageMenu.add(clearMessagesMenuItem);
		
		JMenuItem filterMessagesMenuItem = new JMenuItem(filterMessages);
		messageMenu.add(filterMessagesMenuItem);
		
		JMenu usersMenu = new JMenu("Users");
		menuBar.add(usersMenu);
		
		JMenuItem clearSelectedMenuItem = new JMenuItem(clearSelectionAction);
		usersMenu.add(clearSelectedMenuItem);
		
		JMenuItem kickSelectedMenuItem = new JMenuItem(kickSelected);
		usersMenu.add(kickSelectedMenuItem);


		// --------------------------------------------------------------------
		// Documents
		// récupération du document du textPane ainsi que du documentStyle et du
		// defaultColor du document
		//---------------------------------------------------------------------
		document = textPane.getStyledDocument();
		documentStyle = textPane.addStyle("New Style", null);
		defaultColor = StyleConstants.getForeground(documentStyle);
                
                messages = new ArrayList<Message>();
                Message.addOrder(Message.MessageOrder.DATE);

	}

        private void displayMessages() throws BadLocationException{
            document.remove(0, document.getLength());
            Collections.sort(messages);
            for(Message m : messages){
                writeMessage(m);
            }
        }
        
	/**
	 * Affichage d'un message dans le {@link #document}, puis passage à la ligne
	 * (avec l'ajout de {@link Vocabulary#newLine})
	 * La partie "[yyyy/MM/dd HH:mm:ss]" correspond à la date/heure courante
	 * obtenue grâce à un Calendar et est affichée avec la defaultColor alors
	 * que la partie "utilisateur > message" doit être affichée avec une couleur
	 * déterminée d'après le nom d'utilisateur avec
	 * {@link #getColorFromName(String)}, le nom d'utilisateur est quant à lui
	 * déterminé d'après le message lui même avec {@link #parseName(String)}.
	 * @param message le message à afficher dans le {@link #document}
	 * @throws BadLocationException si l'écriture dans le document échoue
	 * @see {@link examples.widgets.ExampleFrame#appendToDocument(String, Color)}
	 * @see java.text.SimpleDateFormat#SimpleDateFormat(String)
	 * @see java.util.Calendar#getInstance()
	 * @see java.util.Calendar#getTime()
	 * @see javax.swing.text.StyleConstants
	 * @see javax.swing.text.StyledDocument#insertString(int, String,
	 * javax.swing.text.AttributeSet)
	 */
	protected void writeMessage(Message message) throws BadLocationException
	{
		/*
		 * ajout du message "[yyyy/MM/dd HH:mm:ss] utilisateur > message" à
		 * la fin du document avec la couleur déterminée d'après "utilisateur"
		 * (voir AbstractClientFrame#getColorFromName)
		 */
		
		// ajout de l'utilisateur à la liste des utilisateurs
                if(message.hasAuthor()){
                    if(users.size()==0){
                        users.addElement(message.getAuthor());
                    } 
                    else if(!users.contains(message.getAuthor())){
                        Collator c = Collator.getInstance();
                        c.setStrength(Collator.PRIMARY);
                        int i;
                        for(i=0;i<users.size();i++){
                            if(c.compare(message.getAuthor(),users.get(i))<0){
                                users.insertElementAt(message.getAuthor(), i);
                                break;
                            }
                            if(i==users.size()-1){
                                users.addElement(message.getAuthor());
                                break;
                            }
                        }
                    }
                }
                else{
                    Pattern p = Pattern.compile("(.*) logged out$");
                    Matcher m = p.matcher(message.getContent());
                    if(m.matches()){
                        users.removeElement(m.group(1));
                    }
                }
                
		// source et contenu du message avec la couleur du message

                String source = message.getAuthor();
		if ((source != null) && (source.length() > 0))
		{
			/*
			 * Changement de couleur du texte
			 */
			StyleConstants.setForeground(documentStyle,
			                             getColorFromName(source));
		}

		document.insertString(document.getLength(),
                                      message.toString()+"\n",
		                      documentStyle);

		// Retour à la couleur de texte par défaut
		StyleConstants.setForeground(documentStyle, defaultColor);

	}

	/**
	 * Listener lorsque le bouton #btnClear est activé. Efface le contenu du
	 * {@link #document}
	 */
	@SuppressWarnings("serial")
	protected class ClearAction extends AbstractAction
	{
		/**
		 * Constructeur d'une ClearAction : met en place le nom, la description,
		 * le raccourci clavier et les small|Large icons de l'action
		 */
		public ClearAction(String name)
		{
			putValue(SMALL_ICON,
			         new ImageIcon(ClientFrame.class
			             .getResource("/icons/erase-16.png")));
			putValue(LARGE_ICON_KEY,
			         new ImageIcon(ClientFrame.class
			             .getResource("/icons/erase-32.png")));
			putValue(ACCELERATOR_KEY,
			         KeyStroke.getKeyStroke(KeyEvent.VK_L,
			                                InputEvent.META_MASK));
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, "Clear document content");
		}

		/**
		 * Opérations réalisées lorsque l'action est sollicitée
		 * @param e évènement à l'origine de l'action
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e)
		{
			/*
			 * Effacer le contenu du document
			 */
			try
			{
				document.remove(0, document.getLength());
                                messages.clear();
			}
			catch (BadLocationException ex)
			{
				logger.warning("ClientFrame: clear doc: bad location");
				logger.warning(ex.getLocalizedMessage());
			}
		}
	}

	/**
	 * Action réalisée pour envoyer un message au serveur
	 */
	@SuppressWarnings("serial")
	protected class SendAction extends AbstractAction
	{
		/**
		 * Constructeur d'une SendAction : met en place le nom, la description,
		 * le raccourci clavier et les small|Large icons de l'action
		 */
		public SendAction()
		{
			putValue(SMALL_ICON,
			         new ImageIcon(ClientFrame.class
			             .getResource("/icons/logout-16.png")));
			putValue(LARGE_ICON_KEY,
			         new ImageIcon(ClientFrame.class
			             .getResource("/icons/logout-32.png")));
			putValue(ACCELERATOR_KEY,
			         KeyStroke.getKeyStroke(KeyEvent.VK_S,
			                                InputEvent.META_MASK));
			putValue(NAME, "Send");
			putValue(SHORT_DESCRIPTION, "Send text to server");
		}

		/**
		 * Opérations réalisées lorsque l'action est sollicitée
		 * @param e évènement à l'origine de l'action
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e)
		{
			/*
			 * récupération du contenu du textfield et envoi du message au
			 * serveur (ssi le message n'est pas vide), puis effacement du
			 * contenu du textfield.
			 */
			// Obtention du contenu du sendTextField
			String content = sendTextField.getText();

			// logger.fine("Le contenu du textField etait = " + content);

			// envoi du message
			if (content != null)
			{
				if (content.length() > 0)
				{
					sendMessage(content);

					// Effacement du contenu du textfield
					sendTextField.setText("");
				}
			}
		}
	}

	/**
	 * Action réalisée pour se délogguer du serveur
	 */
	@SuppressWarnings("serial")
	private class QuitAction extends AbstractAction
	{
		/**
		 * Constructeur d'une QuitAction : met en place le nom, la description,
		 * le raccourci clavier et les small|Large icons de l'action
		 */
		public QuitAction(String name)
		{
			putValue(SMALL_ICON,
			         new ImageIcon(ClientFrame.class
			             .getResource("/icons/cancel-16.png")));
			putValue(LARGE_ICON_KEY,
			         new ImageIcon(ClientFrame.class
			             .getResource("/icons/cancel-32.png")));
			putValue(ACCELERATOR_KEY,
			         KeyStroke.getKeyStroke(KeyEvent.VK_Q,
			                                InputEvent.META_MASK));
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, "Disconnect from server and quit");
		}

		/**
		 * Opérations réalisées lorsque l'action "quitter" est sollicitée
		 * @param e évènement à l'origine de l'action
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e)
		{
			logger.info("QuitAction: sending bye ... ");

			serverLabel.setText("");
			thisRef.validate();

			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e1)
			{
				return;
			}

			sendMessage(Vocabulary.byeCmd);
		}
	}
	
	/**
	 * Action réalisée pour filtrer les messages pour les utilisateurs sélectionnés
	 */
	@SuppressWarnings("serial")
	protected class FilterMessages extends AbstractAction
	{
		/**
		 * Constructeur d'un filterMessages : met en place le nom, la description,
		 * le raccourci clavier et les small|Large icons de l'action
		 */
		public FilterMessages(String name)
		{
			putValue(SMALL_ICON,
			         new ImageIcon(ClientFrame.class
			             .getResource("/icons/filled_filter-16.png")));
			putValue(LARGE_ICON_KEY,
			         new ImageIcon(ClientFrame.class
			             .getResource("/icons/filled_filter-32.png")));
			putValue(ACCELERATOR_KEY,
			         KeyStroke.getKeyStroke(KeyEvent.VK_S,
			                                InputEvent.META_MASK));
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, "Filter messages by user selected");
		}

		/**
		 * Opérations réalisées lorsque l'action est sollicitée
		 * @param e évènement à l'origine de l'action
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e)
		{	
			// remove all messages from document
			try {
				document.remove(0, document.getLength());
			} catch (BadLocationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			// get selected users
			int minIndex = selectionModel.getMinSelectionIndex();
			int maxIndex = selectionModel.getMaxSelectionIndex();
			DefaultListModel<String> toRemove = new DefaultListModel<String>();
			for (int i = minIndex; i <= maxIndex; i++)
			{
				if (selectionModel.isSelectedIndex(i))
				{
					toRemove.addElement(users.getElementAt(i).toString());
				}
			}
			// display only the messages from selected users
			for(Message m : messages){
               if(toRemove.contains(m.getAuthor()))
				try {
					writeMessage(m);
				} catch (BadLocationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}            	   
            }
		}
	}
	
	/**
	 * Action réalisée pour effacer la selection
	 */
	@SuppressWarnings("serial")
	private class ClearSelectionAction extends AbstractAction
	{
		public ClearSelectionAction(String name)
		{
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.META_MASK));
			putValue(LARGE_ICON_KEY, new ImageIcon(ClientFrame2.class.getResource("/icons/delete_database-32.png")));
			putValue(SMALL_ICON, new ImageIcon(ClientFrame2.class.getResource("/icons/delete_database-16.png")));
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, "Unselect selected items");
		}

		public void actionPerformed(ActionEvent e)
		{
			output.append("Clear selection action triggered" + newline);
			selectionModel.clearSelection();
		}
	}
	
	/**
	 * Action réalisée pour kick les utilisateurs selectionnés
	 */
	@SuppressWarnings("serial")
	protected class KickSelected extends AbstractAction
	{
		/**
		 * Constructeur d'une SendAction : met en place le nom, la description,
		 * le raccourci clavier et les small|Large icons de l'action
		 */
		public KickSelected(String name)
		{
			putValue(SMALL_ICON,
			         new ImageIcon(ClientFrame.class
			             .getResource("/icons/remove_user-16.png")));
			putValue(LARGE_ICON_KEY,
			         new ImageIcon(ClientFrame.class
			             .getResource("/icons/remove_user-32.png")));
			putValue(ACCELERATOR_KEY,
			         KeyStroke.getKeyStroke(KeyEvent.VK_S,
			                                InputEvent.META_MASK));
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, "Kick selected user(s)");
		}

		/**
		 * Opérations réalisées lorsque l'action est sollicitée
		 * @param e évènement à l'origine de l'action
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e)
		{
			// get selected users
			int minIndex = selectionModel.getMinSelectionIndex();
			int maxIndex = selectionModel.getMaxSelectionIndex();
			Stack<Integer> toRemove = new Stack<Integer>();
			for (int i = minIndex; i <= maxIndex; i++)
			{
				if (selectionModel.isSelectedIndex(i))
				{
					toRemove.push(new Integer(i));
				}
			}
			
			while (!toRemove.isEmpty())
			{
				int index = toRemove.pop().intValue();
				output.append("removing element: "
					+ users.getElementAt(index) + newline);
				users.remove(index);
			}
		}
	}

	/**
	 * Classe gérant la fermeture correcte de la fenêtre. La fermeture correcte
	 * de la fenètre implique de lancer un cleanup
	 */
	protected class FrameWindowListener extends WindowAdapter
	{
		/**
		 * Méthode déclenchée à la fermeture de la fenêtre. Envoie la commande
		 * "bye" au serveur
		 */
		@Override
		public void windowClosing(WindowEvent e)
		{
			logger.info("FrameWindowListener::windowClosing: sending bye ... ");
			/*
			 * appeler actionPerformed de quitAction si celle ci est
			 * non nulle
			 */
			if (quitAction != null)
			{
				quitAction.actionPerformed(null);
			}
		}
	}

	/**
	 * Exécution de la boucle d'exécution. La boucle d'exécution consiste à lire
	 * une ligne sur le flux d'entrée avec un BufferedReader tant qu'une erreur
	 * d'IO n'intervient pas indiquant que le flux a été coupé. Auquel cas on
	 * quitte la boucle principale et on ferme les flux d'I/O avec #cleanup()
	 */
	public void run()
	{
            try {
                is = new ObjectInputStream(inPipe);
            } catch (IOException ex) {
                Logger.getLogger(ClientFrame2.class.getName()).log(Level.SEVERE, null, ex);
            }
		Message messageIn;

		while (commonRun.booleanValue())
		{
			messageIn = null;
			/*
			 * - Lecture d'une ligne de texte en provenance du serveur avec inBR
			 * Si une exception survient lors de cette lecture on quitte la
			 * boucle.
			 * - Si cette ligne de texte n'est pas nulle on affiche le message
			 * dans le document avec le format voulu en utilisant
			 * #writeMessage(String)
			 * - Après la fin de la boucle on change commonRun à false de
			 * manière synchronisée afin que les autres threads utilisant ce
			 * commonRun puissent s'arrêter eux aussi :
			 * synchronized(commonRun)
			 * {
			 * commonRun = Boolean.FALSE;
			 * }
			 * Dans toutes les étapes si un problème survient (erreur,
			 * exception, ...) on quitte la boucle en ayant au préalable ajouté
			 * un "warning" ou un "severe" au logger (en fonction de l'erreur
			 * rencontrée) et mis le commonRun à false (de manière synchronisé).
			 */
			try
			{
				/*
				 * read from input (doit être bloquant)
				 */
				messageIn = (Message)is.readObject();
			}
			catch (IOException e)
			{
				synchronized (commonRun)
				{
					commonRun = Boolean.FALSE;
				}
				logger.warning("ClientFrame: I/O Error reading");
				break;
			}catch (ClassNotFoundException ex) {
                            logger.severe(ex.toString());
                        }
			if (messageIn != null)
			{
				// Ajouter le message à la fin du document avec la couleur
				// voulue
				try
				{
                                    messages.add(messageIn);
                                    displayMessages();
                                    //writeMessage(messageIn);
				}
				catch (BadLocationException e)
				{
					synchronized (commonRun)
					{
						commonRun = Boolean.FALSE;
					}
					logger.warning("ClientFrame: write at bad location: "
					    + e.getLocalizedMessage());
					break;
				}
			}
			else // messageIn == null
			{
				break;
			}
		}

		if (commonRun.booleanValue())
		{
			logger
			    .info("ClientFrame::cleanup: changing run state at the end ... ");
			synchronized (commonRun)
			{
				commonRun = Boolean.FALSE;
			}
		}

		cleanup();
	}

	/**
	 * Fermeture de la fenètre et des flux à la fin de l'exécution
	 */
	@Override
	public void cleanup()
	{
		logger.info("ClientFrame::cleanup: closing input buffered reader ... ");
		try
		{
			is.close();
		}
		catch (IOException e)
		{
			logger.warning("ClientFrame::cleanup: failed to close input reader"
			    + e.getLocalizedMessage());
		}

		super.cleanup();
	}
	
	/**
	 * Color Text renderer for drawing list's users in colored text
	 * @author davidroussel
	 */
	public class ColorTextRenderer extends JLabel
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
		public Component getListCellRendererComponent(
			JList<? extends String> list, String value, int index,
			boolean isSelected, boolean cellHasFocus)
		{
			color = list.getForeground();
			if (value != null)
			{
				if (value.length() > 0)
				{
					color = getColorFromName(value);
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
}