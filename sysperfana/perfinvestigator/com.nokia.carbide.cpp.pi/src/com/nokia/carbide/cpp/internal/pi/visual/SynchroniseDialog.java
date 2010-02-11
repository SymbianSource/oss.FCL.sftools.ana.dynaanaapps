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

package com.nokia.carbide.cpp.internal.pi.visual;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SynchroniseDialog extends JPanel
{
	private static final long serialVersionUID = -7841684215277143128L;
	private JPanel buttonPanel = null;
    private JPanel gppPanel = null;
    private JLabel gppLabel = null;
    private JTextField gppField = null;
    private JButton okButton = null;
    private JButton cancelButton = null;
    private PICompositePanel composite = null;

    /**
     * This is the default constructor
     */
    public SynchroniseDialog(PICompositePanel aComposite)
    {
        super();
        try {
        	composite = aComposite;
	        ButtonListener cbl = new ButtonListener();
	        initialize();

	        getOkButton().addActionListener(cbl);
	        getCancelButton().addActionListener(cbl);
        }
        catch(Exception ex) {
          ex.printStackTrace();
        }
    }
    
    public String getGppFileName()
    {
    	if (gppField.isEnabled())
    		return gppField.getText();
    	else
    		return ""; //$NON-NLS-1$
    }
    
    /**
     * This method initializes jButton2
     * 
     * @return javax.swing.JButton
     */
    private JButton getOkButton()
    {
        if (okButton == null)
        {
        	okButton = new JButton();
        	okButton.setText(Messages.getString("SynchroniseDialog.ok")); //$NON-NLS-1$
        	okButton.setActionCommand("ok"); //$NON-NLS-1$
        }
        return okButton;
    }
    
    /**
     * This method initializes jButton2
     * 
     * @return javax.swing.JButton
     */
    private JButton getCancelButton()
    {
        if (cancelButton == null)
        {
        	cancelButton = new JButton();
        	cancelButton.setText(Messages.getString("SynchroniseDialog.cancel")); //$NON-NLS-1$
        	cancelButton.setActionCommand("cancel"); //$NON-NLS-1$
        }
        return cancelButton;
    }
    
    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize()
    {
        java.awt.GridLayout gridLayout = new GridLayout(2, 1);
        this.setLayout(gridLayout);
        this.add(getGppPanel(), null);
        this.add(getButtonPanel(), null);
    }
    
    private JPanel getGppPanel()
    {
        if (gppPanel == null)
        {
        	gppPanel = new JPanel();
        	gppLabel = new JLabel();
        	gppField = new JTextField();
        	
        	gppPanel.setLayout(new BorderLayout());
            gppLabel.setText(Messages.getString("SynchroniseDialog.value")); //$NON-NLS-1$
            gppPanel.setBorder(javax.swing.BorderFactory
                            .createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
            gppPanel.add(gppLabel, java.awt.BorderLayout.NORTH);
            gppPanel.add(gppField, java.awt.BorderLayout.CENTER);
        }
        return gppPanel;
    }
    
    
    /**
     * This method initializes jPanel2
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getButtonPanel()
    {
        if (buttonPanel == null)
        {
            buttonPanel = new JPanel();
            buttonPanel.setBorder(javax.swing.BorderFactory
                            .createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
            buttonPanel.add(getOkButton(), java.awt.BorderLayout.CENTER);
            buttonPanel.add(getCancelButton(), java.awt.BorderLayout.EAST);
        }
        return buttonPanel;
    }

    class ButtonListener implements ActionListener
    {
        public void actionPerformed(ActionEvent ae)
        {
            if (ae.getActionCommand().equals("ok")) //$NON-NLS-1$
            {
            	composite.synchroniseOk();
            }
            else if (ae.getActionCommand().equals("cancel")) //$NON-NLS-1$
            {
            	composite.synchroniseCancel();
            }
        }
    }
}
