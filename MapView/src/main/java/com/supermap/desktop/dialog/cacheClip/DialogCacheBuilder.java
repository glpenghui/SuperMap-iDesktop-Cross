package com.supermap.desktop.dialog.cacheClip;

import com.supermap.data.processing.CacheWriter;
import com.supermap.desktop.Application;
import com.supermap.desktop.GlobalParameters;
import com.supermap.desktop.controls.ControlsProperties;
import com.supermap.desktop.controls.utilities.ComponentFactory;
import com.supermap.desktop.dialog.SmOptionPane;
import com.supermap.desktop.dialog.cacheClip.cache.BuildCache;
import com.supermap.desktop.dialog.cacheClip.cache.ProcessManager;
import com.supermap.desktop.mapview.MapViewProperties;
import com.supermap.desktop.properties.CommonProperties;
import com.supermap.desktop.ui.controls.*;
import com.supermap.desktop.ui.controls.ProviderLabel.WarningOrHelpProvider;
import com.supermap.desktop.ui.controls.button.SmButton;
import com.supermap.desktop.utilities.FileUtilities;
import com.supermap.desktop.utilities.StringUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by xie on 2017/5/3.
 * Multi cache builder dialog
 */
public class DialogCacheBuilder extends SmDialog {
	private JLabel labelTotalTaskPath;
	private JLabel labelTaskPath;
	private JLabel labelWorkspacePath;
	private JLabel labelMapName;
	private JLabel labelProcessCount;
	private JLabel labelCachePath;
	private JLabel labelTotalProcessCount;
	private JLabel labelTotalProgress;
	private JLabel labelDetailProgressInfo;
	public JFileChooserControl fileChooserTotalTaskPath;
	public JFileChooserControl fileChooserTaskPath;
	public JFileChooserControl fileChooserWorkspacePath;
	public JTextField textFieldMapName;
	private JTextField textFieldProcessCount;
	private JButton buttonApply;
	public JFileChooserControl fileChooserCachePath;
	private JTextField textFieldTotalProcessCount;
	private JLabel labelMergeSciCount;
	private JTextField textFieldMergeSciCount;
	private JProgressBar progressBarTotal;
	private JScrollPane scrollPaneProgresses;
	private JButton buttonCreate;
	private JButton buttonClose;
	//	private JButton buttonRefresh;
	private WarningOrHelpProvider helpProviderForTaskPath;
	private WarningOrHelpProvider helpProviderForTotalTaskPath;
	private WarningOrHelpProvider helpProviderForProcessCount;
	private WarningOrHelpProvider helpProviderForMergeSciCount;
	//scipath for restore path of .sci file
	private String sciPath;
	private int nowProcessCount;
	private String[] params;
	private BuildCache buildCache;
	//Total sci file
	private File sciFile;
	private CopyOnWriteArrayList<JProgressBar> progressBars;
	private CopyOnWriteArrayList<String> captions;
	private CopyOnWriteArrayList<Integer> captionCount;

	private ActionListener closeListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			DialogCacheBuilder.this.dispose();
		}
	};
	private ActionListener createListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			buildCache();
		}
	};
	private ActionListener refreshListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (!StringUtilities.isNullOrEmpty(sciPath)) {
				File sciFile = new File(sciPath);
				String[] sciFiles = null;
				if (sciFile.exists()) {
					sciFiles = sciFile.list(getFilter());
				}
				if (null != sciFiles) {
					int totalSciLength = sciFiles.length;
					int buildSciLength = 0;
					//Get success sci length
					buildSciLength = totalSciLength - sciFile.list(getFilter()).length;
					final int value = (int) (((buildSciLength + 0.0) / totalSciLength) * 100);
					progressBarTotal.setValue(value);
				}
			}
		}
	};
	private ActionListener applyListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (null == params || null == buildCache) {
				return;
			}
			try {
				String newProcessStr = textFieldProcessCount.getText();
				if (StringUtilities.isInteger(newProcessStr)) {
					int newProcessCount = Integer.valueOf(newProcessStr);
					if (newProcessCount > nowProcessCount) {
						//Add new process
						int newSize = newProcessCount - nowProcessCount;
						for (int i = 0; i < newSize; i++) {
							buildCache.addProcess(params);
						}
						nowProcessCount = newProcessCount;
					} else if (newProcessCount < nowProcessCount) {
						DialogStopProcess stopProcess = new DialogStopProcess();
						if (stopProcess.showDialog() == DialogResult.OK && stopProcess.isStopRightNow()) {
							//Stop all process and dispose
							ProcessManager.getInstance().dispose();
						} else {
							return;
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	};
	private FileChooserPathChangedListener fileChangeListener = new FileChooserPathChangedListener() {
		@Override
		public void pathChanged() {
			String sciFilePath = fileChooserTotalTaskPath.getPath();
			if (!StringUtilities.isNullOrEmpty(sciFilePath) && FileUtilities.isFilePath(sciFilePath) && hasSciFiles(new File(sciFilePath))) {
				CacheWriter writer = new CacheWriter();
				boolean result = writer.FromConfigFile(sciFilePath);
				if (result) {
					HashMap<Double, String> allScaleCaptions = new HashMap<Double, String>(writer.getCacheScaleCaptions());
					Set<Double> scales = allScaleCaptions.keySet();
					ArrayList<Double> scaleList = new ArrayList<Double>();
					scaleList.addAll(scales);
					Collections.sort(scaleList);
					CopyOnWriteArrayList<String> tempCaptions = new CopyOnWriteArrayList<>();
					for (double scale : scaleList) {
						tempCaptions.add(String.valueOf((int) (1 / scale)));
					}
					setCaptions(tempCaptions);
				}
			}
		}
	};


	public DialogCacheBuilder() {
		init();
	}

	private void init() {
		initComponents();
		initResources();
		initLayout();
		registEvents();
		Dimension dimension = new Dimension(723, 365);
		this.setSize(dimension);
		this.setLocationRelativeTo(null);
		this.componentList.add(this.buttonCreate);
		this.componentList.add(this.buttonClose);
		this.setFocusTraversalPolicy(policy);
	}

	private void initComponents() {
		this.progressBars = new CopyOnWriteArrayList<>();
		this.labelTotalTaskPath = new JLabel();
		this.labelTaskPath = new JLabel();
		this.labelWorkspacePath = new JLabel();
		this.labelMapName = new JLabel();
		this.labelCachePath = new JLabel();
		this.labelTotalProcessCount = new JLabel();
		this.labelProcessCount = new JLabel();
		this.labelTotalProgress = new JLabel();
		this.labelDetailProgressInfo = new JLabel();

		String taskModuleName = "ChooseTotalTaskFile";
		if (!SmFileChoose.isModuleExist(taskModuleName)) {
			String sciFilter = SmFileChoose.bulidFileFilters(
					SmFileChoose.createFileFilter(MapViewProperties.getString("String_SciFileType"), "sci"));
			SmFileChoose.addNewNode(sciFilter, System.getProperty("user.dir"), GlobalParameters.getDesktopTitle(),
					taskModuleName, "OpenOne");
		}

		SmFileChoose fileChooseTotalTask = new SmFileChoose(taskModuleName);
		this.fileChooserTotalTaskPath = new JFileChooserControl();
		this.fileChooserTotalTaskPath.setFileChooser(fileChooseTotalTask);
		this.fileChooserTotalTaskPath.setPath(System.getProperty("user.dir"));

		String moduleName = "ChooseTaskDirectories";
		if (!SmFileChoose.isModuleExist(moduleName)) {
			SmFileChoose.addNewNode("", System.getProperty("user.dir"), GlobalParameters.getDesktopTitle(),
					moduleName, "GetDirectories");
		}

		SmFileChoose fileChoose = new SmFileChoose(moduleName);
		this.fileChooserTaskPath = new JFileChooserControl();
		this.fileChooserTaskPath.setFileChooser(fileChoose);
		this.fileChooserTaskPath.setPath(System.getProperty("user.dir"));
		String modulenameForWorkspace = "ChooseWorkspaceFile";
		if (!SmFileChoose.isModuleExist(modulenameForWorkspace)) {
			String fileFilters = SmFileChoose.createFileFilter(MapViewProperties.getString("String_FileFilters_Workspace"), "smwu", "sxwu");
			SmFileChoose.addNewNode(fileFilters, CommonProperties.getString("String_DefaultFilePath"),
					MapViewProperties.getString("String_OpenWorkspace"), modulenameForWorkspace, "OpenOne");
		}

		SmFileChoose fileChooserForWorkSpace = new SmFileChoose(modulenameForWorkspace);
		this.fileChooserWorkspacePath = new JFileChooserControl();
		this.fileChooserWorkspacePath.setFileChooser(fileChooserForWorkSpace);
		this.fileChooserWorkspacePath.setPath(System.getProperty("user.dir"));
		this.textFieldMapName = new JTextField();
		this.textFieldProcessCount = new JTextField();
		this.textFieldProcessCount.setText("5");
		this.buttonApply = ComponentFactory.createButtonApply();
		String moduleNameForCachePath = "ChooseCacheDirectories";
		if (!SmFileChoose.isModuleExist(moduleNameForCachePath)) {
			SmFileChoose.addNewNode("", System.getProperty("user.dir"), GlobalParameters.getDesktopTitle(),
					moduleNameForCachePath, "GetDirectories");
		}
		SmFileChoose fileChooserForCachePath = new SmFileChoose(moduleNameForCachePath);
		this.fileChooserCachePath = new JFileChooserControl();
		this.fileChooserCachePath.setPath(System.getProperty("user.dir") + "\\build");
		this.fileChooserCachePath.setFileChooser(fileChooserForCachePath);
		this.textFieldTotalProcessCount = new JTextField();
		this.textFieldTotalProcessCount.setText("5");
		this.textFieldTotalProcessCount.setEnabled(false);
		this.progressBarTotal = new JProgressBar();
		this.progressBarTotal.setStringPainted(true);
		this.progressBarTotal.setPreferredSize(new Dimension(200, 23));
		this.labelMergeSciCount = new JLabel();
		this.textFieldMergeSciCount = new JTextField();
		this.textFieldMergeSciCount.setText("1");
		this.scrollPaneProgresses = new JScrollPane();
//		this.buttonRefresh = new SmButton();
		this.buttonCreate = new SmButton();
		this.buttonClose = ComponentFactory.createButtonClose();
		this.helpProviderForTotalTaskPath = new WarningOrHelpProvider(MapViewProperties.getString("String_TotalSciFile"), false);
		this.helpProviderForTaskPath = new WarningOrHelpProvider(MapViewProperties.getString("String_SciFilePath"), false);
		this.helpProviderForProcessCount = new WarningOrHelpProvider(MapViewProperties.getString("String_HelpForProcessCount"), false);
		this.helpProviderForMergeSciCount = new WarningOrHelpProvider(MapViewProperties.getString("String_HelpForMergeSciCount"), false);
	}

	private void initResources() {
		this.setTitle(MapViewProperties.getString("String_MultiProcessClipMapCache"));
		this.labelTotalTaskPath.setText(MapViewProperties.getString("String_TotalTaskPath"));
		this.labelTaskPath.setText(MapViewProperties.getString("String_TaskPath"));
		this.labelWorkspacePath.setText(MapViewProperties.getString("String_WorkspacePath"));
		this.labelMapName.setText(MapViewProperties.getString("String_Label_MapName"));
		this.labelProcessCount.setText(MapViewProperties.getString("String_ProcessCount"));
		this.labelCachePath.setText(MapViewProperties.getString("String_CachePath"));
		this.labelTotalProcessCount.setText(MapViewProperties.getString("String_TotalProcessCount"));
		this.labelTotalProgress.setText(ControlsProperties.getString("String_ProgressControl_TotalProgress"));
		this.labelDetailProgressInfo.setText(MapViewProperties.getString("String_DetailProcessInfo"));
		this.labelMergeSciCount.setText(MapViewProperties.getString("String_MergeSciCount"));
		this.buttonCreate.setText(MapViewProperties.getString("String_BatchAddColorTableOKButton"));
//		this.buttonRefresh.setText(ControlsProperties.getString("String_Refresh"));
	}


	private void initLayout() {
		JPanel panelClip = new JPanel();
		panelClip.setBorder(BorderFactory.createTitledBorder(MapViewProperties.getString("String_MultiProcessClip")));
		panelClip.setLayout(new GridBagLayout());

		panelClip.add(this.labelTotalTaskPath, new GridBagConstraintsHelper(0, 0, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(5, 10, 5, 10));
		panelClip.add(this.helpProviderForTotalTaskPath, new GridBagConstraintsHelper(1, 0, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(5, 0, 5, 10));
		panelClip.add(this.fileChooserTotalTaskPath, new GridBagConstraintsHelper(2, 0, 3, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.HORIZONTAL).setInsets(5, 0, 5, 10).setWeight(1, 0));
		panelClip.add(this.labelTaskPath, new GridBagConstraintsHelper(0, 1, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(0, 10, 5, 10));
		panelClip.add(this.helpProviderForTaskPath, new GridBagConstraintsHelper(1, 1, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(0, 0, 5, 10));
		panelClip.add(this.fileChooserTaskPath, new GridBagConstraintsHelper(2, 1, 3, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.HORIZONTAL).setInsets(0, 0, 5, 10).setWeight(1, 0));
		panelClip.add(this.labelWorkspacePath, new GridBagConstraintsHelper(0, 2, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(0, 10, 5, 10));
		panelClip.add(this.fileChooserWorkspacePath, new GridBagConstraintsHelper(2, 2, 4, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.HORIZONTAL).setInsets(0, 0, 5, 10).setWeight(1, 0));
		panelClip.add(this.labelMapName, new GridBagConstraintsHelper(0, 3, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(0, 10, 5, 10));
		panelClip.add(this.textFieldMapName, new GridBagConstraintsHelper(2, 3, 4, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.HORIZONTAL).setInsets(0, 0, 5, 10).setWeight(1, 0));
		panelClip.add(this.labelProcessCount, new GridBagConstraintsHelper(0, 4, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(0, 10, 5, 10));
		panelClip.add(this.helpProviderForProcessCount, new GridBagConstraintsHelper(1, 4, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(0, 0, 5, 10));
		panelClip.add(this.textFieldProcessCount, new GridBagConstraintsHelper(2, 4, 2, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.HORIZONTAL).setInsets(0, 0, 5, 5).setWeight(1, 0));
		panelClip.add(this.buttonApply, new GridBagConstraintsHelper(4, 4, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(0, 0, 5, 10));
		panelClip.add(this.labelCachePath, new GridBagConstraintsHelper(0, 5, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(0, 10, 5, 10));
		panelClip.add(this.fileChooserCachePath, new GridBagConstraintsHelper(2, 5, 4, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.HORIZONTAL).setInsets(0, 0, 5, 10).setWeight(1, 0));
		panelClip.add(this.labelMergeSciCount, new GridBagConstraintsHelper(0, 6, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(0, 10, 5, 10));
		panelClip.add(this.helpProviderForMergeSciCount, new GridBagConstraintsHelper(1, 6, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(0, 0, 5, 10));
		panelClip.add(this.textFieldMergeSciCount, new GridBagConstraintsHelper(2, 6, 3, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.HORIZONTAL).setInsets(0, 0, 5, 10).setWeight(1, 0));
		panelClip.add(new JPanel(), new GridBagConstraintsHelper(0, 7, 5, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.BOTH).setWeight(1, 1));

		JPanel panelClipProgress = new JPanel();
		panelClipProgress.setBorder(BorderFactory.createTitledBorder(MapViewProperties.getString("String_ClipBuilderProgress")));
		panelClipProgress.setLayout(new GridBagLayout());
		panelClipProgress.add(this.labelTotalProcessCount, new GridBagConstraintsHelper(0, 0, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(5, 10, 5, 10));
		panelClipProgress.add(this.textFieldTotalProcessCount, new GridBagConstraintsHelper(1, 0, 2, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.HORIZONTAL).setInsets(5, 0, 5, 10).setWeight(1, 0));
		panelClipProgress.add(this.labelTotalProgress, new GridBagConstraintsHelper(0, 1, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(0, 10, 5, 10));
		panelClipProgress.add(this.progressBarTotal, new GridBagConstraintsHelper(1, 1, 2, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.HORIZONTAL).setInsets(0, 0, 5, 10).setWeight(1, 0));
//		panelClipProgress.add(this.buttonRefresh, new GridBagConstraintsHelper(2, 1, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(0, 0, 5, 10));
		panelClipProgress.add(this.labelDetailProgressInfo, new GridBagConstraintsHelper(0, 2, 3, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(0, 10, 5, 10));
		panelClipProgress.add(this.scrollPaneProgresses, new GridBagConstraintsHelper(0, 3, 3, 5).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.BOTH).setInsets(0, 10, 5, 10).setWeight(1, 1));
		JPanel panelContent = (JPanel) this.getContentPane();
		panelContent.setLayout(new GridBagLayout());
		panelContent.add(panelClip, new GridBagConstraintsHelper(0, 0, 4, 4).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.BOTH).setInsets(5, 10, 5, 10).setWeight(1, 1));
		panelContent.add(panelClipProgress, new GridBagConstraintsHelper(4, 0, 4, 4).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.BOTH).setInsets(5, 0, 5, 10).setWeight(1, 1));
		panelContent.add(this.buttonCreate, new GridBagConstraintsHelper(6, 4, 1, 1).setAnchor(GridBagConstraints.EAST).setFill(GridBagConstraints.NONE).setInsets(0, 0, 10, 10).setWeight(1, 0));
		panelContent.add(this.buttonClose, new GridBagConstraintsHelper(7, 4, 1, 1).setAnchor(GridBagConstraints.EAST).setFill(GridBagConstraints.NONE).setInsets(0, 0, 10, 10).setWeight(0, 0));
	}

	private void registEvents() {
		removeEvents();
		this.buttonCreate.addActionListener(this.createListener);
		this.buttonClose.addActionListener(this.closeListener);
		this.buttonApply.addActionListener(this.applyListener);
		this.fileChooserTotalTaskPath.addFileChangedListener(this.fileChangeListener);
//		this.buttonRefresh.addActionListener(this.refreshListener);
	}

	private void removeEvents() {
		this.buttonCreate.removeActionListener(this.createListener);
		this.buttonClose.removeActionListener(this.closeListener);
		this.buttonApply.removeActionListener(this.applyListener);
		this.fileChooserTotalTaskPath.removePathChangedListener(this.fileChangeListener);
//		this.buttonRefresh.removeActionListener(this.refreshListener);
	}

	private void buildCache() {
		try {
			String workspacePath = fileChooserWorkspacePath.getPath();
			String mapName = textFieldMapName.getText();
			sciPath = fileChooserTaskPath.getPath();
			String cachePath = fileChooserCachePath.getPath();
			String processCount = textFieldProcessCount.getText();
			if (StringUtilities.isInteger(processCount)) {
				nowProcessCount = Integer.valueOf(processCount);
			}
			String mergeSciCount = textFieldMergeSciCount.getText();
			params = new String[]{sciPath, workspacePath, mapName, cachePath, processCount, mergeSciCount};
//            final String[] params = {workspacePath, mapName, sciPath, cachePath, processCount, mergeSciCount};

			if (!validateValue(sciPath, workspacePath, mapName, cachePath, processCount)) {
				new SmOptionPane().showConfirmDialog(MapViewProperties.getString("String_ParamsException"));
			} else {
				doBuildCache(cachePath);
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
	}

	private void doBuildCache(String cachePath) {
		int totalSciLength = 0;
		String[] sciNames = null;
		File sciFile = new File(sciPath);
		if (sciFile.exists()) {
			sciNames = sciFile.list(getFilter());
		}
		if (null != sciNames) {
			totalSciLength = sciNames.length;
			captionCount = new CopyOnWriteArrayList<>();
			if (null != captions) {
				for (int i = 0; i < captions.size(); i++) {
					captionCount.add(sciFile.list(getFilter(captions.get(i))).length);
				}
				updateSingleProcess(sciFile.getParentFile().getPath(), totalSciLength);
			}
			updateTotalProgress(sciFile.getParentFile().getPath(), cachePath, totalSciLength);
		}
		buildCache = new BuildCache();
		buildCache.startProcess(Integer.valueOf(params[BuildCache.PROCESSCOUNT_INDEX]), params);
	}

	private boolean validateValue(String sciPath, String workspacePath, String mapName, String cachePath, String processCount) {
		boolean result = true;
		File sciDirectory = new File(sciPath);
		if (StringUtilities.isNullOrEmpty(sciPath) || !FileUtilities.isFilePath(sciPath) || !hasSciFiles(sciDirectory)) {
			result = false;
		}
		if (StringUtilities.isNullOrEmpty(workspacePath) || !FileUtilities.isFilePath(workspacePath) || !(workspacePath.endsWith("smwu") || workspacePath.endsWith("sxwu"))) {
			result = false;
		}
		if (StringUtilities.isNullOrEmpty(mapName)) {
			result = false;
		}
		if (StringUtilities.isNullOrEmpty(cachePath) || !FileUtilities.isFilePath(cachePath)) {
			result = false;
		}
		if (StringUtilities.isNullOrEmpty(processCount) || !(StringUtilities.isInteger(processCount) || processCount.equals("0"))) {
			result = false;
		}
		return result;
	}

	private boolean hasSciFiles(File sciDirectory) {
		return sciDirectory.list(getFilter()).length > 0 ? true : false;
	}

	//Update single scale info process
	private void updateSingleProcess(String parentPath, int totalSciLength) {

		final int fianlTotalSciLength = totalSciLength;
		final String finalParentPath = parentPath;
		Thread updateSingleThread = new Thread() {
			@Override
			public void run() {
				try {
					int buildSciLength = -1;
					while (fianlTotalSciLength != buildSciLength) {
						String buildPath = finalParentPath + "\\build";
						File buildFile = new File(buildPath);
						//Ensure that component,count array have sorted as we want;
						if (null != buildFile && buildFile.exists()) {
							buildSciLength = buildFile.list(getFilter()).length;
							for (int i = 0; i < captions.size(); i++) {
								int currentCount = buildFile.list(getFilter(captions.get(i))).length;
								final int value = (int) (((currentCount + 0.0) / captionCount.get(i)) * 100);
								progressBars.get(i).setValue(value);
							}
							//Sleep two seconds or not
							Thread.sleep(1000);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		updateSingleThread.start();

	}

	//Update total progress
	private void updateTotalProgress(String parentPath, String cachePath, int totalSciLength) {
		final String finalCachePath = cachePath;
		final String finalParentPath = parentPath;
		final int finalTotalSciLength = totalSciLength;
		Thread updateThread = new Thread() {
			@Override
			public void run() {
				refresh(finalCachePath, finalParentPath, finalTotalSciLength);
			}
		};
		updateThread.start();
	}

	private void refresh(String cachePath, String parentPath, int totalSciLength) {
		try {
			long startTime = System.currentTimeMillis();
			int buildSciLength = 0;
			while (buildSciLength != totalSciLength) {
				//Get success sci length
				String buildPath = parentPath + "\\build";
				File buildFile = new File(buildPath);
				if (buildFile.exists()) {
					buildSciLength = buildFile.list(getFilter()).length;
				}
				final int value = (int) (((buildSciLength + 0.0) / totalSciLength) * 100);
				progressBarTotal.setValue(value);
				//Sleep two seconds or not
				Thread.sleep(6000);
			}
			boolean result = false;
			File resultDir = new File(cachePath);
			String resultPath = "";
			if (resultDir.isDirectory()) {
				File[] files = resultDir.listFiles();
				for (int i = 0; i < files.length; i++) {
					if (files[i].getName().equals(params[BuildCache.MAPNAME_INDEX])) {
						resultPath = files[i].getAbsolutePath();
						result = true;
						break;
					}
				}
			}
			if (result) {
				long endTime = System.currentTimeMillis();
				File mapNameDir = new File(resultPath);
				String targetDirectory = null;
				if (mapNameDir.isDirectory() && mapNameDir.listFiles().length > 0) {
					targetDirectory = mapNameDir.listFiles()[0].getAbsolutePath();
				}
				//Paste sciFile to the target directory
				sciFile.renameTo(new File(targetDirectory, sciFile.getName()));
				long totalTime = endTime - startTime;
				long hour = 0;
				long minutes = 0;
				long second = 0;
				if (totalTime >= 3600000) {
					hour = totalTime / 3600000;
					minutes = (totalTime % 3600000) / 60000;
					second = ((totalTime % 3600000) % 60000) / 1000;
				}
				if (totalTime >= 60000) {
					minutes = totalTime / 60000;
					second = (totalTime % 60000) / 1000;
				} else {
					second = totalTime / 1000;
				}
				Application.getActiveApplication().getOutput().output(MessageFormat.format(MapViewProperties.getString("String_MultiCacheSuccess"), resultPath, hour, minutes, second));
			} else {
				Application.getActiveApplication().getOutput().output(MapViewProperties.getString("String_MultiCacheFailed"));
			}
			disposeInfo();
		} catch (Exception e) {
			Application.getActiveApplication().getOutput().output(e);
		}
	}


	private FilenameFilter getFilter() {
		return new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".sci");
			}
		};
	}

	private FilenameFilter getFilter(String caption) {
		final String tempCaption = caption;
		return new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".sci") && name.contains(tempCaption);
			}
		};
	}

	private void disposeInfo() {
		removeEvents();
		this.captions = null;
		this.scrollPaneProgresses = null;
		this.captionCount = null;
		DialogCacheBuilder.this.dispose();
		//Dispose instance of process manager
		ProcessManager.getInstance().dispose();
	}

	public void setSciFile(File sciFile) {
		this.sciFile = sciFile;
	}

	public void setCaptions(CopyOnWriteArrayList<String> sourceCaptions) {
		captions = sourceCaptions;
		//Display scale info process
		JPanel panelScaleProcess = new JPanel();
		panelScaleProcess.setLayout(new GridBagLayout());
		int scaleCount = captions.size();
		progressBars.clear();
		for (int i = 0; i < scaleCount; i++) {
			JLabel labelProcess = new JLabel("1:" + captions.get(i));
			JProgressBar process = new JProgressBar();
			process.setStringPainted(true);
			progressBars.add(process);
			panelScaleProcess.add(labelProcess, new GridBagConstraintsHelper(0, i, 1, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.NONE).setInsets(i == 0 ? 10 : 0, 10, 5, 10));
			panelScaleProcess.add(process, new GridBagConstraintsHelper(1, i, 2, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.HORIZONTAL).setInsets(i == 0 ? 10 : 0, 0, 5, 10).setWeight(1, 0));
		}
		panelScaleProcess.add(new JPanel(), new GridBagConstraintsHelper(0, scaleCount, 3, 1).setAnchor(GridBagConstraints.WEST).setFill(GridBagConstraints.BOTH).setWeight(1, 1));
		this.scrollPaneProgresses.setViewportView(panelScaleProcess);
		this.repaint();
	}
}
