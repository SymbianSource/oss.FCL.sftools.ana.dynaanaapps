/*
 * Copyright (c) 2008-2009 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description:  Definitions for the class DropDownMenu
 *
 */

package com.nokia.s60tools.analyzetool.ui.actions;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.nokia.s60tools.analyzetool.global.Constants;
import com.nokia.s60tools.analyzetool.global.Util;
import com.nokia.s60tools.analyzetool.ui.MainView;

/**
 * Creates new drop down menu by given IActions.
 *
 * @author kihe
 *
 */
public class DropDownMenu extends Action implements IMenuCreator {

	/** Menu where to add actions. */
	private Menu ddMenu;

	/** Contains actions of current menu. */
	private final java.util.AbstractList<IAction> actions;

	/** this class parent class reference. */
	private final MainView parentClass;

	/** Need to check actions. */
	private final boolean checkActions;

	/** Is menu for file open actions. */
	private final boolean isFileMenu;

	/**
	 * Constructor.
	 *
	 * @param title
	 *            Drop down menu title
	 * @param parentClassRef
	 *            Parent class reference
	 * @param check
	 * 			  Is it necessary to check each action validity
	 * @param fileMenu
	 * 			  Is current reference file menu
	 *
	 */
	public DropDownMenu(final String title, final MainView parentClassRef,
			final boolean check, final boolean fileMenu) {
		setText(title);
		setMenuCreator(this);
		actions = new ArrayList<IAction>();
		parentClass = parentClassRef;
		checkActions = check;
		isFileMenu = fileMenu;
	}

	/**
	 * Adds actions.
	 *
	 * @param action
	 *            Action to add menu
	 */
	public void addAction(IAction action) {
		actions.add(action);
	}

	/**
	 * Adds new action to drop down menu.
	 *
	 * @param parent
	 *            Current drop down menu
	 * @param action
	 *            Action to add drop down menu
	 */
	protected void addActionToMenu(Menu parent, IAction action) {
		// create new contribution item
		ActionContributionItem item = new ActionContributionItem(action);

		item.fill(parent, -1);
	}

	/**
	 * Creates file open menu.
	 */
	public final void createFileOpenMenu() {

		// add default item to menu
		// analyze results
		IAction analyzeResults = new Action() {
			@Override
			public void run() {
				parentClass.analyzeDataFile(Constants.ANALYZE_ASK_FOR_USER,
						null, true);
			}
		};

		analyzeResults.setText(Constants.ACTION_OPEN_DOTS);
		analyzeResults.setToolTipText(Constants.ACTION_OPEN_DOTS);
		addActionToMenu(ddMenu, analyzeResults);

		// add separator to menu
		new MenuItem(ddMenu, SWT.SEPARATOR);

		// get history of opened files
		AbstractList<String> openedFiles = parentClass.fileOpenHistory
				.getItems();

		if (openedFiles.isEmpty()) {
			Action tmpAction = new Action() {
				@Override
				public void run() {
					// no nothing by design
				}
			};

			tmpAction.setText(Constants.NO_OPENED_FILES);
			tmpAction.setEnabled(false);
			addActionToMenu(ddMenu, tmpAction);
		} else {
			// thru opened files
			final Iterator<String> iterOpenedFiles = openedFiles.iterator();
			while (iterOpenedFiles.hasNext()) {
				// get one history file
				String oneFile = iterOpenedFiles.next();

				// create new action
				FileAction tmpAction = new FileAction() {
					@Override
					public void run() {
						parentClass.analyzeDataFile(
								Constants.ANALYZE_USE_DATA_FILE, super
										.getFileLocation(), true);
					}
				};
				tmpAction.setFileLocation(oneFile);

				addActionToMenu(ddMenu, tmpAction);
				tmpAction.setToolTipText(oneFile);
				tmpAction.setText(parseTextName(oneFile));
			}
		}
	}

	/**
	 * Disposes current menu
	 *
	 * @see org.eclipse.jface.action.IMenuCreator#dispose()
	 */
	public void dispose() {
		if (ddMenu != null) {
			ddMenu.dispose();
			ddMenu = null;
		}
	}

	/**
	 * Gets menu contents.
	 *
	 * @param parent Parent of this menu
	 *
	 * @return Reference of menu
	 */
	public Menu getMenu(Control parent) {
		// if menu is open => close it
		if (ddMenu != null) {
			ddMenu.dispose();
		}

		// create new menu
		ddMenu = new Menu(parent);

		// if this menu is file open menu
		if (isFileMenu) {
			createFileOpenMenu();
		} else {
			// get iterator of menu actions
			java.util.Iterator<IAction> iterActions = actions.iterator();

			// thru action
			while (iterActions.hasNext()) {

				// get one action
				IAction oneAction = iterActions.next();

				// if no need to check actions => just add it to menu
				if (!checkActions) {
					addActionToMenu(ddMenu, oneAction);
					continue;
				}

				// file location
				String targetPath = "";

				// get project reference
				IProject project = parentClass.project;

				// if action text equals to save data file and project exists
				if (Constants.ACTION_SAVE_DATA.equals(oneAction.getText())
						&& project != null && project.isOpen()) {
					// get data file location
					targetPath = Util.getBldInfFolder(project, false)
							+ Constants.FILENAME;
					java.io.File file = new java.io.File(targetPath);

					// check file exists
					if (!file.exists()) {
						// is data file opened by user
						String openedDataFile = parentClass.usedDataFileName;

						// if no data file opened => set data file name to ""
						if (openedDataFile == null) {
							openedDataFile = "";
						}

						// create file and if it exists set targetpath
						java.io.File s60File = new java.io.File(openedDataFile);
						if (s60File.exists()) {
							targetPath = openedDataFile;
						}
					}
				} else if (Constants.ACTION_SAVE_REPORT.equals(oneAction
						.getText())
						&& project != null && project.isOpen()) {
					targetPath = Util.getBldInfFolder(project, false)
							+ Constants.FILENAME_CARBIDE;

				}

				// if file exisits enable this action otherwise disable
				java.io.File file = new java.io.File(targetPath);
				if (file.exists()) {
					oneAction.setEnabled(true);
				} else {
					oneAction.setEnabled(false);
				}
				// add action to menu
				addActionToMenu(ddMenu, oneAction);

			}

		}

		return ddMenu;
	}

	/**
	 * Returns menu.
	 *
	 * @return Returns reference of this menu
	 */
	public Menu getMenu(Menu parent) {
		return null;
	}

	/**
	 * Parsing given name if it is too long.
	 *
	 * @param name
	 *            File name
	 * @return Parsed file name if it is too long otherwise the actual file name
	 */
	public final String parseTextName(String name) {
		int howManyChar = 100;
		if (name.length() > howManyChar) {
			StringBuffer tmpBuff = new StringBuffer();
			tmpBuff.append("...");
			tmpBuff.append(name.substring(name.length() - howManyChar, name
					.length()));
			return tmpBuff.toString();
		}
		return name;
	}

	@Override
	public void run() {

		// get menu
		getMenu(parentClass.runView.getControl());

		// set menu visible
		ddMenu.setVisible(true);

	}
}
