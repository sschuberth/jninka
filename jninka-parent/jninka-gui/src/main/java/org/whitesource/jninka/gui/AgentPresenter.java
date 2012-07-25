/**
 *  Copyright (C) 2012 White Source (www.whitesourcesoftware.com)
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This patch is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this patch.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.whitesource.jninka.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.whitesource.jninka.JNinka;
import org.whitesource.jninka.model.ScanResults;
import org.whitesource.jninka.progress.ScanProgressListener;
import org.whitesource.jninka.update.JNinkaUpdateClient;

/**
 * Main frame of the JNinka GUI application.
 * 
 * @author Rami.Sass
 * @author Edo.Shor
 */
public class AgentPresenter extends Container implements ActionListener, PropertyChangeListener {

	/* --- Static members --- */

	private static final String BROWSE = "Browse";
	
	private static final String RUN = "Run";
	
	private static final Color BG_COLOR = new Color(240, 240, 240);
	
	private static final Logger log = Logger.getLogger(AgentPresenter.class.getName());
	
	private static final long serialVersionUID = -8058069229069729705L;

	/* --- Members --- */

	private JFrame frame;
	
	private JFrame scanFrame;
	
	private JTextField dirText;
	
	private JButton directoryBrowseButton;
	
	private JFileChooser directoryDialog;
	
	private JTextField fileText;
	
	private JButton fileBrowseButton;
	
	private JFileChooser fileDialog;
	
	private String lastDir;
	
	private String lastFile;
	
	private JCheckBox sureMatchChk;
	
	private JButton runButton;
	
	private JProgressBar progressBar;
	
	private DefaultTableModel resultsModel;

	private String version;
	
	/* --- Constructors --- */
	
	/**
	 * Default constructor
	 */
	public AgentPresenter() {
		directoryBrowseButton = null;
		lastDir = null;
		lastFile = null;
		resultsModel = new DefaultTableModel();
	}

	/* --- Public methods --- */

	public void show() {
		// Create and set up the window.
		frame = new JFrame(" White Source JNinka Scanner - v" + version);
		frame.setResizable(false);
		frame.setLocation(300, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel pane = (JPanel) frame.getContentPane();
		pane.setBackground(BG_COLOR);
		GridLayout layout = new GridLayout(9, 0);
		pane.setLayout(layout);

		pane.add(getInfoPanel());
		// Oddly - spaces seem to resolve sizing issue...
		JLabel dirLabel = new JLabel("Project root directory                                                                                                             ");
		pane.add(dirLabel);
		pane.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel dirPanel = getDirectoryPanel();
		pane.add(dirPanel);

		JLabel fileLabel = new JLabel("Save to XML file");
		pane.add(fileLabel);

		JPanel filePanel = getFilePanel();
		pane.add(filePanel);
		
		JPanel originalsPanel = getUnknownsPanel();
		pane.add(originalsPanel);
		pane.add(getRunPanel());

		pane.add(getLinkButton("Load Scan Results", "http://www.whitesourcesoftware.com?ref=scan"));
		pane.add(getCreditPanel());
		
		directoryDialog = getDirectoryChooser();
		fileDialog = getFileChooser();

		// Display the window.
		frame.pack();
		frame.setVisible(true);
		
		scanFrame = getScanningFrame();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName()) {
			int progress = (Integer) evt.getNewValue();
			progressBar.setValue(progress);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			if (e.getSource() == directoryBrowseButton) {
				if (frame != null) {
					int returnValue = directoryDialog.showOpenDialog(frame);

					if (returnValue == JFileChooser.APPROVE_OPTION) {
						File directory = directoryDialog.getSelectedFile();
						lastDir = directory.getAbsolutePath();
						dirText.setText(lastDir);
						if (fileText.getText().isEmpty()) {
							lastFile = lastDir + "\\jninka.xml";
							fileText.setText(lastFile);
						}
					}
				}
			}
			if (e.getSource() == fileBrowseButton) {
				if (frame != null) {
					int returnValue = fileDialog.showOpenDialog(frame);

					if (returnValue == JFileChooser.APPROVE_OPTION) {
						File file = fileDialog.getSelectedFile();
						if (file != null) {
							lastFile = file.getAbsolutePath();
							log.info("Save: " + file.getAbsolutePath());
						}
					}
				}
			}
			if (e.getSource() == runButton) {
				if (dirText.getText().isEmpty() || fileText.getText().isEmpty()) {
					JOptionPane.showConfirmDialog(getParent(), "Please provide both root directory and target file.", "JNinka", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
				} else {
					File directory = new File(dirText.getText());
					File file = new File(fileText.getText());
					if (directory.isDirectory()) {
						run(directory, file);
					} else {
						JOptionPane.showConfirmDialog(getParent(), "Root directory not found", "JNinka", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "General error", ex);
		}
	}
	
	public void checkForUpdates() {
		UpdateTask updateTask = new UpdateTask(version);
		updateTask.execute();
	}
	
	/* --- Private methods --- */
	
	private JPanel getCreditPanel(){
		JPanel result = new JPanel();
		BorderLayout layout = new BorderLayout();
		result.setLayout(layout);
		Label label = new Label("By White Source Software", Label.CENTER);
		result.add(label, BorderLayout.SOUTH);
		result.setBackground(BG_COLOR);
		return result;
	}
	
	
	private JPanel getInfoPanel(){
		JPanel result = new JPanel();
		BorderLayout layout = new BorderLayout();
		result.setLayout(layout);
		Label label = new Label("JNinka Code Scanner");
		result.add(label, BorderLayout.CENTER);
		JButton readmeBtn = getLinkButton("i", "http://github.com/whitesource/jninka/blob/master/README.md");
		readmeBtn.setToolTipText("Info");
		result.add(readmeBtn, BorderLayout.EAST);
		return result;
	}
	
	private JButton getLinkButton(String text, String address){
		JButton result = new JButton();
		final URI uri;
		try {
			uri = new URI(address);
			result.setText(text);
			result.setHorizontalAlignment(SwingConstants.CENTER);
			result.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (Desktop.isDesktopSupported()) {
						Desktop desktop = Desktop.getDesktop();
						try {
							desktop.browse(uri);
						} catch (Exception ex) {
						}
					}
				}
			});
		} catch (URISyntaxException e) {
			log.severe("error: " + e.getMessage());
		}
        return result;
	}

	private JPanel getDirectoryPanel() {
		BorderLayout layout = new BorderLayout();
		JPanel result = new JPanel(layout);
		dirText = new JTextField();
		result.add(dirText, BorderLayout.CENTER);
		directoryBrowseButton = getBrowseBtn();
		result.add(directoryBrowseButton, BorderLayout.EAST);
		result.setBackground(BG_COLOR);
		return result;
	}

	private JPanel getFilePanel() {
		BorderLayout layout = new BorderLayout();
		JPanel result = new JPanel(layout);
		fileText = new JTextField();
		result.add(fileText, BorderLayout.CENTER);
		fileBrowseButton = getBrowseBtn();
		result.add(fileBrowseButton, BorderLayout.EAST);
		result.setBackground(BG_COLOR);
		return result;
	}
	
	private JPanel getUnknownsPanel() {
		BorderLayout layout = new BorderLayout();
		JPanel result = new JPanel(layout);
		sureMatchChk = new JCheckBox("Only get sure matches", true);
		sureMatchChk.setSelected(false);
		result.add(sureMatchChk, BorderLayout.SOUTH);
		result.setBackground(BG_COLOR);
		return result;
	}

	private JPanel getRunPanel() {
		BorderLayout layout = new BorderLayout();
		JPanel result = new JPanel(layout);
		result.add(runButton = getRunBtn(), BorderLayout.CENTER);
//		progressBar = new JProgressBar(0);
//		progressBar.setValue(0);
//		progressBar.setStringPainted(true);
//		progressBar.setVisible(false);
//		result.add(progressBar, BorderLayout.CENTER);
//		progressLabel = new JLabel();
//		progressLabel.setVisible(false);
//		result.add(progressLabel, BorderLayout.SOUTH);
		result.setBackground(BG_COLOR);
		return result;
	}

	private JButton getBrowseBtn() {
		JButton result = new JButton();
		result.setSize(80, 20);
		result.setText(BROWSE);
		result.setActionCommand(BROWSE);
		result.addActionListener(this);
		result.setVerticalAlignment(SwingConstants.BOTTOM);
		result.setHorizontalAlignment(SwingConstants.LEFT);
		return result;
	}

	private JButton getRunBtn() {
		JButton result = new JButton();
		result.setSize(80, 20);
		result.setText(RUN);
		result.setActionCommand(RUN);
		result.addActionListener(this);
		result.setVerticalAlignment(SwingConstants.BOTTOM);
		result.setHorizontalAlignment(SwingConstants.CENTER);
		result.setBackground(new Color(150, 200, 16));
		return result;
	}
	
	private JFrame getScanningFrame(){
		JFrame result = new JFrame("Scanning...");
		result.setResizable(false);
		result.setLocation(330, 330);
		result.setMinimumSize(new Dimension(500, 300));
		result.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		JPanel pane = (JPanel) result.getContentPane();
		pane.setBackground(BG_COLOR);
		BorderLayout layout = new BorderLayout();
		pane.setLayout(layout);

		progressBar = new JProgressBar(0);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		progressBar.setVisible(false);
		pane.add(progressBar, BorderLayout.NORTH);
		pane.add(getTable(),BorderLayout.CENTER);
		result.pack();
		return result;
	}
	
	private JTable getTable(){
		resultsModel.addColumn("Folder");
		JTable result = new JTable(resultsModel);
		return result;
	}

	private JFileChooser getDirectoryChooser() {
		directoryDialog = new JFileChooser(lastDir);
		directoryDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		return directoryDialog;
	}

	private JFileChooser getFileChooser() {
		fileDialog = new JFileChooser(lastFile);
		FileFilter filter = new FileNameExtensionFilter("XML", "xml");
		fileDialog.setFileFilter(filter);
		fileDialog.setFileSelectionMode(JFileChooser.SAVE_DIALOG);
		return fileDialog;
	}

	private void run(File root, File output) {
		ScanTask scanTask = new ScanTask(root, output);
		scanTask.addPropertyChangeListener(this);
		scanTask.execute();
	}

	/* --- Nested classes--- */
	
	/**
	 * Subclass for executing Ninka in the background.
	 */
	public class ScanTask extends SwingWorker<Void, Void> {

		/* --- Members --- */
		
		private File root;
		
		private File output;
		
		private String resultMessage;
		
		/* --- Constructors --- */

		/**
		 * Constructor
		 * 
		 * @param root Root scan folder.
		 * @param output Scan results output file.
		 */
		public ScanTask(File root, File output) {
			this.root = root;
			this.output = output;
		}

		/* --- Concrete implementation methods --- */
		
		@Override
		protected Void doInBackground() throws Exception {
			long startTime = System.currentTimeMillis();
			
			Logger log = Logger.getLogger(getClass().getName());
			log.info("----------------------------------------------------------------------");
			log.info("Start scanning folder " + root);
			log.info("Results file is " + output);
			log.info("----------------------------------------------------------------------");
			
			initProgressBar();
			resultsModel.insertRow(0 ,new String[]{"Warming up..."});
			scanFrame.setVisible(true);
			scanFrame.setAlwaysOnTop(true);
			runButton.setVisible(false);
			resultMessage = "Completed successfully.";
			
			try {
				JNinka ninka = new JNinka();
				final int startIdx = root.getAbsolutePath().length();
				ninka.getMonitor().addListener(new ScanProgressListener() {
					@Override
					public void progress(int pct, String details) {
						setProgress(pct);
						resultsModel.insertRow(0 ,new String[]{details.substring(startIdx)});
					}
				});
				
				boolean sureMatches = sureMatchChk.isSelected();
				ScanResults scanResults = ninka.scanFolderRecursive(root, !sureMatches);
				scanResults.writeXML(output);
			} catch (RuntimeException e) {
				resultMessage ="Completed with errors, see log file.";
			}
			
			log.info("Scan completed in " + (System.currentTimeMillis() - startTime) +" [msec].");
			log.info("Scan results message: " + resultMessage);
			log.info("----------------------------------------------------------------------");
			
			// YEY
			return null;
		}
		
		@Override
		protected void done() {
			setProgress(100);
			scanFrame.setAlwaysOnTop(false);
			JOptionPane.showConfirmDialog(getParent(), resultMessage, "JNinka", JOptionPane.PLAIN_MESSAGE, JOptionPane.INFORMATION_MESSAGE);
			scanFrame.setVisible(false);
			progressBar.setVisible(false);
			runButton.setVisible(true);
		}
		
		/* --- Protected methods --- */
		
		protected void initProgressBar() {
			setProgress(0);
			progressBar.setValue(0);
			progressBar.setVisible(true);
		}
		
	}
	
	/**
	 * Subclass for executing update check in the background.
	 */
	public class UpdateTask extends SwingWorker<Void, Void> {

		/* --- Members --- */
		
		private String version;
		
		/* --- Constructors --- */
		
		/**
		 * Constructor
		 * 
		 * @param version The current JNinka version.
		 */
		public UpdateTask(String version) {
			this.version = version;
		}
		
		/* --- Overridden methods --- */

		@Override
		protected Void doInBackground() throws Exception {
			// check for updates
			log.log(Level.INFO, "Checking for update");
			JNinkaUpdateClient updater = new JNinkaUpdateClient();
			String response = updater.checkForUpdate(version);
			if (!response.isEmpty()) {
				doNewVersionLogic(response);
			}
			
			return null;
		}

		/* --- Private methods --- */
		
		private void doNewVersionLogic(String downloadUrl) {
			int choice = JOptionPane.showConfirmDialog(
					getParent(),
					"A new version is available!\nGo to download page?",
					"Version update",
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.INFORMATION_MESSAGE);
			
			if (choice == 0) {
				final URI uri;
				try {
					uri = new URI(downloadUrl);
					if (Desktop.isDesktopSupported()) {
						Desktop desktop = Desktop.getDesktop();
						try {
							desktop.browse(uri);
						} catch (Exception ex) {
							// do nothing
						}
					}
				} catch (URISyntaxException e) {
					log.severe("error: " + e.getMessage());
				}
			}
		}

		public void setVersion(String version) {
			this.version = version;
		}

		public String getVersion() {
			return version;
		}
		
	}

	/* --- Getters / Setters --- */
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
}
