/*
* Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies).
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
* Description: 
*
*/
package com.nokia.s60tools.swmtanalyser.wizards;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Chapter;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.GrayColor;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.nokia.s60tools.swmtanalyser.SwmtAnalyserPlugin;
import com.nokia.s60tools.swmtanalyser.analysers.AnalyserConstants;
import com.nokia.s60tools.swmtanalyser.data.OverviewData;
import com.nokia.s60tools.util.console.AbstractProductSpecificConsole;
import com.nokia.s60tools.util.console.IConsolePrintUtility;

/**
 * Job to create PDF report.
 *
 */
public class ReportCreationJob extends Job {
	

	/**
	 * Spacing after a header text, to avoid texts to be too close each other
	 */
	private static final int SPACING_AFTER_HEADER_TEXT = 5;
	//Paddings in cells
	private float cellPaddingSmall;
	private float cellPaddingTableHeader;
	
	//Colors used in document
	private Color colorHeading;
	private Color colorTableHeaderBackGrd;
	private Color colorTable2ndHeaderBackGrd;
	private Color colorSeverityNormal;
	private Color colorSeverityHigh;
	private Color colorSeverityCritical;
	
	//Fonts used in document
	private Font fontTable2ndHeaderText;
	private Font fontHeader;
	private Font fontNormalSmallTables;
	private Font fontNormal;
	private Font fontHeading2;
	private Font fontHeading1;
	
	//PDF filename
	private String fileName = null;
	//Use given comments
	private String comment = null;
	//Oveview info to write in pdf
	private OverviewData ov;
	//ROM Checksum to write in pdf
	private String rom_checkSum_string;
	//ROM Version to write in pdf
	private String rom_version_string;
	//Tree object to get the issues info
	private Tree all_tree_items; 
	//To save the report type option
	boolean isOverviewReport;
	//temporary variable
	private PdfPTable table;
	
	/**
	 * Job constructor.
	 * @param name Job name
	 * @param fileName PDF filename
	 * @param comment User given comments
	 * @param ov Overview object
	 * @param checksum ROM Checksum
	 * @param version ROM Version
	 * @param issues_tree Tree object from Analysis view.
	 * @param isOverviewReport report type
	 */
	public ReportCreationJob(String name, String fileName, String comment, OverviewData ov, String checksum, String version, Tree issues_tree, boolean isOverviewReport) {
		super(name);
		this.fileName = fileName;
		this.comment = comment;
		this.ov = ov;
		this.rom_checkSum_string = checksum;
		this.rom_version_string = version;
		this.all_tree_items = issues_tree;
		this.isOverviewReport = isOverviewReport;
		
		//Initialize Colors and Fonts
		initStyles();				
	}

	/**
	 * Initializing Colors and fonts to be used in PDF Document.
	 * Color scheme is taken from Carbide logo.
	 */
	private void initStyles() {
		
		//
		// Colors used are picked up from Carbide.c++ -logo, from dark blue to white
		// #003399 = 0, 51, 153 -Dark blue
		// #0088ff = 0, 136, 255 -mid blue
		// #33aaff = 51, 170, 255 -mid blue 2
		// #88ccff = 136, 204, 255 -thin blue
		// #ffffff = -white
		//
		 
		//
		//Using Carbide.c++ logo colors to decorate report
		//
		colorHeading = new Color(0, 51, 153);
		colorTableHeaderBackGrd = new Color (136, 204, 255);
		colorTable2ndHeaderBackGrd = new Color (0, 136, 255);
		

		//
		// Setting Severity Colors for PDF report from Analyser view colors.
		//
		org.eclipse.swt.graphics.Color color = AnalyserConstants.COLOR_SEVERITY_CRITICAL; 
		colorSeverityCritical = new Color (color.getRed(),color.getGreen(),color.getBlue());		

		color = AnalyserConstants.COLOR_SEVERITY_HIGH;
		colorSeverityHigh = new Color (color.getRed(),color.getGreen(),color.getBlue());		
		
		color = AnalyserConstants.COLOR_SEVERITY_NORMAL; 
		colorSeverityNormal = new Color (color.getRed(),color.getGreen(),color.getBlue());
		
		//
		// Font used in report
		//
		String font =  FontFactory.HELVETICA;

		//
		// Creating fonts
		//
		fontTable2ndHeaderText = FontFactory.getFont(font,10f,Font.NORMAL, Color.WHITE);
		fontHeader = FontFactory.getFont(font, 16, Font.BOLD, Color.BLACK);
		fontNormalSmallTables = FontFactory.getFont(font,9f,Font.NORMAL);
		fontNormal = FontFactory.getFont(font,10f,Font.NORMAL);
		fontHeading2 = FontFactory.getFont(font,10f,Font.BOLD);
		fontHeading1 = FontFactory.getFont(font,12f,Font.BOLD, colorHeading);
		
		cellPaddingSmall = 1.0f;
		cellPaddingTableHeader = 3.0f;
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IStatus run(IProgressMonitor monitor) {
	
		monitor.beginTask("Creating report...", 10);

		try {
			//Instantiation of document object
			Document document = new Document(PageSize.A4, 50, 50, 50, 50);
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(this.fileName));
			document.open();
			
			addGeneralDetails(document);
			
			if(!this.isOverviewReport) //If the report type is 1. i.e., report for selected issues only.
			{
				addSelectedIssuesReport(document);
			}
			else // If the report type is 2. i.e., Overview report
			{
				addOverviewReport(document);
			}
			
			addComments(document);
			
			//Close document
			document.close();
			//Close the writer
			writer.close();
			
			
		} catch (DocumentException e) {
			e.printStackTrace();
			SwmtAnalyserPlugin.getConsole().println("Unable to write document, error was: '" +e +"'", IConsolePrintUtility.MSG_ERROR);
		}		
		catch (FileNotFoundException e) {
			e.printStackTrace();
			SwmtAnalyserPlugin.getConsole().println("Unable to write document, error was: '" +e +"'", IConsolePrintUtility.MSG_ERROR);
		}		
		catch (Exception e) {
			e.printStackTrace();
			AbstractProductSpecificConsole absConsole = (AbstractProductSpecificConsole)SwmtAnalyserPlugin.getConsole();
			absConsole.printStackTrace(e);
			SwmtAnalyserPlugin.getConsole().println("Unable to write document, error was: '" +e +"'", IConsolePrintUtility.MSG_ERROR);
		}		
		return Status.OK_STATUS;
	}

	private void addComments(Document document) throws DocumentException {
		//Create new line
		document.add(Chunk.NEWLINE);
		//Comments heading
		Paragraph comments_title = new Paragraph("User given comments", fontHeading1);
		document.add(comments_title);
		
		Paragraph comments = new Paragraph(this.comment, fontNormal);
		document.add(comments);
	}

	private void addGeneralDetails(Document document) throws DocumentException {
		//Report Title
		Paragraph title = new Paragraph("MemSpy - System Wide Memory Tracking - Analysis Report", fontHeader);
		title.setAlignment(Element.ALIGN_CENTER);
		document.add(title);
		document.add(Chunk.NEWLINE);
		
		//Introduction title
		Paragraph hdng = new Paragraph("Introduction of Memspy (S60) and SWMT Analyser", fontHeading1);
		document.add(hdng);
		
		//About the MemSpy S60 Application
		Paragraph intro_memspy = new Paragraph("The MemSpy S60 application tracks various subsystems that directly or indirectly contribute to overall system memory usage and provides information about the changes in these subsystems at specified time intervals.", fontNormal);
		document.add(intro_memspy);
		document.add(Chunk.NEWLINE);
		//About the SWMT Analyser
		Paragraph intro_swmt = new Paragraph("A System Wide Memory Tracker log file contains information about system wide memory status changes over time. SWMT Analyser is a Carbide.c++ Extension for analyzing System Wide Memory Tracking logs produced by the MemSpy S60 application and imported to PC with the MemSpy Carbide.c++ Extension.", fontNormal);
		document.add(intro_swmt);
		document.add(Chunk.NEWLINE);
		
		//Properties heading
		Paragraph props_title = new Paragraph("Properties", fontHeading1);
		document.add(props_title);
		Chunk no_of_cycles = new Chunk("No of cycles	:	", fontHeading2);
		document.add(no_of_cycles);
		Chunk cycles = new Chunk(ov.noOfcycles+"", fontNormal);
		document.add(cycles);
		document.add(Chunk.NEWLINE);
		
		Chunk time_period = new Chunk("Time period	:	", fontHeading2);
		document.add(time_period);
		Chunk period = new Chunk(ov.fromTime+" to "+ov.toTime, fontNormal);
		document.add(period);
		document.add(Chunk.NEWLINE);
		
		Chunk duration = new Chunk("Time duration	:	", fontHeading2);
		document.add(duration);
		Chunk dur = new Chunk(ov.durationString, fontNormal);
		document.add(dur);
		document.add(Chunk.NEWLINE);
		
		
		document.add(Chunk.NEWLINE);
		Paragraph rom_title = new Paragraph("ROM Details", fontHeading1);
		document.add(rom_title);
		Chunk rom_checksum = new Chunk("ROM Checksum	:	", fontHeading2);
		document.add(rom_checksum);
		Chunk checksum = new Chunk(this.rom_checkSum_string, fontNormal);
		document.add(checksum);
		document.add(Chunk.NEWLINE);
		
		Chunk rom_version = new Chunk("ROM Version	:	", fontHeading2);
		document.add(rom_version);
		Chunk version = new Chunk(this.rom_version_string, fontNormal);
		document.add(version);
		document.add(Chunk.NEWLINE);
		document.add(Chunk.NEWLINE);
	}

	private void addSelectedIssuesReport(Document document) throws DocumentException,
			BadElementException {
		Paragraph selected_title = new Paragraph("Selected issues", fontHeading1);
		selected_title.setSpacingAfter(SPACING_AFTER_HEADER_TEXT);
		document.add(selected_title);
		
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				table = getTableForTheSelectedIssues(all_tree_items);
			}
		});
		document.add(table);

		document.add(Chunk.NEWLINE);
		

		Paragraph graph_title = new Paragraph("Graph for the selected issues", fontHeading1);
		//Using chapter, so title stays together with image 
		Chapter chapter = new Chapter(graph_title,0);
		//Chapter with out number, when depth is 0
		chapter.setNumberDepth(0);

		
		com.lowagie.text.Image img = null;
		try {
			img = com.lowagie.text.Image.getInstance(SwmtAnalyserPlugin.getPluginInstallPath()+"\\swmt_graph.bmp");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		img.scalePercent(50f);
		img.setBorder(Rectangle.BOX);
		img.setBorderWidth(1f);
		img.setBorderColor(new GrayColor(0.5f));
		//Adding image to chapter
		chapter.add(img);
		//Adding chapter to document
		document.add(chapter);
		
	}

	private void addOverviewReport(Document document) throws DocumentException {
		Paragraph res_title = new Paragraph("Overview Of Analysis Results", fontHeading1);
		res_title.setSpacingAfter(SPACING_AFTER_HEADER_TEXT);
		document.add(res_title);
		
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				table = getTableForOverallIssues(all_tree_items);
			}
		});
		document.add(table);
		document.add(Chunk.NEWLINE);
		
		res_title = new Paragraph("Details", fontHeading1);
		document.add(res_title);
		
		res_title = new Paragraph("Critical Severity Issues", fontHeading2);
		res_title.setSpacingAfter(SPACING_AFTER_HEADER_TEXT);
		
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				table = getTableForIssues(all_tree_items, AnalyserConstants.Priority.CRITICAL);;
			}
		});
		if(table!=null)
		{
			document.add(res_title);			
			document.add(table);
			document.add(Chunk.NEWLINE);
		}
		
		res_title = new Paragraph("High Severity Issues", fontHeading2);
		res_title.setSpacingAfter(SPACING_AFTER_HEADER_TEXT);
		
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				table = getTableForIssues(all_tree_items, AnalyserConstants.Priority.HIGH);
			}
		});
		if(table!=null)
		{
			document.add(res_title);
			document.add(table);
			document.add(Chunk.NEWLINE);
		}
		
		res_title = new Paragraph("Normal Severity Issues", fontHeading2);
		res_title.setSpacingAfter(SPACING_AFTER_HEADER_TEXT);
		
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				table = getTableForIssues(all_tree_items, AnalyserConstants.Priority.NORMAL);
			}
		});
		if(table!=null)
		{
			document.add(res_title);
			document.add(table);
		}
	}
	
	/**
	 * Returns a table for issues of given priority type from the selected issues.
	 * @param allTreeItems tree
	 * @param p priority
	 * @return
	 */
	private PdfPTable getTableForIssues(Tree allTreeItems, AnalyserConstants.Priority p) {
		
		float[] relativeWidth = {60, 25, 15};//100% total
		PdfPTable table = new PdfPTable(relativeWidth);
		table.setWidthPercentage(100);
		
		PdfPCell cell = new PdfPCell (new Paragraph ("Item name"));
		cell.setHorizontalAlignment (Element.ALIGN_CENTER);
		cell.setVerticalAlignment (Element.ALIGN_MIDDLE);
		cell.setBackgroundColor (colorTableHeaderBackGrd);
		cell.setPadding (cellPaddingTableHeader);		
		table.addCell (cell);

		cell = new PdfPCell (new Paragraph ("Event"));
		cell.setHorizontalAlignment (Element.ALIGN_CENTER);
		cell.setVerticalAlignment (Element.ALIGN_MIDDLE);
		cell.setBackgroundColor (colorTableHeaderBackGrd);
		cell.setPadding (cellPaddingTableHeader);
		table.addCell (cell);
		
		cell = new PdfPCell (new Paragraph ("Delta"));
		cell.setHorizontalAlignment (Element.ALIGN_CENTER);
		cell.setVerticalAlignment (Element.ALIGN_MIDDLE);
		cell.setBackgroundColor (colorTableHeaderBackGrd);
		cell.setPadding (cellPaddingTableHeader);
		table.addCell (cell);
		
		
		for(TreeItem parent : allTreeItems.getItems())
		{
			for(TreeItem child : parent.getItems())
			{
				if(child.getText(4).toLowerCase().equals(p.toString().toLowerCase()))
				{
					
					cell = new PdfPCell (new Paragraph (child.getText(1),fontNormalSmallTables));
					cell.setHorizontalAlignment (Element.ALIGN_LEFT);
					cell.setVerticalAlignment (Element.ALIGN_MIDDLE);
					cell.setPadding (cellPaddingSmall);
					table.addCell (cell);
					
					cell = new PdfPCell (new Paragraph (child.getText(2),fontNormalSmallTables));
					cell.setHorizontalAlignment (Element.ALIGN_LEFT);
					cell.setVerticalAlignment (Element.ALIGN_MIDDLE);
					cell.setPadding (cellPaddingSmall);
					table.addCell (cell);
					
					cell = new PdfPCell (new Paragraph (child.getText(3),fontNormalSmallTables));
					cell.setHorizontalAlignment (Element.ALIGN_LEFT);
					cell.setVerticalAlignment (Element.ALIGN_MIDDLE);
					cell.setPadding (cellPaddingSmall);
					table.addCell (cell);
				}
			}
		}
		if(table.getRows().size() > 1)
			return table;
		
		return table;
	}

	/**
	 * Returns a table for the overview issues of all types.
	 * @param all
	 * @return
	 */
	private PdfPTable getTableForOverallIssues(Tree all) {
		PdfPTable table = new PdfPTable(2);
		table.setWidthPercentage(100);
		
		PdfPCell cell = new PdfPCell (new Paragraph ("Severity"));
		cell.setHorizontalAlignment (Element.ALIGN_CENTER);
		cell.setVerticalAlignment (Element.ALIGN_MIDDLE);
		cell.setBackgroundColor (colorTableHeaderBackGrd);
		cell.setPadding (cellPaddingTableHeader);
		table.addCell (cell);

		cell = new PdfPCell (new Paragraph ("Count"));
		cell.setHorizontalAlignment (Element.ALIGN_CENTER);
		cell.setVerticalAlignment (Element.ALIGN_MIDDLE);
		cell.setBackgroundColor (colorTableHeaderBackGrd);
		cell.setPadding (cellPaddingTableHeader);
		table.addCell (cell);
		
		int critical = 0;
		int high = 0;
		int normal = 0;
		
		for(TreeItem parent : all.getItems())
		{
			for(TreeItem child : parent.getItems())
			{
				if(child.getText(4).toLowerCase().equals(AnalyserConstants.Priority.CRITICAL.toString().toLowerCase()))
					critical++;
				else if(child.getText(4).toLowerCase().equals(AnalyserConstants.Priority.HIGH.toString().toLowerCase()))
					high++;
				else if(child.getText(4).toLowerCase().equals(AnalyserConstants.Priority.NORMAL.toString().toLowerCase()))
					normal++;
			}
		}
		
		table.addCell(new PdfPCell (new Paragraph ("CRITICAL",fontNormalSmallTables)));
		table.addCell(new PdfPCell (new Paragraph (critical+"",fontNormalSmallTables)));
		table.addCell(new PdfPCell (new Paragraph ("HIGH",fontNormalSmallTables)));
		table.addCell(new PdfPCell (new Paragraph (high+"",fontNormalSmallTables)));
		table.addCell(new PdfPCell (new Paragraph ("NORMAL",fontNormalSmallTables)));
		table.addCell(new PdfPCell (new Paragraph (normal+"",fontNormalSmallTables)));
		
		return table;
	}

	/**
	 * Returns a table for the selected issues.
	 * @param issuesTree
	 * @return
	 */
	private PdfPTable getTableForTheSelectedIssues(Tree issuesTree) {

		float[] relativeWidth = {50, 22, 14, 14};//100% total
		PdfPTable table = new PdfPTable(relativeWidth);
		table.setWidthPercentage(100);
		
		PdfPCell cell = new PdfPCell (new Paragraph ("Item name"));
		cell.setHorizontalAlignment (Element.ALIGN_CENTER);
		cell.setVerticalAlignment (Element.ALIGN_MIDDLE);
		cell.setBackgroundColor (colorTableHeaderBackGrd);
		cell.setPadding (cellPaddingTableHeader);
		table.addCell (cell);

		cell = new PdfPCell (new Paragraph ("Event"));
		cell.setHorizontalAlignment (Element.ALIGN_CENTER);
		cell.setVerticalAlignment (Element.ALIGN_MIDDLE);
		cell.setBackgroundColor (colorTableHeaderBackGrd);
		cell.setPadding (cellPaddingTableHeader);
		table.addCell (cell);
		
		cell = new PdfPCell (new Paragraph ("Delta"));
		cell.setHorizontalAlignment (Element.ALIGN_CENTER);
		cell.setVerticalAlignment (Element.ALIGN_MIDDLE);
		cell.setBackgroundColor (colorTableHeaderBackGrd);
		cell.setPadding (cellPaddingTableHeader);
		table.addCell (cell);
		
		cell = new PdfPCell (new Paragraph ("Severity"));
		cell.setHorizontalAlignment (Element.ALIGN_CENTER);
		cell.setVerticalAlignment (Element.ALIGN_MIDDLE);
		cell.setBackgroundColor (colorTableHeaderBackGrd);
		cell.setPadding (cellPaddingTableHeader);
		table.addCell (cell);
		
		for(TreeItem item : issuesTree.getItems())
		{
			ArrayList<TreeItem> selected = new ArrayList<TreeItem>();
			for(TreeItem child: item.getItems())
				if(child.getChecked())
					selected.add(child);
			
			if(selected.size() > 0)
			{
				cell = new PdfPCell (new Paragraph (item.getText(1),fontTable2ndHeaderText));
				cell.setHorizontalAlignment (Element.ALIGN_LEFT);
				cell.setBackgroundColor (colorTable2ndHeaderBackGrd);
				cell.setPadding (cellPaddingTableHeader);
				cell.setColspan(4);
				table.addCell (cell);
				
				for(TreeItem child: selected)
				{
					cell = new PdfPCell (new Paragraph (child.getText(1),fontNormalSmallTables));
					cell.setHorizontalAlignment (Element.ALIGN_LEFT);
					cell.setPadding (cellPaddingSmall);
					table.addCell (cell);
					
					cell = new PdfPCell (new Paragraph (child.getText(2),fontNormalSmallTables));
					cell.setHorizontalAlignment (Element.ALIGN_LEFT);
					cell.setPadding (cellPaddingSmall);
					table.addCell (cell);
					
					cell = new PdfPCell (new Paragraph (child.getText(3),fontNormalSmallTables));
					cell.setHorizontalAlignment (Element.ALIGN_LEFT);
					cell.setPadding (cellPaddingSmall);
					table.addCell (cell);

					cell = new PdfPCell (new Paragraph (child.getText(4),fontNormalSmallTables));
					cell.setHorizontalAlignment (Element.ALIGN_CENTER);
					
					if(child.getText(4).toLowerCase().equals(AnalyserConstants.Priority.CRITICAL.toString().toLowerCase())){
						cell.setBackgroundColor (colorSeverityCritical);
					}
					else if(child.getText(4).toLowerCase().equals(AnalyserConstants.Priority.HIGH.toString().toLowerCase())){
						cell.setBackgroundColor (colorSeverityHigh);
					}
					else if(child.getText(4).toLowerCase().equals(AnalyserConstants.Priority.NORMAL.toString().toLowerCase())){
						cell.setBackgroundColor (colorSeverityNormal);
					}
					cell.setPadding(cellPaddingSmall);
					table.addCell (cell);
				}
			}
		}		
		return table;
	}


}
