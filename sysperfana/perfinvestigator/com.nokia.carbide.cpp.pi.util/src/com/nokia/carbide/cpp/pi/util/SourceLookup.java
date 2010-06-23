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

package com.nokia.carbide.cpp.pi.util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import com.nokia.carbide.cdt.builder.CarbideBuilderPlugin;
import com.nokia.carbide.cdt.builder.EpocEngineHelper;
import com.nokia.carbide.cdt.builder.project.ICarbideBuildConfiguration;
import com.nokia.carbide.cdt.builder.project.ICarbideProjectInfo;

public class SourceLookup {
	private static SourceLookup instance = null;
	
	public static SourceLookup getInstance() {
		if (instance == null){
			instance = new SourceLookup();
		}
		return instance;
	}
	
	private SourceLookup () {
		// singleton
	}
	
	public void lookupAndopenEditorWithHighlight(final String symbolName, final String binaryName) {
		final IASTFileLocation[] locations = lookupLocations(symbolName, binaryName);
		final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		if (locations.length <= 0) {
			GeneralMessages.showErrorMessage(Messages.getString("SourceLookup.notfound") + "\n" + symbolName); //$NON-NLS-1$	//$NON-NLS-2$
			return;
		}
		
		if (locations.length > 1) {
			final SourceLookupFileChooserDialog dialog = new SourceLookupFileChooserDialog(shell, locations);
			if (dialog.open() == Window.OK && dialog.getLocation() != null) {
				openEditorWithHighlight(dialog.getLocation());
			}
		} else {
			openEditorWithHighlight(locations[0]);
		}
	}
	
	private void openEditorWithHighlight(final IASTFileLocation location) {
		final IPath path = new Path(location.getFileName());
		final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);

		if (file == null) {
			return;
		}
		
		IEditorPart editor = null;

		try {
			editor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		} catch (PartInitException e) {
			e.printStackTrace();
		}

		if (editor != null && editor instanceof ITextEditor) {
			final ITextEditor textEditor = (ITextEditor)editor;
			int nodeOffset = location.getNodeOffset();
			int nodeLength = location.getNodeLength();
			int offset;
			int length;
			if (nodeLength == -1) {
				// This means the offset is actually a line number
				try {
					IDocument document = textEditor.getDocumentProvider().getDocument(editor.getEditorInput());
					offset = document.getLineOffset(nodeOffset);
					length = document.getLineLength(nodeOffset);
				} catch (BadLocationException e) {
					CUIPlugin.getDefault().log(e);
					return;
				}
		} else {
				offset = nodeOffset;
				length = nodeLength;
		}
		
			textEditor.selectAndReveal(offset, length);
		}
	}
	
	private boolean typeEqual(final String param, final IType type) {

		class TypeAttribute {
			private transient boolean isSigned = true;	// Symbian still default to signed
			private transient boolean isConst = true;
			private transient String typeWithoutModifier = "";	//$NON-NLS-1$
			
			TypeAttribute(final String typeString) {
				// In general we don't not have to care about too much, just trim out
				// signed/unsigned/const modifier and take note. We take the rest of them
				// and compare them after removing spaces
				String[] typeStringSplit = typeString.split("[\t ]");	 //$NON-NLS-1$
				int index = 0;
				for (String chunk : typeStringSplit) {
					if (chunk.equals("signed")) {	//$NON-NLS-1$
						continue;
					} else if (chunk.equals("unsigned")) {	//$NON-NLS-1$
						isSigned = false;
					}
					if (index++ > 0 && chunk.equals("*") == false) {	//$NON-NLS-1$ // take out space before * consistently
						typeWithoutModifier += " "; //$NON-NLS-1$
					}
					typeWithoutModifier += chunk;
				}
				
				typeWithoutModifier = typeWithoutModifier.replaceAll("long int", "long"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			private boolean isSameType(TypeAttribute attribute) {
				if (isSigned == attribute.isSigned &&
						isConst == attribute.isConst &&
						typeWithoutModifier != null &&
						attribute.typeWithoutModifier != null &&
						typeWithoutModifier.equals(attribute.typeWithoutModifier)) {
					return true;
				}
				return false;
			}
		}
		TypeAttribute cdtTypeAttribute = new TypeAttribute(ASTTypeUtil.getType(type));
		TypeAttribute paramTypeAttribute = new TypeAttribute(param);
		
		return cdtTypeAttribute.isSameType(paramTypeAttribute);
	}
	
	private IIndexBinding[] findBindingsForSignature(String Signature, IIndex index) {
		ArrayList<IIndexBinding> bindingArrayList = new ArrayList<IIndexBinding>();
		ArrayList<String> signaturesArrayList = new ArrayList<String>();
		IIndexBinding[] bindings = null;
		boolean needMatchingArg = true;
		
		// drop everything after )
		if (Signature.indexOf(')') > 0 ) {	//$NON-NLS-1$
			Signature = Signature.substring(0, Signature.indexOf(')'));	//$NON-NLS-1$
		}
		
		String[] signatureSplit = Signature.split("[(),]"); //$NON-NLS-1$
		for (String chunks: signatureSplit) {
			chunks = chunks.trim();
		}
		
		if (signatureSplit[0].contains(" ") || signatureSplit[0].contains("\t") || //$NON-NLS-1$ //$NON-NLS-2$
				(signatureSplit[0].equals(Signature.trim()))) {	//$NON-NLS-1$	//$NON-NLS-2$
			// -some RVCT function doesn't have arguments e.g. memset
			// in the form of function_name<some space>lib.in(...)
			// -std::nowthrow
			signaturesArrayList.add(signatureSplit[0].split("[\t ]")[0]);	//$NON-NLS-1$
			needMatchingArg = false;
		} else {
			// regular C++ functions/methods
			for (String signature : signatureSplit) {
				signaturesArrayList.add(signature);
			}
		}
		
		try {
			index.acquireReadLock();
			bindings = index.findBindings(Pattern.compile(signaturesArrayList.get(0)), false, IndexFilter.getFilter(ILinkage.CPP_LINKAGE_ID), new NullProgressMonitor());
			if (bindings.length < 1) {
				bindings = index.findBindings(Pattern.compile(signaturesArrayList.get(0)), false, IndexFilter.getFilter(ILinkage.C_LINKAGE_ID), new NullProgressMonitor());
			}
			if (bindings.length < 1) {
				bindings = index.findBindings(Pattern.compile(signaturesArrayList.get(0)), false, IndexFilter.getFilter(ILinkage.NO_LINKAGE_ID), new NullProgressMonitor());
			}
			index.releaseReadLock();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		if (bindings != null) {

			for (IIndexBinding binding : bindings) {
				if (binding instanceof IFunction) {
					IFunction function = (IFunction) binding;
					if (needMatchingArg) {
						boolean signatureMatch = true;
						try {
							IParameter[] params = function.getParameters();
								if (params == null && signaturesArrayList.size() > 1) {	// no args, function name only
									signatureMatch = false;	// # of arg differs
								} else if (params.length == 1 && signaturesArrayList.size() == 1) {
									// () is return as (void) in CDT
									if (ASTTypeUtil.getType(params[0].getType()).equals("void") == false) {	//$NON-NLS-1$
										signatureMatch = false;	// # of args differs
									}
								} else if (params.length != signaturesArrayList.size() -1) {
								signatureMatch = false;	// # of args differs
								} else {
									for (int i = 0; i < params.length; i++) {
										if (typeEqual(signaturesArrayList.get(i + 1), params[i].getType()) == false) {
										signatureMatch = false;
									}
								}
							}
							if (signatureMatch) {
								bindingArrayList.add(binding);
							}
						} catch (DOMException e) {
							e.printStackTrace();
						}
					} else {
						bindingArrayList.add(binding);
					}
				}	
			}
		}

		return bindingArrayList.toArray(new IIndexBinding[0]);
	}
	
	private IASTFileLocation[] lookupLocations(String symbolName, String binaryName) {
		ArrayList<IASTFileLocation> locations = new ArrayList<IASTFileLocation>();
		ArrayList<IProject> projectsMatchExe = new ArrayList<IProject>();
		ArrayList<IProject> projectsNotMatchExe = new ArrayList<IProject>();
		
//		 just look up project that give the matching binary if there is binaryName
		if (binaryName != null)
		{
			IProject[] projects= ResourcesPlugin.getWorkspace().getRoot().getProjects();
			
			for (IProject project: projects) {
				if (!CarbideBuilderPlugin.getBuildManager().isCarbideProject(project)) {
					continue;
				}
				
				ICarbideProjectInfo cpi = CarbideBuilderPlugin.getBuildManager().getProjectInfo(project);
				if (cpi == null){
					continue;
				}
				
				List<ICarbideBuildConfiguration> allConfig = cpi.getBuildConfigurations();
				
				if (allConfig == null){
					continue;
				}
				
				boolean isEligibleProject = false;
				for (ICarbideBuildConfiguration config : allConfig) {
					String projectExePath = EpocEngineHelper.getPathToMainExecutable(config);
					if (projectExePath != null && projectExePath.length() > 0) {
						String projectFileName = new java.io.File(projectExePath).getName().toLowerCase();
						String binaryFileName = new java.io.File(binaryName).getName().toLowerCase();

						if (projectFileName.equals(binaryFileName)) {
							isEligibleProject = true;
						}
					}
				}
				
				if (isEligibleProject) {
					projectsMatchExe.add(project);
				} else {
					projectsNotMatchExe.add(project);
				}
			}
						
			for (IProject project : projectsMatchExe) {
				ICProject cProject = CoreModel.getDefault().getCModel().getCProject(project.getName());
				try {
					IIndex index = CCorePlugin.getIndexManager().getIndex(cProject);
					IASTFileLocation[] locationsFound = lookupLocationsFromIndex(symbolName, index);
					if (locationsFound != null) {
						for (IASTFileLocation location : locationsFound) {
							locations.add(location);
						}
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
		
//		optionally look among all projects if not found
		if (locations.size() == 0) {
			for (IProject project : projectsNotMatchExe) {
				ICProject cProject = CoreModel.getDefault().getCModel().getCProject(project.getName());
		try {
					IIndex index = CCorePlugin.getIndexManager().getIndex(cProject);
					IASTFileLocation[] locationsFound = lookupLocationsFromIndex(symbolName, index);
					if (locationsFound != null) {
						for (IASTFileLocation location : locationsFound) {
							locations.add(location);
						}
					}
		} catch (CoreException e) {
			e.printStackTrace();
		}
			}			
		}
		
		return locations.toArray(new IASTFileLocation[0]);
	}
	
	private IASTFileLocation[] lookupLocationsFromIndex(String symbolName, IIndex index) {
		IIndexBinding[] bindings = new IIndexBinding[0];
		ArrayList<IASTFileLocation> locations = new ArrayList<IASTFileLocation>();
		
		// split by scoping operating ::, we will look for namespace and class later
		String[] scopingOperatorSplit = symbolName.split ("::"); //$NON-NLS-1$
		for (String chunks: scopingOperatorSplit) {
			chunks = chunks.trim();
		}
		
		// last element in scoping operator split is the function signature
		bindings = findBindingsForSignature(scopingOperatorSplit[scopingOperatorSplit.length - 1], index);
		
		if (bindings.length > 0) {
			for (IIndexBinding binding : bindings) {
				try {
					
					index.acquireReadLock();
					
					boolean match = true;
					if (binding instanceof ICPPFunction) {
						ICPPFunction cppFunction = (ICPPFunction) binding;
						String[] cdtQualifiedName = cppFunction.getQualifiedName();
						if (scopingOperatorSplit.length == cdtQualifiedName.length) {
							match = true;
							// match namepace, class etc if there is, skip the function name which we
							// already matched in reading back binding
							for (int i = 0; i < cdtQualifiedName.length - 1; i++) {
								if (scopingOperatorSplit[i].equals(cdtQualifiedName[i]) == false) {
									match = false;
									break;
								}			
							}
						} else {
							match = false;
						}
					} else if (binding instanceof IFunction) {
						if (scopingOperatorSplit.length > 1) {
							match = false;
						}
					}
					
					if (match) {
						IIndexName[] defs= index.findDefinitions(binding);
						for (IIndexName def : defs) {
							locations.add(def.getFileLocation());
						}
						// we could end up not having any definition
						// that is just because that code doesn't exist
						// e.g. constructor is not exposed
						// let's try pointing out the reference as it is
						// useful for PI
						if (locations.size() < 1) {
							IIndexName[] refs = index.findReferences(binding);
							for (IIndexName ref : refs) {
								locations.add(ref.getFileLocation());
							}
						}
					}
					
					index.releaseReadLock();
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (CoreException e) {
					e.printStackTrace();
				} catch (DOMException e) {
					e.printStackTrace();
				}
			}
		}
		
		return locations.toArray(new IASTFileLocation[locations.size()]);
	}
/*	
	void experiment()
	{
		IQuickParseCallback quickParseCallback = ParserFactory.createQuickParseCallback();
		IParser parser = ParserFactory.createParser( 
				ParserFactory.createScanner(new CodeReader("long int foo;".toCharArray()), 
						new ScannerInfo(),
						ParserMode.QUICK_PARSE,
						ParserLanguage.CPP,
						quickParseCallback,
						null,
						null ),
				quickParseCallback, 
				ParserMode.QUICK_PARSE, 
				ParserLanguage.CPP, 
				null );

	}
*/
}
