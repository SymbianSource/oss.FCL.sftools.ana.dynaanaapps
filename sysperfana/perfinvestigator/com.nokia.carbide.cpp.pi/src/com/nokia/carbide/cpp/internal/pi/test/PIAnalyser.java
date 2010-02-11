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

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import com.nokia.carbide.cpp.internal.pi.manager.PluginRegisterer;
import com.nokia.carbide.cpp.pi.core.SessionPreferences;


public class PIAnalyser extends JFrame 
{
	private static final long serialVersionUID = 1L;

	public static final String version = "1.1";  //$NON-NLS-1$	// version number is part of program logic, do not externalize
	public static final String releaseDate = Messages.getString("PIAnalyser.date"); //$NON-NLS-1$
	public static final String releaseYear = Messages.getString("PIAnalyser.year"); //$NON-NLS-1$
	
//	private static boolean disableAllPrintsInGUIRun = true;
	
	//this is used to identify changes in NPI file format
	//update this when new traces are added or old ones modified
//    public static final String NPIFileFormat = "1.1"; //Performance Investigator 1.0 framework  //$NON-NLS-1$ // version number is part of program logic, do not externalize
//    public static final String NPIFileFormat = "1.2"; //Performance Investigator 2.0 M1 framework  //$NON-NLS-1$ // version number is part of program logic, do not externalize
    public static final String NPIFileFormat = "1.3"; //Performance Investigator 2.0 framework  //$NON-NLS-1$ // version number is part of program logic, do not externalize
    //public static String profilerVersion = "Unknown";
	public final static boolean VIEWER_MODE = false;
//	public final static boolean INCLUDE_PLUGINS = true; //if this is set dsp, perf framework
														//and others are enabled
	
	//private TestGUI testGUI = null;
	private String analyserName;
//	private JFileChooser crashPreventingFileChooser;
	
	private static JFrame frame = null;
	/**
	 * This is the default constructor
	 */
	private static void setFrame(JFrame newFrame)
	{
		frame = newFrame;
	}

	public PIAnalyser() 
	{
        analyserName = Messages.getString("PIAnalyser.0") + PIAnalyser.version; //$NON-NLS-1$
	    
	    this.setTitle(analyserName);
	    
		initialize();
		setFrame(this);
	
		Exception ex = null;
		JFileChooser crashPreventingFileChooser;
		for (int i=0;i<10;i++)
		{
			try
			{
				crashPreventingFileChooser = new JFileChooser();
				if (crashPreventingFileChooser != null) break;
				System.out.println(Messages.getString("PIAnalyser.ok")); //$NON-NLS-1$
			}
			catch (Exception e)
			{
				ex = e;
			}
		}
		if (ex != null)
		{
			System.out.println(Messages.getString("PIAnalyser.cannotInitializeJFileChooser")); //$NON-NLS-1$
			ex.printStackTrace();
		}
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		PluginRegisterer.registerAllPlugins(); //setting up static plugin registry before generating the GUI

//		this.setContentPane(getJContentPane());
//		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//configData.loadData();

		int width = SessionPreferences.getInstance().getWindowWidth();
		int height = SessionPreferences.getInstance().getWindowHeight();

		this.addCloseListener();
		this.setSize(width, height);
		this.setVisible(true);
	}
	
	private void addCloseListener()
	{
	    this.addWindowListener(
	        new WindowAdapter()
	    {
	      public void windowClosed(WindowEvent we)
	      {
	      	System.out.println(Messages.getString("PIAnalyser.closingAnalyser")); //$NON-NLS-1$
	      	//ConfigurationPanel config = ConfigurationPanel.getInstance();
	      	//config.saveConfig();
	      	//JButton button = config.getJButton3();
	      	//button.doClick();
	      	
			//configData.loadData();
			int width = frame.getWidth();
			int height = frame.getHeight();
			SessionPreferences.getInstance().setWindowWidth(width);
			SessionPreferences.getInstance().setWindowHeight(height);
			try {
//				configData.saveData();
				throw new Exception(Messages.getString("PIAnalyser.fixException")); //$NON-NLS-1$
			} catch (Exception e) {
				e.printStackTrace();
			}
	      	
	        System.exit(0);
	      }
	      public void windowClosing(WindowEvent we)
		  {
	      	((Frame)we.getSource()).dispose(); 
	      }
	    });

	 }
	
	public static JFrame getFrame()
	{
	    return frame;
	}
	
// 	public static void main(String args[])
//	{
// 	    if (args.length > 0)
//        {
// 	        System.out.println("Commandline parameter: "+args[0]);
// 	        analyserNoGUI(args);
//        }
//        else
//        {
//    		try {
//    			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//    		} catch (Exception e) {
//    			e.printStackTrace();
//    		}
//    		
//    		// disable all debug prints in releases
//    		if (PIAnalyser.disableAllPrintsInGUIRun)
//    		{
//    			PrintStream nullStream = new PrintStream(
//    					new OutputStream()
//    					{
//    						public void write(){}
//    						public void write(int i){}
//    					});
//    			System.setOut(nullStream);
//    		}
//    		
//            new PIAnalyser();
//        }
//
//        analyserNoGUI(args);
//        
//	}
//    private static void analyserNoGUI(String args[])
//    {
//        if (args.length>2)
//        {
//            System.out.println("\n\n*********************************************");
//            System.out.println("Performing scripted analysis");
//            System.out.println("Symbol file: ("+args[0].toString()+") hmms");
//            System.out.println("Stream file: "+args[1].toString());
//            System.out.println("Output file: "+args[2].toString());
//            System.out.println("*********************************************\n\n");
//
//            PluginRegisterer.registerAllPlugins(); //setting up plugin registry
//            new AnalyseTab(args);
//        }
//        else
//        {
//            System.out.println("Guidelines: java -mx1200m -jar Analyser_v131.jar <symbol file> <stream file> <output file>");
//            System.exit(0);
//        }
//    }
    
 }
