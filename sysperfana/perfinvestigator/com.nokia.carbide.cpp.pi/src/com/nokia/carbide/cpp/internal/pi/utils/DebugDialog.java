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
 * DebugDialog.java
 */
package com.nokia.carbide.cpp.internal.pi.utils;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DebugDialog extends JDialog 
{
	private static final long serialVersionUID = -7397509550206778612L;
	private JPanel panel;
	private JLabel freeMemory;
	private JLabel totalMemory;
	private JLabel usedMemory;
	private static Runtime runtime;
	private static long time4;
	
	private static void setTime4(long newTime) {
		time4 = newTime;
	}
	
	private static void setRuntime(Runtime newRuntime)
	{
		runtime = newRuntime;
	}
	
	public DebugDialog()
	{
		setTime4(0);
    	setRuntime(Runtime.getRuntime());
		
		freeMemory  = new JLabel(Messages.getString("DebugDialog.notInitialised")); //$NON-NLS-1$
		totalMemory = new JLabel(Messages.getString("DebugDialog.notInitialised")); //$NON-NLS-1$
		usedMemory  = new JLabel(Messages.getString("DebugDialog.notInitialised")); //$NON-NLS-1$
    	
		this.setTitle(Messages.getString("DebugDialog.debugDialog")); //$NON-NLS-1$
		panel = (JPanel) this.getContentPane();
		panel.setLayout(new BorderLayout());
		panel.add(getMemoryPanel(), BorderLayout.CENTER);
		panel.add(getButtonPanel(), BorderLayout.SOUTH);
		this.pack();
		panel.updateUI();
	}
	
	private JPanel getButtonPanel() 
	{
		JPanel mp = new JPanel();
		mp.add(getRefreshButton());
		mp.add(getGarbageCollectionButton());
		mp.add(getTestButton());
		return mp;
	}
	
	private JPanel getMemoryPanel() 
	{
		JPanel mp = new JPanel();
		mp.setLayout(new GridLayout(3, 2));

		mp.add(new JLabel(Messages.getString("DebugDialog.freeMemory"))); //$NON-NLS-1$
		mp.add(freeMemory);
		mp.add(new JLabel(Messages.getString("DebugDialog.totalMemory"))); //$NON-NLS-1$
		mp.add(totalMemory);
		mp.add(new JLabel(Messages.getString("DebugDialog.usedMemory"))); //$NON-NLS-1$
		mp.add(usedMemory);
//		refreshMemoryStatus();
		return mp;
	}
	
	public void refreshMemoryStatus()
	{
		long free = runtime.freeMemory() / 1000;
		long total = runtime.totalMemory() / 1000;
		long used = total - free;
		
		freeMemory.setText("" + free); //$NON-NLS-1$
		totalMemory.setText("" + total); //$NON-NLS-1$
		usedMemory.setText("" + used); //$NON-NLS-1$
	}
	
	private JButton getRefreshButton() 
	{
		JButton refresh = new JButton(Messages.getString("DebugDialog.refresh")); //$NON-NLS-1$
		refresh.addActionListener(new java.awt.event.ActionListener() 
		{ 
			public void actionPerformed(java.awt.event.ActionEvent e) 
			{    
				refreshMemoryStatus();
			}
		});
		return refresh;
	}

	private JButton getGarbageCollectionButton() 
	{
		JButton garbage = new JButton(Messages.getString("DebugDialog.garbage")); //$NON-NLS-1$
		garbage.addActionListener(new java.awt.event.ActionListener() 
		{ 
			public void actionPerformed(java.awt.event.ActionEvent e) 
			{  
				//runs garbage collection
			    garbageCollection();
			}
		});
		return garbage;
	}
	private JButton getTestButton() 
	{
		JButton refresh = new JButton(Messages.getString("DebugDialog.test")); //$NON-NLS-1$
		refresh.addActionListener(new java.awt.event.ActionListener() 
		{ 
			public void actionPerformed(java.awt.event.ActionEvent e) 
			{    
				refreshMemoryStatus();
			}
		});
		return refresh;
	}
	
	public static void garbageCollection()
	{
	    long time1 = System.currentTimeMillis();
	    //Time time = new Time(System.currentTimeMillis());
	    runtime.runFinalization();
	    runtime.gc();
	    long time2 = System.currentTimeMillis();
	    long time3 = time2 - time1;
	    time4 = time4 + time3;
	    System.out.println(Messages.getString("DebugDialog.garbageCollected1") + time3 + Messages.getString("DebugDialog.garbageCollected2") + time4 + Messages.getString("DebugDialog.garbageCollected3")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    //Time time2 = new Time(System.currentTimeMillis());
	}
	
	public static void printMemoryUsage()
	{
		//Runtime runtime = Runtime.getRuntime();
	    
		long free = runtime.freeMemory() / 1000;
		long total = runtime.totalMemory() / 1000;
		long used = total - free;
		System.out.println(Messages.getString("DebugDialog.currentMemory") + used); //$NON-NLS-1$
	}
	
	public static String getCurrentMemoryUsage()
	{
		long free = runtime.freeMemory() / 1000000;
		long total = runtime.totalMemory() / 1000000;
		long used = total - free;
		return "" + used + "/" + total; //$NON-NLS-1$ //$NON-NLS-2$ 
	}

}
