/*
 * dalserver-interop library - implementation of DAL server for interoperability
 * Copyright (C) 2015  Diversity Arrays Technology
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.diversityarrays.dal.server;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableRowSorter;

import net.pearcan.dnd.TableTransferHandler;
import net.pearcan.ui.GuiUtil;
import net.pearcan.ui.StatusInfoLine;
import net.pearcan.ui.table.BspAbstractTableModel;
import net.pearcan.util.MemoryUsageMonitor;

import org.apache.commons.collections15.Closure;

import com.diversityarrays.dal.db.DalDatabase;
import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.SqlDalDatabase;
import com.diversityarrays.util.QRcodeDialog;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.ServerRunner;
import fi.iki.elonen.SimpleWebServer;

public class ServerGui extends JFrame {
	
	private final Image serverIconImage;
	private final DalServerFactory dalServerFactory;

	protected final DalServerPreferences preferences;
	
	private Action serverStartAction = new AbstractAction("Start...") {
		@Override
		public void actionPerformed(ActionEvent e) {
			
			AskServerParams asker = new AskServerParams(serverIconImage, ServerGui.this, "DAL Server Start", wwwRoot, preferences);
			asker.setVisible(true);
			
			if (! asker.cancelled) {
				
				String host = asker.dalServerHostName;
				Integer port = asker.dalServerPort;
				
				IDalServer srv = dalServerFactory.create(host, port, asker.wwwRoot, asker.dalDatabase);
//				srv.setUseSimpleDatabase(asker.useSimpleDatabase);
				
				srv.setMaxInactiveMinutes(asker.maxInactiveMinutes);
				setServer(srv);

				Thread t = new Thread() {
					public void run() {
						ensureDatabaseInitialisedThenStartServer();
					}
				};
				t.start();
			}
		}
	};
	
	private Action serverStopAction = new AbstractAction("Stop") {

		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("!!! shutting down database");
			try {
				server.getDalDatabase().shutdown();
			} catch (DalDbException e1) {
				System.err.println(">>> "+e1.getMessage());
			}
			
			System.out.println("!!! Stopping server");
			server.getHttpServer().stop();
			setServer(null);
		}
	};
	
	private Action exitAction = new AbstractAction("Exit") {
		@Override
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	};
	
	private SqlDialog sqlDialog = null;

	private JCheckBoxMenuItem doSql = new JCheckBoxMenuItem(new AbstractAction("SQL Command Window") {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (sqlDialog==null) {
				
				DalDatabase db = server.getDalDatabase();
				if (! (db instanceof SqlDalDatabase)) {
					GuiUtil.errorMessage(ServerGui.this, "DalDatabase " + db.getDatabaseName() + " is not an " + SqlDalDatabase.class.getSimpleName(), getTitle());
					return;
				}
				
				sqlDialog = new SqlDialog(ServerGui.this, (SqlDalDatabase) db);
				sqlDialog.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosed(WindowEvent e) {
						sqlDialog = null;
						doSql.setSelected(false);
					}
				});
				doSql.setSelected(true);
				sqlDialog.setVisible(true);
			}
			else {
				sqlDialog.toFront();
			}

		}
	});

	private Action copyDalUrlAction = new AbstractAction("Copy URL") {
		@Override
		public void actionPerformed(ActionEvent e) {
			String dalUrl = getDalUrl();
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(dalUrl), null);
			System.out.println("Copied to clipboard: '"+dalUrl+"'");
		}
	};
	
	private Action showDalUrlQRcodeAction = new AbstractAction("Show QR-Code") {
		
		QRcodeDialog dialog;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if (dialog==null) {
				String dalUrl = getDalUrl();
				dialog = new QRcodeDialog(ServerGui.this, "DAL URL", dalUrl, ModalityType.MODELESS);
				dialog.setResizable(false);
				dialog.addWindowListener(new WindowAdapter() {
					
					@Override
					public void windowOpened(WindowEvent e) {
						GuiUtil.centreOnOwner(dialog);
					}

					@Override
					public void windowClosed(WindowEvent e) {
						dialog = null;
					}
				});
				dialog.setVisible(true);
			}
		}
	};
	
	private String getDalUrl() {
		String serverUrl = server.getListeningUrl();
		String dalUrl = "http://"+serverUrl+"/dal/";
		return dalUrl;
	}
	
	private JCheckBox quietOption = new JCheckBox("Quiet");
	private JTextArea messages = new JTextArea();

	private IDalServer server;

	private StatusInfoLine statusInfoLine = new StatusInfoLine();

	private File wwwRoot;
	
	public ServerGui(Image serverIconImage, IDalServer svr, DalServerFactory factory, File wwwRoot, DalServerPreferences prefs) {

		this.serverIconImage = serverIconImage;
		this.dalServerFactory = factory;
		this.wwwRoot = wwwRoot;
		this.preferences = prefs;
		
		JMenuBar menuBar = new JMenuBar();
		
		JMenu serverMenu = new JMenu("Server");
		menuBar.add(serverMenu);
		serverMenu.add(serverStartAction);
		serverMenu.add(serverStopAction);
		serverMenu.add(exitAction);
		
		
		JMenu commandMenu = new JMenu("Command");
		menuBar.add(commandMenu);
		commandMenu.add(doSql);
		
		JMenu urlMenu = new JMenu("URL");
		menuBar.add(urlMenu);
		urlMenu.add(new JMenuItem(copyDalUrlAction));
		urlMenu.add(new JMenuItem(showDalUrlQRcodeAction));
		
		setJMenuBar(menuBar);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		messages.setFont(GuiUtil.createMonospacedFont(12));
		messages.setEditable(false);

		setServer(svr);
		
		quietOption.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean q = quietOption.isSelected();
				if (server!=null) {
					server.setQuiet(q);
				}
			}
		});

		JScrollPane scrollPane = new JScrollPane(messages, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		final JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();

		JButton clear = new JButton(new AbstractAction("Clear") {
			@Override
			public void actionPerformed(ActionEvent e) {
				messages.setText("");
			}
		});
		
		final boolean[] follow = new boolean[] { true };
		final JCheckBox followTail = new JCheckBox("Follow", follow[0]);

		followTail.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				follow[0] = followTail.isSelected();
			}
		});


		OutputStream os = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				char ch = (char) b;
				messages.append(new Character(ch).toString());
				if (ch=='\n' && follow[0]) {
					verticalScrollBar.setValue(verticalScrollBar.getMaximum());
				}
			}
		};

		PrintStream ps = new PrintStream(os);

		System.setErr(ps);
		System.setOut(ps);

		Box box = Box.createHorizontalBox();
		box.add(clear);
		box.add(followTail);
		box.add(quietOption);
		box.add(Box.createHorizontalGlue());

		JPanel bottom = new JPanel(new BorderLayout());
		bottom.add(BorderLayout.NORTH, box);
		bottom.add(BorderLayout.SOUTH, statusInfoLine);
		
		Container cp = getContentPane();
		cp.add(BorderLayout.CENTER, scrollPane);
		cp.add(BorderLayout.SOUTH,  bottom);

		pack();
		setSize(640, 480);
		
		final MemoryUsageMonitor mum = new MemoryUsageMonitor();
		mum.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				statusInfoLine.setMessage(mum.getMemoryUsage());
			}
		});

		if (server==null) {
			// If initial server is null, allow user to specify
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowOpened(WindowEvent e) {
					removeWindowListener(this);
					serverStartAction.actionPerformed(null);
				}
			});
		}
		else {
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowOpened(WindowEvent e) {
					removeWindowListener(this);
					ensureDatabaseInitialisedThenStartServer();
				}
			});
		}
	}
	
	private PropertyChangeListener serverRunstateChangeListener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			StringBuilder sb = new StringBuilder("DAL-interop Server ");
			sb.append("(").append(server.getDalServerVersion()).append(")");
			sb.append(" listening on ").append(server.getListeningAddressPort());
			if ( ! server.isAlive()) {
				sb.append(" INACTIVE");
			}
			setTitle(sb.toString());
		}
	};
	
	private void setServer(IDalServer svr) {
		if (server!=null) {
			server.removePropertyChangeListener(SimpleWebServer.PROPERTY_RUNSTATE, serverRunstateChangeListener);
		}
		this.server = svr;
		
		boolean haveServer = this.server!=null;
		copyDalUrlAction.setEnabled(haveServer);
		showDalUrlQRcodeAction.setEnabled(haveServer);
		
		serverStartAction.setEnabled(this.server==null);
		serverStopAction.setEnabled(! serverStartAction.isEnabled());
		
		quietOption.setEnabled(server!=null);
		
		if (server!=null) {
			server.addPropertyChangeListener(SimpleWebServer.PROPERTY_RUNSTATE, serverRunstateChangeListener);
			
			quietOption.setSelected(server.isQuiet());
			
			StringWriter sw = new StringWriter();
			
			PrintWriter pw = new PrintWriter(sw);
			String serverUrl = server.getListeningUrl(); // server.getServerAddress();
			pw.println("DAL-interop Server listening on http://"+serverUrl);
			pw.println("DAL is available at http://"+serverUrl+"/dal/");
			pw.println("--------------");

			DalDatabase db = server.getDalDatabase();
			pw.println("Database: " + db.getDatabaseName());
			pw.println("==============");
			pw.close();
			
			messages.setText(sw.toString());
		}
	}
	

	private void ensureDatabaseInitialisedThenStartServer() {
		DalDatabase db = server.getDalDatabase();
		if (db.isInitialiseRequired()) {
			try {
				Closure<String> progress = new Closure<String>() {
					@Override
					public void execute(String msg) {
						messages.append(msg + "\n");
					}
				};
				db.initialise(progress);
			} catch (DalDbException e) {
				GuiUtil.errorMessage(ServerGui.this, e, "Unable to initialise database: " + db.getDatabaseName());
			}
		}
		
		System.out.println("Session Auto-Expiry after "+server.getMaxInactiveMinutes()+" minutes");

		NanoHTTPD httpServer = server.getHttpServer();

    	String hostPort = httpServer.getHostname()+":"+ httpServer.getPort();
    	System.out.println("Starting server: "+hostPort);
    	
		IOException err = ServerRunner.executeInstance(httpServer, false);
		if (err!=null) {
			serverStartAction.setEnabled(true);
			if (err instanceof java.net.BindException) {
				System.err.println("!!!! "+hostPort+": "+err.getMessage());
			}
			else {
				System.err.println("!!!! "+err.getMessage());
				err.printStackTrace();
			}
		}
	}

	static class SqlResultsTableModel extends BspAbstractTableModel {

		private List<String[]> rows;

		SqlResultsTableModel(String[] columnNames, List<String[]> data) {
			super(columnNames);
			
			this.rows = data;
		}

		@Override
		public int getRowCount() {
			return rows==null ? 0 : rows.size();
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			String[] row = rows.get(rowIndex);
			return columnIndex<row.length ? row[columnIndex] : null;
		}
		
	}
	
	
	static class SqlResultsPanel extends JPanel {

		private JTextArea sqlCommand = new JTextArea();
		private SqlResultsTableModel tableModel;
		private JTable table;
		
		private TableTransferHandler tth;

		SqlResultsPanel(boolean includeHeadings, String sql, String[] headings, List<String[]> rows) {
			super(new BorderLayout());

			this.tableModel = new SqlResultsTableModel(headings, rows);
			table = new JTable(tableModel);
			table.setRowSorter(new TableRowSorter<SqlResultsTableModel>(tableModel));
			
			table.setCellSelectionEnabled(true);
			tth = TableTransferHandler.initialiseForCopySelectAll(table, includeHeadings);
			
//			TransferActionListener tal = new TransferActionListener();
//			tal.getCopySelectAllMenuItems()
			
			sqlCommand.setEditable(false);
			sqlCommand.setLineWrap(true);
			sqlCommand.setWrapStyleWord(true);
			sqlCommand.setText(sql);
			
			add(BorderLayout.NORTH, new JScrollPane(sqlCommand));
			add(BorderLayout.CENTER, new JScrollPane(table));
		}

		public void setIncludeHeadings(boolean b) {
			tth.setIncludeHeadings(b);
		}
		
	}

}