/*
 * Copyright (c) 2008-2010 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description:  Definitions for the class PreNature
 *
 */

package com.nokia.s60tools.analyzetool.builder;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

/**
 * Class to implement pre-builder nature.
 *
 * @author kihe
 *
 */
public class PreNature implements IProjectNature {

	/** ID of pre-builder project nature. */
	public static final String NATURE_ID = "com.nokia.s60tools.analyzetool.preNature";

	/** Project reference. */
	private IProject project;

	/**
	 * Sets AnalyzeTool builder to project description
	 *
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException {
		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();

		int qtBuilderIndex = 0;
		boolean qtBuildeFound = false;

		//thru existing builders
		for (int i = 0; i < commands.length; ++i) {
			//if AT builder id is already addred => leave
			if (commands[i].getBuilderName().equals(
					AnalyzeToolBuilder.AT_BUILDER_ID)) {
				return;
			}
			//if current builder is QT builder => save location
			else if(commands[i].getBuilderName().equals(com.trolltech.qtcppproject.QtProConstants.QTBUILDER_ID)) {
				qtBuilderIndex = i;
				qtBuildeFound = true;
			}
		}

		//create new commands
		ICommand[] newCommands = new ICommand[commands.length + 1];

		//create new command(AT command)
		ICommand command = desc.newCommand();
		command.setBuilderName(AnalyzeToolBuilder.AT_BUILDER_ID);

		//QT nature id found => now start to copy existing id and add AT builder id
		if( qtBuildeFound ) {

			if( qtBuilderIndex == 0 ) {
				newCommands[0] = commands[0];
				newCommands[1] = command;
				System.arraycopy(commands, 1, newCommands, 2, commands.length-1);
			}
			else {
				System.arraycopy(commands, 0, newCommands, 0, qtBuilderIndex+1);
				newCommands[qtBuilderIndex+1] =command;
				System.arraycopy(commands, qtBuilderIndex+1, newCommands, qtBuilderIndex+2, commands.length-qtBuilderIndex);
			}
		}
		//no qt builder found => add AT builder to first of the builder list
		else {
			newCommands[0] = command;
			System.arraycopy(commands, 0, newCommands, 1, commands.length);

		}

		//update project description
		desc.setBuildSpec(newCommands);
		project.setDescription(desc, null);
	}

	/**
	 * Removes AnalyzeTool builder from the project description
	 *
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {
		IProjectDescription description = getProject().getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(
					AnalyzeToolBuilder.AT_BUILDER_ID)) {
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i,
						commands.length - i - 1);
				description.setBuildSpec(newCommands);
				project.setDescription(description, null);
				return;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public IProject getProject() {
		return project;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject projectRef) {
		this.project = projectRef;
	}

}
