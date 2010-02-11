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

package com.nokia.carbide.cpp.internal.pi.test;

import java.sql.Date;
import java.sql.Time;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.nokia.carbide.cpp.internal.pi.manager.PluginRegisterer;
import com.nokia.carbide.cpp.internal.pi.manager.PluginRegistry;
import com.nokia.carbide.cpp.internal.pi.plugin.model.AbstractPiPlugin;
import com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace;

public class AnalysisInfoHandler
{
	private static String NOKIA_INTERNAL = Messages.getString("AnalysisInfoHandler.NokiaInternal"); //$NON-NLS-1$

	private String pi_file_version;

	//private Vector analysis_info;
	private String profiler_version;
	private String analyser_version;
	private String profiling_date;
	private String analysing_date;
	private String additional_info_string = "";	//$NON-NLS-1$
	private HashMap <Integer, Object> trace_additional_info = new HashMap <Integer, Object>();
	private String defaultTimeScale = Messages.getString("AnalysisInfoHandler.defaultTimeScale"); //$NON-NLS-1$
//	private ArrayList<EnabledTrace> traceList;

	private PIAnalysisInfo info;

	public AnalysisInfoHandler()
	{
		this.pi_file_version  = PIAnalyser.NPIFileFormat;
		this.profiler_version = Messages.getString("AnalysisInfoHandler.defaultProfilerVersion"); //$NON-NLS-1$
		this.analyser_version = PIAnalyser.version;
		this.profiling_date   = Messages.getString("AnalysisInfoHandler.defaultProfilingDate"); //$NON-NLS-1$
	    Time time = new Time(System.currentTimeMillis());
	    Date date = new Date(System.currentTimeMillis());

	    this.analysing_date = date.toString() + Messages.getString("AnalysisInfoHandler.space") + time.toString(); //$NON-NLS-1$
	}

	public void analysisDataReader(BappeaAnalysisInfo data)
	{
		// convert legacy class to PIAnalysisInfo
		if (data != null)
		{
			String string;
			String legacy_file_version = data.bappea_file_version;

			if (   legacy_file_version.startsWith(Messages.getString("AnalysisInfoHandler.unknown")) //$NON-NLS-1$
				|| legacy_file_version.startsWith(AnalysisInfoHandler.NOKIA_INTERNAL))
			    this.pi_file_version = legacy_file_version;
			else
				this.pi_file_version = AnalysisInfoHandler.NOKIA_INTERNAL + legacy_file_version;

			this.info = new PIAnalysisInfo();

			if (   legacy_file_version.startsWith(Messages.getString("AnalysisInfoHandler.unknown")) //$NON-NLS-1$
				|| legacy_file_version.startsWith(AnalysisInfoHandler.NOKIA_INTERNAL))
				this.info.pi_file_version = legacy_file_version;
			else
				this.info.pi_file_version = AnalysisInfoHandler.NOKIA_INTERNAL + legacy_file_version;

			this.info.analysis_info   = data.analysis_info;

			string = (String) info.analysis_info.elementAt(0);
			if ( !string.startsWith(Messages.getString("AnalysisInfoHandler.unknown")) && !string.startsWith(AnalysisInfoHandler.NOKIA_INTERNAL)) //$NON-NLS-1$
				string = AnalysisInfoHandler.NOKIA_INTERNAL + string;
			info.analysis_info.set(0, string);

			string = (String) info.analysis_info.elementAt(1);
			if ( !string.startsWith(Messages.getString("AnalysisInfoHandler.unknown")) && !string.startsWith(AnalysisInfoHandler.NOKIA_INTERNAL)) //$NON-NLS-1$
				string = AnalysisInfoHandler.NOKIA_INTERNAL + string;
			info.analysis_info.set(1, string);
			
			this.info.trace_info      = new Vector<Object>();
		    this.info.additional_info = data.additional_info;

		    loadVersionInfo();
		    loadAdditionalInfoFromNPI();

		    // create a trace list based on hard-coded ordering
	  		info.trace_info.add(new EnabledTrace(0, Messages.getString("AnalysisInfoHandler.addressThread"))); //$NON-NLS-1$
		    if ((data.trace_info.size() > 1) && ((Boolean)(data.trace_info.get(1))))
		    	info.trace_info.add(new EnabledTrace(1, Messages.getString("AnalysisInfoHandler.call"))); //$NON-NLS-1$
		    if ((data.trace_info.size() > 2) && ((Boolean)(data.trace_info.get(2))))
		  		info.trace_info.add(new EnabledTrace(2, Messages.getString("AnalysisInfoHandler.memory"))); //$NON-NLS-1$
		    if ((data.trace_info.size() > 3) && ((Boolean)(data.trace_info.get(3))))
		  		info.trace_info.add(new EnabledTrace(3, Messages.getString("AnalysisInfoHandler.priority"))); //$NON-NLS-1$
		    if ((data.trace_info.size() > 4) && ((Boolean)(data.trace_info.get(4))))
		  		info.trace_info.add(new EnabledTrace(4, Messages.getString("AnalysisInfoHandler.DSP"))); //$NON-NLS-1$
		    if ((data.trace_info.size() > 5) && ((Boolean)(data.trace_info.get(5))))
		  		info.trace_info.add(new EnabledTrace(5, Messages.getString("AnalysisInfoHandler.instr"))); //$NON-NLS-1$
		    if ((data.trace_info.size() > 6) && ((Boolean)(data.trace_info.get(6))))
		  		info.trace_info.add(new EnabledTrace(6, Messages.getString("AnalysisInfoHandler.TCPIP"))); //$NON-NLS-1$
		    if ((data.trace_info.size() > 7) && ((Boolean)(data.trace_info.get(7))))
		  		info.trace_info.add(new EnabledTrace(7, Messages.getString("AnalysisInfoHandler.custom"))); //$NON-NLS-1$
		    if ((data.trace_info.size() > 8) && ((Boolean)(data.trace_info.get(8))))
		  		info.trace_info.add(new EnabledTrace(8, Messages.getString("AnalysisInfoHandler.map"))); //$NON-NLS-1$
		    if ((data.trace_info.size() > 9) && ((Boolean)(data.trace_info.get(9))))
		  		info.trace_info.add(new EnabledTrace(9, Messages.getString("AnalysisInfoHandler.IRQSWI"))); //$NON-NLS-1$
		    if ((data.trace_info.size() > 10) && ((Boolean)(data.trace_info.get(10))))
		  		info.trace_info.add(new EnabledTrace(10, Messages.getString("AnalysisInfoHandler.button"))); //$NON-NLS-1$
		}
		else
		{
		    this.analyser_version = Messages.getString("AnalysisInfoHandler.unknown"); //$NON-NLS-1$
		    this.analysing_date   = Messages.getString("AnalysisInfoHandler.unknown"); //$NON-NLS-1$
		    this.profiler_version = Messages.getString("AnalysisInfoHandler.unknown"); //$NON-NLS-1$
		    this.pi_file_version  = Messages.getString("AnalysisInfoHandler.unknownFileVersion"); //$NON-NLS-1$
		}
	}

	public void analysisDataReader(PIAnalysisInfo data)
	{
		if (data != null)
		{
		    this.info = data;
		    this.pi_file_version = data.pi_file_version;
		    loadVersionInfo();
		    loadAdditionalInfoFromNPI();
		}
		else
		{
		    this.analyser_version = Messages.getString("AnalysisInfoHandler.unknown"); //$NON-NLS-1$
		    this.analysing_date   = Messages.getString("AnalysisInfoHandler.unknown"); //$NON-NLS-1$
		    this.profiler_version = Messages.getString("AnalysisInfoHandler.unknown"); //$NON-NLS-1$
		    this.pi_file_version  = Messages.getString("AnalysisInfoHandler.unknownFileVersion"); //$NON-NLS-1$
		    this.additional_info_string = Messages.getString("AnalysisInfoHandler.unknown"); //$NON-NLS-1$
		}
	}

	public Vector getTraceInfo() {
		if (this.info.trace_info == null)
			this.info.trace_info = new Vector<Object>();

		return this.info.trace_info;
	}

	private void loadVersionInfo()
	{
		this.profiler_version = (String) info.analysis_info.elementAt(0);
		this.analyser_version = (String) info.analysis_info.elementAt(1);
		this.profiling_date   = (String) info.analysis_info.elementAt(2);
		this.analysing_date   = (String) info.analysis_info.elementAt(3);
		if (info.analysis_info.size() > 4)  //file format version 1.2 or newer
			this.defaultTimeScale = (String) info.analysis_info.elementAt(4);

		// ignore info.additional_info, which only contains a single placeholder button trace comment
	}
	
	private void loadAdditionalInfoFromNPI()
	{
		int additional_info_size = info.additional_info.size();
		for (int i = 0; i < additional_info_size; i++) {
			Object additional = info.additional_info.elementAt(i);
			if (additional instanceof TraceAdditionalInfo) {
				TraceAdditionalInfo traceInfo = (TraceAdditionalInfo)additional;
				Set<Entry<Integer, Vector<Object>>> infoSet = traceInfo.getAdditionalInfoSet();
				for (Entry<Integer, Vector<Object>> info : infoSet) {
					// this code is clumsy because the plugin, not the trace, has the trace ID info
					PluginRegisterer.registerAllPlugins();	// I do not agree this kind of laziness for loading, but what else can we do now
					Enumeration<AbstractPiPlugin> enuPlugins = PluginRegistry.getInstance().getRegistryEntries(); //$NON-NLS-1$
					while (enuPlugins.hasMoreElements()) {	// find plugin corresponding to this info and ask it to process
						AbstractPiPlugin plugin = enuPlugins.nextElement();
						if (!(plugin instanceof ITrace)) {
							continue;
						}
						if (!info.getKey().equals(((ITrace)plugin).getTraceId())) {
							continue;
						}
						if (plugin instanceof IProvideTraceAdditionalInfo) {
							((IProvideTraceAdditionalInfo)plugin).additionalInfoToAnalysisInfoHandler(info.getValue(), this);
							String displayString = ((IProvideTraceAdditionalInfo)plugin).InfoHandlerToDisplayString(this);
							if (displayString.length() > 0) {
								this.additional_info_string += ((ITrace)plugin).getTraceName() + ":\n"; //$NON-NLS-1$
								this.additional_info_string += displayString + "\n"; //$NON-NLS-1$
							}
						}
					}
				}
			}
		}
	}
	
	public void setTraceDefinedInfo(int traceId, Object list) {
		trace_additional_info.put(traceId, list);
	}
	
	public Object getTraceDefinedInfo(int traceId) {
		Object result = trace_additional_info.get(traceId);
		return result;
	}

	public void setFileVersion(String pi_file_version)
	{
		this.pi_file_version = pi_file_version;
	}

	public String getFileVersion()
	{
		return this.pi_file_version;
	}

	public String getAnalysingDate()
	{
		return this.analysing_date;
	}

	public String getProfilingDate()
	{
		return this.profiling_date;
	}
 
	public String getAnalyserVersion()
	{
		return this.analyser_version;
	}

	public String getProfilerVersion()
	{
		return this.profiler_version;
	}

	public String getAdditional_info_string() {
		return additional_info_string;
	}

	public void setPITimeScale(String scale)
	{
		this.defaultTimeScale = scale;
	}
  
	public String getPITimeScaleString()
	{
		return this.defaultTimeScale;
	}
  
	public float getPITimeScaleFloat()
	{
		if (this.defaultTimeScale.indexOf("-") != -1) //$NON-NLS-1$
			return 1.025f;
		else
		    return Float.parseFloat(this.defaultTimeScale);
	}
  	  
	//this is used to get data for storing into file system
	public PIAnalysisInfo getAnalysisInfo()
	{
		PIAnalysisInfo info;
		info = new PIAnalysisInfo();
	    info.pi_file_version = this.pi_file_version;

	    //creating version info
	    info.analysis_info.add(this.profiler_version);
	    info.analysis_info.add(this.analyser_version);
	    info.analysis_info.add(this.profiling_date);
	    info.analysis_info.add(this.analysing_date);
	    info.analysis_info.add(this.defaultTimeScale);

	    //creating the trace info vector
	    info.trace_info.clear();

	    return info;
	}
	
	// internal test method
	public void eraseTimeStamp () {
		int count = analysing_date.length();
		analysing_date = ""; //$NON-NLS-1$
		
		// replace with underscore
		for (int i = 0; i < count; i++)
			analysing_date += "_"; //$NON-NLS-1$
	}

	// return a table of analysis info
	public Table getAnalysisInfoTable(Composite parent)
	{
		TableItem item;

		// create a two column table - descriptions and values
		Table table = new Table(parent, SWT.HIDE_SELECTION);
		table.setRedraw(false);
		table.setHeaderVisible(false);
		table.setLinesVisible(false);
		table.setBackground(parent.getBackground());

		TableColumn col1 = new TableColumn(table, SWT.LEFT);
		TableColumn col2 = new TableColumn(table, SWT.LEFT);

		item = new TableItem(table, SWT.NONE);
		item.setText(0, Messages.getString("AnalysisInfoHandler.analyzerVersion")); //$NON-NLS-1$
		item.setText(1, this.getAnalyserVersion());
		item = new TableItem(table, SWT.NONE);
		item.setText(0, Messages.getString("AnalysisInfoHandler.analyzeFileFormat")); //$NON-NLS-1$
		item.setText(1, this.pi_file_version);
		item = new TableItem(table, SWT.NONE);
		item.setText(0, Messages.getString("AnalysisInfoHandler.analyzeFileDate")); //$NON-NLS-1$
		item.setText(1, this.getAnalysingDate());

		item = new TableItem(table, SWT.NONE);
		item.setText(0, Messages.getString("AnalysisInfoHandler.profilingDate")); //$NON-NLS-1$
		item.setText(1, this.getProfilingDate());
		item = new TableItem(table, SWT.NONE);

		// sort by trace id number
		boolean swapped = true;

		while (swapped) {
  			EnabledTrace a;
  			EnabledTrace b;
  			swapped = false;
  			for (int i = 0; i < info.trace_info.size() - 1; i++) {
  				a = (EnabledTrace) info.trace_info.get(i);
  				b = (EnabledTrace) info.trace_info.get(i + 1);
  				if (a.traceId > b.traceId) {
  					info.trace_info.set(i, b);
  					info.trace_info.set(i + 1, a);
  					swapped = true;
  				}
  			}
  		}

		item = new TableItem(table, SWT.NONE);
    	item.setText(0, Messages.getString("AnalysisInfoHandler.traces")); //$NON-NLS-1$

		for (int i = 0; i < info.trace_info.size(); i++) {
	    	EnabledTrace data = (EnabledTrace) info.trace_info.get(i);
			item = new TableItem(table, SWT.NONE);
	    	item.setText(0, Messages.getString("AnalysisInfoHandler.whitespace") + data.traceName); //$NON-NLS-1$
		}
		
		if (additional_info_string != null) {
			item = new TableItem(table, SWT.NONE);
			item.setText(additional_info_string);			
		}

		col1.pack();
		col2.pack();
		table.setRedraw(true);
		return table;
	}

	// return a table of analysis info
	public void getAnalysisInfoLabels(Composite parent)
	{
		Label label;
		String string;

		label = new Label(parent, SWT.NONE);
		string = Messages.getString("AnalysisInfoHandler.analyzerVersion2"); //$NON-NLS-1$
		label.setText(string + this.getAnalyserVersion());
		label = new Label(parent, SWT.NONE);
		string = Messages.getString("AnalysisInfoHandler.analyzeFileFormat2"); //$NON-NLS-1$
		label.setText(string + this.pi_file_version);
		label = new Label(parent, SWT.NONE);
		string = Messages.getString("AnalysisInfoHandler.analyzeFileDate2"); //$NON-NLS-1$
		label.setText(string + this.getAnalysingDate());

		label = new Label(parent, SWT.NONE);

		// sort by trace id number
		boolean swapped = true;

		while (swapped) {
  			EnabledTrace a;
  			EnabledTrace b;
  			swapped = false;
  			for (int i = 0; i < info.trace_info.size() - 1; i++) {
  				a = (EnabledTrace) info.trace_info.get(i);
  				b = (EnabledTrace) info.trace_info.get(i + 1);
  				if (a.traceId > b.traceId) {
  					info.trace_info.set(i, b);
  					info.trace_info.set(i + 1, a);
  					swapped = true;
  				}
  			}
  		}

		label = new Label(parent, SWT.NONE);
    	label.setText(Messages.getString("AnalysisInfoHandler.traces2")); //$NON-NLS-1$

		for (int i = 0; i < info.trace_info.size(); i++) {
	    	EnabledTrace data = (EnabledTrace) info.trace_info.get(i);
			label = new Label(parent, SWT.NONE);
	    	label.setText(Messages.getString("AnalysisInfoHandler.whitespace2") + data.traceName); //$NON-NLS-1$
		}
		
		if (additional_info_string != null) {
			label = new Label(parent, SWT.NONE);
			label.setText(additional_info_string);			
		}
	}
}
