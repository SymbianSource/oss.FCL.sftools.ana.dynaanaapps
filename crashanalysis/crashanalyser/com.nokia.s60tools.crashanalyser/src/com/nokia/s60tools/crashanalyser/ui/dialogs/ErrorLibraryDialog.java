/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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

package com.nokia.s60tools.crashanalyser.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import com.nokia.s60tools.crashanalyser.resources.HelpContextIDs;
import com.nokia.s60tools.crashanalyser.search.TextViewerFilter;
import com.nokia.s60tools.crashanalyser.containers.ErrorLibraryError;
import com.nokia.s60tools.crashanalyser.data.*;
import com.nokia.s60tools.crashanalyser.model.PanicsViewerSorter;
import com.nokia.s60tools.crashanalyser.model.ErrorsViewerSorter;
import com.nokia.s60tools.crashanalyser.model.HtmlFormatter;
import org.eclipse.jface.viewers.ISelectionChangedListener;

/**
 * A dialog which contains all errors, panics and panic categories.
 * 
 * Dialog contains three tab pages: Panic search, Category search and Error search.
 *
 */
public class ErrorLibraryDialog extends Dialog implements 	ModifyListener,
															SelectionListener,
															ISelectionChangedListener {
	Text textPanicSearch;
	Text textCategorySearch;
	Text textErrorSearch;
	ListViewer listPanics;
	ListViewer listCategories;
	ListViewer listErrors;
	Browser browserPanicDescription;
	Browser browserCategoryDescription;
	Browser browserErrorDescription;
	ErrorLibraryContentProvider contentPanics;
	ErrorLibraryContentProvider contentCategories;
	ErrorLibraryContentProvider contentErrors;
	ErrorLibrary errorLibrary;
	Link linkMailTo;
	
	/**
	 * Constructor
	 * @param parentShell shell
	 * @param library error library
	 */
	public ErrorLibraryDialog(Shell parentShell, ErrorLibrary library) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.HELP);
		errorLibrary = library;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialogAreaComposite = (Composite) super.createDialogArea(parent);
		
		GridLayout gdl = new GridLayout(1, false);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 600;
		gd.heightHint = 600;
		dialogAreaComposite.setLayout(gdl);
		dialogAreaComposite.setLayoutData(gd);

		// create tab
		TabFolder tabFolder = new TabFolder(dialogAreaComposite, SWT.NONE);
		tabFolder.setLayoutData(gd);
		
		// create tab pages
		createPanicsPage(tabFolder);
		createCategoriesPage(tabFolder);
		createErrorsPage(tabFolder);
		
		// mail to link, which can be used to notify about missing panic descriptions
		linkMailTo = new Link(dialogAreaComposite, SWT.NONE);
		linkMailTo.setText("<a href=\"mailto:S60RnDtools@nokia.com?subject=Crash Analyser - Missing Panic\">Report a missing panic or error</a>");
		linkMailTo.addSelectionListener(this);
		
		tabFolder.setSelection(0);

		// load data to tab pages asynchronously
		loadDataAsync();
		
		return dialogAreaComposite;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL, false);
		setHelps();
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Crash Analyser - Error Library");
	}
	
	/**
	 * Called when user types something into search boxes
	 */
	public void modifyText(ModifyEvent item) {
		// panic search text was modified, filter list accordingly
		if (item.widget == textPanicSearch) {
			listPanics.resetFilters();
			String text = textPanicSearch.getText();
			if (text.length() > 0) {
				listPanics.addFilter(new TextViewerFilter(text));
			}
			if (listPanics.getList().getItemCount() > 0) {
				listPanics.setSelection(new StructuredSelection(listPanics.getElementAt(0)), true);
			} else {
				browserPanicDescription.setText("");
			}
			
		// category search text was modified, filter list accordingly
		} else if (item.widget == textCategorySearch) {
			listCategories.resetFilters();
			String text = textCategorySearch.getText();
			if (text.length() > 0) {
				listCategories.addFilter(new TextViewerFilter(text));
			}
			if (listCategories.getList().getItemCount() > 0) {
				listCategories.setSelection(new StructuredSelection(listCategories.getElementAt(0)), true);
			} else {
				browserCategoryDescription.setText("");
			}

		// error search text was modified, filter list accordingly
		} else if (item.widget == textErrorSearch) {
			listErrors.resetFilters();
			String text = textErrorSearch.getText();
			if (text.length() > 0) {
				listErrors.addFilter(new TextViewerFilter(text));
			}
			if (listErrors.getList().getItemCount() > 0) {
				listErrors.setSelection(new StructuredSelection(listErrors.getElementAt(0)), true);
			} else {
				browserErrorDescription.setText("");
			}
		}
	}	
	
	/**
	 * Called when user selects something from list
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		// user selected from panics list
		if (event.getSource() == listPanics) {
			int index = listPanics.getList().getSelectionIndex();
			if (index >= 0) {
				ErrorLibraryError p = (ErrorLibraryError)listPanics.getElementAt(index);
				if (p != null)
					browserPanicDescription.setText(HtmlFormatter.formatHtmlStyle(textPanicSearch.getFont(), 
													p.getDescription()));
			}
			
		// user selected from categories list
		} else if (event.getSource() == listCategories) {
			int index = listCategories.getList().getSelectionIndex();
			if (index >= 0) {
				ErrorLibraryError p = (ErrorLibraryError)listCategories.getElementAt(index);
				if (p != null)
					browserCategoryDescription.setText(HtmlFormatter.formatHtmlStyle(textPanicSearch.getFont(),
														p.getDescription()));
			}
			
		// user selected from errors list
		} else if (event.getSource() == listErrors) {
			int index = listErrors.getList().getSelectionIndex();
			if (index >= 0) {
				ErrorLibraryError p = (ErrorLibraryError)listErrors.getElementAt(index);
				if (p != null)
					browserErrorDescription.setText(HtmlFormatter.formatHtmlStyle(textPanicSearch.getFont(),
													p.getDescription()));
			}			
		}
	}
	
	public void widgetDefaultSelected(SelectionEvent arg0) {
		// no implementation needed
	}

	public void widgetSelected(SelectionEvent event) {
		try	{
			browserCategoryDescription.setUrl(event.text);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates panics tab page
	 * @param tabFolder
	 */
	void createPanicsPage(TabFolder tabFolder) {
		TabItem panicsPage = new TabItem(tabFolder, SWT.NONE);
		panicsPage.setText("Panic Search");
		
		Composite composite = getComposite(tabFolder);
		
		textPanicSearch = new Text(composite, SWT.BORDER | SWT.SINGLE);
		textPanicSearch.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		textPanicSearch.addModifyListener(this);
		
		listPanics = new ListViewer(composite, SWT.BORDER | SWT.V_SCROLL);
		listPanics.getList().setLayoutData(getListGridData());
		listPanics.addSelectionChangedListener(this);
		listPanics.setSorter(new PanicsViewerSorter());
		
		browserPanicDescription = new Browser(composite, SWT.BORDER);
		browserPanicDescription.setLayoutData(new GridData(GridData.FILL_BOTH));
		browserPanicDescription.setText("");
		
		panicsPage.setControl(composite);
	}
	
	/**
	 * Creates categories tab page
	 * @param tabFolder
	 */
	void createCategoriesPage(TabFolder tabFolder) {
		TabItem categoriesPage = new TabItem(tabFolder, SWT.NONE);
		categoriesPage.setText("Category Search");

		Composite composite = getComposite(tabFolder);
		
		textCategorySearch = new Text(composite, SWT.BORDER | SWT.SINGLE);
		textCategorySearch.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		textCategorySearch.addModifyListener(this);
		
		listCategories = new ListViewer(composite, SWT.BORDER | SWT.V_SCROLL);
		listCategories.getList().setLayoutData(getListGridData());
		listCategories.addSelectionChangedListener(this);
		listCategories.setSorter(new ViewerSorter());
		
		browserCategoryDescription = new Browser(composite, SWT.BORDER);
		browserCategoryDescription.setLayoutData(new GridData(GridData.FILL_BOTH));
		browserCategoryDescription.setText("");
		
		categoriesPage.setControl(composite);
	}

	/**
	 * Creates errors tab page
	 * @param tabFolder
	 */
	void createErrorsPage(TabFolder tabFolder) {
		TabItem errorsPage = new TabItem(tabFolder, SWT.NONE);
		errorsPage.setText("Error Search");

		Composite composite = getComposite(tabFolder);
		
		textErrorSearch = new Text(composite, SWT.BORDER | SWT.SINGLE);
		textErrorSearch.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		textErrorSearch.addModifyListener(this);
		
		listErrors = new ListViewer(composite, SWT.BORDER | SWT.V_SCROLL);
		listErrors.getList().setLayoutData(getListGridData());
		listErrors.addSelectionChangedListener(this);
		listErrors.setSorter(new ErrorsViewerSorter());
		
		browserErrorDescription = new Browser(composite, SWT.BORDER);
		browserErrorDescription.setLayoutData(new GridData(GridData.FILL_BOTH));
		browserErrorDescription.setText("");
		
		errorsPage.setControl(composite);
	}
	
	/**
	 * Creates a composite which tab pages can utilize.
	 * @param tabFolder
	 * @return composite
	 */
	Composite getComposite(TabFolder tabFolder) {
		Composite composite = new Composite(tabFolder, SWT.NONE);
		composite.setLayout(new RowLayout());
		GridLayout gdl = new GridLayout(1, false);
		GridData gd = new GridData(GridData.FILL_BOTH);
		composite.setLayout(gdl);
		composite.setLayoutData(gd);
		return composite;
	}
	
	/**
	 * Creates a grid data object for lists
	 * @return
	 */
	GridData getListGridData() {
		GridData listGd = new GridData(GridData.FILL_HORIZONTAL);
		listGd.heightHint = 100;
		return listGd;
	}
	
	/**
	 * Sets context sensitive helps to all UI items.
	 */
	void setHelps() {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(textPanicSearch,
				HelpContextIDs.CRASH_ANALYSER_HELP_ERROR_LIBRARY);	
		PlatformUI.getWorkbench().getHelpSystem().setHelp(textErrorSearch,
				HelpContextIDs.CRASH_ANALYSER_HELP_ERROR_LIBRARY);	
		PlatformUI.getWorkbench().getHelpSystem().setHelp(textCategorySearch,
				HelpContextIDs.CRASH_ANALYSER_HELP_ERROR_LIBRARY);	
		PlatformUI.getWorkbench().getHelpSystem().setHelp(listPanics.getControl(),
				HelpContextIDs.CRASH_ANALYSER_HELP_ERROR_LIBRARY);	
		PlatformUI.getWorkbench().getHelpSystem().setHelp(listCategories.getControl(),
				HelpContextIDs.CRASH_ANALYSER_HELP_ERROR_LIBRARY);	
		PlatformUI.getWorkbench().getHelpSystem().setHelp(listErrors.getControl(),
				HelpContextIDs.CRASH_ANALYSER_HELP_ERROR_LIBRARY);	
		PlatformUI.getWorkbench().getHelpSystem().setHelp(browserCategoryDescription,
				HelpContextIDs.CRASH_ANALYSER_HELP_ERROR_LIBRARY);	
		PlatformUI.getWorkbench().getHelpSystem().setHelp(browserErrorDescription,
				HelpContextIDs.CRASH_ANALYSER_HELP_ERROR_LIBRARY);	
		PlatformUI.getWorkbench().getHelpSystem().setHelp(browserPanicDescription,
				HelpContextIDs.CRASH_ANALYSER_HELP_ERROR_LIBRARY);	
		PlatformUI.getWorkbench().getHelpSystem().setHelp(linkMailTo,
				HelpContextIDs.CRASH_ANALYSER_HELP_ERROR_LIBRARY);	
	}
		
	/**
	 * Loads panics, categories and errors into lists asynchronously so that
	 * dialog can be opened fast
	 */
	public void loadDataAsync() {
		Runnable refreshRunnable = new Runnable(){
			public void run(){
				// panics
				contentPanics = new ErrorLibraryContentProvider(ErrorLibraryContentProvider.ContentTypes.PANIC, errorLibrary);
				listPanics.setContentProvider(contentPanics);
				listPanics.setInput(contentPanics);
				
				// categories
				contentCategories = new ErrorLibraryContentProvider(ErrorLibraryContentProvider.ContentTypes.CATEGORY, errorLibrary);
				listCategories.setContentProvider(contentCategories);
				listCategories.setInput(contentCategories);
				
				// errors
				contentErrors = new ErrorLibraryContentProvider(ErrorLibraryContentProvider.ContentTypes.ERROR, errorLibrary);
				listErrors.setContentProvider(contentErrors);
				listErrors.setInput(contentErrors);
			}
		};
		
		Display.getDefault().asyncExec(refreshRunnable);        		
	}
}
