package gui;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;
import datatypes.Aircraft;
import datatypes.ICAOCodeBlock;
import database.DBHandler;
import subscriber.SubscriberFactory;

public final class MainPanel extends JPanel implements ActionListener {

	private static Properties properties = new Properties();

	private static final long serialVersionUID = 1L;

	static SubscriberFactory oFactory = null;

	static DBHandler oHandler = null;

	static DefaultTableModel model = null;

	public static int numbSubs = 4;

	public static boolean onlyMil = false;

	public static JFrame theFrame = null;

	public static long lastMilcall = new Date().getTime();

	public static String[] address = null;

	public static MainPanel thePanel = null;

	public static String baseDB = null;

	public static String standingDB = null;

	public static String baseOutPath = null;
	
	public static String adsbURL= null;

	javax.swing.Timer timer = new javax.swing.Timer(1000, this);

	public MainPanel() {
		super(new BorderLayout());

		JPanel top = new JPanel();
		top.setLayout(new BorderLayout());

		JButton button = new JButton();
		if (MainPanel.onlyMil) {
			
			button.setText("Unmute civil");

		} else {
			button.setText("Mute civil");
		}

		button.setMargin(new Insets(8, 8, 8, 8));
		
		JToolBar toolBar = new JToolBar("");
		toolBar.add(button);
	
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (MainPanel.onlyMil) {
					MainPanel.onlyMil = false;
					button.setText("Mute civil");

				} else {
					MainPanel.onlyMil = true;
					button.setText("Unmute civil");
				}
			}
		});
	
		button.setFont(new Font("Dialog", Font.PLAIN, 14));
		add(toolBar, BorderLayout.NORTH);

		add(new JScrollPane(makeTable()), BorderLayout.CENTER);
		
		setBorder(BorderFactory.createCompoundBorder());
		setPreferredSize(new Dimension(950, 45 * numbSubs));

		timer.start();

		oFactory = new SubscriberFactory();
		oFactory.setup(numbSubs);
		oFactory.startAll();

		thePanel = this;

	}

	private JTable makeTable() {

		oHandler = new database.DBHandler();

		System.out.println("loading basestation");

		if (oHandler.loadDB(baseDB) != 1) {
			JOptionPane.showMessageDialog(null, "Unable to load basestation database, check path in properties");
		}

		System.out.println("loading blocks");

		if (oHandler.loadBlocks(standingDB) != 1) {
			JOptionPane.showMessageDialog(null, "Unable to load StandingData database, check path in properties");
			// Without the standing data cannot determine mil or not
			onlyMil = false;

		}

		System.out.println("finished loading blocks");

		String empty = "";
		String[] columnNames = { "Line", "Last received", "Mute", "Hex", "Errors", "Options", "Timestamp" };
		Object[][] data = new Object[numbSubs][7];

		for (int a = 0; a < numbSubs; a++) {

			data[a][0] = "Line " + (a + 1);
			data[a][1] = ""; // last rec
			data[a][2] = "Unmuted"; // mute unmute
			data[a][3] = ""; // hex
			data[a][4] = ""; // errors
			data[a][5] = empty; // buttons
			data[a][6] = new Long(0); // hidden
		}

		model = new DefaultTableModel(data, columnNames) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Class<?> getColumnClass(int column) {
				return getValueAt(0, column).getClass();
			}
		};

		final JTable table = new JTable(model);
		table.setRowHeight(36);
		table.setAutoCreateRowSorter(true);
		TableColumn column = table.getColumnModel().getColumn(5);
		column.setCellRenderer(new ButtonsRenderer());
		column.setCellEditor(new ButtonsEditor(table));
		
		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent mouseEvent) {

				JTable table = (JTable) mouseEvent.getSource();
			
				if (table.getSelectedRow() != -1) {

					String hex = table.getValueAt(table.getSelectedRow(), 3).toString().split(" ")[0];

					if (hex.length() == 6) {

						if (mouseEvent.getButton() == MouseEvent.BUTTON3) {

							if (Desktop.isDesktopSupported()
									&& Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
								try {
									Desktop.getDesktop().browse(new URI(adsbURL + hex));
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (URISyntaxException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}

					}
				}
			}
		});

		table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
	
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int col) {

				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

				Long last = (Long) table.getModel().getValueAt(row, 6);

				long diff = new Date().getTime() - last.longValue();

				if (diff < 10000) {
					setBackground(Color.decode("#90EE90"));
					setForeground(Color.BLACK);
				} else {
					setBackground(table.getBackground());
					setForeground(table.getForeground());
				}
				return this;
			}
		});

		TableColumnModel tcm = table.getColumnModel();
		tcm.removeColumn(tcm.getColumn(6));

		return table;
	}

	public static DefaultTableModel getModel() {
		return model;
	}

	public static void setModel(DefaultTableModel model) {
		MainPanel.model = model;
	}

	public static void main(String... args) {

		try (final InputStream stream = MainPanel.class.getClassLoader().getClass()
				.getResourceAsStream("/aero_c_panel.properties")) {
			properties.load(stream);

			numbSubs = Integer.parseInt(properties.getProperty("NUMBER_OF_CHANNELS"));

			address = new String[numbSubs + 1];
			for (int a = 1; a <= numbSubs; a++) {

				address[a] = properties.getProperty("ADDRESS_" + a, "tcp://127.0.0.1:555" + a);
			}

			if (properties.getProperty("ONLY_MIL").equals("TRUE")) {
				onlyMil = true;
			} else {
				onlyMil = false;
			}

			baseDB = properties.getProperty("BASE_STN_DB");
			standingDB = properties.getProperty("STANDING_DB");
			baseOutPath = properties.getProperty("AUDIO_OUT_PATH");
			adsbURL = properties.getProperty("ADSB_URL");

		} catch (Exception e) {

			System.err.println("Caught exception reading properties file:" + e.getMessage());
		}

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowGUI();

			}
		});

	}

	public static void createAndShowGUI() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException ex) {
			ex.printStackTrace();
		}
		JFrame frame = new JFrame("AERO C Panel");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.getContentPane().add(new MainPanel());
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		theFrame = frame;
	}



	public static Aircraft getAircraft(String hex) {

		if (oHandler != null) {
			return oHandler.getAircraft(hex);
		} else
			return null;
	}

	public static boolean isMil(String hex) {

		if (oHandler != null) {
			ICAOCodeBlock block = oHandler.getBlock(hex);

			if ((block != null && block.getIsMilitary())) {
				return true;
			}

		}
		return false;
	}

	public static void blink() {

		theFrame.toFront();

	}

	public void startPanel() {

	}

	public void actionPerformed(ActionEvent ev) {

		if (ev.getSource() == timer) {

			// this will call at every 1 second
			repaint();
     	}

	}

}
