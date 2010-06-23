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

package com.nokia.carbide.cpp.internal.pi.wizards.ui;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.CarbidePiWizardHelpIds;
import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.RofsObySymbolPair;
import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.SdkChooserBase;
import com.nokia.carbide.cpp.sdk.core.ISymbianSDK;

public class NewPIWizardPageRomSubTask extends NewPIWizardPage implements INewPIWizardSettings
{
	private boolean visableBefore = false;
	
	protected NewPIWizardPageRomSubTask() {
		super(""); //$NON-NLS-1$
		setTitle(Messages.getString("NewPIWizardPageRomSubTask.title")); //$NON-NLS-1$
	    setDescription(Messages.getString("NewPIWizardPageRomSubTask.description")); //$NON-NLS-1$
	}
	
	// use base SDK chooser object for layout
	private SdkChooserBase sdkCommon = new SdkChooserBase();
	private static final String FILTER_EXT_KEY = "extension"; //$NON-NLS-1$
	private static final String FILTER_NAME_KEY = "name"; //$NON-NLS-1$
	private static final String MY_WIGET_KEY = "myWiget"; //$NON-NLS-1$
	private static final String ROFS_ITEM_PAIR_KEY = "ROFS_ITEM_PAIR_KEY";	//$NON-NLS-1$
	private static final int TEXT_MARGIN = 2;
	private static final int ARC_WIDTH = 2;
	private static String lastGoodDir = null;
	private ModifyListener textModifyListener = new ModifyListener () {

		public void modifyText(ModifyEvent arg0) {
			Object source = arg0.getSource();
			if (source instanceof Text) {
				String pathString = ((Text)source).getText();
				java.io.File file = new java.io.File(pathString);
				if (file.exists()) {
					if (file.isFile()) {
						lastGoodDir = file.getParent();
					} else if (file.isDirectory()){
						lastGoodDir = pathString;
					} else {
						lastGoodDir = null;
					}
				}
			}
			if (source == symbolText) {
				NewPIWizardSettings.getInstance().romSymbolFile = symbolText.getText();
			}
			if (source == obyText) {
				NewPIWizardSettings.getInstance().romObyFile = obyText.getText();
			}
			validatePage();
		}
		
	};
	private SelectionListener browseSelectionListener = new SelectionListener() {

		public void widgetDefaultSelected(SelectionEvent arg0) {
		}

		public void widgetSelected(SelectionEvent arg0) {
			Button button = (Button) arg0.getSource();
			String selectedFilePath;
			FileDialog dialog = new FileDialog(getShell());
			ArrayList<String> pkgExtensions = new ArrayList<String>();
			ArrayList<String> pkgNames = new ArrayList<String>();
			pkgExtensions.add((String)button.getData(FILTER_EXT_KEY));
			pkgNames.add((String)button.getData(FILTER_NAME_KEY));
			pkgExtensions.add("*.*"); //$NON-NLS-1$"
			pkgNames.add(Messages.getString("NewPIWizardPageRomSubTask.name.filter")); //$NON-NLS-1$
			dialog.setFilterExtensions(pkgExtensions.toArray(new String[pkgExtensions.size()]));
			dialog.setFilterNames(pkgNames.toArray(new String[pkgNames.size()]));
			ISymbianSDK sdk = sdkCommon.getSelectedSdk();
			if (sdk != null) {
				String epocroot = sdk.getEPOCROOT();
				if (lastGoodDir != null) {
					// pick last known good before EPOCROOT
					dialog.setFilterPath(lastGoodDir);
				} else if (epocroot != null) {
					if (epocroot != "") { //$NON-NLS-1$
						String romDir = epocroot;
						if (romDir.endsWith(File.separator))
							romDir += "epoc32" + File.separator + "rom";	//$NON-NLS-1$" //$NON-NLS-2$"
						else
							romDir += File.separator + "epoc32" + File.separator + "rom";	//$NON-NLS-1$" //$NON-NLS-2$"
						// S60 and Techview symbol locations
						dialog.setFilterPath(romDir);
					}
				}
			}
			
			selectedFilePath = dialog.open();
			
			if (selectedFilePath != null) {
				((Text)button.getData(MY_WIGET_KEY)).setText(selectedFilePath);
			}
		}
		
	};

	//	 controls
	private Composite composite = null;
//	private Label headerLabel = null;
	private Label symbolLabel = null;
	private Composite symbolComposite = null;
	private Text symbolText = null;
	private Button symbolButton = null;
	private Label obyLabel = null;
	private Composite obyComposite = null;
	private Text obyText = null;
	private Button obyButton = null;
	private Label rofsObySymbolLabel = null;
	private Composite rofsObySymbolComposite = null;
	private Table rofsObySymbolTable = null;
	private Composite rofsObySymbolButtonComposite = null;
	private Button rofsAddButton = null;
	private Button rofsRemoveButton = null;
//	private Label detailLabel = null;
	
	public void validatePage() {
		if (symbolText.getText().length() > 1 ||
				obyText.getText().length() > 1 ||
				rofsObySymbolTable.getItemCount() > 1) {
				if (sdkCommon.getSelectedSdk() == null) {
					setErrorMessage(Messages.getString("NewPIWizardPageRomSubTask.error.1")); //$NON-NLS-1$
					setPageComplete(false);
					return;
				}
		}
		if (symbolText.getText().length() > 0 ) {
			java.io.File symFile = new java.io.File(symbolText.getText());
			if (!symFile.exists() || !symFile.isFile()) {
				setErrorMessage(Dialog.shortenText(Messages.getString("NewPIWizardPageRomSubTask.symbol.label") +  " " + symbolText.getText() + " " + Messages.getString("NewPIWizardPageRomSubTask.error.2"), symbolComposite)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				setPageComplete(false);
				return;
			}
		}
		if (obyText.getText().length() > 0 ) {
			java.io.File obyFile = new java.io.File(obyText.getText());
			if (!obyFile.exists() || !obyFile.isFile()) {
				setErrorMessage(Dialog.shortenText(Messages.getString("NewPIWizardPageRomSubTask.oby.label") +  " " + obyText.getText() + " " + Messages.getString("NewPIWizardPageRomSubTask.error.2"), obyComposite)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				setPageComplete(false);
				return;
			}
		}
		
		setErrorMessage(null);
		setPageComplete(true);
		return;
	}
	
	private void writeRofsTableToStatus() {
		NewPIWizardSettings npws = NewPIWizardSettings.getInstance();
		npws.rofsObySymbolPairList.clear();
		TableItem[] items = rofsObySymbolTable.getItems();
		for (TableItem item : items) {
			RofsObySymbolPair pair = (RofsObySymbolPair) item.getData(ROFS_ITEM_PAIR_KEY);
			if (pair.haveObyFile() || pair.haveSymbolFile()) {
				npws.rofsObySymbolPairList.add(pair);
			}
		}
	}

	public void createControl(Composite parent) {
		super.createControl(parent);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(gridLayout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
//		headerLabel = new Label(composite, SWT.NONE);
//		headerLabel.setText(Messages.getString("NewPIWizardPageRomSubTask.header.label")); //$NON-NLS-1$

		// the SDK top half of the page
		sdkCommon.layout(composite);
		sdkCommon.sdkTable.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				// write back our status
				NewPIWizardSettings.getInstance().romSdk = sdkCommon.getSelectedSdk();
				validatePage();
			}
		});

//		detailLabel = new Label(composite, SWT.NONE);
//		detailLabel.setText(Messages.getString("NewPIWizardPageRomSubTask.detail.label")); //$NON-NLS-1$
		obyLabel = new Label(composite, SWT.NONE);
		obyLabel.setText(Messages.getString("NewPIWizardPageRomSubTask.oby.label")); //$NON-NLS-1$
		createObyControl();
		symbolLabel = new Label(composite, SWT.NONE);
		symbolLabel.setText(Messages.getString("NewPIWizardPageRomSubTask.symbol.label")); //$NON-NLS-1$
		createSymbolControl();
		rofsObySymbolLabel = new Label(composite, SWT.NONE);
		rofsObySymbolLabel.setText(Messages.getString("NewPIWizardPageRomSubTask.rofs.pairs")); //$NON-NLS-1$
		createRofsObySymbolControl();
		validatePage();
		
		// top tap get focus
		sdkCommon.sdkTable.setFocus();
		
		setControl(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), CarbidePiWizardHelpIds.PI_IMPORT_WIZARD_ROM);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#performHelp()
	 */
	@Override
	public void performHelp() {
		WizardDialog wizardDialog = (WizardDialog)getContainer();	
		if(wizardDialog.buttonBar != null){	
			PlatformUI.getWorkbench().getHelpSystem().setHelp(wizardDialog.buttonBar,
					CarbidePiWizardHelpIds.PI_IMPORT_WIZARD_ROM);
		}
		
	}
	
	public void createSymbolControl() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		symbolComposite = new Composite(composite, SWT.NONE);
		symbolComposite.setLayout(gridLayout);
		symbolComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		symbolText = new Text(symbolComposite, SWT.BORDER | SWT.SINGLE);
		symbolText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		symbolText.addModifyListener(textModifyListener);
		symbolButton = new Button(symbolComposite, SWT.NONE);
		symbolButton.setText(Messages.getString("NewPIWizardPageRomSubTask.symbol.button")); //$NON-NLS-1$
		symbolButton.setData(FILTER_EXT_KEY, Messages.getString("NewPIWizardPageRomSubTask.symbol.filter")); //$NON-NLS-1$
		symbolButton.setData(FILTER_NAME_KEY, Messages.getString("NewPIWizardPageRomSubTask.symbol.filter.text")); //$NON-NLS-1$
		symbolButton.setData(MY_WIGET_KEY, symbolText);
		symbolButton.addSelectionListener(browseSelectionListener);
	}

	public void createObyControl() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		obyComposite = new Composite(composite, SWT.NONE);
		obyComposite.setLayout(gridLayout);
		obyComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		obyText = new Text(obyComposite, SWT.BORDER | SWT.SINGLE);
		obyText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		obyText.addModifyListener(textModifyListener);
		obyButton = new Button(obyComposite, SWT.NONE);
		obyButton.setText(Messages.getString("NewPIWizardPageRomSubTask.oby.button")); //$NON-NLS-1$
		obyButton.setData(FILTER_EXT_KEY, Messages.getString("NewPIWizardPageRomSubTask.oby.filter")); //$NON-NLS-1$
		obyButton.setData(FILTER_NAME_KEY, Messages.getString("NewPIWizardPageRomSubTask.oby.filter.text")); //$NON-NLS-1$
		obyButton.setData(MY_WIGET_KEY, obyText);
		obyButton.addSelectionListener(browseSelectionListener);
	}
	
	public void createRofsObySymbolControl() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		rofsObySymbolComposite = new Composite(composite, SWT.NONE);
		rofsObySymbolComposite.setLayout(gridLayout);
		rofsObySymbolComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		rofsObySymbolTable = new Table(rofsObySymbolComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.MULTI );
		rofsObySymbolTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout buttonGridLayout = new GridLayout();
		buttonGridLayout.numColumns = 1;
		// custom widget for multiple line
		rofsObySymbolTable.addListener(SWT.MeasureItem, new Listener() {
			public void handleEvent(Event event) {
				TableItem item = (TableItem)event.item;
				String text = item.getText(event.index);
				Point size = event.gc.textExtent(text);
				event.width = Math.max(event.width, size.x + 2 * TEXT_MARGIN);
				event.height = Math.max(event.height, size.y + 2 * TEXT_MARGIN);
			}});
		rofsObySymbolTable.addListener(SWT.EraseItem, new Listener() {
			public void handleEvent(Event event) {
				event.detail &= ~SWT.FOREGROUND;
			}
		});
		rofsObySymbolTable.addListener(SWT.PaintItem, new Listener() {

			public void handleEvent(Event event) {
				TableItem item = (TableItem)event.item;
				String text = item.getText(event.index);
				event.gc.drawText(text, event.x, event.y, true);
				event.gc.drawLine(event.x , event.y + event.height - TEXT_MARGIN, event.x + rofsObySymbolTable.getSize().x , event.y + event.height - TEXT_MARGIN);
			}});
		
		rofsObySymbolTable.pack();
		rofsObySymbolButtonComposite = new Composite(rofsObySymbolComposite, SWT.NONE);
		rofsObySymbolButtonComposite.setLayout(buttonGridLayout);
		rofsObySymbolButtonComposite.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		rofsAddButton = new Button(rofsObySymbolButtonComposite, SWT.NONE);
		rofsAddButton.setText(Messages.getString("NewPIWizardPageRomSubTask.add")); //$NON-NLS-1$
		rofsAddButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				NewPIWizardObySymbolPairDialog dialog = new NewPIWizardObySymbolPairDialog(getShell());
				dialog.open();
				RofsObySymbolPair pair = dialog.getPair();
				if (pair.haveObyFile() || pair.haveSymbolFile() ) {
					boolean found = false;
					TableItem[] items = rofsObySymbolTable.getItems();
					for (TableItem item : items) {
						if (((RofsObySymbolPair)item.getData(ROFS_ITEM_PAIR_KEY)).equals(pair)) {
							found = true;
							break;
						}
					}
					// don't add new entry if it's already there
					if (found == false) {
						TableItem newItem = new TableItem(rofsObySymbolTable, SWT.NONE);
						newItem.setData(ROFS_ITEM_PAIR_KEY, pair);
						newItem.setText(pair.getDisplayString());	
					} else {
						MessageDialog.openInformation(getShell(), Messages.getString("NewPIWizardPageRomSubTask.add.rofs.pair"), Messages.getString("NewPIWizardPageRomSubTask.entry.already.exisits")); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				writeRofsTableToStatus();
			}
		
		});
		rofsRemoveButton = new Button(rofsObySymbolButtonComposite, SWT.NONE);
		rofsRemoveButton.setText(Messages.getString("NewPIWizardPageRomSubTask.remove")); //$NON-NLS-1$
		rofsRemoveButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				rofsObySymbolTable.remove(rofsObySymbolTable.getSelectionIndices());
				writeRofsTableToStatus();
			}
			
		});
		
		GridData buttonWidthGridData = new GridData();
		buttonWidthGridData.widthHint = Math.max(rofsAddButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x, rofsRemoveButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		rofsAddButton.setLayoutData(buttonWidthGridData);
		rofsRemoveButton.setLayoutData(buttonWidthGridData);
	}
		
	public void setupPageFromFromNewPIWizardSettings() {
		NewPIWizardSettings npws = NewPIWizardSettings.getInstance();
		sdkCommon.setSelectedSdk(npws.romSdk);
		symbolText.setText(npws.romSymbolFile);
		obyText.setText(npws.romObyFile);
		rofsObySymbolTable.removeAll();
		for(RofsObySymbolPair pair:npws.rofsObySymbolPairList) {
			TableItem newItem = new TableItem(rofsObySymbolTable, SWT.NONE);
			newItem.setData(ROFS_ITEM_PAIR_KEY, pair);
			newItem.setText(pair.getDisplayString());
		}
	}

	public void setVisible(boolean visable) {
		super.setVisible(visable);
		if (visable) {
//			 block the text if it's wrong on startup, so user know what's wrong
			if (visableBefore == false) {
				String errorMessage = getErrorMessage();
				if (errorMessage != null) {
					if (errorMessage.startsWith(Messages.getString("NewPIWizardPageRomSubTask.symbol.label"))) { //$NON-NLS-1$
						symbolText.selectAll();
						symbolText.setFocus();
					} else if (errorMessage.startsWith(Messages.getString("NewPIWizardPageRomSubTask.oby.label"))) { //$NON-NLS-1$
						obyText.selectAll();
						symbolText.setFocus();
					}
				}
			}
			lastGoodDir = null;
		}
		visableBefore = visable;
	}
}
