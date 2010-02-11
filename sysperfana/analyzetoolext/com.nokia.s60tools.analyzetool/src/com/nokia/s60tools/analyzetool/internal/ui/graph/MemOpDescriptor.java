/*
 * Copyright (c) 2008-2009 Nokia Corporation and/or its subsidiary(-ies).
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
 * MemOpDescriptor provides properties of any Alloc/Leak/Free suitable to be displayed in eclipse
 * Properties View.
 * 
 */
@SuppressWarnings("restriction")
class MemOpDescriptor implements IPropertySource {
	BaseInfo memInfo;
	
	/** process Id descriptor */
	private static final String PID_ID = "pid";
	private final DotTextPropertyDescriptor PID_DESCRIPTOR = new DotTextPropertyDescriptor(PID_ID, "Process Id");

	/** Memory operation type Allocation/Leak/Free descriptor */
	private static final String TYPE_ID = "type";
	private final DotTextPropertyDescriptor TYPE_DESCRIPTOR = new DotTextPropertyDescriptor(TYPE_ID, "Operation Type");

	/** Time descriptor */
	private static final String TIME_ID = "time";
	private final DotTextPropertyDescriptor TIME_DESCRIPTOR = new DotTextPropertyDescriptor(TIME_ID, "Time");

	/** size descriptor */
	private static final String SIZE_ID = "size";
	private final DotTextPropertyDescriptor SIZE_DESCRIPTOR = new DotTextPropertyDescriptor(SIZE_ID, "Size");

	/** memory address descriptor */
	private static final String ADDR_ID = "address";
	private final DotTextPropertyDescriptor ADDR_DESCRIPTOR = new DotTextPropertyDescriptor(ADDR_ID, "Address");

	/** total memory size consumed by the process descriptor */
	private static final String TSIZE_ID = "total";
	private final DotTextPropertyDescriptor TSIZE_DESCRIPTOR = new DotTextPropertyDescriptor(TSIZE_ID, "Total Size");

	/** life time of an allocation descriptor. This applies only to non Leaked allocations  */
	private static final String LIFETIME_ID = "lifetime";
	private final DotTextPropertyDescriptor LIFETIME_DESCRIPTOR = new DotTextPropertyDescriptor(LIFETIME_ID, "Life Time");
	private static final String ATTRIBUTES_GROUP = "Attributes";
	/** used for making absolute time values relative */
	private long baseTime;

	/** callstack item descriptor id*/
	private static final String CALL_STACK_ID = "callstack";

	/** current project */
	IProject iCurrentProject = null;
	/** Symbol Reader */
	SymReader iSymReader = null;
	/** c++ files from the current project */
	AbstractList<String> cppFileNames;
	static final String LINE_SEPARATOR = " :: ";
	/**
	 * Constructor
	 * @param baseTime usually process start time, used for making absolute time values relative   
	 * @param info the alloc or free info
	 * @param project IProject to use for locating source file 
	 * @param symReader the SymReader to use for pinpointing
	 * @param cppFiles 
	 */
	public MemOpDescriptor(Long baseTime, BaseInfo info, IProject project, SymReader symReader, AbstractList<String> cppFiles) {
		memInfo = info;
		iCurrentProject = project;
		iSymReader = symReader;
		cppFileNames = cppFiles;
		this.baseTime = baseTime;	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.IPropertySource#getEditableValue()
	 */
	public Object getEditableValue() {
		return null; // no edit
	}




	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyDescriptors()
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
		if (memInfo instanceof AllocInfo && ((AllocInfo)memInfo).isFreed()) {
			LIFETIME_DESCRIPTOR.setCategory(ATTRIBUTES_GROUP);
			completeList.add(LIFETIME_DESCRIPTOR); //only non leaks
		}
		// add callstack descriptors
		AbstractList<AllocCallstack> callstack = memInfo.getCallstack();
		for (int i = 0; i < callstack.size(); i++) {
			
			final DotTextPropertyDescriptor propDesc = new DotTextPropertyDescriptor(i, CALL_STACK_ID);
			propDesc.setCategory("CallStack");
			completeList.add(propDesc);
		}
		return completeList.toArray(new TextPropertyDescriptor[completeList.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyValue(java.lang.Object)
	 */
	public Object getPropertyValue(Object id) {
		//Note, this method must return String values for the CellEditor to work
		if (PID_ID.equals(id)) {
			return String.valueOf(memInfo.getProcessID());//process ids are usually decimal values
		}
		if (TYPE_ID.equals(id))
			return memInfo instanceof AllocInfo ? ((AllocInfo)memInfo).isFreed() ? "Allocation" : "Leak" : "Free";
		if (TIME_ID.equals(id))
			return GraphUtils.renderTime(memInfo.getTime() - baseTime);
		if (SIZE_ID.equals(id))
			return String.format("%,d B",memInfo.getSizeInt());
		if (ADDR_ID.equals(id))
			return Long.toString(memInfo.getMemoryAddress(),16);
		if (TSIZE_ID.equals(id))
			return String.format("%,d B",memInfo.getTotalMem());
		if (LIFETIME_ID.equals(id)) {
			if (memInfo instanceof AllocInfo && ((AllocInfo)memInfo).isFreed()) {
				AllocInfo info = (AllocInfo) memInfo;
				return GraphUtils.renderTime(info.getFreedBy().getTime() - info.getTime()); 
			}
			throw new IllegalStateException("Should not happen because we did not provide a lifetime descriptor for leak and free");
		}
		if (id instanceof Integer && memInfo.getCallstack() != null){
			int callstackId = (Integer)id;
			if (callstackId < memInfo.getCallstack().size()){
				AllocCallstack callstackItem = memInfo.getCallstack().get(callstackId);
				DllLoad tempLoad = callstackItem.getDllLoad();
				long addr = callstackItem.getMemoryAddress();
				
				String name = String.format("%1$08x",addr);
				if (tempLoad != null && callstackItem.getMemoryAddress()!= tempLoad.getStartAddress()) {
					SourceFile aSourcefile = pinpoint(callstackItem.getMemoryAddress(),
							callstackItem.getDllLoad());
					if (aSourcefile != null) { //callstack resolved to a file-function-line
						return name + LINE_SEPARATOR + callstackItem.getDllLoad().getName() + LINE_SEPARATOR + aSourcefile.getFileName()+ LINE_SEPARATOR + aSourcefile.getFunctionName() + LINE_SEPARATOR + aSourcefile.getLineNumber();
					}
				}
				return name + (tempLoad != null ? LINE_SEPARATOR + tempLoad.getName() : "");
				}
			}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.IPropertySource#isPropertySet(java.lang.Object)
	 */
	public boolean isPropertySet(Object id) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.IPropertySource#resetPropertyValue(java.lang.Object)
	 */
	public void resetPropertyValue(Object id) {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.IPropertySource#setPropertyValue(java.lang.Object, java.lang.Object)
	 */
	public void setPropertyValue(Object id, Object value) {
		// do nothing
	}

	
	/**
	 * DotTextPropertyDescriptor  defines a read only TextPropertyDescriptor
	 * It also add a customised key/mouse listener.
	 * This listener will be useful for pinpointing the source.
	 *
	 */
	private class DotTextPropertyDescriptor extends TextPropertyDescriptor {

		/**
		 * Constructor
		 * @param id Descriptor Id
		 * @param displayName Descriptor display name
		 */
		public DotTextPropertyDescriptor(Object id, String displayName) {
			super(id, displayName);
		}

		/**
		 * create a Read Only TextCellEditor and add the defined listener to its Control.
		 * @see org.eclipse.ui.views.properties.TextPropertyDescriptor#createPropertyEditor(org.eclipse.swt.widgets.Composite)
		 */
		@Override
		public CellEditor createPropertyEditor(Composite parent) {
			TextCellEditor editor = new SimpleTextCellEditor(parent,SWT.READ_ONLY);
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
			super(parent,readOnly);
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
	 * This class is only for convenience for the moment.
	 * It's very likely that the listener will be provided by AnalyzeToolGraph
	 *
	 */
	private class DotPropKeyMouseListener implements KeyListener, MouseListener {

		//key

		public void keyPressed(org.eclipse.swt.events.KeyEvent e) {
		}

		public void keyReleased(org.eclipse.swt.events.KeyEvent keyEvent) {
			//System.out.println("key released.");
			if (keyEvent.character == '\r') {
				String text = ((Text)keyEvent.getSource()).getText();
				if (text != null) {
					String[] segs = text.split(LINE_SEPARATOR);
					if (segs.length == 5) {
						openEditor(segs[2], segs[4]);
					}
				}
			}
			
		}

		//Mouse
		
		public void mouseDoubleClick(MouseEvent e) {
			String text = ((Text)e.getSource()).getText();
			if (text != null) {
				String[] segs = text.split(LINE_SEPARATOR);
				if (segs.length == 5) {
					openEditor(segs[2], segs[4]);
				}
			}
		}

		public void mouseDown(MouseEvent e) {
		}

		public void mouseUp(MouseEvent e) {
		}
	}
	
	
	/**
	 * Pinpoints one memory address to source code line.
	 *
	 * @param memoryAddress
	 *            Memory address
	 * @param dllLoad
	 *           DllLoad item
	 * @return SourceFile if found otherwise null
	 */
	private SourceFile pinpoint(Long memoryAddress, DllLoad dllLoad) {

		if (dllLoad != null) {

			ISymbolFile symbolFile = iSymReader.getSymbolFile(dllLoad.getName(),
					false);
			if( symbolFile != null ) {
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
			long calculated = (memoryAddress - dllLoad.getStartAddress()) + defaultLinkAddress;

			java.math.BigInteger bigAddress = new java.math.BigInteger(Long
					.toHexString(calculated), 16);
			IFunction func = symbolFile.findFunctionByAddress(bigAddress);
			ISourceLocation loc = symbolFile.findSourceLocation(bigAddress);
			if (func != null && loc != null) {
				String sourceFile = loc.getSourceFile();
				if (sourceFile == null
						|| sourceFile.equalsIgnoreCase(""))
					return null;
				int lineNumber = loc.getLineNumber();
				if (lineNumber == 0)
					return null;
				String name = func.getName();
				if (name == null
						|| name.equalsIgnoreCase(""))
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

		//check that used information is given
		//we need to know file name and file line number
		//that we could open the right line in editor
		if (cppFileName == null || ("").equals(cppFileName)
				|| lineNumber == null || ("").equals(lineNumber)) {
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
							new Path(projects[i].getName() + "\\"
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
	 *            Cpp file name
	 * @return Found cpp file location
	 */
	private String getFileNames(String fileName) {
		int slash = Util.getLastSlashIndex(fileName);
		String tempFile = fileName.substring(slash + 1, fileName.length());

		String realFileName = fileName;
		for(String tempFileName : cppFileNames){
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
