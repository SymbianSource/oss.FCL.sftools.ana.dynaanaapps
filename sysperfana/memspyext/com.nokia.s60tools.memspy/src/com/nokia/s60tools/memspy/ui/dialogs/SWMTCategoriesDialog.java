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
 
package com.nokia.s60tools.memspy.ui.dialogs;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.nokia.s60tools.memspy.model.SWMTCategoryConstants;
import com.nokia.s60tools.memspy.model.SWMTCategorys;
import com.nokia.s60tools.memspy.preferences.MemSpyPreferences;
import com.nokia.s60tools.memspy.resources.HelpContextIDs;
import com.nokia.s60tools.memspy.resources.ImageKeys;
import com.nokia.s60tools.memspy.resources.ImageResourceManager;
import com.nokia.s60tools.memspy.util.MemSpyConsole;
import com.nokia.s60tools.ui.S60ToolsTable;
import com.nokia.s60tools.ui.S60ToolsTableColumnData;
import com.nokia.s60tools.ui.S60ToolsTableFactory;
import com.nokia.s60tools.ui.S60ToolsUIConstants;



/**
 * Dialog for selecting SWMT categories to be tracked. 
 */
public class SWMTCategoriesDialog extends TitleAreaDialog{	


	
	//
	// Private classes
	//
	
	private static final String HEAP_DATA_THREAD_FILTER_TXT = "Heap Data Thread Filter";

	/**
	 * Label provider for table viewer component.
	 */
	class SWMTCategoryViewerLabelProvider extends LabelProvider implements ITableLabelProvider{

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			return null; // No images used
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			String label = element.toString();
			
			SWMTCategoryEntry entryData = (SWMTCategoryEntry) element;

			switch (columnIndex) {
		
				case SWMTCategoryEntry.NAME_COLUMN_INDEX:
					label = entryData.getCategoryName();
					break;
							
				default:
					MemSpyConsole.getInstance().println("Unexpected column index: " + columnIndex, MemSpyConsole.MSG_ERROR); //$NON-NLS-1$ //$NON-NLS-2$
					break;
			}
			
			return label;
		}
		
	}
	
	//
	// Private constants
	//
	
	/**
	 * Columns in the container area.
	 */
	private static final int COLUMN_COUNT = 1;	  

	/**
	 * Dialog width.
	 */
	private static final int DIALOG_WIDTH = 425;
	
	/**
	 * Dialog height.
	 */
	private static final int DIALOG_HEIGHT = 550;
		
	/**
	 * Percentage as decimal number how much table viewer is taking space horizontally. 
	 */
	private static final double TABLE_VIEWER_WIDTH_PERCENTAGE = 0.8;
	
	/**
	 * Default guiding message shown to the user in add new mode.
	 */
	private static final String DEFAULT_MESSAGE = "Set advanced options and tracked SWMT categories.";
	
	/**
	 * Complete message shown to the user in add new mode.
	 */
	private static final String COMPLETE_MESSAGE = "Press OK to save advanced options and set categories to be tracked";
	
	/**
	 * Error message shown to the user in case no categories are selected.
	 */
	private static final String ERROR_MESSAGE = "At least single category must be selected";
	
	/**
	 * UI text for closing Symbian agent
	 */
	private static final String  CLOSE_MEM_SPY_BETWEEN_CYCLES_TEXT  = "Close MemSpy Symbian Agent Between Cycles";
	
	/**
	 * Tip text for closing Symbian agent
	 */
	private static final String  CLOSE_MEM_SPY_BETWEEN_CYCLES_TIP_TEXT = "Choose if the MemSpy Symbian agent in S60 target is closed between cycles. That is more realistic use case for memory usage point of view.";	
	
	
	//
	// Member data
	//
	
	/**
	 * Flag used to make sure that create() and open() are called in correct order.
	 */
	private boolean isCreateCalled = false;
	

	//
	// UI Controls
	//	
	
	/**
	 * Container area for individual fields for the user for entering information.
	 */
	private Composite container;

	/**
	 * Reference to OK button that can be disabled/enabled 
	 * due to current category selection-
	 */
	private Button okActionBtn;

	/**
	 * Viewer showing currently selected category file entries.
	 */
	private CheckboxTableViewer categoryViewer;

	/**
	 * Selected categories as a result of bitwise OR.
	 */
	private int categorySelection = SWMTCategoryConstants.CATEGORY_ALL;
	
	//User Heap filter text field
	private Text heapText;

	private Button closeBetweenCyclesButton;

	private Button heapDumpBtn;	
	
	/**
	 * Constructor. Used to open dialog in order to add new entry.
	 * @param parentShell Parent shell for the dialog.
	 * @param categorySelection integer containing initially selected categories as a result of bitwise OR. 
	 */
	public SWMTCategoriesDialog(Shell parentShell, int categorySelection) {
		super(parentShell);
		this.categorySelection = categorySelection;
		// Setting banner image
		String bannerImage = ImageKeys.IMG_WIZARD;
		setTitleImage(ImageResourceManager.getImage(bannerImage));
	}



	/* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL,
                true);     
		okActionBtn = getButton(IDialogConstants.OK_ID);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("SWMT Categories and Advanced Options");
    }    
    
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		
		Composite dialogAreaComposite = (Composite) super.createDialogArea(parent);
		
		//
		// Creating container and layout for it
		//
		container = new Composite(dialogAreaComposite, SWT.NONE);
		GridLayout gdl = new GridLayout(COLUMN_COUNT, false);
		// Settings margins according Carbide branding guideline
		gdl.marginLeft = S60ToolsUIConstants.MARGIN_BTW_FRAME_AND_CONTENTS;
		gdl.marginRight = S60ToolsUIConstants.MARGIN_BTW_FRAME_AND_CONTENTS;
		gdl.marginTop = S60ToolsUIConstants.MARGIN_BTW_FRAME_AND_CONTENTS;
		gdl.marginBottom = S60ToolsUIConstants.MARGIN_BTW_FRAME_AND_CONTENTS;		
		container.setLayout(gdl);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		//
		// Symbian agent options group
		//
		
		createSymbianAgentOptionsGroup();				
		
		//
		// Head dump group
		//
		createHeapDumpGroup();		
		
		
		//
		// Creating table viewer for showing category file entries
		//
		
		categoryViewer = createCategoryCheckBoxTableViewer(container);
	    GridData categoryViewerGd = new GridData(GridData.FILL_BOTH);
	    // Spanning as many rows as there are actions buttons on the right
	    categoryViewerGd.verticalSpan = 3;
	    categoryViewerGd.widthHint = (int) (TABLE_VIEWER_WIDTH_PERCENTAGE * DIALOG_WIDTH);
		categoryViewer.getControl().setLayoutData(categoryViewerGd);
		categoryViewer.setSorter(new SWMTCategoryEntryTableViewerSorter());
		// Adding selection change listener
		categoryViewer.addSelectionChangedListener(new ISelectionChangedListener(){

			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
			 */
			public void selectionChanged(SelectionChangedEvent event) {
				notifySelectionChanged();
			}
			
		});
		
		//
		// Setting providers for table viewer
		//
		
		// Creating content provider
		GategoryProvider categoryViewerContentProvider = new GategoryProvider();
		// Setting content provider
		categoryViewer.setContentProvider(categoryViewerContentProvider);
		categoryViewer.setInput(categoryViewerContentProvider);
		
		// Label provider
		categoryViewer.setLabelProvider(new SWMTCategoryViewerLabelProvider());

		// Setting initial category selection state
		InitializeSelectionState();
		
		// Setting context-sensitive help ID		
		PlatformUI.getWorkbench().getHelpSystem().setHelp( dialogAreaComposite, HelpContextIDs.MEMSPY_IMPORT_SWMT_CATEGORIES_DIALOG);
		
		// Dialog are composite ready
		return dialogAreaComposite;
	}

	
	/**
	 * Private class to provide content for category viewer
	 */
	class GategoryProvider implements IStructuredContentProvider {

				/* (non-Javadoc)
				 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
				 */
				public Object[] getElements(Object inputElement) {
					return SWMTCategorys.getInstance().getCategoryEntries().toArray();
				}
	
				/* (non-Javadoc)
				 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
				 */
				public void dispose() {
					// Not needed but needs to be implemented
				}
	
				public void inputChanged(Viewer viewer, Object oldInput,
						Object newInput) {				
					// Not used but needs to be implemented				
				}

			};	
	
	/**
	 * Creates group for Symbian agent options
	 */
	private void createSymbianAgentOptionsGroup() {
		Group closeSymbianGroup = new Group(container, SWT.SHADOW_NONE);
		closeSymbianGroup.setText("Symbian Agent Options");
		GridLayout closegl = new GridLayout(2, false);
		GridData closegd = new GridData(GridData.FILL_BOTH);				
		closeSymbianGroup.setLayout(closegl);
		closeSymbianGroup.setLayoutData(closegd);
		
		closeBetweenCyclesButton = new Button(closeSymbianGroup, SWT.CHECK);		
		GridData closeLayoutData = new GridData(GridData.VERTICAL_ALIGN_END);
		closeBetweenCyclesButton.setLayoutData( closeLayoutData);
		closeBetweenCyclesButton.setToolTipText(CLOSE_MEM_SPY_BETWEEN_CYCLES_TIP_TEXT);
		boolean isToBeClosedBetweenCycles = MemSpyPreferences.isCloseSymbianAgentBetweenCyclesSelected();		
		closeBetweenCyclesButton.setSelection(isToBeClosedBetweenCycles);
		
		//Label for button, separate label is used because setting text to button wont align text to same vertical 
		//position than next label.
		Label closeBetweenIntervalsLabel = new Label(closeSymbianGroup, SWT.LEFT );
		GridData closeBetweenLabelData = new GridData(GridData.VERTICAL_ALIGN_CENTER);
		closeBetweenIntervalsLabel.setText( CLOSE_MEM_SPY_BETWEEN_CYCLES_TEXT );
		closeBetweenIntervalsLabel.setLayoutData(closeBetweenLabelData);		
		
	}
	

	/**
	 * Creates group for Head Dumps
	 */
	private void createHeapDumpGroup() {
		//
		// Head dump group
		//
		Group headDumpGroup = new Group(container, SWT.SHADOW_NONE);
		headDumpGroup.setText("Heap Dumps During SWMT Logging");
		GridLayout hdgl = new GridLayout(2, false);
		GridData hdgd = new GridData(GridData.FILL_BOTH);				
		headDumpGroup.setLayout(hdgl);
		headDumpGroup.setLayoutData(hdgd);		
		
		heapDumpBtn = new Button(headDumpGroup, SWT.CHECK);
		heapDumpBtn.setText("Get Heap Dumps for Threads to Analyse with Heap Analyser");
		heapDumpBtn.setToolTipText("Set if Heap Dumps is to be received for Threads during SWMT logging");
		heapDumpBtn.addSelectionListener(new HeadDumpSelectionListener());
		heapDumpBtn.setSelection(MemSpyPreferences.isSWMTHeapDumpSelected());
		GridData hdBtnGd = new GridData();
		hdBtnGd.horizontalSpan = 2;
		heapDumpBtn.setLayoutData(hdBtnGd);		
		
		//Label for Heap gategory limit
		Label heapTextLabel = new Label(headDumpGroup,  SWT.LEFT );
		heapTextLabel.setText(HEAP_DATA_THREAD_FILTER_TXT + ":");
		String heapFilterToolTipText = "When filter string is specified, only Threads that contain text specified will be tracked.";
		heapTextLabel.setToolTipText(heapFilterToolTipText);
		GridData heapTextLimitLayoutData = new GridData(GridData.VERTICAL_ALIGN_CENTER);
		heapTextLabel.setLayoutData( heapTextLimitLayoutData );	
		
		//heap limit text
		heapText = new Text(headDumpGroup, SWT.LEFT | SWT.BORDER | SWT.BORDER);
		heapText.setTextLimit(120);//Text limit 128 specified in S60 side		
		GridData heapTextLayoutData = new GridData(GridData.VERTICAL_ALIGN_CENTER);
		heapTextLayoutData.widthHint = 140;				
		heapText.setLayoutData( heapTextLayoutData );
		heapText.setToolTipText(heapFilterToolTipText);		
		if(heapDumpBtn.getSelection()){
			heapText.setText(MemSpyPreferences.getSWMTHeapNameFilter());
		}else{
			heapText.setEnabled(false);
		}
	}
	
	/**
	 * Listener for Heap Dumps button, updates preferences and text in Heap Dumps group.
	 */
	private class HeadDumpSelectionListener implements SelectionListener{

		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetDefaultSelected(SelectionEvent e) {
			// not needed
			
		}

		/* 
		 * Change heapText and save preferences
		 * (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetSelected(SelectionEvent e) {
			boolean selection = heapDumpBtn.getSelection();
			if(selection){
				heapText.setEnabled(true);
				heapText.setText(MemSpyPreferences.getSWMTHeapNameFilter());
			}else{
				heapText.setText("");
				heapText.setEnabled(false);
			}
		}

		
	}
	
	/**
	 * Save the heap filter text if enabled
	 */
	private void saveHeapFilterText() {
		//Check if heaptext is enabled, if it is, saving text to preferences
		if(heapText.isEnabled()){
			String heapFilterText = heapText.getText().trim();
			MemSpyPreferences.setSWMTHeapNameFilter(heapFilterText);
		}
	}	
	/**
	 * Save the Close between cycles selection
	 */
	private void saveCloseS60AgentBetweenCycles() {
		boolean selection = closeBetweenCyclesButton.getSelection();
		// User selects to close MemSpy S60 application between cycles 
		MemSpyPreferences.setCloseSymbianAgentBetweenCycles(selection);
	}
	/**
	 * Save the heap dump selection
	 */
	private void saveHeapDumpSelection() {
		boolean selection = heapDumpBtn.getSelection();
		MemSpyPreferences.setSWMTHeapDumpSelected(selection);
	}
	
	
		
	 /**
	 * Sets initial selection state for entries
	 */
	private void InitializeSelectionState() {
		int size = SWMTCategorys.getInstance().getCategoryEntries().size();
		for (int i = 0; i < size; i++) {
			SWMTCategoryEntry entry = (SWMTCategoryEntry) categoryViewer.getElementAt(i);
			int isCategoryBitUp = categorySelection & entry.getCategoryId();
			if(isCategoryBitUp != 0){
				categoryViewer.setChecked(entry, true);
			}			
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#getInitialSize()
	 */
	protected Point getInitialSize() {
			return new Point(DIALOG_WIDTH, DIALOG_HEIGHT);
	    }
	 
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#create()
	 */
	public void create() {
		super.create();
		// Currently just does creation by super call and stores status
		isCreateCalled = true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		
		if(heapDumpBtn.getSelection()){
			if(heapText.getText().trim().equals("")){
				System.out.println("DEBUG: WARNING! Empty heap filter text!");
				MessageBox box = new MessageBox(getShell(),SWT.ICON_WARNING | SWT.YES | SWT.NO);
				String warningMsg = "Empty \"" +HEAP_DATA_THREAD_FILTER_TXT 
						+ "\" field causes huge amount of data to be traced and tracing may take a very long time. "
						+"It is highly recommended to use \"" +HEAP_DATA_THREAD_FILTER_TXT 
						+ "\"."
						+"\n\nDo you want to continue anyway?";
				box.setMessage(warningMsg);
				box.setText("Using \"" +HEAP_DATA_THREAD_FILTER_TXT 
						+ "\" is recommended");
				int yes_no = box.open();
				//If user does not want to continue but cancel, returning, otherwise just continue to save all data and exit
				if(yes_no == SWT.NO){
					return;
				}
				
			}
		}
		
		saveHeapFilterText(); 
		saveCloseS60AgentBetweenCycles();		
		saveHeapDumpSelection();
		super.close();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#open()
	 */
	public int open(){
		try {
			// Making sure that create is called
			if(!isCreateCalled){
				create();
			}
			showDefaultMessage();			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return super.open();
	}

	/**
	 * Resets possible error messages and show the default message.
	 */
	private void showDefaultMessage() {
		setErrorMessage(null);
		setMessage(DEFAULT_MESSAGE, IMessageProvider.INFORMATION);			
	}

	/**
	 * Informs user that parameters are valid and dialog can be
	 * dismissed with OK button..
	 */
	private void setCompleteOkMessage() {
		setErrorMessage(null);
		setMessage(COMPLETE_MESSAGE, IMessageProvider.INFORMATION);			
	}

	/**
	 *  Shows error message in case no categories are selected.
	 */
	private void showErrorMessage() {
		setErrorMessage(ERROR_MESSAGE);
		setMessage(null);			
	}

	/**
	 * Disables OK button.
	 * This method is guarded against call during construction
	 * when button row has not been created yet and widget is <code>null</code>.
	 */
	private void disableOk() {
		if(okActionBtn != null){
			okActionBtn.setEnabled(false);			
		}
	}

	/**
	 * Enables OK button.
	 * This method is guarded against call during construction
	 * when button row has not been created yet and widget is <code>null</code>.
	 */
	private void enableOk() {
		if(okActionBtn != null){
			okActionBtn.setEnabled(true);
		}
	}	

	/**
	 * Updates buttons statuses according the current selection
	 * status of table entry viewer contents and refreshes contents.
	 */
	private void notifySelectionChanged() {
		Object[] selectedCategoryEntries = getSelectedCategoryEntries();
		if(selectedCategoryEntries.length > 0){
			updateSelectedCategories(selectedCategoryEntries);
			enableOk();
			setCompleteOkMessage();
		}
		else{
			disableOk();
			showErrorMessage();			
		}		
		categoryViewer.refresh();
	}

	/**
	 * Creates checkbox viewer component for showing available SWMT categories. 
	 * @param parent Parent composite for the created composite.
	 * @return New <code>CheckboxTableViewer</code> object instance.
	 */
	protected CheckboxTableViewer createCategoryCheckBoxTableViewer(Composite parent) {
		
		ArrayList<S60ToolsTableColumnData> columnDataArr = new ArrayList<S60ToolsTableColumnData>();
		
		//
		// NOTE: Column indices must start from zero (0) and
		// the columns must be added in ascending numeric
		// order.
		//
		columnDataArr.add(new S60ToolsTableColumnData("Category", //$NON-NLS-1$
														350,
														SWMTCategoryEntry.NAME_COLUMN_INDEX,
														SWMTCategoryEntryTableViewerSorter.CRITERIA_NAME));
		
		S60ToolsTableColumnData[] arr 
				= columnDataArr.toArray(
									   new S60ToolsTableColumnData[0]);
		
		S60ToolsTable tbl = S60ToolsTableFactory.createCheckboxTable(parent, arr);
		
		CheckboxTableViewer tblViewer = new CheckboxTableViewer(tbl.getTableInstance());
		tbl.setHostingViewer(tblViewer);
		
		return tblViewer;
	}
	
	/**
	 * Returns currently selected categories.
	 * @return currently selected categories
	 */
	private Object[] getSelectedCategoryEntries() {
		return categoryViewer.getCheckedElements();
	}

	/**
	 * Stores currently selected categories combined with bitwise as a single integer.
	 * @param selectedCategoryEntries array of selected category entries
	 */
	private void updateSelectedCategories(Object[] selectedCategoryEntries) {
		// Re-initializing category selection
		categorySelection = SWMTCategoryConstants.CATEGORY_NONE;
		for (int i = 0; i < selectedCategoryEntries.length; i++) {
			SWMTCategoryEntry categoryEntry = (SWMTCategoryEntry) selectedCategoryEntries[i];
			categorySelection = categorySelection | categoryEntry.getCategoryId();
		}			
	}
	
	/**
	 * Returns currently selected categories combined with bitwise as a single integer.
	 * @return currently selected categories combined with bitwise as a single integer.
	 */
	public int getSelectedCategories() {
		return categorySelection;
	}

}
