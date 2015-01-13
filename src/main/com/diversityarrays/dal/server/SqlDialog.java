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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLEditorKit;

import net.pearcan.ui.DefaultBackgroundRunner;
import net.pearcan.ui.GuiUtil;
import net.pearcan.ui.widget.MessagesPanel;
import net.pearcan.util.BackgroundTask;

import org.apache.commons.collections15.Closure;

import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.DbUtil;
import com.diversityarrays.dal.db.SqlDalDatabase;
import com.diversityarrays.dal.server.ServerGui.SqlResultsPanel;
import com.diversityarrays.dal.sqldb.SqlUtil;

class SqlDialog extends JDialog {
	
	class SqlQueryTask extends BackgroundTask<TaskResult,Void> {
		
		private TaskResult taskResult = new TaskResult();
		private String sql;

		SqlQueryTask(String sql, String msg) {
			super(msg, false);
			this.sql = sql;
		}
		
		@Override
		public TaskResult generateResult(Closure<Void> publish) throws Exception {
			doit(sql, taskResult);
			return taskResult;
		}

		@Override
		public void onException(Throwable cause) {
			taskResult.error = cause;
			processTaskResult(taskResult);
		}

		@Override
		public void onTaskComplete(TaskResult tr) {
			processTaskResult(tr);
		}

		@Override
		public void onCancel(CancellationException ce) {
			taskResult.error = ce;
			processTaskResult(taskResult);
		}
		
	}
	
	private static final String CMD_SHOW = "show";

	private static final String CMD_DESCRIBE = "describe";

	private static final String CMD_TABLES = "tables";

	private final DefaultBackgroundRunner runner;
	
	private JTextPane sqlCommands = new JTextPane();
	
	private SqlDalDatabase database;

	private MessagesPanel messagesPanel = new MessagesPanel("Messages");
	
	private Action runAction = new AbstractAction("Run") {	
		@Override
		public void actionPerformed(ActionEvent e) {
			
			String sql = sqlCommands.getSelectedText();
			if (sql==null) {
				sql = sqlCommands.getText().trim();
			}
			
			if (sql.isEmpty()) {
				messagesPanel.println("Empty command");
			} else if ("help".equalsIgnoreCase(sql)) {
				showHelp();
			} else {
				SqlQueryTask task = new SqlQueryTask(sql, "Running...");
				runner.runBackgroundTask(task);
			}
		}
	};
	
	private Action closePanelAction = new AbstractAction("Close Result Panel") {	
		@Override
		public void actionPerformed(ActionEvent e) {
			int index = tabbedPane.getSelectedIndex();
			if (index>=0) {
				tabbedPane.remove(index);
			}
		}
	};
	
	private Action helpAction = new AbstractAction("Help") {	
		@Override
		public void actionPerformed(ActionEvent e) {
			showHelp();
		}
	};
	
	private JCheckBox includeHeadingsInCopy = new JCheckBox("Include Headings in Copy");
	
	private JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	
	SqlDialog(JFrame owner, SqlDalDatabase db) {
		super(owner, "SQL", ModalityType.MODELESS);
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		this.database = db;
		
		runner = new DefaultBackgroundRunner("SQL Command", this);
		setGlassPane(runner.getBlockingPane());
		
		sqlCommands.setFont(GuiUtil.createMonospacedFont(12));
		
		includeHeadingsInCopy.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean b = includeHeadingsInCopy.isSelected();
				for (int n = tabbedPane.getTabCount(); --n>=0; ) {
					Component c = tabbedPane.getComponentAt(n);
					if (c instanceof SqlResultsPanel) {
						((SqlResultsPanel) c).setIncludeHeadings(b);
					}
				}
			}
		});
		
		tabbedPane.addContainerListener(new ContainerListener() {
			@Override
			public void componentRemoved(ContainerEvent e) {
				updateClosePanelAction();
			}
			
			@Override
			public void componentAdded(ContainerEvent e) {
				updateClosePanelAction();
			}
		});
		updateClosePanelAction();
		
		Box buttons = Box.createHorizontalBox();
		buttons.add(Box.createHorizontalStrut(10));
		buttons.add(new JButton(runAction));
		buttons.add(Box.createHorizontalStrut(20));
		buttons.add(new JButton(closePanelAction));
		
		buttons.add(Box.createHorizontalGlue());
		buttons.add(includeHeadingsInCopy);
		buttons.add(Box.createHorizontalStrut(10));
		buttons.add(new JButton(helpAction));
		buttons.add(Box.createHorizontalStrut(10));
		
		JPanel top = new JPanel(new BorderLayout());
		top.add(BorderLayout.CENTER, new JScrollPane(sqlCommands));
		top.add(BorderLayout.SOUTH,  buttons);
		
		final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, tabbedPane);
		splitPane.setResizeWeight(0.25);
		
		setContentPane(splitPane);
		
		pack();
		
		setSize(800, 600);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				splitPane.setDividerLocation(0.25);
				removeWindowListener(this);
			}
			
		});
	}
	
	private void updateClosePanelAction() {
		closePanelAction.setEnabled(tabbedPane.getTabCount()>0);
	}
	
	private int sequence = 0;
	private void addPanelToTabbedPane(JComponent c) {
		String title = "Result-"+(++sequence);
		tabbedPane.addTab(title, c);
		int n = tabbedPane.getTabCount();
		if (n>0) {
			tabbedPane.setSelectedIndex(n-1);
		}
	}
	
	static private final String[] HELP_LINES = new String[] {
		"<head>",
		"<style>",
		"DL { margin-top: 0; }",
		"DT { font-weight: bold; }",
		"DD { margin-top: 10px; margin-left: 20px; }",
		"</style>",
		"</head><body>",
		"<h3>Commands</h3>",
		"<dl>",
		"<dt>"+CMD_TABLES+"</dt>",
		"<dd>lists the tables in the database</dd>",
		
		"<dt>"+CMD_DESCRIBE+" <i>tableName</i></dt>",
		"<dd>lists the columns in the specified table</dd>",
		
		"</dl>",
		"</body>"
	};

	private void showHelp() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println("<html>");

		for (String line : HELP_LINES) {
			pw.println(line);
		}
		pw.println("</html>");
		pw.close();
		
		JTextPane helpText = new JTextPane();
		helpText.setEditorKit(new HTMLEditorKit());
		helpText.setText(sw.toString());
		helpText.setEditable(false);
		 
		addPanelToTabbedPane(new JScrollPane(helpText));
	}
	
	class TaskResult {

		Throwable error = null;
		String whyError = null;
		public boolean isQuery;
		public int sqlResult;
		public String sql;
		List<String> headings = new ArrayList<String>();
		List<String[]>rows = new ArrayList<String[]>();
		
		public void showQueryResult() {
			switch (sqlResult) {
			case -1:
				messagesPanel.println("No result rows");
				JOptionPane.showMessageDialog(SqlDialog.this, sql, "No result rows", JOptionPane.INFORMATION_MESSAGE);
				break;
			case 0:
				messagesPanel.println("No columns in results");
				JOptionPane.showMessageDialog(SqlDialog.this, sql, "No columns in results", JOptionPane.INFORMATION_MESSAGE);
				break;
			default:
				messagesPanel.println(headings.size()+" columns in "+rows.size()+" rows");
				String[] columnHeadings = headings.toArray(new String[headings.size()]);
				SqlResultsPanel panel = new SqlResultsPanel(includeHeadingsInCopy.isSelected(), sql, columnHeadings, rows);
				addPanelToTabbedPane(panel);
				break;
			}
		}

		public void showUpdateResult() {
			JPanel panel = new JPanel(new BorderLayout());
			JTextArea sqlText = new JTextArea(sql);
			sqlText.setEditable(false);
			panel.add(BorderLayout.CENTER, new JScrollPane(sqlText));
			panel.add(BorderLayout.SOUTH, new JLabel(sqlResult+" result(s) from update"));
			addPanelToTabbedPane(panel);
		}
	}
	
	private void doit(String initialSql, TaskResult taskResult) {
		
		String sql = initialSql;
		
		if (sql!=null && ! sql.trim().isEmpty()) {
			
			if (CMD_TABLES.equalsIgnoreCase(sql)) {
				sql = database.createShowTablesSql();
			}
			else {
				String[] parts = sql.toLowerCase().split("\\s+", 3);
				if (parts.length==2 && ! parts[1].isEmpty()) {
					if (CMD_DESCRIBE.indexOf(parts[0])==0 ) {
						sql = database.createShowTableColumnsSql(parts[1]);
					}
					else if (CMD_SHOW.equalsIgnoreCase(parts[0])) {
						if (CMD_TABLES.equalsIgnoreCase(parts[1])) {
							sql = database.createShowTablesSql();
						}
					}
				}
			}
			
			taskResult.sql = sql;
			
			String losql = sql.toLowerCase().replaceFirst("^\\s*", "");

			
			Connection conn = null;
			Statement stmt = null;
			ResultSet rs = null;
			try {
				conn = database.getConnection(false);

				stmt = conn.createStatement();

				if (losql.startsWith("select")) {
					taskResult.isQuery = true;
					try {
						rs = stmt.executeQuery(sql);
						
						taskResult.headings = new ArrayList<String>();
						taskResult.rows = new ArrayList<String[]>();
						
						taskResult.sqlResult = doSqlQuery(rs, taskResult.headings, taskResult.rows);
					}
					catch (SQLException queryFailed) {
						taskResult.whyError = "SQL Query failed";
						taskResult.error = queryFailed;
					}
				}
				else {
					taskResult.isQuery = false;
					try {
						taskResult.sqlResult = stmt.executeUpdate(sql);
					}
					catch (SQLException updateFailed) {
						taskResult.whyError = "SQL Update failed";
						taskResult.error = updateFailed;
					}
				}
			} catch (SQLException setupFailed) {
				taskResult.whyError = "SQL Setup failed";
				taskResult.error = setupFailed;
			} catch (DalDbException e) {
				taskResult.whyError = "Unable to establlish Connection";
				taskResult.error = e;
			}
			finally {
				SqlUtil.closeSandRS(stmt, rs);
				if (conn!=null) {
					try { conn.close(); } catch (SQLException ignore) {}
				}
			}
		}
	}
	
	private void processTaskResult(TaskResult taskResult) {
		if (taskResult.error==null) {
			if (taskResult.isQuery) {
				taskResult.showQueryResult();
			}
			else {
				taskResult.showUpdateResult();
			}
		}
		else {
			StringWriter ew = new StringWriter();
			PrintWriter  ep = new PrintWriter(ew);
			taskResult.error.printStackTrace(ep);
			ep.close();
			JPanel panel = new JPanel(new BorderLayout());

			panel.add(BorderLayout.NORTH, GuiUtil.createLabelSeparator(taskResult.whyError));
			
			
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);

			pw.println("<html><body><h3>SQL:</h3><pre>");
			pw.println(DbUtil.htmlEscape(taskResult.sql));
			pw.println("</pre><hr/>");
			pw.println("<h3>Stacktrace</h3><pre>");
			pw.print(DbUtil.htmlEscape(ew.toString()));
			pw.println("</pre>");

			pw.println("</body></html>");
			pw.close();
			
			JTextPane text = new JTextPane();
			text.setEditorKit(new HTMLEditorKit());
			text.setEditable(false);;
			text.setText(sw.toString());;
			panel.add(BorderLayout.CENTER, new JScrollPane(text));
			
			addPanelToTabbedPane(panel);
		}
	}

	public int doSqlQuery(ResultSet rs, List<String> headings, List<String[]> rows) throws SQLException {
		int nColumns = -1;
		
		while (rs.next()) {
			if (nColumns<0) {
				ResultSetMetaData rsmd = rs.getMetaData();
				nColumns = rsmd.getColumnCount();

				for (int i = 1; i <= nColumns; ++i) {
					String hdg = rsmd.getColumnLabel(i);
					headings.add(hdg);
				}
			}
			
			String[] values = new String[nColumns];
			rows.add(values);
			for (int i = 1; i <= nColumns; ++i) {
				values[i-1] = rs.getString(i);
			}
		}
		
		return nColumns;
	}
}