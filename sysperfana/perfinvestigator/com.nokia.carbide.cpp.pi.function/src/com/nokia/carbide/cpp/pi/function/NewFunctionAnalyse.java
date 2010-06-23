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

/*
 * NewFunctionAnalyse.java
 */
package com.nokia.carbide.cpp.pi.function;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.eclipse.swt.graphics.RGB;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.model.Function;
import com.nokia.carbide.cpp.internal.pi.model.GUITooltips;
import com.nokia.carbide.cpp.internal.pi.model.GenericSampledTrace;
import com.nokia.carbide.cpp.internal.pi.model.GenericTrace;
import com.nokia.carbide.cpp.internal.pi.model.IFunction;
import com.nokia.carbide.cpp.internal.pi.model.ParsedTraceData;
import com.nokia.carbide.cpp.internal.pi.model.TraceDataRepository;
import com.nokia.carbide.cpp.internal.pi.test.PIAnalyser;
import com.nokia.carbide.cpp.internal.pi.utils.QuickSortImpl;
import com.nokia.carbide.cpp.internal.pi.utils.Sortable;
import com.nokia.carbide.cpp.internal.pi.visual.Defines;
import com.nokia.carbide.cpp.internal.pi.visual.PICompositePanel;
import com.nokia.carbide.cpp.pi.address.GppSample;
import com.nokia.carbide.cpp.pi.address.GppTrace;
import com.nokia.carbide.cpp.pi.call.GfcSample;
import com.nokia.carbide.cpp.pi.call.GfcTrace;
import com.nokia.carbide.cpp.pi.util.AWTColorPalette;


public class NewFunctionAnalyse extends JPanel implements ListSelectionListener, ChangeListener
{
	private static final long serialVersionUID = 3803217794859007866L;

	private PICompositePanel compositePanel;
	//private TraceDataRepository traceData;
	
	private JList threadList;
	private JList binaryList;
	private JList percentList;	
	
	private JDialog frame;
	
	private GppTrace gppTrace;
	private GfcTrace gfcTrace;
	private GenericTrace ittTrace;
	
	private Vector gppSamples;
	private Vector gfcSamples;
	//private Vector ittSamples;
	
	private Hashtable threads;
	private Vector binaryVector;
	
	private Hashtable binaries;
	private Vector threadVector;
	
	private JSplitPane split;
	private JSplitPane functionSplit;
	
	private JButton ittPrimary;
	private JButton symPrimary;
	
	private JCheckBox totalPercents;
	private JCheckBox functionPercents;
	
	private String[] originallySelectedThreads;
	private String[] originallySelectedBinaries;
	private int[] originallySelectedIndices;
	private Vector selectedButNotVisibleBinaries;
	private Vector selectedButNotVisibleThreads;
	
	private GfcTraceVisualiser gfcTraceVisualiser;
	private Thread updateThread = null;
	
	private boolean symbolPrimary = true;
	
	private boolean gfcEnabled;
	
	//private int stateChangedCounter = 0;
	//private Object[] oldSelectedBinaries;
	private Object[] selectedBinaries;
	//private boolean stateChanged = false;
	
	private Hashtable functionNameCacheSym;
	private Hashtable functionNameCacheItt;
	private final int mode; //thread mode(0) or binary mode(1)
	
public NewFunctionAnalyse(PICompositePanel compositePanel,
								/*TraceDataRepository traceData,*/
								String[] selectedItems,
								boolean gfcEnabled, int mode)
	{		
		this.selectedButNotVisibleBinaries = new Vector();
		this.selectedButNotVisibleThreads = new Vector();
		this.gfcEnabled = gfcEnabled;
		this.functionNameCacheSym = new Hashtable();
		this.functionNameCacheItt = new Hashtable();
		
		//this.originallySelectedThreads = selectedThreads;
		this.mode = mode;
		if (selectedItems != null)
		{
			if (mode == Defines.BINARIES || mode == Defines.BINARIES_FUNCTIONS)
			    this.originallySelectedBinaries = (String[])selectedItems.clone();
			else
			    this.originallySelectedThreads = (String[])selectedItems.clone();
		}
		
		this.compositePanel = compositePanel;
		//this.traceData = traceData;
		int uid = NpiInstanceRepository.getInstance().activeUid();
		
		ParsedTraceData ptd = TraceDataRepository.getInstance().getTrace(uid, "com.nokia.carbide.cpp.pi.address.GppTrace"); //$NON-NLS-1$
		if (ptd != null)
			this.gppTrace = (GppTrace)ptd.traceData;
		ptd = TraceDataRepository.getInstance().getTrace(uid, "com.nokia.carbide.cpp.pi.call.GfcTrace"); //$NON-NLS-1$
		if (ptd != null)
			this.gfcTrace = (GfcTrace)ptd.traceData;
		
		//if (traceData.getIttTrace() instanceof IttTrace)
		ptd = TraceDataRepository.getInstance().getTrace(uid, "com.nokia.carbide.cpp.pi.instr.IttTrace"); //$NON-NLS-1$
		if (ptd != null)
			this.ittTrace = ptd.traceData;
		
		this.refreshFrame();
		if (mode == Defines.THREADS || mode == Defines.THREADS_FUNCTIONS)
		    this.refreshThreadListComponent(true);
		else
		{
		    this.refreshBinaryListComponent(true);
		    this.refreshThreadListComponentSec();
		}
		//this.binaryList.grabFocus();
		this.updateGfc();
	}
	
	private void clearReferences()
	{
		// clear all possible references to other
		// parts of the trace, to avoid memory leak
		// execute this before disposing the frame
		compositePanel = null;
		//traceData = null;
		
		threadList = null;
		binaryList = null;
		percentList = null;	
		
		frame = null;
		
		gppTrace = null;
		gfcTrace = null;
		ittTrace = null;
		
		gppSamples = null;
		gfcSamples = null;
		//ittSamples = null;
		
		threads = null;
		binaryVector = null;
		
		binaries = null;
		threadVector = null;
		
		split = null;
		functionSplit = null;
		
		ittPrimary = null;
		symPrimary = null;
		
		totalPercents = null;
		functionPercents = null;
		
		originallySelectedThreads = null;
		originallySelectedBinaries = null;
		originallySelectedIndices = null;
		selectedButNotVisibleBinaries = null;
		selectedButNotVisibleThreads = null;
		
		gfcTraceVisualiser = null;
		updateThread = null;
		selectedBinaries = null;
		functionNameCacheSym = null;
		functionNameCacheItt = null;
	}
	
	private void refreshFrame()
	{
		this.extractSamples();
		if (mode == Defines.THREADS || mode == Defines.THREADS_FUNCTIONS)
		{
			this.refreshThreadListComponent(false);
			this.refreshBinaryListComponentSec();
		}
		else
		{
		    this.refreshBinaryListComponent(false);
		    this.refreshThreadListComponentSec();
		}
		this.refreshPercentListComponent();

		if (frame == null)
		{
			frame = new JDialog(PIAnalyser.getFrame());
			frame.setSize(1000,900);
			frame.getContentPane().setLayout(new BorderLayout());
			frame.addComponentListener(new ComponentAdapter()
			{
				public void componentResized(ComponentEvent e) 
				{
					split.setDividerLocation((double)0.4);
					if (gfcEnabled)
						functionSplit.setDividerLocation((double)0.4);
					else
						functionSplit.setDividerLocation((double)1);					
				}
			});
			
			frame.addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
				{
					// clear all possible references to the trace
					//System.err.println("Closing");
					frame.dispose();
					clearReferences();
				}
				public void windowClosed(WindowEvent e)
				{
					// clear all possible references to the trace
					//System.err.println("Closed");
					clearReferences();
				}
			});
			
			JPanel leftPanel = new JPanel();
			JPanel rightPanel = new JPanel();
			this.functionSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			this.functionSplit.setOneTouchExpandable(true);
			
			leftPanel.setLayout(new GridLayout(3,1));
			rightPanel.setLayout(new BorderLayout());

			JButton clearSelectionButton = null;
			if (mode == Defines.THREADS || mode == Defines.THREADS_FUNCTIONS)
			    clearSelectionButton = new JButton(Messages.getString("NewFunctionAnalyse.clearSelectedBinaries")); //$NON-NLS-1$
			else 
			    clearSelectionButton = new JButton(Messages.getString("NewFunctionAnalyse.clearSelectedThreads")); //$NON-NLS-1$
			clearSelectionButton.setToolTipText(GUITooltips.getClearSelectedBinariesButton());
			clearSelectionButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{
					selectedButNotVisibleBinaries.clear();
					binaryList.clearSelection();
					selectedButNotVisibleThreads.clear();
					threadList.clearSelection();
				}
			});
			JButton setOriginallySelectedButton = null;
			if (mode == Defines.THREADS || mode == Defines.THREADS_FUNCTIONS)
			    setOriginallySelectedButton = new JButton(Messages.getString("NewFunctionAnalyse.selectOriginalThreads")); //$NON-NLS-1$
			else
			    setOriginallySelectedButton = new JButton(Messages.getString("NewFunctionAnalyse.selectOriginalBinaries")); //$NON-NLS-1$
			setOriginallySelectedButton.setToolTipText(GUITooltips.getSetOriginalThreads());
			setOriginallySelectedButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{
					if (originallySelectedIndices != null && threadList != null && 
					        (mode == Defines.THREADS || mode == Defines.THREADS_FUNCTIONS))
						threadList.setSelectedIndices(originallySelectedIndices);
					else if (originallySelectedIndices != null && binaryList != null && 
					        (mode == Defines.BINARIES || mode == Defines.BINARIES_FUNCTIONS))
					    binaryList.setSelectedIndices(originallySelectedIndices);
				}
			});
			
			this.ittPrimary = new JButton(Messages.getString("NewFunctionAnalyse.primarilyUseITT")); //$NON-NLS-1$
			this.ittPrimary.setToolTipText(GUITooltips.getUsePrimarilyItt());
			this.ittPrimary.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{
					symbolPrimary = false;
					
					if (gfcEnabled) updateGfc();

					if (mode == Defines.BINARIES || mode == Defines.BINARIES_FUNCTIONS)
					{
						refreshBinaryListComponent(false);
						//refreshBinaryListComponentSec();
					    refreshThreadListComponentSec();
					}
					else
					{
						//refreshBinaryListComponent(false);
					    refreshBinaryListComponentSec();
					}
					refreshPercentListComponent();
				}
			});
			
			this.symPrimary = new JButton(Messages.getString("NewFunctionAnalyse.primarilyUseSymbolFile")); //$NON-NLS-1$
			this.symPrimary.setToolTipText(GUITooltips.getUsePrimarilySymbol());
			this.symPrimary.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{
					symbolPrimary = true;
					
					if (gfcEnabled) updateGfc();
					
				    if (mode == Defines.BINARIES || mode == Defines.BINARIES_FUNCTIONS)
					{
						refreshBinaryListComponent(false);
						//refreshBinaryListComponentSec();				    	
				    	refreshThreadListComponentSec();
					}
					else
					{
						//refreshBinaryListComponent(false);
					    refreshBinaryListComponentSec();
					}
					refreshPercentListComponent();
					
				}
			});
			
			this.totalPercents = new JCheckBox(Messages.getString("NewFunctionAnalyse.showPercentsOfTotalLoad")); //$NON-NLS-1$
			this.totalPercents.addChangeListener(this);
			this.totalPercents.setToolTipText(GUITooltips.getAbsolutePercentage());
			this.totalPercents.setSelected(true);
			
			this.functionPercents = new JCheckBox(Messages.getString("NewFunctionAnalyse.showPositionSpecificPercents")); //$NON-NLS-1$
			this.functionPercents.addChangeListener(this);
			this.functionPercents.setSelected(false);
			
			if (this.ittTrace == null)
			{
				this.ittPrimary.setEnabled(false);
				this.symPrimary.setEnabled(false);
			}
			
			JPanel checkBoxPanel = new JPanel();
			checkBoxPanel.setLayout(new GridLayout(6,1));
			checkBoxPanel.add(clearSelectionButton);
			checkBoxPanel.add(setOriginallySelectedButton);
			checkBoxPanel.add(this.symPrimary);
			checkBoxPanel.add(this.ittPrimary);
			checkBoxPanel.add(this.totalPercents);
			checkBoxPanel.add(this.functionPercents);
			
			JScrollPane temp = new JScrollPane(this.threadList);
			temp.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Messages.getString("NewFunctionAnalyse.threadList"), //$NON-NLS-1$
			        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
			        javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
			//temp.setColumnHeaderView(new JLabel("Thread list")); //tulee exceptioneja tästä
			leftPanel.add(temp);
			temp = new JScrollPane(this.binaryList);
			temp.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Messages.getString("NewFunctionAnalyse.binaryList"), //$NON-NLS-1$
			        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
			        javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
			//temp.setColumnHeaderView(new JLabel("Binary list")); //tulee exceptioneja tästä
			leftPanel.add(temp);
			
			leftPanel.add(checkBoxPanel);
			
			rightPanel.add(new JScrollPane(this.percentList));
			this.functionSplit.setTopComponent(rightPanel);
			this.functionSplit.setBottomComponent(new JPanel());
						
			split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			split.setTopComponent(leftPanel);
			split.setBottomComponent(this.functionSplit);
			
			frame.getContentPane().add(split);
			

            // frame.setVisible(true); //poikkeuksia


			this.split.setDividerLocation((double)0.4);
			if (this.gfcEnabled)
				this.functionSplit.setDividerLocation((double)0.4);
			else
				this.functionSplit.setDividerLocation((double)1);					

		}

		String analysisName = ""; //$NON-NLS-1$
		String modeString = ""; //$NON-NLS-1$
		if (mode == Defines.THREADS || mode == Defines.THREADS_FUNCTIONS)
		    modeString = Messages.getString("NewFunctionAnalyse.threadMode"); //$NON-NLS-1$
		else if (mode == Defines.BINARIES || mode == Defines.BINARIES_FUNCTIONS)
		    modeString = Messages.getString("NewFunctionAnalyse.binaryMode"); //$NON-NLS-1$
		frame.setTitle(Messages.getString("NewFunctionAnalyse.functionAnalysis1") + analysisName + Messages.getString("NewFunctionAnalyse.functionAnalysis2")+ //$NON-NLS-1$ //$NON-NLS-2$
							this.compositePanel.getSelectionStart()/1000.0+Messages.getString("NewFunctionAnalyse.functionAnalysis3")+ //$NON-NLS-1$
							this.compositePanel.getSelectionEnd()/1000.0+Messages.getString("NewFunctionAnalyse.functionAnalysis4") + modeString); //$NON-NLS-1$
		frame.validate();
		frame.setVisible(true);
	}
	
	private void extractSamples()
	{		
		getSelectedSamples(gppTrace);
		getSelectedSamples(gfcTrace);
		//getSelectedSamples(ittTrace);				
	}
	
	private void getSelectedSamples(GenericSampledTrace gst)
	{
		if (gst != null)
		{
			int selectionStart = (int) this.compositePanel.getSelectionStart()+1;
			int selectionEnd = (int) this.compositePanel.getSelectionEnd();
		
			System.out.println(Messages.getString("NewFunctionAnalyse.start")+selectionStart+Messages.getString("NewFunctionAnalyse.end")+selectionEnd); //$NON-NLS-1$ //$NON-NLS-2$
			Vector s = gst.getSamplesInsideTimePeriod(selectionStart,selectionEnd);
			System.out.println(Messages.getString("NewFunctionAnalyse.gotSamples")+s.size()); //$NON-NLS-1$
			
			if (gst instanceof GppTrace)
			{
				this.gppSamples = s;
			}		
			else if (gst instanceof GfcTrace)
			{
				this.gfcSamples = s;
			}	
			//else if (gst instanceof IttTrace)
			//{
			//	this.ittSamples = s;
			//}
		}	
	}
	
	private void refreshThreadListComponent(boolean setValuesFromGraph)
	{
		threads = new Hashtable();
		Vector listData = new Vector();
		Enumeration enumer = this.gppSamples.elements();
		Hashtable threadSampleAmount = new Hashtable();
		
		while(enumer.hasMoreElements())
		{
			//System.out.println("Class:"+enumer.nextElement().getClass().getName());
			GppSample sample = (GppSample)enumer.nextElement();
			String s = sample.thread.process.name+"::"+sample.thread.threadName+"_"+sample.thread.threadId; //$NON-NLS-1$ //$NON-NLS-2$
			
			if (!threads.containsKey(sample.thread.process.name+"::"+sample.thread.threadName+"_"+sample.thread.threadId)) //$NON-NLS-1$ //$NON-NLS-2$
			{
				threads.put(s,sample.thread);
				//listData.add(sample.thread.process.name+"::"+sample.thread.threadName);
				threadSampleAmount.put(s,Integer.valueOf(1));
			}
			else
			{
				int value = ((Integer)threadSampleAmount.get(s)).intValue();
				threadSampleAmount.remove(s);
				threadSampleAmount.put(s,Integer.valueOf(value+1));
			}
		}
		
		while(threadSampleAmount.size() > 0)
		{
			Enumeration sampEnum = threadSampleAmount.keys();
			String threadName = ""; //$NON-NLS-1$
			int maxValue = 0;
			String maxString = ""; //$NON-NLS-1$

			while(sampEnum.hasMoreElements())
			{
				threadName = (String)sampEnum.nextElement();
				int value = ((Integer)threadSampleAmount.get(threadName)).intValue();
				if (maxValue < value)
				{
					maxString = threadName;
					maxValue = value;
				}
			}
			threadSampleAmount.remove(maxString);
			listData.add( maxValue+Messages.getString("NewFunctionAnalyse.samplesEqual")+((float)maxValue*100f)/this.gppSamples.size()+"% "+maxString); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		if (this.threadList == null)
		{
			this.threadList = new JList(listData);
			customKeys(threadList);
			customKeys(threadList);
			this.threadList.addListSelectionListener(this);
		}
		else
		{
			this.threadList.removeListSelectionListener(this);
			
			this.threadList.setListData(listData);

			if (setValuesFromGraph && this.originallySelectedThreads != null)
			{
				Vector<Integer> selectedIndx = new Vector<Integer>();
				for (int i=0;i<this.originallySelectedThreads.length;i++)
				{
					for (int k=0;k<listData.size();k++)
					{
						String test = ((String)listData.get(k));
						test = test.substring(test.indexOf('%')+2,test.length());
						if (test != null && this.originallySelectedThreads[i] != null)
						{				
							if (this.originallySelectedThreads[i].equals(test))
							{
								//System.out.println("Match:"+this.originallySelectedThreads[i]+" index "+k);
								selectedIndx.add(Integer.valueOf(k));
							}
						}
					}
				}
				
				if (selectedIndx.size() > 0)
				{
					this.originallySelectedIndices = new int[selectedIndx.size()];
					for (int i=0;i<this.originallySelectedIndices.length;i++)
					{
						this.originallySelectedIndices[i] = selectedIndx.elementAt(i);
					}
					this.threadList.setSelectedIndices(this.originallySelectedIndices);
				}
			}
			this.threadList.addListSelectionListener(this);
		}
	}
	
	private void refreshBinaryListComponent(boolean setValuesFromGraph)
	{
	    binaries = new Hashtable();
		Vector<BinaryNameItem> listData = new Vector<BinaryNameItem>();
		Enumeration enumer = this.gppSamples.elements();
		Hashtable<BinaryNameItem,Integer> binarySampleAmount = new Hashtable<BinaryNameItem,Integer>();
		
		while(enumer.hasMoreElements())
		{
			//System.out.println("Class:"+enumer.nextElement().getClass().getName());
			GppSample sample = (GppSample)enumer.nextElement();
			//String s = this.getBinaryNameForGppSample(sample).toString();
			BinaryNameItem bi = this.getBinaryNameForGppSample(sample);
			boolean containsKey = false;
			for (Enumeration e = binaries.keys(); e.hasMoreElements();)
			{
			    String tmp = ((BinaryNameItem)e.nextElement()).toString();
			    if (tmp.equalsIgnoreCase(bi.toString()))
			    {
			        containsKey = true;
			        break;
			    }
			}
			if (!containsKey)
		    {
		        binaries.put(bi, sample);
		        binarySampleAmount.put(bi, Integer.valueOf(1));
		    }
		    else
		    {
		        for (Enumeration<BinaryNameItem> e = binarySampleAmount.keys(); e.hasMoreElements();)
		        {
		            BinaryNameItem biTemp = e.nextElement();
		            if (biTemp.toString().equalsIgnoreCase(bi.toString()))
		            {
		                int value = ((Integer)binarySampleAmount.get(biTemp)).intValue();
		                binarySampleAmount.remove(biTemp);
						binarySampleAmount.put(biTemp, Integer.valueOf(value+1));
		            }
		        }
		    }
		}

		int totalSamples = 0;
		while(binarySampleAmount.size() > 0)
		{
		    Enumeration<BinaryNameItem> sampEnum = binarySampleAmount.keys();

			BinaryNameItem binaryName;
			int maxValue = 0;
			BinaryNameItem maxItem = null;

			while(sampEnum.hasMoreElements())
			{
			    binaryName = sampEnum.nextElement();
				int value = binarySampleAmount.get(binaryName);
				if (maxValue < value)
				{
					maxItem = binaryName;
					maxValue = value;
				}
			}
			binarySampleAmount.remove(maxItem);
			totalSamples += maxValue;

			maxItem.setSampleAmount(maxValue);
			listData.add(maxItem);
		}
		
		for (Enumeration<BinaryNameItem> e = listData.elements();e.hasMoreElements();)
		{
			BinaryNameItem item = e.nextElement();
			item.setTotalSampleAmount(totalSamples);
		}
		
		if (this.binaryList == null)
		{
			this.binaryList = new JList(listData);
			customKeys(binaryList);
			this.binaryList.setCellRenderer(new FunctionItemRenderer());
			this.binaryList.addListSelectionListener(this);
		}
		else
		{
			this.binaryList.removeListSelectionListener(this);
			this.binaryList.setListData(listData);

			if (setValuesFromGraph && this.originallySelectedBinaries != null)
			{
				Vector<Integer> selectedIndx = new Vector<Integer>();
				for (int i=0;i<this.originallySelectedBinaries.length;i++)
				{
					for (int k=0;k<listData.size();k++)
					{
						//String test = ((String)listData.get(k));
						String test = listData.get(k).toString();
						//test = test.substring(test.indexOf('%')+2,test.length());
						if (test != null && this.originallySelectedBinaries[i] != null)
						{				
							if (this.originallySelectedBinaries[i].equalsIgnoreCase(test))
							{
								//System.out.println("Match:"+this.originallySelectedThreads[i]+" index "+k);
								selectedIndx.add(Integer.valueOf(k));
							}
						}
					}
				}
				
				if (selectedIndx.size() > 0)
				{
					this.originallySelectedIndices = new int[selectedIndx.size()];
					for (int i=0;i<this.originallySelectedIndices.length;i++)
					{
						this.originallySelectedIndices[i] = selectedIndx.elementAt(i);
					}
					this.binaryList.setSelectedIndices(this.originallySelectedIndices);
				}
			}
			this.binaryList.addListSelectionListener(this);
		}
	}
	
	private void refreshThreadListComponentSec()
	{
	    threadVector = new Vector();
		Vector listData = new Vector();
		Vector threadSampleAmount = new Vector();
//		Object[] selectedBinaries = this.removePercents(this.binaryList.getSelectedValues());
		Object[] selectedBinaries = this.binaryList.getSelectedValues();
		
		Enumeration enumer = this.gppSamples.elements();
		
		while(enumer.hasMoreElements())
		{
			boolean match = false;
			GppSample sample = (GppSample)enumer.nextElement();
			for (int i=0;i<selectedBinaries.length;i++)
			{
				if ((this.getBinaryNameForGppSample(sample).toString()).
				        equalsIgnoreCase(selectedBinaries[i].toString()))
				{
					match = true;
					break;
				}
			}
			
			if (match == true)
			{
				String threadName = sample.thread.process.name+"::"+sample.thread.threadName+"_"+sample.thread.threadId; //$NON-NLS-1$ //$NON-NLS-2$
	
				if (!threadVector.contains(threadName))
				{
					threadVector.add(threadName);
					threadSampleAmount.add(Integer.valueOf(1));
				}
				else
				{	
					//int value = ((Integer)binarySampleAmount.get(binaryName)).intValue();
					int index = threadVector.indexOf(threadName);
					int value = ((Integer)threadSampleAmount.get(index)).intValue();
					
					threadSampleAmount.remove(index);
					threadSampleAmount.add(index,Integer.valueOf(value+1));
				}
			}
		}
		
		int totalSamples = 0;
		while(threadSampleAmount.size() > 0)
		{
			String sampItem = null;
			int maxValue = 0;
			String maxString = null;
			int maxIndex = 0; 
			
			for (int i=0;i<this.threadVector.size();i++)
			{
				sampItem = (String)threadVector.get(i);
				int value = ((Integer)threadSampleAmount.get(i)).intValue();
				if (maxValue < value)
				{
					maxString = sampItem;
					maxValue = value;
					maxIndex = i;
				}
			}
			threadSampleAmount.remove(maxIndex);
			threadVector.remove(maxIndex);
			totalSamples += maxValue;
			
			listData.add( maxValue+Messages.getString("NewFunctionAnalyse.samplesEqual")+((float)maxValue*100f)/this.gppSamples.size()+"% "+maxString); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		if (this.threadList == null)
		{
			this.threadList = new JList(listData);
			customKeys(threadList);
			//this.threadList.setCellRenderer(new FunctionItemRenderer());
			this.threadList.addListSelectionListener(this);
		}
		else
		{			
			Object[] valueStore = this.threadList.getSelectedValues();
			Vector indexStore = new Vector();
			Vector notVisibleStore = new Vector();
			
			for (int i=0;i<valueStore.length;i++)
			{
				int counter = 0;
				boolean found = false;
			    for (Enumeration e = listData.elements(); e.hasMoreElements(); counter++)
			    {
			        String listElement = (String)e.nextElement();
			        listElement =  listElement.substring(listElement.indexOf("% ")+2,listElement.length()); //$NON-NLS-1$
			        String valueString = (String)valueStore[i];
			        valueString =  valueString.substring(valueString.indexOf("% ")+2,valueString.length()); //$NON-NLS-1$
			        if (listElement.equals(valueString))
			        {
			            found = true;
			            break;
			        }
			    }
//			    int index = listData.indexOf(valueStore[i]);
			    int index = (found) ? counter : -1;
				if (index != -1)
				{
					indexStore.add(Integer.valueOf(index));
				}
				else
				{
					notVisibleStore.add(valueStore[i]);
				}
			}
			
			for (Enumeration e=this.selectedButNotVisibleThreads.elements();e.hasMoreElements();)
			{
				Object value = e.nextElement();
				int counter = 0;
				boolean found = false;
				for (Enumeration enume = listData.elements(); enume.hasMoreElements(); counter++)
			    {
			        String listElement = (String)enume.nextElement();
			        listElement =  listElement.substring(listElement.indexOf("% ")+2,listElement.length()); //$NON-NLS-1$
			        String valueString = (String)value;
			        valueString =  valueString.substring(valueString.indexOf("% ")+2,valueString.length()); //$NON-NLS-1$
			        if (listElement.equals(valueString))
			        {
			            found = true;
			            break;
			        }
			    }
			    int index = (found) ? counter : -1;
//				int index = listData.indexOf(value);
				if (index != -1)
				{
					indexStore.add(Integer.valueOf(index));
				}
				else
				{
					notVisibleStore.add(value);
				}
			}
			this.threadList.removeListSelectionListener(this);
			this.threadList.setListData(listData);

			if (indexStore.size() != 0)
			{
				int[] indexList = new int[indexStore.size()];
				for (int i=0;i<indexStore.size();i++)
				{
					indexList[i] = ((Integer)indexStore.elementAt(i)).intValue();
				}
				this.threadList.setSelectedIndices(indexList);
			}			
			
			this.selectedButNotVisibleThreads.clear();
			this.selectedButNotVisibleThreads.addAll(notVisibleStore);
			this.threadList.addListSelectionListener(this);

		}
	}
	private void refreshBinaryListComponentSec()
	{
		binaryVector = new Vector();
		Vector listData = new Vector();
		Vector binarySampleAmount = new Vector();
		Object[] selectedThreads = this.removePercents(this.threadList.getSelectedValues());
		
		Enumeration enumer = this.gppSamples.elements();
		
		while(enumer.hasMoreElements())
		{
			boolean match = false;
			GppSample sample = (GppSample)enumer.nextElement();
			for (int i=0;i<selectedThreads.length;i++)
			{
				if ((sample.thread.process.name+"::"+sample.thread.threadName+"_"+sample.thread.threadId).equals(selectedThreads[i])) //$NON-NLS-1$ //$NON-NLS-2$
				{
					match = true;
					break;
				}
			}
			
			if (match == true)
			{
				BinaryNameItem binaryName = getBinaryNameForGppSample(sample);
	
				if (!binaryVector.contains(binaryName))
				{
					binaryVector.add(binaryName);
					binarySampleAmount.add(Integer.valueOf(1));
				}
				else
				{	
					//int value = ((Integer)binarySampleAmount.get(binaryName)).intValue();
					int index = binaryVector.indexOf(binaryName);
					int value = ((Integer)binarySampleAmount.get(index)).intValue();
					
					binarySampleAmount.remove(index);
					binarySampleAmount.add(index,Integer.valueOf(value+1));
				}
			}
		}
		
		int totalSamples = 0;
		while(binarySampleAmount.size() > 0)
		{
			BinaryNameItem sampItem = null;
			int maxValue = 0;
			BinaryNameItem maxItem = null;
			int maxIndex = 0; 
			
			for (int i=0;i<this.binaryVector.size();i++)
			{
				sampItem = (BinaryNameItem)binaryVector.get(i);
				int value = ((Integer)binarySampleAmount.get(i)).intValue();
				if (maxValue < value)
				{
					maxItem = sampItem;
					maxValue = value;
					maxIndex = i;
				}
			}
			binarySampleAmount.remove(maxIndex);
			binaryVector.remove(maxIndex);
			totalSamples += maxValue;

			maxItem.setSampleAmount(maxValue);
			listData.add(maxItem);
		}
		
		for (Enumeration e = listData.elements();e.hasMoreElements();)
		{
			BinaryNameItem item = (BinaryNameItem)e.nextElement();
			item.setTotalSampleAmount(totalSamples);
		}
		
		if (this.binaryList == null)
		{
			this.binaryList = new JList(listData);
			customKeys(binaryList);
			this.binaryList.setCellRenderer(new FunctionItemRenderer());
			this.binaryList.addListSelectionListener(this);
		}
		else
		{			
			Object[] valueStore = this.binaryList.getSelectedValues();
			Vector indexStore = new Vector();
			Vector notVisibleStore = new Vector();
			
			for (int i=0;i<valueStore.length;i++)
			{
				int index = listData.indexOf(valueStore[i]);
				if (index != -1)
				{
					indexStore.add(Integer.valueOf(index));
				}
				else
				{
					notVisibleStore.add(valueStore[i]);
				}
			}
			
			for (Enumeration e=this.selectedButNotVisibleBinaries.elements();e.hasMoreElements();)
			{
				Object value = e.nextElement();
				int index = listData.indexOf(value);
				if (index != -1)
				{
					indexStore.add(Integer.valueOf(index));
				}
				else
				{
					notVisibleStore.add(value);
				}
			}

			this.binaryList.removeListSelectionListener(this);
			this.binaryList.setListData(listData);

			if (indexStore.size() != 0)
			{
				int[] indexList = new int[indexStore.size()];
				for (int i=0;i<indexStore.size();i++)
				{
					indexList[i] = ((Integer)indexStore.elementAt(i)).intValue();
				}
				this.binaryList.setSelectedIndices(indexList);
			}			
			
			this.selectedButNotVisibleBinaries.clear();
			this.selectedButNotVisibleBinaries.addAll(notVisibleStore);
			this.binaryList.addListSelectionListener(this);
		}
	}
	
	private Object[] removePercents(Object[] data)
	{
		String[] fin = new String[data.length];
		for (int i=0;i<data.length;i++)
		{
			String s = (String)data[i];
			fin[i] = s.substring(s.indexOf("% ")+2,s.length()); //$NON-NLS-1$
		}
		return fin;
	}
	
	private void refreshPercentListComponent()
	{
	    if (threadList == null)
	        return;
	    Vector percentData = new Vector();
		Object[] selectedThreads = this.removePercents(this.threadList.getSelectedValues());
		selectedBinaries = this.binaryList.getSelectedValues();
//		if ((selectedBinaries != null) && (oldSelectedBinaries != null) && !stateChanged)
//			if (selectedBinaries.length == oldSelectedBinaries.length)
//			{
//				boolean similar = true;
//				for (int i=0;i<selectedBinaries.length;i++)
//				{
//					if (selectedBinaries[i].hashCode() != oldSelectedBinaries[i].hashCode())
//					{
//						similar = false;
//						break;
//					}
//				}
//				if (similar)
//					return;
//			}
//		stateChanged = false;
				
		this.percentList = new JList();
		customKeys(percentList);
		Vector percentValueList = new Vector();
		Vector functionIndexList = new Vector();
		
		int totalSamples = 0;
		
		Enumeration enumer = this.gppSamples.elements();
		while(enumer.hasMoreElements())
		{
			GppSample s = (GppSample)enumer.nextElement();
			boolean match = false;
			// check if the thread has been selected from the thread list
			for (int i=0;i<selectedThreads.length;i++)
			{
				String st = (String)selectedThreads[i];
				int threadId = Integer.parseInt(st.substring (st.lastIndexOf("_")+1,st.length()) ); //$NON-NLS-1$
				if (s.thread.threadId.intValue() == threadId)
				{
					match = true;
					// yes, go ahead
					break;
				}
			}
			
			if (match == true)
			{
				match = false;
				for (int i=0;i<selectedBinaries.length;i++)
				{
					BinaryNameItem binaryName = getBinaryNameForGppSample(s);
					
					if (binaryName.equals(selectedBinaries[i]))
					{
						match = true;
						break;
					}
				}
			}
			
			// from now on, the match variable tells whether the function has already been
			// found from the previous samples
			if (match == true)
			{
				match = false;
				for (int i=0;i<percentData.size();i++)
				{
					FunctionNameItem test = (FunctionNameItem)percentData.elementAt(i);
					if (test.equals(getFunctionNameForGppSample(s)))
					{
						match = true;
						break;
					}
				}
					
				if (!match)
				{
					// add the function name to percentData
					percentData.add(getFunctionNameForGppSample(s));
					// this is the first sample for this function, thus add number 1
					// to the percent value list
					percentValueList.add(Integer.valueOf(1));
					// add a vector for the function offset values
					
					if (this.functionPercents.isSelected() == true)
					{
						Vector functionIndexVector = new Vector();
						Long offset = getFunctionOffsetForGppSample(s);
						functionIndexVector.add(offset);
						functionIndexList.add(functionIndexVector);
					}

					//System.out.println("Added name "+s.currentFunctionSym.functionName);
					totalSamples++;
				}
				else
				{
					int index = percentData.indexOf(getFunctionNameForGppSample(s));
					
					/*
					System.out.println("old "+((String)percentData.elementAt(index))+
							" = "+
							((Integer)percentValueList.elementAt(index)).intValue());
					*/
					
					Integer value = (Integer)percentValueList.elementAt(index);
					percentValueList.remove(index);
					percentValueList.add(index,Integer.valueOf(value.intValue()+1));
					
					if (this.functionPercents.isSelected() == true)
					{					
						Vector functionIndexVector = (Vector)functionIndexList.elementAt(index);
						Long offset = getFunctionOffsetForGppSample(s);
						functionIndexVector.add(offset);
					}
					
					totalSamples++;
				}
			}
		}
		
		if (totalSamples != 0)
		{
			sortFunctionIndexList(functionIndexList);
			Vector finalList = this.sortPercentData(percentValueList,percentData,functionIndexList,totalSamples);
			
			if (this.percentList == null)
			{
				this.percentList = new JList(finalList);
				customKeys(percentList);
				this.percentList.setCellRenderer(new FunctionItemRenderer());
				JScrollPane temp = new JScrollPane(this.percentList);
				if (this.gfcEnabled)
				{
				    temp.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Messages.getString("NewFunctionAnalyse.functionPercentListDoubleClick"), //$NON-NLS-1$
					        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
					        javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
//					temp.setColumnHeaderView(
//							new JLabel("Function percent list - double click to see the function in the linked function view"));
				}
				else
				{
				    temp.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Messages.getString("NewFunctionAnalyse.functionPercentList"), //$NON-NLS-1$
					        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
					        javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
//				    temp.setColumnHeaderView(
//							new JLabel("Function percent list"));
				}
				this.functionSplit.setTopComponent(temp);
//				this.split.setDividerLocation((double)0.4);
				if (this.gfcEnabled)
					this.functionSplit.setDividerLocation((double)0.4);
				else
					this.functionSplit.setDividerLocation((double)1);					
			}
			else
			{
				this.percentList = new JList(finalList);
				customKeys(percentList);
				this.percentList.setCellRenderer(new FunctionItemRenderer());
				this.percentList.addMouseListener(new MouseAdapter()
				{
				 public void mouseClicked(MouseEvent e) 
					{
				 	if (e.getClickCount() > 1)
				 		{
				 			showLinkedData();
				 		}
					}
				});

				JScrollPane temp = new JScrollPane(this.percentList);
				if (this.gfcEnabled)
				{
				    temp.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Messages.getString("NewFunctionAnalyse.functionPercentListDoubleClick"), //$NON-NLS-1$
					        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
					        javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
//					temp.setColumnHeaderView(
//							new JLabel("Function percent list - double click to see the function in the linked function view"));
				}
				else
				{
				    temp.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Messages.getString("NewFunctionAnalyse.functionPercentList"), //$NON-NLS-1$
					        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
					        javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
//				    temp.setColumnHeaderView(
//							new JLabel("Function percent list"));
				}	
				this.functionSplit.setTopComponent(temp);
				
//				this.split.setDividerLocation((double)0.4);
				if (this.gfcEnabled)
					this.functionSplit.setDividerLocation((double)0.4);
				else
					this.functionSplit.setDividerLocation((double)1);					
			}
		}	
		else if (this.split != null)
		{
			Vector finalList = new Vector();
			this.percentList = new JList(finalList);
			customKeys(percentList);
			this.percentList.setCellRenderer(new FunctionItemRenderer());
			this.functionSplit.setTopComponent(new JScrollPane(this.percentList));
//			this.split.setDividerLocation((double)0.4);
			if (this.gfcEnabled)
				this.functionSplit.setDividerLocation((double)0.4);
			else
				this.functionSplit.setDividerLocation((double)1);					
		}
//		oldSelectedBinaries = selectedBinaries;
	}
	
	private BinaryNameItem getBinaryNameForGppSample(GppSample s)
	{
		String binaryName;
		if (this.symbolPrimary || this.ittTrace == null)
		{
			binaryName = s.getCurrentFunctionSym().getFunctionBinary().getBinaryName();
			if (!binaryName.endsWith(Messages.getString("NewFunctionAnalyse.notFound"))) //$NON-NLS-1$
				return new BinaryNameItem(binaryName,false);
		}

		if (s.getCurrentFunctionItt() != null)
		{
			binaryName = s.getCurrentFunctionItt().getFunctionBinary().getBinaryName();
			if (!binaryName.endsWith(Messages.getString("NewFunctionAnalyse.notFound"))) //$NON-NLS-1$
			{
				return new BinaryNameItem(binaryName,true);
			}
			else
			{
				binaryName = s.getCurrentFunctionSym().getFunctionBinary().getBinaryName();
				if (binaryName.endsWith(Messages.getString("NewFunctionAnalyse.notFound"))) //$NON-NLS-1$
					return new BinaryNameItem(Messages.getString("NewFunctionAnalyse.binaryNotFound"),true); //$NON-NLS-1$
				else
					return new BinaryNameItem(binaryName,false);
			}			
		}
		else if (!s.getCurrentFunctionSym().getFunctionBinary().getBinaryName().endsWith(Messages.getString("NewFunctionAnalyse.notFound"))) //$NON-NLS-1$
		{
			binaryName = s.getCurrentFunctionSym().getFunctionBinary().getBinaryName();
			return new BinaryNameItem(binaryName,false);
		}
		else
		{
			return new BinaryNameItem(Messages.getString("NewFunctionAnalyse.binaryNotFound"),false); //$NON-NLS-1$
		}
	}
/*	
	private IttSample getIttSampleForGppSample(GppSample s)
	{
		IttSample first = (IttSample)this.ittSamples.firstElement();
		IttSample ittSample = null;
		int diff = (int)(s.sampleSynchTime-first.sampleSynchTime);
		
		// the sample was found from the first index
		if (diff == 0) return first;
		
		// normal case, the first itt sample number is smaller than the
		// required sample number
		if (diff > 0)
		{
			if (diff < this.ittSamples.size())
			{
				// try from the itt sample array, from the location
				// marked by the diff value between the sample synch times
				ittSample = (IttSample)this.ittSamples.elementAt(diff);
				
				if (ittSample.sampleSynchTime == s.sampleSynchTime)
				{
					// the right sample was found with the diff offset 
					return ittSample; 
				}
			}
			else
			{
				// the index points outside the itt sample array
				// take the last itt sample
				ittSample = (IttSample)this.ittSamples.lastElement();
				diff = this.ittSamples.size()-1;
			}
		}
		else
		{
			// the requested sample is outside the itt sample range
			// (smaller than the first element)
			return null;
		}
		
		if ( ((IttSample)this.ittSamples.lastElement()).sampleSynchTime < s.sampleSynchTime)
		{
			// the requested sample is outside the itt sample range
			// (larger than the last element)
			return null;
		}
		
		// the requested sample is in the itt sample range
		// start with the current element and go backwards until it
		// is found, or the correct one is not found
		while(ittSample.sampleSynchTime > s.sampleSynchTime)
		{
			diff--;
			ittSample = (IttSample)this.ittSamples.elementAt(diff);
		}
		
		if (ittSample.sampleSynchTime == s.sampleSynchTime)
		{
			// the sample was finally found
			return ittSample;
		}
		else
		{
			// this sample number is missing from the ITT trace
			return null;
		}
		
	}
	*/
	
	private Long getFunctionOffsetForGppSample(GppSample s)
	{
		//String functionName;
		if ((this.symbolPrimary || this.ittTrace == null) && 
				!s.getCurrentFunctionSym().getFunctionBinary().getBinaryName().endsWith(Messages.getString("NewFunctionAnalyse.notFound")) ) //$NON-NLS-1$
		{
			return Long.valueOf(s.programCounter-s.getCurrentFunctionSym().getStartAddress().longValue());
		}
		if (s.getCurrentFunctionItt() != null && !s.getCurrentFunctionItt().getFunctionName().endsWith(Messages.getString("NewFunctionAnalyse.notFound"))) //$NON-NLS-1$
		{
			//System.out.println("OFF:"+(s.programCounter-s.currentFunctionItt.startAddress.longValue())+" PC: "+Long.toHexString(s.programCounter)+" start"+Long.toHexString(s.currentFunctionItt.startAddress.longValue()));
			return Long.valueOf(s.programCounter-s.getCurrentFunctionItt().getStartAddress().longValue());
		}
		else if (s.getCurrentFunctionSym() != null)
		{
			return Long.valueOf(s.programCounter-s.getCurrentFunctionSym().getStartAddress().longValue());
		}
		else
		{
			return Long.valueOf(666666);
		}		
	}
	
	private FunctionNameItem getFunctionNameForGppSample(GppSample s)
	{
		String functionName;
		if (this.symbolPrimary || this.ittTrace == null)
		{
//			functionName = s.currentFunctionSym.functionName;
//			if (!functionName.endsWith("not found"))
//				return new FunctionNameItem(functionName,s.currentFunctionSym,false);
			FunctionNameItem item = (FunctionNameItem)this.functionNameCacheSym.get(s);
			if (item != null) 
			{	
				return item;
			}
			else
			{
				functionName = s.getCurrentFunctionSym().getFunctionName();
				if (!functionName.endsWith(Messages.getString("NewFunctionAnalyse.notFound"))) //$NON-NLS-1$
				{					
					item = new FunctionNameItem(functionName,s.getCurrentFunctionSym(),false);
					this.functionNameCacheSym.put(s,item);
					return item;
				}
			}
		}
		
		if (s.getCurrentFunctionItt() != null)
		{
			FunctionNameItem item = (FunctionNameItem)this.functionNameCacheItt.get(s);
			if (item != null) return item;
			
			functionName = s.getCurrentFunctionItt().getFunctionName();

			if (!functionName.endsWith(Messages.getString("NewFunctionAnalyse.notFound"))) //$NON-NLS-1$
			{
				item = new FunctionNameItem(functionName,s.getCurrentFunctionItt(),true);
				this.functionNameCacheItt.put(s,item);
				return item;
//				return new FunctionNameItem(functionName,s.currentFunctionItt,true);
			}
			else
			{
				item = (FunctionNameItem)this.functionNameCacheSym.get(s);
				if (item != null) return item;
				
				functionName = s.getCurrentFunctionSym().getFunctionName();
				item = new FunctionNameItem(functionName,s.getCurrentFunctionSym(),false);
				this.functionNameCacheSym.put(s,item);
				return item;
//				return new FunctionNameItem(functionName,s.currentFunctionSym,false);
			}
		}
		else if (s.getCurrentFunctionSym() != null)
		{
			FunctionNameItem item = (FunctionNameItem)this.functionNameCacheSym.get(s);
			if (item != null) return item;
			
			functionName = s.getCurrentFunctionSym().getFunctionName();
			item = new FunctionNameItem(functionName,s.getCurrentFunctionSym(),false);
			this.functionNameCacheSym.put(s,item);
			return item;
//			return new FunctionNameItem(functionName,s.currentFunctionSym,false);
		}
		else
		{
			FunctionNameItem item = new FunctionNameItem(Messages.getString("NewFunctionAnalyse.functionNotFound"),null,false); //$NON-NLS-1$
			this.functionNameCacheItt.put(s,item);
			this.functionNameCacheSym.put(s,item);
			return item;
//			return new FunctionNameItem("Function not found",null,false);
		}
	}
	
	private void sortFunctionIndexList(Vector offsets)
	{
		class IndexElement implements Sortable
		{
			long offset;
			int count;
			
			public long valueOf()
			{
				return count;
			}
		}
		
		for (Enumeration e=offsets.elements();e.hasMoreElements();)
		{
			Vector functionOffsets = (Vector)e.nextElement();

			Hashtable temp = new Hashtable();
			
			// count together the amounts of each offset
			for (Enumeration b=functionOffsets.elements();b.hasMoreElements();)
			{
				Long offset = (Long)b.nextElement();
				IndexElement ie = (IndexElement)temp.get(offset);
				if (ie != null)
				{
					ie.count++;
					//temp.remove(offset);
					//count = new Long(count.longValue()+1);
					//temp.put(offset,count);
				}
				else
				{
					ie = new IndexElement();
					ie.count = 1;
					ie.offset = offset.longValue();
					temp.put(offset,ie);
					//count = new Long(1);
					//temp.put(offset,count);
				}
			}

			//int tempCount = 0;
			//for (Enumeration b=temp.elements();b.hasMoreElements();)
			//{
			//	Long l = (Long)b.nextElement();
			//	tempCount+=l.longValue();
			//}
			
			// all offsets counted together, now sort and put back to original vector
			// the vector will then contain amount/offset pairs of data each element pair
			// one after another
			functionOffsets.clear();
			Vector tempVec = new Vector();
			tempVec.addAll(temp.values());
			QuickSortImpl.sort(tempVec);
			Enumeration finalEnum = tempVec.elements();
			
			while(finalEnum.hasMoreElements())
			{
				IndexElement ie = (IndexElement)finalEnum.nextElement();
				functionOffsets.add(Long.valueOf(ie.offset));
				functionOffsets.add(Long.valueOf(ie.count));
			}
			//System.out.print(" new count: "+functionOffsets.size()+"\n");
			/*
			long totalAmount=0;

			while(temp.size()>0)
			{
				long largestAmount=0;
				long largestOffset=0;

				for (Enumeration keys = temp.keys();keys.hasMoreElements();)
				{
					Long offset = (Long)keys.nextElement();
					Long amount = (Long)temp.get(offset);
					if (amount.longValue()>largestAmount) 
					{
						largestAmount = amount.longValue();
						largestOffset = offset.longValue();
					}
				}

				temp.remove(new Long(largestOffset));
				functionOffsets.add(new Long(largestOffset));
				functionOffsets.add(new Long(largestAmount));
				totalAmount+=largestAmount;
			}	*/	
		}
	}
	
	private Vector sortPercentData(Vector percents, Vector names,Vector offsets,int totalSamples)
	{
		for (int i=0;i<names.size();i++)
		{
			FunctionNameItem item = (FunctionNameItem)names.get(i);
			item.setTotalSampleAmount(totalSamples);
			item.setSampleAmount(((Integer)percents.elementAt(i)).intValue());
			
			Vector offsetVector = null;
			if (functionPercents.isSelected() == true)
			{
				offsetVector = (Vector)offsets.elementAt(i);
			}
			item.setOffsetVector(offsetVector);
		}
		QuickSortImpl.sortReversed(names);
		return names;
	}

	private boolean updating = false;
	
	public void valueChanged(ListSelectionEvent lse)
	{
		if (lse.getValueIsAdjusting())
			return;
		
		if (updating == true) 
		{
			return;
		}
		
		updating = true;
		
		if (lse.getSource() == this.threadList && (mode == Defines.THREADS || mode == Defines.THREADS_FUNCTIONS))
			this.refreshBinaryListComponentSec();
		else if (lse.getSource() == this.binaryList && (mode == Defines.BINARIES || mode == Defines.BINARIES_FUNCTIONS))
		    this.refreshThreadListComponentSec();
		
		this.refreshPercentListComponent();
		
		updating = false;
	}
	
	public void updateGfc()
	{
		if (this.gfcEnabled == false)
			return;
		
		if (this.updateThread != null)
			return;

		updateThread = new Thread()
		{
			public void run()
			{
			if (gfcTrace != null)
				{
				gfcTraceVisualiser = new GfcTraceVisualiser(gfcTrace);
		
				gfcTraceVisualiser.setStartAndEnd((int)((GfcSample)gfcSamples.firstElement()).sampleNumber
						,(int)((GfcSample)gfcSamples.lastElement()).sampleNumber,
						symbolPrimary);
		
				JPanel linkedPanel = new JPanel();
				linkedPanel.setLayout(new BorderLayout());
				linkedPanel.add(gfcTraceVisualiser.getCalleeList(),BorderLayout.NORTH);
				linkedPanel.add(gfcTraceVisualiser.getCenterPanel(),BorderLayout.CENTER);
				linkedPanel.add(gfcTraceVisualiser.getCallerList(),BorderLayout.SOUTH);
				linkedPanel.add(gfcTraceVisualiser.getSelectionPanel(),BorderLayout.WEST);
      
				functionSplit.setBottomComponent(linkedPanel);
				functionSplit.setDividerLocation((double)0.4);
				functionSplit.revalidate();
				functionSplit.updateUI();
				updateUI();
	
				if (mode == Defines.BINARIES || mode == Defines.BINARIES_FUNCTIONS)
				{
				    refreshThreadListComponentSec();
				}
				else
				{
				    refreshBinaryListComponentSec();
				};
				refreshPercentListComponent();
				updateThread = null;
				}
			}
		};
		updateThread.start();
	}
	
	public void stateChanged(ChangeEvent ce)
	{
		if (this.updating == true) return;
		
		this.updating = true;
		
		if (ce.getSource() == this.totalPercents)
		{
			this.refreshPercentListComponent();
			if (mode == Defines.BINARIES || mode == Defines.BINARIES_FUNCTIONS)
			{
			    refreshThreadListComponentSec();
			}
			else
			{
			    refreshBinaryListComponentSec();
			}
		}
		else if (ce.getSource() == this.functionPercents)
		{
			this.refreshPercentListComponent();
		}
		
		this.updating = false;
	}
	
	public void customKeys(JList list)
	{
	    final JList tmpList = list;
	    list.getInputMap().put(KeyStroke.getKeyStroke("control C"), "ctrl c"); //$NON-NLS-1$ //$NON-NLS-2$
		
		list.getActionMap().put("ctrl c", //$NON-NLS-1$
	            new AbstractAction("ctrl c")  //$NON-NLS-1$
	            {
					private static final long serialVersionUID = 4692089714529176119L;

					public void actionPerformed(ActionEvent evt) 
	                {      
	                    String toClipboard = ""; //$NON-NLS-1$
	                    Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
	        			StringSelection contents;
	                    Object[] listValues = tmpList.getSelectedValues();
	                    for (int i = 0; i < listValues.length; i++)
	                    {
	                        Object listValue = listValues[i];
	                        if (listValue instanceof String)
	                        {
	                            toClipboard += (String)listValue + "\n"; //$NON-NLS-1$
	                        }
	                        else if (listValue instanceof BinaryNameItem)
	                        {
	                            BinaryNameItem item = (BinaryNameItem)listValue;
	                            if (totalPercents.isSelected())
	            					toClipboard += item.sampleAmount+Messages.getString("NewFunctionAnalyse.samplesEqual")+((float)item.sampleAmount*100f)/gppSamples.size()+"% "+item + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	            				else 
	            					toClipboard += item.sampleAmount+Messages.getString("NewFunctionAnalyse.samplesEqual")+((float)item.sampleAmount*100f)/item.totalSamples+"% "+item + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	                        }
	                        else if (listValue instanceof FunctionNameItem)
	                        {
	                            FunctionNameItem item = (FunctionNameItem)listValue;
	                            String offsetString = ""; //$NON-NLS-1$
	            				
	            				if (functionPercents.isSelected() == true)
	            				{
	            					DecimalFormat df = new DecimalFormat(Messages.getString("NewFunctionAnalyse.decimalFormat")); //$NON-NLS-1$
	            					float total = 0;
	            					for (Enumeration e = item.offsetVector.elements();e.hasMoreElements();)
	            					{
	            						Long offset = (Long)e.nextElement();
	            						Long amount = (Long)e.nextElement();
	            						offsetString += "(+"+offset.longValue()+"b "; //$NON-NLS-1$ //$NON-NLS-2$
	            						offsetString += amount.longValue()+"s "; //$NON-NLS-1$
	            						offsetString += "="+df.format(100f*(float)amount.longValue()/item.sampleAmount)+"% ) "; //$NON-NLS-1$ //$NON-NLS-2$
	            						total+=(float)amount.longValue()/item.sampleAmount;
	            					}
	            				}

	            				if (totalPercents != null)
	            				{
	            					if (!totalPercents.isSelected())
	            					    toClipboard += item.sampleAmount+Messages.getString("NewFunctionAnalyse.samplesEqual")+((float)item.sampleAmount*100f)/item.totalSamples+"% "+item; //$NON-NLS-1$ //$NON-NLS-2$
	            					else
	            					    toClipboard += item.sampleAmount+Messages.getString("NewFunctionAnalyse.samplesEqual")+((float)item.sampleAmount*100f)/gppSamples.size()+"% "+item; //$NON-NLS-1$ //$NON-NLS-2$

	            					toClipboard += offsetString;
	            				}
	            				toClipboard += "\n"; //$NON-NLS-1$
	                        }
	                    }
	                    contents = new StringSelection(toClipboard);
	        	        cb.setContents(contents, contents);
				    }
	            });
	}
	
	public void showLinkedData()
	{
		if (this.gfcTraceVisualiser != null)
		{
			FunctionNameItem item = (FunctionNameItem)this.percentList.getSelectedValue();
			this.gfcTraceVisualiser.selectFunction(item.name);
		} 
	}
	
	private class BinaryNameItem
	{
		public String name;
		public boolean itt;
		public int sampleAmount;
		public int totalSamples;
		
		public BinaryNameItem(String name, boolean itt)
		{
			this.name = name.toLowerCase();
			this.itt = itt;
			this.sampleAmount = 0;
		}
		
		public String toString()
		{
			return this.name;
		}
		
		public void setSampleAmount(int amount)
		{
			this.sampleAmount = amount;
		}
		
		public void setTotalSampleAmount(int total)
		{
			this.totalSamples = total;
		}

		public boolean equals(Object o)
		{
			if (o == null) return false;

			if (o instanceof String)
			{
				if ( ((String)o).equals(this.name) ) return true;
			}
			else if (o instanceof BinaryNameItem)
			{
				if ( ((BinaryNameItem)o).name.equals(this.name) ) return true;
			}
			else if (this.name.equals(o.toString())) 
			{
				return true;
			}
						
			return false;
		}
		
		public int hashCode()
		{
			return this.name.hashCode();
		}

	}
	
	private static class FunctionNameItem implements Sortable
	{
		public String name;
		public boolean itt;
		public int sampleAmount;
		public int totalSamples;
		public IFunction function;
		public Vector offsetVector;
		public double currentPercent;
		
		public FunctionNameItem(String name, IFunction function, boolean itt)
		{
			this.offsetVector = new Vector();
			this.sampleAmount = 0;
			this.totalSamples = 0;
			this.function = function;
			this.name = name;
			this.itt = itt;
		}
		
		public long valueOf()
		{
			return this.sampleAmount;
		}
		
		public String toString()
		{
			return this.name;
		}
		
		public void setSampleAmount(int amount)
		{
			this.sampleAmount = amount;
		}
		
		public void setTotalSampleAmount(int total)
		{
			this.totalSamples = total;
		}
		public void setOffsetVector(Vector offsetVector)
		{
			this.offsetVector = offsetVector;
		}
		
		public boolean equals(Object o)
		{
			if (o == null) return false;

			if (o instanceof String)
			{
				if ( ((String)o).equals(this.name) ) return true;
			}
			else if (o instanceof FunctionNameItem)
			{
				if ( ((FunctionNameItem)o).name.equals(this.name) ) return true;
			}
			else if (this.name.equals(o.toString())) 
			{
				return true;
			}

			return false;
		}
		
		public int hashCode()
		{
			return this.name.hashCode();
		}
	}
	
	private class FunctionItemRenderer extends JLabel implements ListCellRenderer
	{
		private static final long serialVersionUID = 2036360032509070443L;

		public Color ittColor = Color.ORANGE.darker();
		public Color gppColor = Color.BLUE.darker();
		public Color selectionColor = Color.YELLOW.brighter();
		public FunctionNameItem functionNameItem = null;
		
		public void paint(Graphics g)
		{
			super.paint(g);

			if (this.functionNameItem != null && this.functionNameItem.offsetVector != null)
			{
				
				long length = this.functionNameItem.function.getLength();
				g.setColor(
						// AWT
					    AWTColorPalette.getColor(new RGB(255, 255, 255))
					    // SWT
					    //ColorPalette.getColor(new RGB(255, 255, 255))
				);
				g.fillRect(0,0,100,14);
				
				int[] array = new int[100];
				int maxAmount = 0;
				
				for (Enumeration e=functionNameItem.offsetVector.elements();e.hasMoreElements();)
				{
					long offset = ((Long)e.nextElement()).longValue();
					long amount = ((Long)e.nextElement()).longValue();
					//int color = (int)(amount*255/this.functionNameItem.sampleAmount);
					
					if (length > 0 /*&& !this.functionNameItem.itt*/)
					{
						//float width = (100f/(float)length);

						if (offset > length)
						{
							// there is a problem with the offsets
							// bail out and display a blue box
							g.setColor(
									// AWT
								    AWTColorPalette.getColor(new RGB(100, 100, 200))
								    // SWT
								    //ColorPalette.getColor(new RGB(100, 100, 200))
							);
							g.fillRect(0,0,100,14);
							break;
						}
						else
						{	
							if (offset == length) offset = length-1;
							
							float x1 = (float)(offset*100f)/((float)length);
							float x2 = (float)((offset+1)*100f)/((float)length);
							for (int j=(int)x1;j<(int)x2;j++)
							{
								array[j]+=amount;
								if (maxAmount<array[j]) maxAmount = array[j];
							}
						}
					}
					else
					{
						// there is a problem with the offsets
						// bail out and display a blue box
						g.setColor(
								// AWT
							    AWTColorPalette.getColor(new RGB(100, 100, 200))
							    // SWT
							    //ColorPalette.getColor(new RGB(100, 100, 200))
						);
						g.fillRect(0,0,100,14);
					}
				}
				
				if (maxAmount != 0)
				{
					for (int j=0;j<100;j++)
					{
						if (array[j] > 0)
						{
							array[j] = (array[j]*255)/maxAmount;
							Color c = 
								// AWT
							    AWTColorPalette.getColor(new RGB(array[j], 255-array[j], array[j]/2));
							    // SWT
							    //ColorPalette.getColor(new RGB(array[j], 255-array[j], array[j]/2));
							g.setColor(c);
							g.drawLine(j,0,j,14);
						}
					}
				}
				
				g.setColor(					// AWT
					    AWTColorPalette.getColor(new RGB(0, 0, 0))
					    // SWT
					    //ColorPalette.getColor(new RGB(0, 0, 0))
				);
				g.drawRect(0,0,100,14);
				g.setColor(Color.BLACK);
				g.drawString(""+length,10,12); //$NON-NLS-1$
			}
		}
		
		public Component getListCellRendererComponent(
				JList list,
				Object value,            // value to display
				int index,               // cell index
				boolean isSelected,      // is the cell selected
				boolean cellHasFocus)    // the list and the cell have the focus
		{	
			if (value instanceof BinaryNameItem)
			{
				BinaryNameItem item = (BinaryNameItem)value;
				String s;
				
				if (totalPercents.isSelected())
					s = item.sampleAmount+Messages.getString("NewFunctionAnalyse.samplesEqual")+((float)item.sampleAmount*100f)/gppSamples.size()+"% "+item; //$NON-NLS-1$ //$NON-NLS-2$
				else 
					s = item.sampleAmount+Messages.getString("NewFunctionAnalyse.samplesEqual")+((float)item.sampleAmount*100f)/item.totalSamples+"% "+item; //$NON-NLS-1$ //$NON-NLS-2$
				
				setText(s);
				
				Color color;
				if (item.itt)
				{
					color = this.ittColor;
				}
				else
				{
					color = this.gppColor;
				}
					
				setOpaque(true);
				
				if (isSelected)
				{
					setForeground(color);					
					if (cellHasFocus)
						setBorder(new LineBorder(
							// AWT
						    AWTColorPalette.getColor(new RGB(0, 0, 0))
						    // SWT
						    //ColorPalette.getColor(new RGB(0, 0, 0))
						));
						else setBorder(null);
					setBackground(this.selectionColor);	
				}
				else
				{
					setForeground(color);
					setBackground(
							// AWT
						    AWTColorPalette.getColor(new RGB(255, 255, 255))
						    // SWT
						    //ColorPalette.getColor(new RGB(255, 255, 255))
					);
					setBorder(null);
				}

				setEnabled(list.isEnabled());
				setFont(list.getFont());
			}
			else if (value instanceof FunctionNameItem)
			{	
				FunctionNameItem item = (FunctionNameItem)value;
				this.functionNameItem = item;
				String s = ""; //$NON-NLS-1$
					
				String offsetString = ""; //$NON-NLS-1$
				
				if (functionPercents.isSelected() == true)
				{
					DecimalFormat df = new DecimalFormat(Messages.getString("NewFunctionAnalyse.decimalFormal")); //$NON-NLS-1$
					float total = 0;
					for (Enumeration e = item.offsetVector.elements();e.hasMoreElements();)
					{
						Long offset = (Long)e.nextElement();
						Long amount = (Long)e.nextElement();
						offsetString += "(+"+offset.longValue()+"b "; //$NON-NLS-1$ //$NON-NLS-2$
						offsetString += amount.longValue()+"s "; //$NON-NLS-1$
						offsetString += "="+df.format(100f*(float)amount.longValue()/item.sampleAmount)+"% ) "; //$NON-NLS-1$ //$NON-NLS-2$
						total+=(float)amount.longValue()/item.sampleAmount;
						s = "                                         "; //$NON-NLS-1$
					}
				}

				if (totalPercents != null)
				{
					if (!totalPercents.isSelected())
						s += item.sampleAmount+Messages.getString("NewFunctionAnalyse.samplesEqual")+((float)item.sampleAmount*100f)/item.totalSamples+"% "+item; //$NON-NLS-1$ //$NON-NLS-2$
					else
						s += item.sampleAmount+Messages.getString("NewFunctionAnalyse.samplesEqual")+((float)item.sampleAmount*100f)/gppSamples.size()+"% "+item; //$NON-NLS-1$ //$NON-NLS-2$

					setText(s +"  "+ offsetString); //$NON-NLS-1$
				}
				
				
				Color color;
				if (item.itt)
				{
					color = this.ittColor;
				}
				else
				{
					color = this.gppColor;
				}
					
				setOpaque(true);
				
				if (isSelected)
				{
					setForeground(color);
					setBackground(this.selectionColor);
					
					if (cellHasFocus)
						setBorder(new LineBorder(
							// AWT
						    AWTColorPalette.getColor(new RGB(0, 0, 0))
						    // SWT
						    //ColorPalette.getColor(new RGB(0, 0, 0))
						));
						else setBorder(null);
				}
				else
				{
					setForeground(color);
					setBackground(
							// AWT
						    AWTColorPalette.getColor(new RGB(255, 255, 255))
						    // SWT
						    //ColorPalette.getColor(new RGB(255, 255, 255))
					);					
					setBorder(null);
				}
				
				if (item.function != null)
				{
					if (item.function.getFunctionBinary() != null && item.function.getFunctionBinary().getBinaryName() != null)
					{
						this.setToolTipText(Messages.getString("NewFunctionAnalyse.function1")+item.function.getFunctionName()+Messages.getString("NewFunctionAnalyse.function2")+ //$NON-NLS-1$ //$NON-NLS-2$
								Long.toHexString(item.function.getStartAddress().longValue())+
								Messages.getString("NewFunctionAnalyse.function3")+item.function.getFunctionBinary().getBinaryName()); //$NON-NLS-1$
					}
					else
					{
						this.setToolTipText(Messages.getString("NewFunctionAnalyse.function1")+item.function.getFunctionName()+Messages.getString("NewFunctionAnalyse.function2")+ //$NON-NLS-1$ //$NON-NLS-2$
								Long.toHexString(item.function.getStartAddress().longValue())+
								Messages.getString("NewFunctionAnalyse.function3") + Messages.getString("NewFunctionAnalyse.binaryNotFound")); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				else
				{
					this.setToolTipText(Messages.getString("NewFunctionAnalyse.functionNotFound")); //$NON-NLS-1$
				}
			}
		
			return this;
		}
	}

}
