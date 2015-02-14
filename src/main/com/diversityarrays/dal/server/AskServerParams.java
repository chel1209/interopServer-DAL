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
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.prefs.BackingStoreException;

import javax.imageio.spi.ServiceRegistry;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.html.HTMLEditorKit;

import net.pearcan.ui.GuiUtil;
import net.pearcan.ui.widget.NumberSpinner;
import net.pearcan.util.GBH;

import org.apache.commons.collections15.Closure;
import org.apache.commons.collections15.Factory;

import com.diversityarrays.dal.db.DalDatabase;
import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.DbUtil;
import com.diversityarrays.dal.service.DalDbProviderService;
import com.diversityarrays.dal.service.Parameter;
import com.diversityarrays.dal.service.ParameterException;
import com.diversityarrays.dal.service.ParameterValue;
import com.diversityarrays.dal.service.Parameters;

/**
 * User interface to query the user for the parameters of a DalDatabase.
 * @author brian
 *
 */
public class AskServerParams extends JDialog {
	
	private static final boolean SHOW_STACKTRACE = Boolean.getBoolean(AskServerParams.class.getSimpleName()+".SHOW_STACKTRACE");
	
	class DalDatabaseCreatorDialog extends JDialog {

		private boolean running;
		public DalDatabase database;
		private JTextPane textpane = new JTextPane();
		private JScrollPane scrollPane = new JScrollPane(textpane);
		
		private Action closeAction = new AbstractAction("Close") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (running) {
					worker.cancel(true);
				}
				else {
					dispose();
				}
			}
		};
		private JLabel message = new JLabel();
		
		private SwingWorker<DalDatabase,String> worker;
		private HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
		
		public DalDatabaseCreatorDialog(Window owner, 
				final ProviderPanel providerPanel,
				final Set<ParameterValue<?>> paramValues,
				final boolean forTest) 
		{
			super(owner, "Creating DalDatabase: " + providerPanel.service.getProviderName(), ModalityType.APPLICATION_MODAL);
			
			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			closeAction.setEnabled(false);
			
			textpane.setEditorKit(htmlEditorKit);
			textpane.setEditable(false);
			
			worker = new SwingWorker<DalDatabase, String>() {
				
				Closure<String> progress = new Closure<String>() {
					@Override
					public void execute(String s) {
						System.out.println("progress: "+s);
						publish(s);
					}
				};
				
				long startMillis;
				@Override
				protected DalDatabase doInBackground() throws Exception {
					startMillis = System.currentTimeMillis();
					publish("Creating database ...");
					DalDatabase result = providerPanel.service.createDatabase(paramValues, progress, true);
					long elapsed = System.currentTimeMillis() - startMillis;
					publish("Elapsed: "+elapsed+" ms");
					if (! forTest) {
						long sleepMillis = 2000 - elapsed;
						if (sleepMillis > 0) {
							try {
								Thread.sleep(sleepMillis);
							}
							catch (InterruptedException ignore) {}
						}
					}
					
					return result;
				}

				@Override
				protected void process(List<String> chunks) {
					for (String l : chunks) {
						System.out.println("progress: " + l);
						if (! l.isEmpty()) {
							// TODO make this work !
							textpane.setText(textpane.getText()+"<BR>"+DbUtil.htmlEscape(l));
//							System.err.println("---- " + (++n) + " ------\n" + textpane.getText());
						}
					}
				}

				@Override
				protected void done() {
					Throwable error = null;
					try {
						database = get();
					} catch (InterruptedException e) {
						error = e;
					} catch (ExecutionException e) {
						error = e.getCause();
					} catch (CancellationException e) {
						error = e;
					}
					stopRunning(forTest, error);
				}
				
			};
			
			StringBuilder sb = new StringBuilder("<HTML>");
			sb.append("Creating <B>").append(providerPanel.service.getProviderName()).append("</B>");
			sb.append("<HR>").append(providerPanel.service.getHtmlDescription())
				.append("<BR>Please wait ...");
			
			textpane.setText(sb.toString());
			
			Box bot = Box.createHorizontalBox();
			bot.add(new JButton(closeAction));
			bot.add(message);
			bot.add(Box.createHorizontalGlue());
			
			getContentPane().add(scrollPane, BorderLayout.CENTER);
			getContentPane().add(bot, BorderLayout.SOUTH);
			pack();
			
			setSize(400, 200);

			GuiUtil.centreOnOwner(DalDatabaseCreatorDialog.this);

			addWindowListener(new WindowAdapter() {
				@Override
				public void windowOpened(WindowEvent e) {
					running = true;
					closeAction.putValue(Action.NAME, "Cancel");
					closeAction.setEnabled(true);
					worker.execute();
				}

				@Override
				public void windowClosing(WindowEvent e) {
					if (running) {
						worker.cancel(true);
					}
					else {
						dispose();
					}
				}
			});
		}
		
		private void stopRunning(boolean forTest, Throwable error) {
			
			running = false;
			closeAction.putValue(Action.NAME, "Close");
			
			if (error != null) {
				
				StringBuilder sb = new StringBuilder("<HTML>");
				
				if (error instanceof CancellationException) {
					sb.append("Cancelled by user request");
				}
				else {
					sb.append("<B>Error encountered:</B>");
					if (SHOW_STACKTRACE) {
						error.printStackTrace();
						
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						error.printStackTrace(pw);
						pw.close();
						
						sb.append("<HR>");
						for (String line : sw.toString().split("\n", 0)) {
							sb.append("<BR>").append(DbUtil.htmlEscape(line));
						}
					}
					else {
						sb.append(' ').append(DbUtil.htmlEscape(error.getMessage()));
					}
				}

				textpane.setForeground(Color.RED);
				textpane.setText(sb.toString());
				
				textpane.setCaretPosition(0);
			}
			else {
				if (forTest) {
					textpane.setText(textpane.getText() + "<BR>Database Test Completed.");
					message.setText("Success !");
				}
				else {
					dispose();
				}
			}
		}
	}
	
	private JComboBox<String> hostnameChoice;
    private SpinnerNumberModel dalServerPortModel = new SpinnerNumberModel(DalServerUtil.DEFAULT_DAL_SERVER_PORT, 1001, 65535, 10);
    private NumberSpinner dalServerPortSpinner = new NumberSpinner(dalServerPortModel, "0");
	
	private SpinnerNumberModel maxInactiveMinutesModel = new SpinnerNumberModel(DalServerUtil.DEFAULT_MAX_INACTIVE_MINUTES, 1, Integer.MAX_VALUE, 5);
	private NumberSpinner maxInactiveMinutesSpinner = new NumberSpinner(maxInactiveMinutesModel, "0");
	
	private JFileChooser fileChooser = null;
	
	public String dalServerHostName;
	public Integer dalServerPort;
	
	public Integer maxInactiveMinutes;
	public DalDatabase dalDatabase;
	public boolean cancelled = true;
	
	public File wwwRoot;
	
	
	private GBH.Anchor labelAnchor = GBH.EAST;
	
	private JButton okButton = new JButton(new AbstractAction("Start Server") {
		@Override
		public void actionPerformed(ActionEvent e) {
			
			String err = null;
			String rpath = wwwRootPath.getText().trim();
			File wroot = null;
			if (rpath.isEmpty()) {
				err = "No WWW Root path";
			}
			else {
				wroot = new File(rpath);
				if (wroot.isDirectory()) {
					wwwRoot = wroot;
				}
				else {
					err = "Not a directory:\n" + wroot.getPath();
				}
			}
			if (err!=null) {
				JOptionPane.showMessageDialog(AskServerParams.this, err, getTitle(), JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			dalServerHostName = hostnameChoice.getSelectedItem().toString(); // dalServerHostNameField.getText();
			dalServerPort = dalServerPortModel.getNumber().intValue();
			maxInactiveMinutes = maxInactiveMinutesModel.getNumber().intValue();
			
			preferences.setWebRoot(wwwRoot);
			preferences.setServerHostName(dalServerHostName);
			preferences.setServerPort(dalServerPort);
			
			preferences.save();

			ProviderPanel pp = (ProviderPanel) factoryTabbedPane.getSelectedComponent();
			List<String> errors = new ArrayList<String>();
			Set<ParameterValue<?>> paramValues = pp.getParameterValues(errors);
			
			if (! errors.isEmpty()) {
				StringBuilder sb = new StringBuilder("Error(s):");
				for (String er : errors) {
					sb.append("\n").append(er);
				}
				GuiUtil.errorMessage(AskServerParams.this, sb.toString(), "Unable to create DalDatabase");
			} else {
				dalDatabase = createDalDatabase(pp, paramValues, false);
				if (dalDatabase != null) {
					cancelled = false;
					dispose();
				}
			}
		}
	});

	private JButton cancelButton = new JButton(new AbstractAction("Exit") {
		@Override
		public void actionPerformed(ActionEvent e) {
			cancelled = true;
			dispose();
		}
	});

	private JTextField wwwRootPath = new JTextField();
	private Action browseWwwRoot = new AbstractAction("Choose...") {
		@Override
		public void actionPerformed(ActionEvent e) {

			String userDir = System.getProperty("user.dir");
			File wwwHome = new File(userDir, "www");
			
			ensureFileChooser(wwwRootPath, wwwHome);
			
			if (JFileChooser.APPROVE_OPTION==fileChooser.showDialog(AskServerParams.this, "Choose")) {
				wwwRootPath.setText(fileChooser.getSelectedFile().getPath());
			}
		}
		
	};
	
	private JTabbedPane factoryTabbedPane = new JTabbedPane();
	
	private JFileChooser propertiesFileChooser;
	
	static private FileFilter PROPERTIES_FILE_FILTER = new FileFilter() {
		@Override
		public String getDescription() {
			return ".properties files";
		}
		
		@Override
		public boolean accept(File f) {
			return f.isDirectory() || f.getName().endsWith(".properties");
		}
	};
	
	class ProviderPanel extends JPanel {
		
		protected static final boolean MESSAGE = false;
		protected static final boolean ERROR   = true;

		private Action testCreateAction = new AbstractAction("Test") {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				message.setText("Testing...");
				List<String> errors = new ArrayList<String>();
				Set<ParameterValue<?>> paramValues = getParameterValues(errors);
				
				boolean ok = false;
				if (errors.isEmpty()) {
					DalDatabase db = createDalDatabase(ProviderPanel.this, paramValues, true);
					if (db != null) {
						ok = true;
						try {
							db.shutdown();
						} catch (DalDbException er) {
							er.printStackTrace();
						}
					}
				}
				else {
					StringBuilder sb = new StringBuilder("Error(s):");
					for (String er : errors) {
						sb.append("\n").append(er);
					}
					GuiUtil.errorMessage(AskServerParams.this, sb.toString(), "Unable to create DalDatabase");
				}
				
				message.setText(ok ? "Tested OK" : "");
			}
		};
		
		private Action saveAsPropertiesAction = new AbstractAction("Save as Properties file") {

			File propertiesFile;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (propertiesFileChooser==null) {
					propertiesFileChooser = new JFileChooser();
					propertiesFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					propertiesFileChooser.setAcceptAllFileFilterUsed(true);
					propertiesFileChooser.setFileFilter(PROPERTIES_FILE_FILTER);
				}
				
				propertiesFileChooser.setSelectedFile(propertiesFile);
				
				if (JFileChooser.APPROVE_OPTION != propertiesFileChooser.showSaveDialog(ProviderPanel.this)) {
					return;
				}
				
				
				propertiesFile = propertiesFileChooser.getSelectedFile();

				boolean ok = false;
				
				List<String> errors = new ArrayList<String>();
				Set<ParameterValue<?>> values = getParameterValues(errors);
				
				StringBuilder warnings = null;
				if (! errors.isEmpty()) {
					warnings = new StringBuilder("<HTML><UL>");
					for (String s : errors) {
						warnings.append("<LI>").append(DbUtil.htmlEscape(s)).append("</LI>");
					}
					warnings.append("</UL>");
				}
				
				if (! values.isEmpty()) {
					OutputStream os = null;
					try {
						os = new FileOutputStream(propertiesFile);
						DalServerPreferences.saveAsPropertiesFile(service, values, os);
						ok = true;
					} catch (IOException er) {
						message.setText("");
						JOptionPane.showMessageDialog(ProviderPanel.this, er, "Can't Save", JOptionPane.ERROR_MESSAGE);
					} finally {
						if (os != null) {
							try { os.close(); }
							catch (IOException ignore) {}
						}
					}
				}

				if (warnings != null) {
					JOptionPane.showMessageDialog(ProviderPanel.this, warnings.toString(), "Parameter Problems", JOptionPane.WARNING_MESSAGE);	
				}
				
				message.setText(ok ? "Settings Saved" : "");
			}
		};
		
		private Action saveSettingsAction = new AbstractAction("Save Settings") {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				message.setText("Saving...");

				boolean ok = false;
				
				List<String> errors = new ArrayList<String>();
				Set<ParameterValue<?>> values = getParameterValues(errors);
				
				StringBuilder warnings = null;
				if (! errors.isEmpty()) {
					warnings = new StringBuilder("<HTML><UL>");
					for (String s : errors) {
						warnings.append("<LI>").append(DbUtil.htmlEscape(s)).append("</LI>");
					}
					warnings.append("</UL>");
				}
				
				if (! values.isEmpty()) {
					try {
						preferences.saveServiceSettings(service, values);
						ok = true;
					} catch (BackingStoreException e1) {
						message.setText("");
						JOptionPane.showMessageDialog(ProviderPanel.this, e1, "Can't Save", JOptionPane.ERROR_MESSAGE);
					}
				}

				if (warnings != null) {
					JOptionPane.showMessageDialog(ProviderPanel.this, warnings.toString(), "Parameter Problems", JOptionPane.WARNING_MESSAGE);	
				}
				
				message.setText(ok ? "Settings Saved" : "");
			}
		};
		
		private DalDbProviderService service;
		
		private Map<Parameter<?>,Factory<String>> factoryByParameter = new LinkedHashMap<Parameter<?>,Factory<String>>();
		
		private JLabel message = new JLabel(" ");

		ProviderPanel(DalDbProviderService srvce) {
			super(new BorderLayout());
			
			this.service = srvce;
			
			message.setForeground(Color.GRAY);
			
			maxInactiveMinutesSpinner.setToolTipText("Minutes before client sessions are automatically logged out");

			Map<Parameter<?>, ParameterValue<?>> defaults = new HashMap<Parameter<?>, ParameterValue<?>>();
			
			// Replace values from 

			List<ParameterValue<?>> defaultsFromService = service.getDefaultParameterValues();
			if (defaultsFromService != null) {
				for (ParameterValue<?> pv : defaultsFromService) {
					defaults.put(pv.parameter, pv);
				}
			}
			
			Set<Parameter<?>> parametersRequired = service.getParametersRequired();
			
			Map<Parameter<?>, Throwable> errors = new HashMap<Parameter<?>, Throwable>();

			// Replace defaults from service with saved settings
			Map<Parameter<?>,ParameterValue<?>> saved = preferences.loadSavedSettings(service, parametersRequired, errors);
			for (Parameter<?> p : parametersRequired) {
				ParameterValue<?> pv = saved.get(p);
				if (pv != null) {
					defaults.put(p, pv);
				}
				else if (defaults.containsKey(p)) {
					// No saved value, but if in defaults then don't report it
					errors.remove(p);
				}
			}
			
			if (! errors.isEmpty()) {
				System.err.println("ProviderPanel: errors while loading parameters for " + service.getProviderName());
				for (Parameter<?> p : errors.keySet()) {
					System.err.println(p.name + ": " + errors.get(p).getMessage());
				}
			}
			
			JPanel ppanel = new JPanel();
			GBH gbh = new GBH(ppanel);
			int y = 0;
			
			for (final Parameter<?> param : parametersRequired) {
				if (String.class==param.valueClass) {
					addStringParameterControls(gbh, y, param, defaults);
				}
				else if (Integer.class==param.valueClass) {
					addIntegerParameterControls(gbh, y, param, defaults);
				}
				else if (File.class==param.valueClass) {
					addFileParameterControls(gbh, y, param, defaults);
				}
				else if (URI.class == param.valueClass) {
					addUriParameterControls(gbh, y, param, defaults);
				}
				else if (Boolean.class == param.valueClass) {
					addBooleanParameterControls(gbh, y, param, defaults);
				}
				else {
					JLabel error = new JLabel("Unsupported parameter class: "+param.valueClass.getName());
					error.setForeground(Color.RED);
					gbh.add(0,y, 2,1, GBH.HORZ, 1,0, GBH.CENTER, error);
				}
				++y;
			}
			
			Box box = Box.createHorizontalBox();
			if (service.getSupportsTest()) {
				box.add(new JButton(testCreateAction));
			}
			box.add(message);
			box.add(Box.createHorizontalGlue());
			box.add(new JButton(saveSettingsAction));
			box.add(new JButton(saveAsPropertiesAction));
			gbh.add(0,y, 2,1, GBH.HORZ, 1,1, GBH.WEST, box);
			++y;
			
			JLabel label = new JLabel("<HTML>"+service.getHtmlDescription()+"<HR>");
			label.setFont(label.getFont().deriveFont(Font.BOLD));
			label.setBackground(Color.LIGHT_GRAY);
			
			add(label, BorderLayout.NORTH);
			add(ppanel, BorderLayout.CENTER);
			
			pack();
		}	
	
		private void addStringParameterControls(GBH gbh, int y, final Parameter<?> param, Map<Parameter<?>, ParameterValue<?>> defaults) {
			final JTextField tf = new JTextField();
			tf.setToolTipText(param.description);
			@SuppressWarnings("unchecked")
			String s = ParameterValue.getValue((Parameter<String>) param, defaults);
			if (s != null) {
				tf.setText(s);
			}
			
			gbh.add(0,y, 1,1, GBH.NONE, 0,0, labelAnchor, createParamLabel(param));
			gbh.add(1,y, 1,1, GBH.HORZ, 1,1, GBH.CENTER, tf);
				
			factoryByParameter.put(param, new Factory<String>() {
				@Override
				public String create() {
					return tf.getText().trim();
				}
			});
		}

		private void addBooleanParameterControls(GBH gbh, int y, final Parameter<?> param, Map<Parameter<?>, ParameterValue<?>> defaults) 
		{
			final JCheckBox checkBox = new JCheckBox(param.name);
			checkBox.setToolTipText(param.description);

			@SuppressWarnings("unchecked")
			Boolean b = ParameterValue.getValue((Parameter<Boolean>) param, defaults);
			if (b != null) {
				checkBox.setSelected(b);
			}
			
			gbh.add(0,y, 2,1, GBH.NONE, 1,1, GBH.WEST, checkBox);
			
			factoryByParameter.put(param, new Factory<String>() {
				@Override
				public String create() {
					return Boolean.toString(checkBox.isSelected());
				}
			});
		}

		private void addIntegerParameterControls(GBH gbh, int y, final Parameter<?> param, Map<Parameter<?>, ParameterValue<?>> defaults) {
			final SpinnerNumberModel model = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);

			NumberSpinner numberSpinner = new NumberSpinner(model, "0");
			numberSpinner.setToolTipText(param.description);
			
			@SuppressWarnings("unchecked")
			Integer i = ParameterValue.getValue((Parameter<Integer>) param, defaults);
			if (i != null) {
				model.setValue(i);
			}

			gbh.add(0,y, 1,1, GBH.NONE, 0,0, labelAnchor, createParamLabel(param));
			gbh.add(1,y, 1,1, GBH.NONE, 1,1, GBH.WEST, numberSpinner);
			
			factoryByParameter.put(param, new Factory<String>() {
				@Override
				public String create() {
					return model.getNumber().toString();
				}
			});
		}

		private void addUriParameterControls(GBH gbh, int y, final Parameter<?> param, Map<Parameter<?>, ParameterValue<?>> defaults) {
			final JTextField path = new JTextField();
			path.setToolTipText(param.description);
			
			@SuppressWarnings("unchecked")
			URI u = ParameterValue.getValue((Parameter<URI>) param, defaults);
			if (u != null) {
				path.setText(u.toString());
			}
			
			gbh.add(0,y, 1,1, GBH.NONE, 0,0, labelAnchor, createParamLabel(param));
			gbh.add(1,y, 1,1, GBH.HORZ, 1,1, GBH.CENTER, path);
			
			factoryByParameter.put(param, new Factory<String>() {
				@Override
				public String create() {
					return path.getText().trim();
				}
			});
		}

		private void addFileParameterControls(GBH gbh, int y, final Parameter<?> param, Map<Parameter<?>, ParameterValue<?>> defaults) 
		{
			final JTextField path = new JTextField();
			path.setToolTipText(param.description);
			
			@SuppressWarnings("unchecked")
			String s = ParameterValue.getValue((Parameter<String>) param, defaults);
			if (s != null) {
				path.setText(s);
			}
			
			Action action = new AbstractAction("Choose...") {
				JFileChooser chooser;
				@Override
				public void actionPerformed(ActionEvent e) {
					if (chooser == null) {
						chooser = new JFileChooser();
						chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					}
					
					if (JFileChooser.APPROVE_OPTION==chooser.showOpenDialog(AskServerParams.this)) {
						path.setText(chooser.getSelectedFile().getPath());
					}
				}
			};
			Box box = Box.createHorizontalBox();
			box.add(path);
			box.add(new JButton(action));

			gbh.add(0,y, 1,1, GBH.NONE, 0,0, labelAnchor, createParamLabel(param));
			gbh.add(1,y, 1,1, GBH.HORZ, 1,1, GBH.CENTER, box);
			
			factoryByParameter.put(param, new Factory<String>() {
				@Override
				public String create() {
					return path.getText().trim();
				}
			});
		}
		
		private String createParamLabel(Parameter<?> param) {
			if (param.required) {
				return "<HTML>" + DbUtil.htmlEscape(param.name) + ": <b><span style='color:#ff0000;'>*</span></b>";
			}
			else {
				return param.name + ":";
			}
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public Set<ParameterValue<?>> getParameterValues(List<String> errors) {
			
			Set<ParameterValue<?>> result = new LinkedHashSet<ParameterValue<?>>();
			for (Parameter<?> param : factoryByParameter.keySet()) {
				Object value;
				Throwable err = null;
				if (Parameters.HOST_NAME==param) {
					String s = hostnameChoice.getSelectedItem().toString(); 
					if (s.isEmpty()) {
						value = null;
					}
					else {
						value = s;
					}
				}
				else if (Parameters.HOST_PORT==param) {
					value = dalServerPortModel.getNumber().intValue();
				}
				else {
					try {
						String valueString = factoryByParameter.get(param).create();
						value = param.stringToValue(valueString);
					}
					catch (ParameterException e) {
						err = e;
						value = null;
					}
				}
				
				if (err != null) {
					errors.add(err.getMessage());
				}
				else {
					result.add(new ParameterValue(param, value));
					if (param.required) {
						if (value == null) {
							errors.add("No value has been set for "+param.name);
						}
						else if (param.valueClass==String.class && value.toString().isEmpty()) {
							errors.add("No value has been set for "+param.name);
						}
					}
				}
			}
			return result;
		}
	}
	
	private JLabel hostnameInfo = new JLabel(
            "<html><i>Hostname</i> of 'localhost'<br/>"+
                      "means you can only access<br/>"+
                      "from this computer") ;
	private DalServerPreferences preferences;
	
	public AskServerParams(Image serverIconImage, final Window owner, String title, File www, DalServerPreferences prefs) {
		super(owner, title, ModalityType.APPLICATION_MODAL);

		this.wwwRoot = www;
		this.preferences = prefs;
		
		if (serverIconImage != null) {
			setIconImage(serverIconImage);
		}
		
		
		Iterator<DalDbProviderService> iter = ServiceRegistry.lookupProviders(DalDbProviderService.class);
		while (iter.hasNext()) {
			DalDbProviderService s = iter.next();
			factoryTabbedPane.addTab(s.getProviderName(), new ProviderPanel(s));
		}
		
		List<String> hostnames = DalServerUtil.collectHostnamesForChoice(true);
		hostnameChoice = new JComboBox<String>(hostnames.toArray(new String[hostnames.size()]));

		wwwRootPath.setText(wwwRoot.getPath());

		Box buttons = Box.createHorizontalBox();
		buttons.add(Box.createHorizontalStrut(10));
		buttons.add(okButton);
		buttons.add(Box.createHorizontalGlue());
		buttons.add(cancelButton);
		buttons.add(Box.createHorizontalStrut(10));
		
		getContentPane().add(BorderLayout.NORTH, initGui());
		getContentPane().add(BorderLayout.SOUTH, buttons);
		pack();
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				removeWindowListener(this);
				if (owner!=null) {
					GuiUtil.centreOnOwner(AskServerParams.this);
				}
				else {
					GuiUtil.centreOnScreen(AskServerParams.this);
				}
				okButton.requestFocus(); // unless it is disabled !
			}
		});
	}



	public JPanel initGui() {
		JPanel p = new JPanel();
		
		GBH gbh = new GBH(p, 2, 2, 2, 2);
		int y = 0;
		
		gbh.add(0,y, 3,1, GBH.HORZ, 1,0, GBH.CENTER, GuiUtil.createLabelSeparator("WWW Root"));
		++y;
		
		gbh.add(0,y, 1,1, GBH.NONE, 1,0, GBH.EAST, "Web root directory:");
		gbh.add(1,y, 1,1, GBH.HORZ, 1,0, GBH.CENTER, wwwRootPath);
		gbh.add(2,y, 1,1, GBH.NONE, 1,0, GBH.WEST, new JButton(browseWwwRoot));
		++y;

		// ========================
		
		gbh.add(0,y, 3,1, GBH.HORZ, 1,0, GBH.CENTER, GuiUtil.createLabelSeparator("DAL Server"));
		++y;
		
		gbh.add(0,y, 1,1, GBH.NONE, 1,0, GBH.EAST, "DAL Server Hostname:");
		gbh.add(1,y, 1,1, GBH.HORZ, 1,0, GBH.CENTER, hostnameChoice);
		gbh.add(2,y, 1,2, GBH.NONE, 1,0, GBH.WEST, hostnameInfo);
		++y;
		
		gbh.add(0,y, 1,1, GBH.NONE, 1,0, GBH.EAST,   "DAL Server Port:");
		gbh.add(1,y, 1,1, GBH.HORZ, 1,0, GBH.WEST,   dalServerPortSpinner);
		
		++y;
		gbh.add(0,y, 1,1, GBH.NONE, 1,0, GBH.EAST, "Auto Expiry Minutes:");
		gbh.add(1,y, 1,1, GBH.HORZ, 1,0, GBH.WEST, maxInactiveMinutesSpinner);
		++y;

		gbh.add(0,y, 3,1, GBH.HORZ, 1,0, GBH.CENTER, GuiUtil.createLabelSeparator("Select and configure DAL Database:"));
		++y;
		
		if (factoryTabbedPane.getTabCount() <= 0) {
			okButton.setEnabled(false);
			JLabel error = new JLabel("<HTML>No DalDbProviderService instances are available"+
					"<BR>Please check your CLASSPATH");
			error.setForeground(Color.RED);
			gbh.add(0,y, 3,1, GBH.BOTH, 1,1, GBH.CENTER, error);
			++y;
		}
		else {
			gbh.add(0,y, 3,1, GBH.BOTH, 1,1, GBH.CENTER, factoryTabbedPane);
			++y;
		}
		return p;
	}
	

	private void ensureFileChooser(JTextField path, File initCurrentDir) {
		String tmp = path.getText().trim();
		if (fileChooser==null) {
			if (tmp!=null) {
				fileChooser = new JFileChooser(new File(tmp));
			}
			else {
				fileChooser = new JFileChooser(initCurrentDir);
			}
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		else {
			if (tmp!=null) {
				fileChooser.setCurrentDirectory(new File(tmp));
			}
		}
	}



	public DalDatabase createDalDatabase(ProviderPanel pp, Set<ParameterValue<?>> paramValues, boolean forTest) {
		DalDatabaseCreatorDialog cd = new DalDatabaseCreatorDialog(AskServerParams.this, pp, paramValues, forTest);
		cd.setVisible(true);
		
		DalDatabase db = cd.database;
		return db;
	}

}