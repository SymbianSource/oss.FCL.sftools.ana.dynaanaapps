/*
 * Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
 * All rights reserved.
 * This component and the accompanying materials are made available
 * under the terms of the License "Eclipse Public License v1.0"
 * which accompanies this distribution, and is available
 * at the URL "http://www.eclipse.org/legal/epl-v10.html".
 *
 * Initial Contributors:
 * Nokia Corporation - initial contribution.
 *
 * Contributors:
 *
 * Description: 
 *
 */

package com.nokia.carbide.cpp.pi.editors;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.osgi.framework.Bundle;

import com.nokia.carbide.cpp.internal.featureTracker.FeatureUseTrackerConsts;
import com.nokia.carbide.cpp.internal.pi.actions.TimeSetDialog;
import com.nokia.carbide.cpp.internal.pi.analyser.AnalyserDataProcessor;
import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.analyser.PIChangeEvent;
import com.nokia.carbide.cpp.internal.pi.analyser.ProfileReader;
import com.nokia.carbide.cpp.internal.pi.analyser.ProfileVisualiser;
import com.nokia.carbide.cpp.internal.pi.interfaces.IToolBarActionListener;
import com.nokia.carbide.cpp.internal.pi.manager.PluginInitialiser;
import com.nokia.carbide.cpp.internal.pi.manager.PluginRegisterer;
import com.nokia.carbide.cpp.internal.pi.model.GUITooltips;
import com.nokia.carbide.cpp.internal.pi.model.TraceDataRepository;
import com.nokia.carbide.cpp.internal.pi.plugin.model.AbstractPiPlugin;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IEventListener;
import com.nokia.carbide.cpp.internal.pi.plugin.model.IFinalizeTrace;
import com.nokia.carbide.cpp.pi.core.SessionPreferences;
import com.nokia.carbide.cpp.pi.importer.SampleImporter;


public class PIPageEditor extends MultiPageEditorPart implements IResourceChangeListener
{
	public static final String PI_ID = "com.nokia.carbide.cpp.pi"; //$NON-NLS-1$

	// PI menu groups for adding plug-in specific actions/menu items
	public static final String includeFilesGroup = "includeFile";	//$NON-NLS-1$
	public static final String reportsGroup      = "reports";		//$NON-NLS-1$
	public static final String viewOptionsGroup  = "viewOptions";	//$NON-NLS-1$
	public static final String exportsGroup      = "exports";		//$NON-NLS-1$
	public static final String additionsGroup    = "additions";		//$NON-NLS-1$
	
	// PI menu identifier
	public static final String menuID = "com.nokia.carbide.cpp.pi.menuID"; //$NON-NLS-1$
	
	// indices of the three standard pages that have graphs
	public static final int THREADS_PAGE   = 0;
	public static final int BINARIES_PAGE  = 1;
	public static final int FUNCTIONS_PAGE = 2;
	public static final int NEXT_AVAILABLE_PAGE = -1;
	
	public static Font helvetica_8;
	public static Font helvetica_9;
	public static Font helvetica_10;
	public static Font helvetica_12;
	public static Font helvetica_14;
	
	// menu manager to which plugin's can add their actions
	private static IMenuManager menuManager;
	private static MenuManager currentManager;
	private static MenuManager includeManager;

	// info related to the current page editor and its pages
	private static int          currentPageIndex;
	private static PIPageEditor currentPageEditor;
	private static int			currentUid;
	
	private static boolean openingFile = false; 
	
	private static boolean isCanceled = false;
	private boolean isErrorPage = false;
	
	private static boolean startedPlugins = false;
	
	// actions available to all pages
	
	// toolbar actions for zooming
	private static Action zoomInAction;
	private static Action zoomOutAction;
	private static Action zoomToSelectionAction;
	private static Action zoomToTraceAction;
	
	// toolbar start and end times
	private static Action selectTimeAction;
	
	// UID for this page
	private int uid;

	private double maxEndTime = Double.MAX_VALUE;
	private double startTime = -1;
	private double endTime   = -1;

	private boolean tooltipsEnabled = true;
	private int activePageIndex = -1;
	
	private static IActionBars actionBars;
	
	private boolean dirty = false;

	private IPartListener partListener;

	// full path name of the NPI file, so we can make all open copies of the file dirty
	// or clean at the same time
//	private String fullPath;

	/**
	 * Creates a multi-page editor.
	 */
	public PIPageEditor() {
		super();
		
		// initialize the current page editor
		setCurrentPageEditor(this);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}
	
	protected static void createFonts(Display display) {
		if (helvetica_8 == null) {
			helvetica_8  = new Font(display, "Helvetica",  8, SWT.NORMAL); //$NON-NLS-1$
			helvetica_9  = new Font(display, "Helvetica",  9, SWT.NORMAL); //$NON-NLS-1$
			helvetica_10 = new Font(display, "Helvetica", 10, SWT.NORMAL); //$NON-NLS-1$
			helvetica_12 = new Font(display, "Helvetica", 12, SWT.NORMAL); //$NON-NLS-1$
			helvetica_14 = new Font(display, "Helvetica", 14, SWT.NORMAL); //$NON-NLS-1$
		}
	}
	
	protected void createBlankPage() {
		Composite container = new Composite(getContainer(), SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;
		layout.verticalSpacing = 9;
		Label message = new Label(container, SWT.NONE);
		//addPage(container);
		setIsErrorPage(false);
	}
	
	protected void createDummyErrorPage(String msg) {
		Composite container = new Composite(getContainer(), SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;
		layout.verticalSpacing = 9;
		Label message = new Label(container, SWT.NONE);
		message.setText(msg);
		addPage(container);
		setIsErrorPage(true);
	}
	
	private void createErrorPageForException() {
		IFileEditorInput editorInput = (IFileEditorInput)getEditorInput();
		String message = Messages.getString("PIPageEditor.invalidPiFile") + editorInput.getName() + "\n" ; //$NON-NLS-1$ //$NON-NLS-2$
		Throwable e = AnalyserDataProcessor.getInstance().getLastException();
		if (e != null) {
			if (e instanceof InvocationTargetException) {
				if (e.getMessage() != null) {
					message += e.getMessage() + "\n";	//$NON-NLS-1$
				}
				e = ((InvocationTargetException)e).getTargetException();
			}
			message += "\n" + Messages.getString("PIPageEditor.bugReport") + "\n\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			message += e.getClass().toString() + "\n"; //$NON-NLS-1$
			if (e.getMessage() != null) {
				message += e.getMessage() + "\n"; //$NON-NLS-1$
			}
			if (e.getStackTrace() != null) {
				StackTraceElement [] wholeStack = e.getStackTrace();
				for (StackTraceElement element: wholeStack) {
					String className = element.getClassName();
					if (className.toString().contains("fi.vtt.") || className.toString().contains("com.nokia.")) { //$NON-NLS-1$ //$NON-NLS-2$
						message += Messages.getString("PIPageEditor.CarbideCPlusPlusSource") + element.getFileName() + ":" + element.getLineNumber() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
					else {
						message += element.toString() + "\n"; //$NON-NLS-1$
					}
				}
			}
		}
		createDummyErrorPage(message);

	}

	/**
	 * Creates the pages of the multi-page editor.
	 */
	protected void createPages() {
		IURIEditorInput editorInput = (IURIEditorInput)getEditorInput();
		
		// Kludge because of the way Eclipse opens files: disable ability of page.addFocusListener().focusGained() to change
		// current editor and tab info
		openingFile = true;

		// make sure we can open an input stream to the trace file
		
		InputStream inputStream;
		try {
			inputStream = editorInput.getURI().toURL().openStream();
			inputStream.read();
			inputStream.close();
		} catch (MalformedURLException e) {
			System.out.println(Messages.getString("PIPageEditor.MalformedURL1") + editorInput.getURI().toString());  //$NON-NLS-1$
			createDummyErrorPage(Messages.getString("PIPageEditor.MalformedURL2") + editorInput.getURI().toString());  //$NON-NLS-1$
			openingFile = false;
			return;
			
		} catch (IOException e) {
			System.out.println(Messages.getString("PIPageEditor.cannotReadFile") + editorInput.getName()); //$NON-NLS-1$
			// something is wrong, print a dummy page
			createDummyErrorPage(Messages.getString("PIPageEditor.cannotReadFile") + editorInput.getName()); //$NON-NLS-1$
			openingFile = false;
			return;
		}
		
		try {
			if (SampleImporter.getInstance().isStrippingTimeStamp()) {	
				uid = NpiInstanceRepository.getInstance().register(getContainer());
				AnalyserDataProcessor.getInstance().importForStrippingTimeStamp(getContainer());
				openingFile = false;
				return;
			}
			
			if (AnalyserDataProcessor.getInstance().getState() == AnalyserDataProcessor.STATE_IMPORTING) {
				uid = NpiInstanceRepository.getInstance().activeUid();
				NpiInstanceRepository.getInstance().setParentComposite(uid, getContainer());
			} else {
				// register this editor and ask repository to prepare per instance data
				uid = NpiInstanceRepository.getInstance().register(getContainer());
				NpiInstanceRepository.getInstance().switchActiveUid(uid);
			}
			AnalyserDataProcessor.getInstance().openNpiForPIPageEditor(editorInput.getURI(), getContainer(), uid);
			
			if (AnalyserDataProcessor.getInstance().getState() != AnalyserDataProcessor.STATE_OK) {
				openingFile = false;
				
				// user may have canceled, print a dummy page
				// if we deleted the importer file, then don't create a dummy page
				if (AnalyserDataProcessor.getInstance().getState() == AnalyserDataProcessor.STATE_CANCELED) {
					createDummyErrorPage(Messages.getString("PIPageEditor.userCancelled") + editorInput.getName()); //$NON-NLS-1$
				} else {
					createErrorPageForException();
				}

				return;
			}
		} catch (Exception ex) {
			openingFile = false;
					
			// user may have canceled, print a dummy page
			// if we deleted the importer file, then don't create a dummy page
			if (AnalyserDataProcessor.getInstance().getState() == AnalyserDataProcessor.STATE_CANCELED) {
				createDummyErrorPage(Messages.getString("PIPageEditor.userCancelled") + editorInput.getName()); //$NON-NLS-1$
			} else {
				createErrorPageForException();
			}

			return;	
		}
		
		// add to the editor tab all pages create by AnalyseTab 
	  	ArrayList<ProfileVisualiser> pages = NpiInstanceRepository.getInstance().activeUidGetProfilePages();
	  	
	  	int index = -1;
	  	for (Iterator<ProfileVisualiser> i = pages.iterator();i.hasNext();) {
	  		ProfileVisualiser myPv = i.next();
	  		final Composite page = myPv.getContentPane();
	  		
	  		index = addPage(page);
	  		setPageText(index, myPv.getPageName());
	  		
	  		// set the composite page's data to its page number
	  		page.setData("pageIndex", new Integer(index)); //$NON-NLS-1$
	  		page.setData("pageEditor", this); //$NON-NLS-1$
	  		page.setData("pageUID", new Integer(uid)); //$NON-NLS-1$

	  		page.addFocusListener(new FocusAdapter() {

				public void focusGained(FocusEvent e) {
					Object data;
					int          nextPageIndex;
					PIPageEditor nextPageEditor;
					Integer			nextUIDInteger;
					
					data = page.getData("pageIndex"); //$NON-NLS-1$
					if ((data != null) && (data instanceof Integer))
						nextPageIndex = ((Integer) data).intValue();
					else
						nextPageIndex = -1;
					
					data = page.getData("pageEditor"); //$NON-NLS-1$
					if ((data != null) && (data instanceof PIPageEditor))
						nextPageEditor = (PIPageEditor) data;
					else
						nextPageEditor = null;
					
					data = page.getData("pageUID"); //$NON-NLS-1$
					if ((data != null) && (data instanceof Integer))
						nextUIDInteger = (Integer) data;
					else
						nextUIDInteger = null;
					
					// add the new tab's menu items
					if ((nextUIDInteger != null) && (nextUIDInteger != NpiInstanceRepository.getInstance().activeUid())) {
						/* Note: some items are tab-specific and some are page-specific
						 *       (check for the current tab when they are called)
						 */
						addTabMenuItems(nextUIDInteger.intValue());
					}
					
					// show the new page editor's start and end times
					if ((nextPageEditor != null) && (nextPageEditor != currentPageEditor)) {
						double start = nextPageEditor.getStartTime();
						double end   = nextPageEditor.getEndTime();
						PIPageEditor.setTime(start, end);
					}

					if (!PIPageEditor.isOpeningFile()) {
						setCurrentPageIndex(nextPageIndex);
						setCurrentPageEditor(nextPageEditor);
						setCurrentUid(nextUIDInteger);
					}
				}
	  		});
	  	}
	  	
	  	if (!pages.isEmpty()) {
	  		currentPageIndex = 0;
	  		setActivePage(0);
	  	} else {
			new Composite(getContainer(), SWT.NONE);
	  	}
	  	currentPageEditor = this;
		openingFile = false;
	}

	public static int currentPageIndex() {
		return currentPageIndex;
	}
	
	public static PIPageEditor currentPageEditor() {
		return currentPageEditor;
	}
	
	public static boolean isOpeningFile() {
		return openingFile;
	}
	
	public static MenuManager currentMenuManager() {
		return currentManager;
	}
	
	public static void startPlugins() {
		startedPlugins = true;
	}
	
	public static boolean arePluginsStarted() {
		return startedPlugins;
	}

	// set the overall menu manager for PI
	public static void setPIMenuManager(MenuManager piManager) {
		if (piManager == null)
			return;
		
		currentManager = piManager;
		
		// Force an update because Eclipse hides empty menus.
		currentManager.addMenuListener(new IMenuListener() {
			 public void menuAboutToShow(IMenuManager menuManager) {
				 menuManager.updateAll(true);
			 }
		 });

		initialiseMenuManager();
	}

	// start with a fresh overall menu manager, to which you add contribution items
	public static void initialiseMenuManager() {
		if (currentManager == null)
			return;
		
		// This currentManager can only be modified in UI thread if the menu ever gets called by user
		Display.getDefault().syncExec( new Runnable() {
			public void run() {
				currentManager.removeAll();
				
				/* 
				 * A typical menu hierarchy might look like:
				 * 
				 * ... PI ...
				 * 		Include Other Profile File ->
				 * 		-----------------------------
				 * 		Profile File Summary
				 *		Profile File Report
				 *		-----------------------------
				 *		CPU Load Graph			   ->
				 *		-----------------------------
				 */
				currentManager.add(new Separator(PIPageEditor.includeFilesGroup));
				currentManager.add(new Separator(PIPageEditor.reportsGroup));
				currentManager.add(new Separator(PIPageEditor.viewOptionsGroup));
				currentManager.add(new Separator(PIPageEditor.exportsGroup));
				currentManager.add(new Separator(PIPageEditor.additionsGroup));

			}			
		});
	}

	public static void addExportAction(IAction action) {
		if (action == null)
			return;
		
		currentManager.appendToGroup(PIPageEditor.exportsGroup, action);
	}

	public static void addIncludeAction(IAction action) {
		if (action == null)
			return;
		
		if (currentManager.find(menuID + Messages.getString("PIPageEditor.includeActionEnding")) == null) { //$NON-NLS-1$
			includeManager = new MenuManager(Messages.getString("PIPageEditor.includeOtherFile"), menuID + Messages.getString("PIPageEditor.includeActionEnding")); //$NON-NLS-1$ //$NON-NLS-2$
			currentManager.add(includeManager);
		}
			
		includeManager.add(action);
	}

	public static void addReportAction(IAction action) {
		if (action == null)
			return;
		
		currentManager.appendToGroup(PIPageEditor.reportsGroup, action);
	}

	public static void addReportManager(IMenuManager menuManager) {
		if (menuManager == null)
			return;
		
		currentManager.appendToGroup(PIPageEditor.reportsGroup, menuManager);
	}

	public static void addViewOptionManager(IMenuManager menuManager) {
		if (menuManager == null)
			return;
		
		if (menuManager == currentManager)
			return;
		
		currentManager.appendToGroup(PIPageEditor.viewOptionsGroup, menuManager);
	}

	/**
	 * The <code>MultiPageEditorPart</code> implementation of this 
	 * <code>IWorkbenchPart</code> method disposes all nested editors.
	 * Subclasses may extend.
	 */
	public void dispose() {
		getSite().getPage().removePartListener(partListener);
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		ArrayList<AbstractPiPlugin> plugins = NpiInstanceRepository.getInstance().getPlugins(uid);
		for (AbstractPiPlugin plugin : plugins) {
			if (plugin instanceof IFinalizeTrace) {
				((IFinalizeTrace)plugin).runOnDispose();
			}
		}

		NpiInstanceRepository.getInstance().unregister(uid);
		if (NpiInstanceRepository.getInstance().size() == 0) {
			// free up the current file's items
			setCurrentPageEditor(null);
			setCurrentUid(0);
			if (NpiInstanceRepository.getInstance().activeUid() == uid) {
				NpiInstanceRepository.getInstance().switchActiveUid(NpiInstanceRepository.DISPOSED_UID);
			}
			TraceDataRepository.getInstance().removeAll();
			PluginInitialiser.removeAllTraceInstances();

			// stop using the feature
			com.nokia.carbide.cpp.internal.featureTracker.FeatureUseTrackerPlugin.getFeatureUseProxy().stopUsingFeature(FeatureUseTrackerConsts.CARBIDE_PROFILER);
		}
		super.dispose();
		System.gc();
	}

	/**
	 * Saves the multi-page editor's document.
	 */
	public void doSave(IProgressMonitor monitor) {
		final IFileEditorInput newInput= new FileEditorInput(((IFileEditorInput)getEditorInput()).getFile());
		saveDocument(newInput);
	}
	
	/**
	 * Saves the multi-page editor's document as another file.
	 * Also updates the text for page 0's tab, and updates this multi-page editor's input
	 * to correspond to the nested editor's.
	 */
	public void doSaveAs() {
		SaveAsDialog dialog = new SaveAsDialog(getSite().getShell());
		
		dialog.open();
		IPath filePath = dialog.getResult();
		
		if (filePath == null)
			return;
		if (filePath.getFileExtension() == null || filePath.getFileExtension().equals("npi") == false) { //$NON-NLS-1$
			MessageBox errorMessage = new MessageBox(getSite().getShell(), SWT.PRIMARY_MODAL | SWT.ICON_ERROR);
			errorMessage.setText(Messages.getString("PIPageEditor.error")); //$NON-NLS-1$
			errorMessage.setMessage(Messages.getString("PIPageEditor.mustBeNpi")); //$NON-NLS-1$
			errorMessage.open();
			return;
		}
		
		IWorkspaceRoot workspaceRoot= ResourcesPlugin.getWorkspace().getRoot();
		IFile file= workspaceRoot.getFile(filePath);
		final IFileEditorInput newInput= new FileEditorInput(file);

		saveDocument(newInput);
		
		dialog.close();
	}
	
	public boolean isSaveAsAllowed() {
		return true;
	}
	
	public boolean isDirty() {
		return dirty;
	}
	
	public boolean isSaveOnCloseNeeded() {
		return true;
	}
	
	public void setDirty() {
		dirty = true;
		this.firePropertyChange(PROP_DIRTY);
		
//		for (int i = 0; i < pageEditorList.size(); i++) {
//			if (   (pageEditorList.get(i) != this)
//				&& (pageEditorList.get(i).getFullPath().equals(this.getFullPath())))
//				pageEditorList.get(i).setDirtyOnce();
//		}
	}
	
	public void setDirtyOnce() {
		dirty = true;
		this.firePropertyChange(PROP_DIRTY);
	}

	public void resetDirty() {
		dirty = false;
		this.firePropertyChange(PROP_DIRTY);
		
//		for (int i = 0; i < pageEditorList.size(); i++) {
//			if (   (pageEditorList.get(i) != this)
//				&& (pageEditorList.get(i).getFullPath().equals(this.getFullPath())))
//				pageEditorList.get(i).resetDirtyOnce();
//		}
	}

	public void resetDirtyOnce() {
		dirty = false;
		this.firePropertyChange(PROP_DIRTY);
	}
	
	// Try to get close to saveDocument in IDocumentProvider in case we do rewrite
	// see JDT CompilationUnitEditor.java for reference
	public void saveDocument(IFileEditorInput newInput) {
		// this block would be getDocumentProvider().saveDocument() in rewrite
		IFile newFile = newInput.getFile();
//		IFile oldFile = ((IFileEditorInput)getEditorInput()).getFile();
		
		
		try {
			// the Dialog already asked the user if they want to overwrite
			// grant them the wish and remove the file
			if(!newFile.exists()) {
				newFile.create(new ByteArrayInputStream("".getBytes()), false, null);	//$NON-NLS-1$
				newFile.refreshLocal(0, null);
			}
			AnalyserDataProcessor.getInstance().saveAnalysis(newFile.getLocation().toString(), uid);			
			newFile.refreshLocal(0, null);
			setInput(newInput);
			setPartName(newInput.getName());
			resetDirty();
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	public void gotoMarker(IMarker marker) {
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
	}
	
	/**
	 * The <code>MultiPageEditorExample</code> implementation of this method
	 * checks that the input is an instance of <code>IFileEditorInput</code>.
	 */
	public void init(IEditorSite site, IEditorInput editorInput)
		throws PartInitException {
		if (!(editorInput instanceof IURIEditorInput))
			throw new PartInitException(Messages.getString("PIPageEditor.mustBeIURIEditorInput")); //$NON-NLS-1$

		// store the full path
//		this.fullPath = ((FileEditorInput)editorInput).getPath().toFile().getAbsolutePath();

		super.init(site, editorInput);

		// now the site is initialized, so start using the feature
		com.nokia.carbide.cpp.internal.featureTracker.FeatureUseTrackerPlugin.getFeatureUseProxy().startUsingFeature(FeatureUseTrackerConsts.CARBIDE_PROFILER);

		// allocate a few fonts
		createFonts(getEditorSite().getShell().getDisplay());
		site.getPage().addPartListener(partListener = new IPartListener(){
			
			private void setMenus(IWorkbenchPart part) {
				if (!(part instanceof PIPageEditor) || isErrorPage)
					return;
				
				PIPageEditor editor = (PIPageEditor) part;
				currentPageEditor.setActiveActions(false);
				currentPageEditor = editor;
				
				setTime(editor.getStartTime(),editor.getEndTime());
				NpiInstanceRepository.getInstance().switchActiveUid(currentPageEditor().uid);
				addTabMenuItems(currentPageEditor().uid);
				currentPageEditor.setActiveActions(true);
			}

			public void partActivated(IWorkbenchPart part) {
				setMenus(part);
			}

			public void partBroughtToTop(IWorkbenchPart part) {
				setMenus(part);
			}

			public void partClosed(IWorkbenchPart part) {
			}

			public void partDeactivated(IWorkbenchPart part) {
			}

			public void partOpened(IWorkbenchPart part) {
			}
		});

		setSite(site);
		setInput(editorInput);
		this.setPartName(editorInput.getName());

		// set up registry of PI plugins
		PluginRegisterer.registerAllPlugins();
	}
	
//	public String getFullPath() {
//		return this.fullPath;
//	}

	public static void setTime(double start, double end) {
		// set the visible time
//		PIPageEditorContributor.getTimeSet().setTime(start, end);

		// set the local copy of the time
		if (currentPageEditor != null)
			currentPageEditor.setLocalTime(start, end);
	}

	public void setLocalTime(double start, double end) {
		this.startTime = start;
		this.endTime   = end;
	}

	protected void setLocalStartTime(double start) {
		this.startTime = start;
	}

	protected void setLocalEndTime(double end) {
		this.endTime   = end;
	}
	
	public void setMaxEndTime(double maxEndTime) {
		this.maxEndTime = maxEndTime;
	}

	public double getStartTime() {
		return startTime;
	}

	public double getEndTime() {
		return endTime;
	}

	public double getMaxEndTime() {
		return maxEndTime;
	}
	
	public static void createActions() {
		URL url;
		ImageDescriptor createFromURL;
		
		Bundle piBundle = Platform.getBundle("com.nokia.carbide.cpp.pi"); //$NON-NLS-1$
		if (piBundle == null)
			return;
		
		zoomInAction = new Action(Messages.getString("PIPageEditor.zoomIn")) { //$NON-NLS-1$
			public void run() {
				PIChangeEvent.action("-"); //$NON-NLS-1$
			}
		};
		zoomInAction.setToolTipText(Messages.getString("PIPageEditor.zoomIn")); //$NON-NLS-1$
		url = Platform.find(piBundle, new Path(Messages.getString("PIPageEditorContributor.zoomInIcon"))); //$NON-NLS-1$
		if (url != null) {
			createFromURL = ImageDescriptor.createFromURL(url);
			zoomInAction.setImageDescriptor(createFromURL);
		}

		zoomOutAction = new Action(Messages.getString("PIPageEditor.zoomOut")) { //$NON-NLS-1$
			public void run() {
				PIChangeEvent.action("+"); //$NON-NLS-1$
			}
		};
		zoomOutAction.setToolTipText(Messages.getString("PIPageEditor.zoomOut"));  //$NON-NLS-1$
		url = Platform.find(piBundle, new Path(Messages.getString("PIPageEditor.PIPageEditorContributor.zoomOutIcon"))); //$NON-NLS-1$
		if (url != null) {
			createFromURL = ImageDescriptor.createFromURL(url);
			zoomOutAction.setImageDescriptor(createFromURL);
		}

		zoomToTraceAction = new Action(Messages.getString("PIPageEditor.showEntireGraph")) { //$NON-NLS-1$
			public void run() {
				PIChangeEvent.action("++"); //$NON-NLS-1$
			}
		};
		zoomToTraceAction.setToolTipText(Messages.getString("PIPageEditor.showEntireGraph"));  //$NON-NLS-1$
		url = Platform.find(piBundle, new Path(Messages.getString("PIPageEditor.PIPageEditorContributor.showEntireGraphIcon"))); //$NON-NLS-1$
		if (url != null) {
			createFromURL = ImageDescriptor.createFromURL(url);
			zoomToTraceAction.setImageDescriptor(createFromURL);
		}

		zoomToSelectionAction = new Action(Messages.getString("PIPageEditor.zoomToInterval")) { //$NON-NLS-1$
			public void run() {
				PIChangeEvent.action("--"); //$NON-NLS-1$
			}
		};
		zoomToSelectionAction.setToolTipText(Messages.getString("PIPageEditor.zoomToInterval"));  //$NON-NLS-1$
		url = Platform.find(piBundle, new Path(Messages.getString("PIPageEditor.PIPageEditorContributor.ZoomToIntervalIcon"))); //$NON-NLS-1$
		if (url != null) {
			createFromURL = ImageDescriptor.createFromURL(url);
			zoomToSelectionAction.setImageDescriptor(createFromURL);
		}

		selectTimeAction = new Action(Messages.getString("PIPageEditor.selectInterval")) { //$NON-NLS-1$
			public void run() {
				// remember the old time interval
				double startTime = PIPageEditor.currentPageEditor().getStartTime();
				double endTime   = PIPageEditor.currentPageEditor().getEndTime();
				
				// get the new time interval
				new TimeSetDialog(
						PIPageEditor.currentPageEditor.getSite().getShell().getDisplay(),
						startTime, endTime);
				
				if (   (startTime == PIPageEditor.currentPageEditor().getStartTime())
					&& (endTime   == PIPageEditor.currentPageEditor().getEndTime()))
					return;
				
				// propagate the new time interval
				PIChangeEvent.action("changeInterval"); //$NON-NLS-1$
				
				// after the graphs have been updated, notify plugins that might have tables but no graphs
        		Enumeration enu = PluginInitialiser.getPluginInstances(
        									"com.nokia.carbide.cpp.internal.pi.plugin.model.IEventListener"); //$NON-NLS-1$
        		if (enu != null) {
        			Event event = new Event();
        			Double[] times = new Double[2];
           			times[0] = PIPageEditor.currentPageEditor().getStartTime();
           			times[1] = PIPageEditor.currentPageEditor().getEndTime();
           			event.data = times;
        			
            		while (enu.hasMoreElements())
            		{
            			IEventListener plugin = (IEventListener)enu.nextElement();
            			plugin.receiveEvent("changeInterval", event); //$NON-NLS-1$
            		}
        		}
			}
		};
		selectTimeAction.setToolTipText(Messages.getString("PIPageEditor.selectInterval"));  //$NON-NLS-1$
		url = Platform.find(piBundle, new Path(Messages.getString("PIPageEditor.PIPageEditorContributor.selectIntervalIcon"))); //$NON-NLS-1$
		if (url != null) {
			createFromURL = ImageDescriptor.createFromURL(url);
			selectTimeAction.setImageDescriptor(createFromURL);
		}
	}

	
	public static Action getZoomInAction() {
		return zoomInAction;
	}
	
	public static Action getZoomOutAction() {
		return zoomOutAction;
	}
	
	public static Action getZoomToSelectionAction() {
		return zoomToSelectionAction;
	}
	
	public static Action getZoomToTraceAction() {
		return zoomToTraceAction;
	}

	public static Action getSelectTimeAction() {
		return selectTimeAction;
	}

	/**
	 * Closes all project files on project close.
	 */
	public void resourceChanged(final IResourceChangeEvent event){
		IResourceDelta delta = event.getDelta();

		if (delta == null)
			return;

		if (delta.getKind() == IResourceDelta.CHANGED) {
			IPath editorPath = ((IFileEditorInput)getEditorInput()).getFile().getFullPath();
			// see if current change event is relevant to this Editor
			delta = delta.findMember(editorPath);
			if (delta != null) {
				int deltaKind = delta.getKind();
				switch(deltaKind) {
				case IResourceDelta.REMOVED:
				case IResourceDelta.REMOVED_PHANTOM:
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
								PIPageEditor.this.getSite().getPage().closeEditor(PIPageEditor.this, false);
						}
					});
					break;
				}
			}
		}
	}
	
	protected void pageChange(int newPageIndex) {
		// on a page change, set the current page's leaving actions and the new pages' entering action
		if (this.activePageIndex != -1)
			setActions(false, this.activePageIndex);
		super.pageChange(newPageIndex);
		setCurrentPageIndex(newPageIndex);
		setActions(true, newPageIndex);
		this.activePageIndex = newPageIndex;
	}
	
	// set the actions on entering or leaving the current page
	public void setActiveActions(boolean entering) {
		if (this.activePageIndex != -1)
			setActions(entering, this.activePageIndex);
	}
	
	private void setActions(boolean entering, int pageIndex) {
		Enumeration enu;
		AbstractPiPlugin plugin;

		enu = PluginInitialiser.getPluginInstances(uid, "com.nokia.carbide.cpp.internal.pi.interfaces.IToolBarActionListener"); //$NON-NLS-1$
		while (enu.hasMoreElements())
		{
			// can only set actions if the plugin is associated with this tab
			plugin = (AbstractPiPlugin)enu.nextElement();
			if (   (plugin instanceof IToolBarActionListener)
				&& (NpiInstanceRepository.getInstance().getPlugins(uid) != null)
				&& (NpiInstanceRepository.getInstance().getPlugins(uid).indexOf(plugin) >= 0)) {
				((IToolBarActionListener)plugin).setActions(entering, pageIndex);
			}
		}
	}
	
	public int getCurrentPage() {
		return getActivePage();
	}
	
	// whether tooltips are enabled for this file
	public boolean getTooltipsEnabled() {
		return this.tooltipsEnabled;
	}
	
	public void setTooltipsEnabled(boolean tooltipsEnabled) {
		SessionPreferences.getInstance().setToolTipsEnabled(tooltipsEnabled);
    	GUITooltips.setTooltipsOn(tooltipsEnabled);
		this.tooltipsEnabled = tooltipsEnabled;
	}
	
	// menu bar changes let tables and graphs change IDE menu items when focus changes
	public static void setMenuManager(IMenuManager menuManager) {
		PIPageEditor.menuManager = menuManager;
	}
	
	public static IMenuManager getMenuManager() {
		return PIPageEditor.menuManager;
	}
	
	// action bar changes let tables and graphs change IDE menu items when focus changes
	public static void setActionBars(IActionBars actionBars) {
		PIPageEditor.actionBars = actionBars;
	}
	
	public static IActionBars getActionBars() {
		return PIPageEditor.actionBars;
	}

	private static void setCurrentPageIndex(int pageIndex)
	{
		currentPageIndex = pageIndex;
	}

	private static void setCurrentPageEditor(PIPageEditor pageEditor)
	{
		currentPageEditor = pageEditor;
	}
	
	private static void setCurrentUid(int uid)
	{
		currentUid = uid;
	}

	private void setIsErrorPage(boolean isIt)
	{
		isErrorPage = isIt;
	}
	
	private static void addTabMenuItems(int uid) {
		ProfileReader.getInstance().setTraceMenus(NpiInstanceRepository.getInstance().getPlugins(uid), uid);
	}
}
