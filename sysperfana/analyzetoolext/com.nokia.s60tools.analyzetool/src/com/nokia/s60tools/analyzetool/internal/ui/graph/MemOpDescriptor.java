/*
 * Copyright (c) 2008-2010 Nokia Corporation and/or its subsidiary(-ies).
 * All rights reserved.
 * This component and the accompanying materials are made available
 * under the terms of "Eclipse Public License v1.0"
 * which accompanies this distribution, and is available
 * at the URL "http://www.eclipse.org/legal/epl-v10.html".
 *
 * Initial Contributors:
 * Nokia Corporation - initial contribution.
 *
 * Contributors:
 *
 * Description:  Definitions for the class MemOpDescriptor
 *
 */

package com.nokia.s60tools.analyzetool.internal.ui.graph;

import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.nokia.carbide.cdt.builder.CarbideBuilderPlugin;
import com.nokia.carbide.cdt.builder.project.ICarbideProjectInfo;
import com.nokia.cdt.debug.cw.symbian.symbolreader.IFunction;
import com.nokia.cdt.debug.cw.symbian.symbolreader.ISourceLocation;
import com.nokia.cdt.debug.cw.symbian.symbolreader.ISymbolFile;
import com.nokia.s60tools.analyzetool.Activator;
import com.nokia.s60tools.analyzetool.engine.ICallstackManager;
import com.nokia.s60tools.analyzetool.engine.statistic.AllocCallstack;
import com.nokia.s60tools.analyzetool.engine.statistic.AllocInfo;
import com.nokia.s60tools.analyzetool.engine.statistic.BaseInfo;
import com.nokia.s60tools.analyzetool.engine.statistic.DllLoad;
import com.nokia.s60tools.analyzetool.engine.statistic.SourceFile;
import com.nokia.s60tools.analyzetool.engine.statistic.SymReader;
import com.nokia.s60tools.analyzetool.global.Constants;
import com.nokia.s60tools.analyzetool.global.Util;
import com.nokia.s60tools.analyzetool.internal.ui.util.GraphUtils;

/**
 * MemOpDescriptor provides properties of any Alloc/Leak/Free suitable to be
 * displayed in eclipse Properties View.
 * 
 */
@SuppressWarnings("restriction")
class MemOpDescriptor implements IPropertySource {
	BaseInfo memInfo;

	/** process Id descriptor */
	private static final String PID_ID = "pid"; //$NON-NLS-1$
	private final DotTextPropertyDescriptor PID_DESCRIPTOR = new DotTextPropertyDescriptor(
			PID_ID, Messages.MemOpDescriptor_1);

	/** Memory operation type Allocation/Leak/Free descriptor */
	private static final String TYPE_ID = "type"; //$NON-NLS-1$
	private final DotTextPropertyDescriptor TYPE_DESCRIPTOR = new DotTextPropertyDescriptor(
			TYPE_ID, Messages.MemOpDescriptor_3);

	/** Time descriptor */
	private static final String TIME_ID = "time"; //$NON-NLS-1$
	private final DotTextPropertyDescriptor TIME_DESCRIPTOR = new DotTextPropertyDescriptor(
			TIME_ID, Messages.MemOpDescriptor_5);

	/** size descriptor */
	private static final String SIZE_ID = "size"; //$NON-NLS-1$
	private final DotTextPropertyDescriptor SIZE_DESCRIPTOR = new DotTextPropertyDescriptor(
			SIZE_ID, Messages.MemOpDescriptor_7);

	/** memory address descriptor */
	private static final String ADDR_ID = "address"; //$NON-NLS-1$
	private final DotTextPropertyDescriptor ADDR_DESCRIPTOR = new DotTextPropertyDescriptor(
			ADDR_ID, Messages.MemOpDescriptor_9);

	/** total memory size consumed by the process descriptor */
	private static final String TSIZE_ID = "total"; //$NON-NLS-1$
	private final DotTextPropertyDescriptor TSIZE_DESCRIPTOR = new DotTextPropertyDescriptor(
			TSIZE_ID, Messages.MemOpDescriptor_11);

	/** thread id descriptor */
	private static final String THREAD_ID = "thread"; //$NON-NLS-1$
	private final DotTextPropertyDescriptor THREAD_DESCRIPTOR = new DotTextPropertyDescriptor(
			THREAD_ID, Messages.MemOpDescriptor_10);

	/**
	 * life time of an allocation descriptor. This applies only to non Leaked
	 * allocations
	 */
	private static final String LIFETIME_ID = "lifetime"; //$NON-NLS-1$
	private final DotTextPropertyDescriptor LIFETIME_DESCRIPTOR = new DotTextPropertyDescriptor(
			LIFETIME_ID, Messages.MemOpDescriptor_13);
	private static final String ATTRIBUTES_GROUP = "Attributes"; //$NON-NLS-1$
	/** used for making absolute time values relative */
	private long baseTime;

	/** callstack item descriptor id */
	private static final String CALL_STACK_ID = "callstack"; //$NON-NLS-1$

	/** current project */
	IProject iCurrentProject = null;
	/** Symbol Reader */
	SymReader iSymReader = null;
	/** c++ files from the current project */
	AbstractList<String> cppFileNames;
	static final String LINE_SEPARATOR = " :: "; //$NON-NLS-1$

	private ICallstackManager callstackManager;

	/**
	 * Constructor
	 * 
	 * @param newBaseTime
	 *            usually process start time, used for making absolute time
	 *            values relative
	 * @param info
	 *            the alloc or free info
	 * @param project
	 *            IProject to use for locating source file
	 * @param symReader
	 *            the SymReader to use for pinpointing
	 * @param cppFiles
	 * @param callstackManager
	 *            CallstackManager for reading callstacks from BaseInfo
	 */
	public MemOpDescriptor(Long newBaseTime, BaseInfo info, IProject project,
			SymReader symReader, AbstractList<String> cppFiles,
			ICallstackManager callstackManager) {
		memInfo = info;
		iCurrentProject = project;
		iSymReader = symReader;
		cppFileNames = cppFiles;
		this.baseTime = newBaseTime;
		this.callstackManager = callstackManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.properties.IPropertySource#getEditableValue()
	 */
	public Object getEditableValue() {
		return null; // no edit
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.views.properties.IPropertySource#getPropertyDescriptors()
	 */
	public IPropertyDescriptor[] getPropertyDescriptors() {
		final List<IPropertyDescriptor> completeList = new ArrayList<IPropertyDescriptor>();
		PID_DESCRIPTOR.setCategory(ATTRIBUTES_GROUP);
		completeList.add(PID_DESCRIPTOR);
		TYPE_DESCRIPTOR.setCategory(ATTRIBUTES_GROUP);
		completeList.add(TYPE_DESCRIPTOR);
		TIME_DESCRIPTOR.setCategory(ATTRIBUTES_GROUP);
		completeList.add(TIME_DESCRIPTOR);
		SIZE_DESCRIPTOR.setCategory(ATTRIBUTES_GROUP);
		completeList.add(SIZE_DESCRIPTOR);
		ADDR_DESCRIPTOR.setCategory(ATTRIBUTES_GROUP);
		completeList.add(ADDR_DESCRIPTOR);
		TSIZE_DESCRIPTOR.setCategory(ATTRIBUTES_GROUP);
		completeList.add(TSIZE_DESCRIPTOR);
		THREAD_DESCRIPTOR.setCategory(ATTRIBUTES_GROUP);
		completeList.add(THREAD_DESCRIPTOR);
		if (memInfo instanceof AllocInfo && ((AllocInfo) memInfo).isFreed()) {
			LIFETIME_DESCRIPTOR.setCategory(ATTRIBUTES_GROUP);
			completeList.add(LIFETIME_DESCRIPTOR); // only non leaks
		}
		// add callstack descriptors
		if (callstackManager != null && callstackManager.hasCallstack(memInfo)) {
			try {
				List<AllocCallstack> callstack = callstackManager
						.readCallstack(memInfo);
				if (callstack != null) {
					for (int i = 0; i < callstack.size(); i++) {

						final DotTextPropertyDescriptor propDesc = new DotTextPropertyDescriptor(
								i, CALL_STACK_ID);
						propDesc.setCategory("CallStack"); //$NON-NLS-1$
						completeList.add(propDesc);
					}
				}
			} catch (IOException e) {
				// since callstacks aren't fatal and we can't handle it usefully
				// here, log this to the error log
				Activator.getDefault().log(IStatus.ERROR,
						Messages.MemOpDescriptor_18, e);
			}
		}
		return completeList.toArray(new TextPropertyDescriptor[completeList
				.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.views.properties.IPropertySource#getPropertyValue(java
	 * .lang.Object)
	 */
	public Object getPropertyValue(Object id) {
		// Note, this method must return String values for the CellEditor to
		// work
		if (PID_ID.equals(id)) {
			return String.valueOf(memInfo.getProcessID());// process ids are
			// usually decimal
			// values
		}
		if (TYPE_ID.equals(id))
			return memInfo instanceof AllocInfo ? ((AllocInfo) memInfo)
					.isFreed() ? Messages.MemOpDescriptor_19
					: Messages.MemOpDescriptor_20 : Messages.MemOpDescriptor_21;
		if (TIME_ID.equals(id))
			return GraphUtils.renderTime(memInfo.getTime() - baseTime);
		if (SIZE_ID.equals(id))
			return String.format(Messages.MemOpDescriptor_22, memInfo
					.getSizeInt());
		if (ADDR_ID.equals(id))
			return Long.toString(memInfo.getMemoryAddress(), 16);
		if (TSIZE_ID.equals(id))
			return String.format(Messages.MemOpDescriptor_23, memInfo
					.getTotalMem());
		if (THREAD_ID.equals(id))
			return String.valueOf(memInfo.getThreadId());
		if (LIFETIME_ID.equals(id)) {
			if (memInfo instanceof AllocInfo && ((AllocInfo) memInfo).isFreed()) {
				AllocInfo info = (AllocInfo) memInfo;
				return GraphUtils.renderTime(info.getFreedBy().getTime()
						- info.getTime());
			}
			throw new IllegalStateException(
					"Should not happen because we did not provide a lifetime descriptor for leak and free."); //$NON-NLS-1$
		}
		if (id instanceof Integer && callstackManager.hasCallstack(memInfo)) {
			int callstackId = (Integer) id;
			try {
				List<AllocCallstack> callstackList = callstackManager
						.readCallstack(memInfo);
				if (callstackId < callstackList.size()) {
					AllocCallstack callstackItem = callstackList
							.get(callstackId);
					DllLoad tempLoad = callstackItem.getDllLoad();
					long addr = callstackItem.getMemoryAddress();

					String name = String.format(Messages.MemOpDescriptor_25,
							addr);
					if (tempLoad != null
							&& callstackItem.getMemoryAddress() != tempLoad
									.getStartAddress()) {
						SourceFile aSourcefile = pinpoint(callstackItem
								.getMemoryAddress(), callstackItem.getDllLoad());
						if (aSourcefile != null) { // callstack resolved to a
							// file-function-line
							return name + LINE_SEPARATOR
									+ callstackItem.getDllLoad().getName()
									+ LINE_SEPARATOR
									+ aSourcefile.getFileName()
									+ LINE_SEPARATOR
									+ aSourcefile.getFunctionName()
									+ LINE_SEPARATOR
									+ aSourcefile.getLineNumber();
						}
					}
					return name
							+ (tempLoad != null ? LINE_SEPARATOR
									+ tempLoad.getName() : ""); //$NON-NLS-1$
				}
			} catch (IOException e) {
				// since callstacks aren't fatal and we can't handle it usefully
				// here, log this to the error log
				Activator.getDefault().log(IStatus.ERROR,
						Messages.MemOpDescriptor_27, e);
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.views.properties.IPropertySource#isPropertySet(java.lang
	 * .Object)
	 */
	public boolean isPropertySet(Object id) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.views.properties.IPropertySource#resetPropertyValue(java
	 * .lang.Object)
	 */
	public void resetPropertyValue(Object id) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.views.properties.IPropertySource#setPropertyValue(java
	 * .lang.Object, java.lang.Object)
	 */
	public void setPropertyValue(Object id, Object value) {
		// do nothing
	}

	/**
	 * DotTextPropertyDescriptor defines a read only TextPropertyDescriptor It
	 * also add a customised key/mouse listener. This listener will be useful
	 * for pinpointing the source.
	 * 
	 */
	private class DotTextPropertyDescriptor extends TextPropertyDescriptor {

		/**
		 * Constructor
		 * 
		 * @param id
		 *            Descriptor Id
		 * @param displayName
		 *            Descriptor display name
		 */
		public DotTextPropertyDescriptor(Object id, String displayName) {
			super(id, displayName);
		}

		/**
		 * create a Read Only TextCellEditor and add the defined listener to its
		 * Control.
		 * 
		 * @see org.eclipse.ui.views.properties.TextPropertyDescriptor#createPropertyEditor(org.eclipse.swt.widgets.Composite)
		 */
		@Override
		public CellEditor createPropertyEditor(Composite parent) {
			TextCellEditor editor = new SimpleTextCellEditor(parent,
					SWT.READ_ONLY);
			Text control = (Text) editor.getControl();
			control.setEditable(false);

			DotPropKeyMouseListener listener = new DotPropKeyMouseListener();
			control.addKeyListener(listener);
			control.addMouseListener(listener);
			if (getValidator() != null) {
				editor.setValidator(getValidator());
			}
			return editor;
		}
	}

	private class SimpleTextCellEditor extends TextCellEditor {
		public SimpleTextCellEditor(Composite parent, int readOnly) {
			super(parent, readOnly);
		}

		@Override
		public boolean isCopyEnabled() {
			return false;
		}

		@Override
		public boolean isCutEnabled() {
			return false;
		}

		@Override
		public boolean isPasteEnabled() {
			return false;
		}

		@Override
		public boolean isFindEnabled() {
			return false;
		}
	}

	/**
	 * This class is only for convenience for the moment. It's very likely that
	 * the listener will be provided by AnalyzeToolGraph
	 * 
	 */
	private class DotPropKeyMouseListener implements KeyListener, MouseListener {

		// Key

		public void keyPressed(org.eclipse.swt.events.KeyEvent e) {
			// do nothing by design
		}

		public void keyReleased(org.eclipse.swt.events.KeyEvent keyEvent) {
			// System.out.println("key released.");
			if (keyEvent.character == '\r') {
				String text = ((Text) keyEvent.getSource()).getText();
				if (text != null) {
					String[] segs = text.split(LINE_SEPARATOR);
					if (segs.length == 5) {
						openEditor(segs[2], segs[4]);
					}
				}
			}
		}

		// Mouse

		public void mouseDoubleClick(MouseEvent e) {
			String text = ((Text) e.getSource()).getText();
			if (text != null) {
				String[] segs = text.split(LINE_SEPARATOR);
				if (segs.length == 5) {
					openEditor(segs[2], segs[4]);
				}
			}
		}

		public void mouseDown(MouseEvent e) {
			// do nothing by design
		}

		public void mouseUp(MouseEvent e) {
			// do nothing by design
		}
	}

	/**
	 * Pinpoints one memory address to source code line.
	 * 
	 * @param memoryAddress
	 *            Memory address
	 * @param dllLoad
	 *            DllLoad item
	 * @return SourceFile if found otherwise null
	 */
	private SourceFile pinpoint(Long memoryAddress, DllLoad dllLoad) {

		if (dllLoad != null) {

			ISymbolFile symbolFile = iSymReader.getSymbolFile(
					dllLoad.getName(), false);
			if (symbolFile != null) {
				return pinpointToSrcLine(symbolFile, memoryAddress, dllLoad);
			}
		}
		return null;
	}

	/**
	 * Pinpoints memory address to source code line
	 * 
	 * @param symbolFile
	 *            Opened symbol file
	 * @param memoryAddress
	 *            Used memory address
	 * @param dllLoad
	 *            DllLoad object where to memory address belongs
	 * @return SourceFile
	 */
	private SourceFile pinpointToSrcLine(ISymbolFile symbolFile,
			Long memoryAddress, DllLoad dllLoad) {

		ICarbideProjectInfo cpi = CarbideBuilderPlugin.getBuildManager()
				.getProjectInfo(iCurrentProject);
		String platform = cpi.getDefaultConfiguration().getPlatformString();

		try {
			// this is the start address of each symbol file
			long defaultLinkAddress = 0;

			// if the platform is other than winscw => adjust the memory address
			if (!(Constants.BUILD_TARGET_WINSCW).equalsIgnoreCase(platform)) {
				defaultLinkAddress = 32768L;
			} else {
				defaultLinkAddress = -4096L;
			}

			// calculate memory address in symbol file
			long calculated = (memoryAddress - dllLoad.getStartAddress())
					+ defaultLinkAddress;

			java.math.BigInteger bigAddress = new java.math.BigInteger(Long
					.toHexString(calculated), 16);
			IFunction func = symbolFile.findFunctionByAddress(bigAddress);
			ISourceLocation loc = symbolFile.findSourceLocation(bigAddress);
			if (func != null && loc != null) {
				String sourceFile = loc.getSourceFile();
				if (sourceFile == null || sourceFile.equalsIgnoreCase("")) //$NON-NLS-1$
					return null;
				int lineNumber = loc.getLineNumber();
				if (lineNumber == 0)
					return null;
				String name = func.getName();
				if (name == null || name.equalsIgnoreCase("")) //$NON-NLS-1$
					return null;
				/*
				 * if( onlyForProjectFiles &&
				 * !isFilePartOfTheProject(loc.getSourceFile()) ) return null;
				 */
				SourceFile file = new SourceFile();
				file.setFileName(sourceFile);
				file.setLineNumber(lineNumber);
				file.setFunctionName(name);
				return file;
			}
		} catch (java.lang.NumberFormatException nfe) {
			// do nothing by design
			nfe.printStackTrace();
		} catch (Exception e) {
			// do nothing by design
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Opens current callstack item on default editor and pinpoints memory leak
	 * line
	 * 
	 * @param cppFileName
	 *            Cpp file name
	 * @param lineNumber
	 *            Cpp file line number
	 */
	private void openEditor(String cppFileName, String lineNumber) {

		// check that used information is given
		// we need to know file name and file line number
		// that we could open the right line in editor
		if (cppFileName == null || ("").equals(cppFileName) //$NON-NLS-1$
				|| lineNumber == null || ("").equals(lineNumber)) { //$NON-NLS-1$
			return;
		}
		try {
			IFile file = null;
			String usedFileName = null;
			usedFileName = getFileNames(cppFileName);
			if (iCurrentProject.isOpen()) {
				file = ResourcesPlugin.getWorkspace().getRoot().getFile(
						new Path(iCurrentProject.getName()
								+ usedFileName.toLowerCase(Locale.US)));
			}

			// if file not found in active project
			if (file == null || !file.exists()) {
				IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace()
						.getRoot();
				IProject[] projects = myWorkspaceRoot.getProjects();
				for (int i = 0; i < projects.length; i++) {
					file = ResourcesPlugin.getWorkspace().getRoot().getFile(
							new Path(projects[i].getName() + "\\" //$NON-NLS-1$
									+ usedFileName));

					// file found => skip the rest of the projects
					if (file != null && file.exists()) {
						break;
					}
				}
			}

			// if file still not found
			// display info to user and leave
			if (file == null || !file.exists()) {
				Util.showMessage(Constants.SOURCE_NOT_FOUND);
				return;
			}

			IWorkbenchPage page = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();

			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put(IMarker.LINE_NUMBER, Integer.parseInt(lineNumber));
			// map.put(IDE.EDITOR_ID_ATTR,
			// "org.eclipse.jdt.ui.ClassFileEditor");
			map.put(IDE.EDITOR_ID_ATTR, Constants.SOURCE_FILE_EDITOR_ID);
			IMarker marker = file.createMarker(IMarker.TEXT);
			marker.setAttributes(map);
			IDE.openEditor(page, marker, true);

		} catch (PartInitException pie) {
			pie.printStackTrace();
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		} catch (CoreException ce) {
			ce.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Find symbol reader api pinpointed class file for project class files.
	 * 
	 * @param fileName
	 *            cpp file name
	 * @return found cpp file location
	 */
	private String getFileNames(String fileName) {
		int slash = Util.getLastSlashIndex(fileName);
		String tempFile = fileName.substring(slash + 1, fileName.length());

		String realFileName = fileName;
		for (String tempFileName : cppFileNames) {
			int slashTemp = Util.getLastSlashIndex(tempFileName);
			String tempFileWithoutExt = tempFileName.substring(slashTemp + 1,
					tempFileName.length());
			if (tempFileWithoutExt.equalsIgnoreCase(tempFile)) {
				realFileName = tempFileName;
				break;
			}
		}
		return realFileName;
	}
}
