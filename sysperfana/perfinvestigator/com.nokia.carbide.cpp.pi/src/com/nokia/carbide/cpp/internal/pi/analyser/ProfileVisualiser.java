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

package com.nokia.carbide.cpp.internal.pi.analyser;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Sash;

import com.nokia.carbide.cpp.internal.pi.utils.PIUtilities;
import com.nokia.carbide.cpp.internal.pi.visual.PICompositePanel;
import com.nokia.carbide.cpp.internal.pi.visual.PIEvent;
import com.nokia.carbide.cpp.pi.editors.PIPageEditor;


public class ProfileVisualiser {
	// rather than abitrary topologies, we will support pages containing:
	//	only a top composite
	//  a top composite and a bottom composite separated by a sash
	public static final int TOP_ONLY       = 1;
	public static final int TOP_AND_BOTTOM = 2;

	private ProfileVisualiser thisVisualiser;
	
	// page for this visualiser
	private Composite page;
	
	// title
	private Label title;
	
	// secondary title
	private Label title2;
	
	// current time selection string
	private Label timeInterval;
	private static final String noInterval = Messages.getString("ProfileVisualiser.noInterval"); //$NON-NLS-1$

	public static final DecimalFormat timeFormat = new DecimalFormat(Messages.getString("ProfileVisualiser.decimalFormat")); //$NON-NLS-1$

	// composite at the top of the page
	private PICompositePanel topComposite;
	
	// composite at the bottom of the page (optional)
	private SashForm bottomComposite;
	
	private Component currentInfoComponent;
	private ParserRepository parserRepository;
	private String pageName = null;
		
	public boolean visualiserEnabled = true;
  
//	public ProfileVisualiser(int topology, Composite parent, AnalyseTab tab)
//	{
//		initialize(parent, tab, "");
//	}
	
	public ProfileVisualiser(int topology, Composite parent, String pageName)
	{
		this.thisVisualiser = this;
		this.pageName = pageName;
		this.page = new Composite(parent, SWT.NONE);

		initialize(topology, page);
	}

	private void initialize(int topology, Composite newPage)
	{
		// newPage (Composite)
		//		titleBar (Composite)
		//		holder   (Composite)
		FormData formData;
		
		if (   (topology != TOP_ONLY)
			&& (topology != TOP_AND_BOTTOM)) {
			return;
		}
		
		this.parserRepository = new ParserRepository();
		
		
		// add the title bar at the top
		Composite titleBar = addTitleBar(newPage);
		
		// all graphs and tables go into a composite below the title
		Composite holder = new Composite(newPage, SWT.NONE);
		
    	// FormData for the title bar
		formData = new FormData();
		formData.top    = new FormAttachment(0);
		formData.left   = new FormAttachment(0);
		formData.right  = new FormAttachment(100);
		titleBar.setLayoutData(formData);
		titleBar.setLayout(new FormLayout());
		
		// FormData for the overall holder composite
		formData = new FormData();
		formData.top    = new FormAttachment(titleBar);
		formData.bottom = new FormAttachment(100);
		formData.left   = new FormAttachment(0);
		formData.right  = new FormAttachment(100);
		holder.setLayoutData(formData);
		holder.setLayout(new FormLayout());

		newPage.setLayout(new FormLayout());
		
		setGraphsAndTablesHolder(holder, topology, formData);
	}

	private Composite addTitleBar(Composite newPage)
	{
		// titleBar (Composite)
		//		title2 (Label)  title (Label) timeInterval (Label)

		FormData formData;
		Composite titleBar = new Composite(newPage, SWT.NONE);
		
		title2 = new Label(titleBar, SWT.LEFT);
		title2.setBackground(newPage.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
		title2.setFont(PIPageEditor.helvetica_9);
		
		title = new Label(titleBar, SWT.CENTER);
		title.setBackground(newPage.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
		title.setFont(PIPageEditor.helvetica_9);

		timeInterval = new Label(titleBar, SWT.RIGHT);
		timeInterval.setBackground(newPage.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
		timeInterval.setText(noInterval);
		timeInterval.setFont(PIPageEditor.helvetica_9);

		formData = new FormData();
		formData.left   = new FormAttachment(0);
		formData.right  = new FormAttachment(30);
		title2.setLayoutData(formData);
		
		formData = new FormData();
		formData.left   = new FormAttachment(30);
		formData.right  = new FormAttachment(70);
		title.setLayoutData(formData);
		
		formData = new FormData();
		formData.left   = new FormAttachment(70);
		formData.right  = new FormAttachment(100);
		timeInterval.setLayoutData(formData);

		return titleBar;
	}

	private void setGraphsAndTablesHolder(Composite holder, int topology, FormData formData)
	{
		// holder (Composite)
		//		topComposite (PICompositePanel/ScrolledComposite/SashForm)
		//		bottomComposite (SashForm)

		// create a top composite that can hold graphs and/or tables
		// NOTE: Sometimes we really only need a Composite(newPage, SWT.NONE)
		this.topComposite = new PICompositePanel(holder, thisVisualiser);
		
		if (topology == TOP_ONLY) {
			formData.left   = new FormAttachment(0);
			formData.right  = new FormAttachment(100);
			this.topComposite.getSashForm().setLayoutData(formData);
			this.topComposite.getSashForm().setLayout(new FormLayout());
			return;
		}
		
		// create a bottom composite only suitable for holding tables
		this.bottomComposite = new SashForm(holder, SWT.VERTICAL);
		
		// A sash separates top composite from bottom composite
		final Sash acrossSash = new Sash(holder, SWT.HORIZONTAL);
		
		// FormData for top
		formData = new FormData();
		formData.top    = new FormAttachment(0);
		formData.bottom = new FormAttachment(acrossSash);
		formData.left   = new FormAttachment(0);
		formData.right  = new FormAttachment(100);
		this.topComposite.getSashForm().setLayoutData(formData);
		this.topComposite.getSashForm().setLayout(new FormLayout());
		
		// FormData for bottom
		formData = new FormData();
		formData.top    = new FormAttachment(acrossSash);
		formData.bottom = new FormAttachment(100);
		formData.left   = new FormAttachment(0);
		formData.right  = new FormAttachment(100);
		bottomComposite.setLayoutData(formData);
		bottomComposite.setLayout(new FormLayout());
		
		// FormData for acrossSash
		// Put it initially in the middle
		formData = new FormData();
		formData.top    = new FormAttachment(50);
		formData.left   = new FormAttachment(0);
		formData.right  = new FormAttachment(100);
		acrossSash.setLayoutData(formData);

		final FormData acrossSashData = formData;
		final Composite parentFinal = acrossSash.getParent();
		acrossSash.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (event.detail != SWT.DRAG) {
					acrossSashData.top = new FormAttachment(0, event.y);
					parentFinal.layout();
				}
			}
		});
	}

	public void setTimeInterval(final double start, final double end)
	{
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				PIPageEditor.currentPageEditor().setLocalTime(start, end);
			}
		});
	}

	public void fetchSelection()
	{
		double start = this.topComposite.getSelectionStart() / 1000;
		double end =   this.topComposite.getSelectionEnd() / 1000;
		PIPageEditor.currentPageEditor().setLocalTime(start, end);
	}
	  
	public String getPageName()
	{
		return this.pageName;
	}
	
	public void setPageName(String pageName)
	{
		this.pageName = pageName;
	}
	
	public void saveScreenshot() throws IOException
	{
	  	File filePath = null;
	  	
	  	try
		{
	  	    filePath = PIUtilities.getAFile(true, "jpg"); //$NON-NLS-1$
		}
	  	catch (Exception e)
		{
	  		return;
		}
	  	
	    if (filePath == null)
	    {
	      System.out.println(Messages.getString("ProfileVisualiser.noPath")); //$NON-NLS-1$
	      return;
	    }
	    System.out.println(Messages.getString("ProfileVisualiser.savingScreenshot")); //$NON-NLS-1$
	}

	public void saveListScreenshot() throws IOException
	{
	  	if (this.currentInfoComponent == null) return;
	  		
	   	File filePath = null;
	  	
	  	try
		{
	  	    filePath = PIUtilities.getAFile(true, "jpg"); //$NON-NLS-1$
		}
	  	catch (Exception e)
		{
	  		return;
		}
	  	
	    if (filePath == null)
	    {
	      System.out.println(Messages.getString("ProfileVisualiser.noPath")); //$NON-NLS-1$
	      return;
	    }
	    System.out.println(Messages.getString("ProfileVisualiser.savingScreenshot")); //$NON-NLS-1$
	}

	public void action(String actionString)
	{
		// if there is any action that is applicable for panel without
		// a graph put it here
		
		// below actions are only applicable for panels that have a graph
    	if (this.topComposite.getActiveGraph() == null)
    		return;

	    if (actionString.equals("screenshot")) //$NON-NLS-1$
	  	{
	  		try
			{
	  			this.saveScreenshot();
		    }
		    catch (Exception e)
		    {
		        e.printStackTrace();
		    }
	  	}
	  	else if (actionString.equals("listscreenshot")) //$NON-NLS-1$
	  	{
	  		try
		    {
		       this.saveListScreenshot();
		    }
		    catch (Exception e)
		    {
		        e.printStackTrace();
		    }
	  	}
	  	else if (actionString.equals("+")) //$NON-NLS-1$
	  	{
	  		this.topComposite.performZoomCommand("+"); //$NON-NLS-1$
	  	}
	  	else if (actionString.equals("-")) //$NON-NLS-1$
	  	{
	  		this.topComposite.performZoomCommand("-"); //$NON-NLS-1$
	  	}
	  	else if (actionString.equals("++")) //$NON-NLS-1$
	  	{
	  		this.topComposite.performZoomCommand("++"); //$NON-NLS-1$
	  	}
	  	else if (actionString.equals("--")) //$NON-NLS-1$
	  	{
	  		this.topComposite.performZoomCommand("--"); //$NON-NLS-1$
	  	}
		else if (actionString.equals("changeSelection")) //$NON-NLS-1$
	  	{
			// change the time interval selected
	  	    this.topComposite.selectionChanged();
	  	}		
	  	else if (actionString.equals("changeInterval")) //$NON-NLS-1$
	  	{
	  		// change the time interval displayed
        	double startTime = PIPageEditor.currentPageEditor().getStartTime();
        	double endTime   = PIPageEditor.currentPageEditor().getEndTime();

            if (startTime == -1 || endTime == -1)
            {
                this.timeInterval.setText(noInterval);
            }
            else
            {
            	this.timeInterval.setText(ProfileVisualiser.getTimeInterval(startTime, endTime));
            }
            // now the data are updated, let the zoomCommand of the panel 
            // handle the selection centering and refresh
            this.topComposite.performZoomCommand("changeInterval"); //$NON-NLS-1$
	  	}
	  	else if (actionString.equals("stretch")) //$NON-NLS-1$
	  	{
	  	}
		else if (actionString.equals("changeResolution")) //$NON-NLS-1$
		{
		}
		else if (actionString.equals("fetchSelection")) //$NON-NLS-1$
	  	{
	  	    this.fetchSelection();
	  	}
		else if (actionString.equals("changeThresholdThread")) //$NON-NLS-1$
		{
	        PIEvent be = new PIEvent(
	        		null, PIEvent.THRESHOLD_THREAD_CHANGED);
	        this.topComposite.piEventReceived(be); 
		}
		else if (actionString.equals("changeThresholdBinary")) //$NON-NLS-1$
		{
	        PIEvent be = new PIEvent(
	        		null, PIEvent.THRESHOLD_BINARY_CHANGED);
	        this.topComposite.piEventReceived(be); 
		}
		else if (actionString.equals("changeThresholdFunction")) //$NON-NLS-1$
		{
	        PIEvent be = new PIEvent(
	        		null, PIEvent.THRESHOLD_FUNCTION_CHANGED);
	        this.topComposite.piEventReceived(be); 
		}
	}

	public Composite getContentPane()
	{
		return this.page;
	}

	public ParserRepository getParserRepository()
	{
		return this.parserRepository;
	}
	  
	public int getLastSampleX()
	{
		return this.topComposite.lastSampleX;
	}

	public void setLastSampleX(int aksa)
	{
	  	if (this.topComposite.lastSampleX < aksa)
	  		this.topComposite.lastSampleX = aksa;
	  	
	  	// do the set even if nothing has changed except that a new graph was added to the tab
  		this.topComposite.setSizeX(true);
	}
	  
	public void vPanelRepaint(Component infoComponent)
	{
		//make this   
		if (infoComponent != null &&  infoComponent != this.currentInfoComponent)
		{
			this.currentInfoComponent = infoComponent;
		}
	}
	  
	public SashForm getBottomComposite()
	{
		return this.bottomComposite;
	}
	  
	public PICompositePanel getTopComposite()
	{
		return this.topComposite;
	}
	
	public Label getTitle()
	{
		return this.title;
	}
	
	public Label getTitle2()
	{
		return this.title2;
	}
	
	public Label getTimeString()
	{
		return this.timeInterval;
	}
	
	public static String getTimeInterval(double startTime, double endTime)
	{
		return Messages.getString("ProfileVisualiser.interval1") + timeFormat.format(startTime) //$NON-NLS-1$
		     + Messages.getString("ProfileVisualiser.interval2") + timeFormat.format(endTime) //$NON-NLS-1$
		     + Messages.getString("ProfileVisualiser.interval3") + timeFormat.format(endTime - startTime) //$NON-NLS-1$
		     + Messages.getString("ProfileVisualiser.interval4"); //$NON-NLS-1$
	}
}
