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

package com.nokia.carbide.cpp.pi.function;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.eclipse.swt.graphics.RGB;

import com.nokia.carbide.cpp.internal.pi.model.GUITooltips;
import com.nokia.carbide.cpp.pi.call.GfcFunctionItem;
import com.nokia.carbide.cpp.pi.call.GfcTrace;
import com.nokia.carbide.cpp.pi.util.AWTColorPalette;


public class GfcTraceVisualiser extends JPanel
{
  private static final long serialVersionUID = 1L;
  private GfcTrace myTrace;
  private GfcFunctionItem[] sortedData;
  private JList leftList;
  private JList rightList;
  private JList centerList;
  private JPanel selectionPanel;
  private JButton sortByTotalButton;
  private JButton sortByCallerButton;
  private JButton sortByRecursiveButton;
  private JButton goBackButton;
  private int lastIndex = -1;
  private JPanel callerPanel;
  private JPanel calleePanel;
  private JPanel centerPanel;

  public GfcTraceVisualiser(GfcTrace trace)
  {
    this.myTrace = trace;

    this.selectionPanel = new JPanel(new GridLayout(6,1));
    sortByTotalButton = new JButton(Messages.getString("GfcTraceVisualiser.totalLoad")); //$NON-NLS-1$
    sortByCallerButton = new JButton(Messages.getString("GfcTraceVisualiser.callerLoad")); //$NON-NLS-1$
    sortByRecursiveButton = new JButton(Messages.getString("GfcTraceVisualiser.recursiveLoad")); //$NON-NLS-1$
    goBackButton = new JButton(Messages.getString("GfcTraceVisualiser.previousSelection")); //$NON-NLS-1$
    
    sortByTotalButton.setToolTipText(GUITooltips.getTotalLoadButton());
    sortByCallerButton.setToolTipText(GUITooltips.getCallerLoadButton());
    sortByRecursiveButton.setToolTipText(GUITooltips.getRecursiveLoadButton());
    goBackButton.setToolTipText(GUITooltips.getPreviousSelectionButton());

    ActionListener al = new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        if (ae.getSource() == sortByCallerButton)
        {
          GfcFunctionItem[] sortedData = myTrace.getEntriesSorted(GfcTrace.SORT_BY_CALLED_LOAD);
          centerList.setListData(sortedData);
        }
        else if (ae.getSource() == sortByTotalButton)
        {
          GfcFunctionItem[] sortedData = myTrace.getEntriesSorted(GfcTrace.SORT_BY_TOTAL_LOAD);
          centerList.setListData(sortedData);
        }
        else if (ae.getSource() == sortByRecursiveButton)
        {
          GfcFunctionItem[] sortedData = myTrace.getEntriesSorted(GfcTrace.SORT_BY_RECURSIVE_LOAD);
          centerList.setListData(sortedData);
        }
        else if (ae.getSource() == goBackButton)
        {
          if (lastIndex != -1)
          {
            int temp = centerList.getSelectedIndex();
            centerList.setSelectedIndex(lastIndex);

            if (lastIndex>5 && lastIndex < centerList.getModel().getSize()-5)
            {
              centerList.ensureIndexIsVisible(lastIndex-4);
              centerList.ensureIndexIsVisible(lastIndex+4);
              centerList.ensureIndexIsVisible(lastIndex);
            }
            else
            {
              centerList.ensureIndexIsVisible(lastIndex);
            }

            lastIndex = temp;
          }
        }
      }
    };

    sortByTotalButton.addActionListener(al);
    sortByCallerButton.addActionListener(al);
    sortByRecursiveButton.addActionListener(al);
    goBackButton.addActionListener(al);

    this.selectionPanel.add(new JLabel(Messages.getString("GfcTraceVisualiser.sortBy"))); //$NON-NLS-1$
    this.selectionPanel.add(sortByTotalButton);
    this.selectionPanel.add(sortByCallerButton);
    this.selectionPanel.add(sortByRecursiveButton);
    this.selectionPanel.add(new JLabel(Messages.getString("GfcTraceVisualiser.goBackTo"))); //$NON-NLS-1$
    this.selectionPanel.add(goBackButton);

    this.leftList = new JList();
    this.leftList.setCellRenderer(new GfcListCellRenderer(myTrace));
    this.leftList.addListSelectionListener(new ListSelectionListener()
    {
      public void valueChanged(ListSelectionEvent lse)
      {
        Object[] element = (Object[]) leftList.getSelectedValue();
        if (element != null)
        {
          setLeftListDataToFunctionsThisFunctionCalls((GfcFunctionItem)element[0]);
          setRightListDataToFunctionsThisFunctionCalls((GfcFunctionItem)element[0]);

          lastIndex = centerList.getSelectedIndex();
          for (int i=0;i<centerList.getModel().getSize();i++)
          {
            if (centerList.getModel().getElementAt(i).equals(element[0]))
            {
              centerList.setSelectedIndex(i);
              if (i>5 && i < centerList.getModel().getSize()-5)
              {
                centerList.ensureIndexIsVisible(i-4);
                centerList.ensureIndexIsVisible(i+4);
                centerList.ensureIndexIsVisible(i);
              }
              else
              {
                centerList.ensureIndexIsVisible(i);
              }
              break;
            }
          }
        }
      }
    });

    this.centerList = new JList();
    this.centerList.setCellRenderer(new GfcListCellRenderer(myTrace));
    this.centerList.addListSelectionListener(new ListSelectionListener()
    {
      public void valueChanged(ListSelectionEvent lse)
      {
        GfcFunctionItem element = (GfcFunctionItem)centerList.getSelectedValue();
        if (element != null)
        {
          setLeftListDataToFunctionsThisFunctionCalls((GfcFunctionItem)element);
          setRightListDataToFunctionsThisFunctionCalls((GfcFunctionItem)element);
        }
        else
        {
          leftList.setListData(new Object[]{});
          rightList.setListData(new Object[]{});
        }
      }
    });

    this.rightList = new JList();
    this.rightList.setCellRenderer(new GfcListCellRenderer(myTrace));
    this.rightList.addListSelectionListener(new ListSelectionListener()
    {
      public void valueChanged(ListSelectionEvent lse)
      {
        Object[] element = (Object[]) rightList.getSelectedValue();
        if (element != null)
        {
          setLeftListDataToFunctionsThisFunctionCalls((GfcFunctionItem)element[0]);
          setRightListDataToFunctionsThisFunctionCalls((GfcFunctionItem)element[0]);

          lastIndex = centerList.getSelectedIndex();
          for (int i=0;i<centerList.getModel().getSize();i++)
          {
            if (centerList.getModel().getElementAt(i).equals(element[0]))
            {
              centerList.setSelectedIndex(i);
              if (i>5 && i < centerList.getModel().getSize()-5)
              {
                centerList.ensureIndexIsVisible(i-4);
                centerList.ensureIndexIsVisible(i+4);
                centerList.ensureIndexIsVisible(i);
              }
              else
              {
                centerList.ensureIndexIsVisible(i);
              }

              break;
            }
          }

        }

      }
    });

    this.setLayout(new GridLayout(3,1));

    // create and add the top panel
    callerPanel = new JPanel();
    JPanel labeledPanel = new JPanel();
    labeledPanel.setLayout(new BorderLayout());
    callerPanel.setLayout(new BorderLayout());
    //Component c = new BasicArrowButton(BasicArrowButton.NORTH);
    //c.set

    labeledPanel.add(new JLabel(Messages.getString("GfcTraceVisualiser.calledSelectedFunction")),BorderLayout.CENTER); //$NON-NLS-1$
    labeledPanel.setBackground(
    	AWTColorPalette.getColor(new RGB(220, 220, 220)));
    //labeledPanel.add(c,BorderLayout.EAST);
    //c = new BasicArrowButton(BasicArrowButton.NORTH);
    //labeledPanel.add(c,BorderLayout.WEST);
    //labeledPanel.setBorder(BorderFactory.createEtchedBorder());
    //callerPanel.add(labeledPanel,BorderLayout.SOUTH);
    callerPanel.add(labeledPanel,BorderLayout.NORTH);
    callerPanel.add(new JScrollPane(this.leftList));
    this.add(callerPanel);

    // create and add the center panel
    centerPanel = new JPanel();
    centerPanel.setLayout(new BorderLayout());
    centerPanel.add(new JScrollPane(this.centerList),BorderLayout.CENTER);
    centerPanel.add(this.selectionPanel,BorderLayout.WEST);

    this.add(centerPanel);

    // create and add the lower panel
    calleePanel = new JPanel();
    
    
    labeledPanel = new JPanel();
    labeledPanel.setLayout(new BorderLayout());
    calleePanel.setLayout(new BorderLayout());
    //c = new BasicArrowButton(BasicArrowButton.SOUTH);

    labeledPanel.add(new JLabel(Messages.getString("GfcTraceVisualiser.calledBySelectedFunction")),BorderLayout.CENTER); //$NON-NLS-1$
    labeledPanel.setBackground(
  			AWTColorPalette.getColor(new RGB(220, 220, 220)));
    //labeledPanel.add(c,BorderLayout.EAST);
    //c = new BasicArrowButton(BasicArrowButton.SOUTH);
    //labeledPanel.add(c,BorderLayout.WEST);
    //labeledPanel.setBorder(BorderFactory.createEtchedBorder());
    calleePanel.add(labeledPanel,BorderLayout.NORTH);
    calleePanel.add(new JScrollPane(this.rightList));
    this.add(calleePanel);
  }
  
  public JPanel getCalleeList()
  {
  	return this.calleePanel;
  }
  
  public JPanel getCallerList()
  {
  	return this.callerPanel;
  }
  
  public JPanel getCenterPanel()
  {
  	return this.centerPanel;
  }
  
  public void selectFunction(String functionName)
  {
  	if (this.sortedData == null) return;
  	
  	for (int i = 0; i < this.sortedData.length; i++)
  	{
  		GfcFunctionItem gfi = this.sortedData[i];
  		if (gfi.name.equals(functionName))
  		{
  			this.centerList.setSelectedValue(gfi,true);
  			return;
  		}  		 		
  	}
  }
  
  public JPanel getSelectionPanel()
  {
  	return this.selectionPanel;
  }

  public void setStartAndEnd(int start,int end,boolean symPrimary)
  {
    myTrace.parseEntries(start,end);
    sortedData = myTrace.getEntriesSorted(GfcTrace.SORT_BY_TOTAL_LOAD);
    
    this.centerList.setListData(sortedData);
  }


  public void setLeftListDataToFunctionsThisFunctionCalls(GfcFunctionItem function)
  {
    GfcFunctionItem[] list = function.getCallerList();
    Double[] perc = function.getCallerPercentages();

    Object[] listData = new Object[list.length];

    for (int i = 0;i<list.length;i++)
    {
      Object[] element = new Object[2];
      element[0] = list[i];
      element[1] = perc[i];
      listData[i] = element;
    }

    this.leftList.setListData(listData);
  }

  public void setRightListDataToFunctionsThisFunctionCalls(GfcFunctionItem function)
  {
    GfcFunctionItem[] list = function.getCalleeList();
    Double[] perc = function.getCalleePercentages();

    Object[] listData = new Object[list.length];

    for (int i = 0;i<list.length;i++)
    {
      Object[] element = new Object[2];
      element[0] = list[i];
      element[1] = perc[i];
      listData[i] = element;
    }
    this.rightList.setListData(listData);
  }

  private static class GfcListCellRenderer extends JLabel implements ListCellRenderer
  {
    private GfcTrace myTrace;
	public Color ittColor = Color.ORANGE.darker();
	public Color gppColor = Color.BLUE.darker();

    public GfcListCellRenderer(GfcTrace trace)
    {
      this.setOpaque(true);
      myTrace = trace;
    }

    public Component getListCellRendererComponent(
            JList list,
            Object value,            // value to display
            int index,               // cell index
            boolean isSelected,      // is the cell selected
            boolean cellHasFocus)    // the list and the cell have the focus
    {

      if (value instanceof GfcFunctionItem)
      {
        GfcFunctionItem item = (GfcFunctionItem)value;

        double percent = myTrace.getAbsoluteCallerPercentageFor(item);
        String perc2 = (((double)Math.round(100*percent)) / 100)+Messages.getString("GfcTraceVisualiser.percent"); //$NON-NLS-1$

        percent = myTrace.getAbsoluteTraditionalPercentageFor(item);
        String perc3 = (((double)Math.round(100*percent)) / 100)+Messages.getString("GfcTraceVisualiser.percent"); //$NON-NLS-1$

        percent = myTrace.getRecursiveCallerPrecentageFor(item);
        String perc4 = (((double)Math.round(100*percent)) / 100)+Messages.getString("GfcTraceVisualiser.percent"); //$NON-NLS-1$

        if (isSelected)
        {
          String s = Messages.getString("GfcTraceVisualiser.functionString1")+perc3+Messages.getString("GfcTraceVisualiser.functionString2")+perc2+Messages.getString("GfcTraceVisualiser.functionString3")+perc4+Messages.getString("GfcTraceVisualiser.functionString4")+ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                     Messages.getString("GfcTraceVisualiser.functionString5")+Integer.toHexString((int)item.address)+Messages.getString("GfcTraceVisualiser.functionString6")+item.name; //$NON-NLS-1$ //$NON-NLS-2$
          //s = item.getAccumulatedLoad() + s;

          setText(s);
        }
        else
        {
          String s = Messages.getString("GfcTraceVisualiser.functionString1")+perc3+Messages.getString("GfcTraceVisualiser.functionString2")+perc2+Messages.getString("GfcTraceVisualiser.functionString3")+perc4+Messages.getString("GfcTraceVisualiser.functionString4")+ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                     Messages.getString("GfcTraceVisualiser.functionString5")+Integer.toHexString((int)item.address)+Messages.getString("GfcTraceVisualiser.functionString6")+item.name; //$NON-NLS-1$ //$NON-NLS-2$
          //s = item.getAccumulatedLoad() + s;

          setText(s);
        }

        if (isSelected)
        {
          setBackground(list.getSelectionBackground());
        }
        else
        {
          setBackground(list.getBackground());
        }
        
        if (item.isSymbolParsed())
        	setForeground(this.gppColor);
        else
        	setForeground(this.ittColor);	
      }

      else if (value instanceof Object[])
      {

        Object[] element = (Object[])value;
        GfcFunctionItem item = (GfcFunctionItem)element[0];

        double percent = ((Double)element[1]).doubleValue();
        String perc1 = (((double)Math.round(100*percent)) / 100)+Messages.getString("GfcTraceVisualiser.percent"); //$NON-NLS-1$

        // set color according to the relative load
        setBackground(new Color((int)(155+(percent)),155,(int)(255-(percent))));
        
        if (item.isSymbolParsed())
        	setForeground(this.gppColor);
        else
        	setForeground(this.ittColor);

        percent = myTrace.getAbsoluteCallerPercentageFor(item);
        String perc2 = (((double)Math.round(100*percent)) / 100)+Messages.getString("GfcTraceVisualiser.percent"); //$NON-NLS-1$

        percent = myTrace.getAbsoluteTraditionalPercentageFor(item);
        String perc3 = (((double)Math.round(100*percent)) / 100)+Messages.getString("GfcTraceVisualiser.percent"); //$NON-NLS-1$

        if (isSelected)
        {
          String s = Messages.getString("GfcTraceVisualiser.addressString1")+Integer.toHexString((int)item.address)+ //$NON-NLS-1$
                     Messages.getString("GfcTraceVisualiser.addressString2")+perc3+Messages.getString("GfcTraceVisualiser.addressString3")+perc2+Messages.getString("GfcTraceVisualiser.addressString4")+perc1+Messages.getString("GfcTraceVisualiser.addressString5")+item.name; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
          setText(s);
        }
        else
        {
          String s = Messages.getString("GfcTraceVisualiser.addressString2a")+perc3+Messages.getString("GfcTraceVisualiser.addressString3")+perc2+Messages.getString("GfcTraceVisualiser.addressString4")+perc1+Messages.getString("GfcTraceVisualiser.addressString5")+item.name; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
          setText(s);

        }
      }
      else
      {
        setText(Messages.getString("GfcTraceVisualiser.listIsNotWorking")+value.getClass()); //$NON-NLS-1$
      }

      setEnabled(list.isEnabled());
      setFont(list.getFont());

      return this;
    }
  }
}
