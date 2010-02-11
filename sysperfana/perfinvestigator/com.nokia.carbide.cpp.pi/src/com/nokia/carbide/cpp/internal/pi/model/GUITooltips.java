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

package com.nokia.carbide.cpp.internal.pi.model;

public class GUITooltips {

	private static String absolutePercentage = Messages.getString("GUITooltips.percentagesInFunctionList"); //$NON-NLS-1$
	private static String addButton = Messages.getString("GUITooltips.addSelectedToGraph"); //$NON-NLS-1$
	private static String addObyOrBinary = Messages.getString("GUITooltips.addObyAndIby"); //$NON-NLS-1$
	private static String bothButton = Messages.getString("GUITooltips.showTotalMemory"); //$NON-NLS-1$
	private static String browseAnalyseEXEButton = Messages.getString("GUITooltips.locationOfAnalyse.Exe"); //$NON-NLS-1$
	private static String browseDatButton = Messages.getString("GUITooltips.tempFilesFolder"); //$NON-NLS-1$
	private static String browseMMCButton = Messages.getString("GUITooltips.MMCCardDriveLetter"); //$NON-NLS-1$
	private static String browseSymbolButton = Messages.getString("GUITooltips.locationOfROMSymbolFile"); //$NON-NLS-1$
	private static String callerLoadButton = Messages.getString("GUITooltips.sortByCallerLoad"); //$NON-NLS-1$
	private static String clearSelectedBinariesButton = Messages.getString("GUITooltips.clearsBinaryListSelections"); //$NON-NLS-1$
	private static String closeTabButton = Messages.getString("GUITooltips.closeTab"); //$NON-NLS-1$
	private static String configTab = Messages.getString("GUITooltips.toolConfiguration"); //$NON-NLS-1$
	private static String deeperKernelAnalyseButton = Messages.getString("GUITooltips.deeperKernelAnalyseButton"); //$NON-NLS-1$
	private static String endTextField = Messages.getString("GUITooltips.setEndTime"); //$NON-NLS-1$
	private static String fileNewButton = Messages.getString("GUITooltips.combineTraceAndStreamFiles"); //$NON-NLS-1$
	private static String functionAnalyseButton = Messages.getString("GUITooltips.doSelectedFunctionAnalysis"); //$NON-NLS-1$
	private static String heapButton = Messages.getString("GUITooltips.showHeapMemory"); //$NON-NLS-1$
	private static String kernelAnalyseButton = Messages.getString("GUITooltips.useOnlyWithKRNFile"); //$NON-NLS-1$
	private static String linkedFunctionAnalyseButton = Messages.getString("GUITooltips.useOnlyWithCallTrace"); //$NON-NLS-1$
	private static String loadAlreadyCreatedAnalysisButton = Messages.getString("GUITooltips.openExistingBapFile"); //$NON-NLS-1$
	private static String minusButton = Messages.getString("GUITooltips.zoomIn"); //$NON-NLS-1$
	private static String minusMinusButton = Messages.getString("GUITooltips.showSelection"); //$NON-NLS-1$
	private static String newFileAnalysisButton = Messages.getString("GUITooltips.openTraceFilesFromFileSystem"); //$NON-NLS-1$
	private static String openAllFilesButton = Messages.getString("GUITooltips.openTraceFilesFromLocation"); //$NON-NLS-1$
	private static String openCusFileButton = Messages.getString("GUITooltips.openCustomTraceFile"); //$NON-NLS-1$
	private static String openDSPFileButton = Messages.getString("GUITooltips.openDSPTraceFile"); //$NON-NLS-1$
	private static String openGFCFileButton = Messages.getString("GUITooltips.openCallTraceFile"); //$NON-NLS-1$
	private static String openGPPFileButton = Messages.getString("GUITooltips.openAddressThreadTraceFile"); //$NON-NLS-1$
	private static String openISAFileButton = Messages.getString("GUITooltips.openISAMapFile"); //$NON-NLS-1$
	private static String openITTFileButton = Messages.getString("GUITooltips.openInstrTraceFile"); //$NON-NLS-1$
//	private static String openKRNFileButton = "Open KRN trace file, enables kernel analysis option";
	private static String openMEMFileButton = Messages.getString("GUITooltips.openMemoryPriorityTraceFile"); //$NON-NLS-1$
	private static String openTIPFileButton = Messages.getString("GUITooltips.openTIPTraceFile"); //$NON-NLS-1$
	private static String plusButton = Messages.getString("GUITooltips.zoomOut"); //$NON-NLS-1$
	private static String plusPlusButton = Messages.getString("GUITooltips.showGraph"); //$NON-NLS-1$
	private static String previousSelectionButton = Messages.getString("GUITooltips.goBack"); //$NON-NLS-1$
	private static String readMMCCardButton = Messages.getString("GUITooltips.openTraceFilesFromMMCCard"); //$NON-NLS-1$
	private static String recolorButton = Messages.getString("GUITooltips.recolor"); //$NON-NLS-1$
	private static String recursiveLoadButton = Messages.getString("GUITooltips.sortByRecursiveLoad"); //$NON-NLS-1$
	private static String removeButton = Messages.getString("GUITooltips.removeSelectedFromGraph"); //$NON-NLS-1$
	private static String removeObyOrBinary = Messages.getString("GUITooltips.removeSelectedFromList"); //$NON-NLS-1$
	private static String renameTab = Messages.getString("GUITooltips.renameTab"); //$NON-NLS-1$
	private static String reportButton = Messages.getString("GUITooltips.generateReportForInterval"); //$NON-NLS-1$
	private static String rofsSettingsButton = Messages.getString("GUITooltips.configureInstrTrace"); //$NON-NLS-1$
	private static String saveAnalysisButton = Messages.getString("GUITooltips.saveAnalysis"); //$NON-NLS-1$
	private static String saveConfiguration = Messages.getString("GUITooltips.saveConfiguration"); //$NON-NLS-1$
	private static String saveIttSettings = Messages.getString("GUITooltips.saveSettings"); //$NON-NLS-1$
	private static String saveListScreenshotButton = Messages.getString("GUITooltips.saveListScreenshot"); //$NON-NLS-1$
	private static String saveScreenshotButton = Messages.getString("GUITooltips.saveGraphScreenshot"); //$NON-NLS-1$
	private static String selectButton = Messages.getString("GUITooltips.selectTimeInterval"); //$NON-NLS-1$
	private static String selectionTabButton = Messages.getString("GUITooltips.createNewAnalysis"); //$NON-NLS-1$
	private static String serialAnalyseButton = Messages.getString("GUITooltips.getTracesViaSerialPort"); //$NON-NLS-1$
	private static String setOriginalThreads = Messages.getString("GUITooltips.selectAddressThreadItems"); //$NON-NLS-1$
	private static String setReleaseRootLetter = Messages.getString("GUITooltips.rootLetterOfEpoc32Folder"); //$NON-NLS-1$
	private static String showFunctionInformationButton = Messages.getString("GUITooltips.showSamplesAndStatistics"); //$NON-NLS-1$
	private static String stackButton = Messages.getString("GUITooltips.showStackMemory"); //$NON-NLS-1$
	private static String startTextField = Messages.getString("GUITooltips.setStartTime"); //$NON-NLS-1$
	private static String threadList = Messages.getString("GUITooltips.selectThreads"); //$NON-NLS-1$
	private static String totalLoadButton = Messages.getString("GUITooltips.sortByTotalLoad"); //$NON-NLS-1$
	private static String usePrimarilyItt = Messages.getString("GUITooltips.resolveByITTFirst"); //$NON-NLS-1$
	private static String usePrimarilySymbol = Messages.getString("GUITooltips.resolveBySymbolFileFirst"); //$NON-NLS-1$
	private static String visualiserPanel = Messages.getString("GUITooltips.dragMouse"); //$NON-NLS-1$
	private static String xminusDeepButton = Messages.getString("GUITooltips.xminusDeepButton"); //$NON-NLS-1$
	private static String xplusDeepButton = Messages.getString("GUITooltips.xplusDeepButton"); //$NON-NLS-1$
	private static String yminusButton = Messages.getString("GUITooltips.yminusButton"); //$NON-NLS-1$
	private static String yminusDeepButton = Messages.getString("GUITooltips.yminusDeepButton"); //$NON-NLS-1$
	private static String yminusminusButton = Messages.getString("GUITooltips.yminusminusButton"); //$NON-NLS-1$
	private static String yplusButton = Messages.getString("GUITooltips.yplusButton"); //$NON-NLS-1$
	private static String yplusDeepButton = Messages.getString("GUITooltips.yplusDeepButton"); //$NON-NLS-1$
	private static String yplusplusButton = Messages.getString("GUITooltips.yplusplusButton"); //$NON-NLS-1$

	private static boolean tooltipsOn = true;

	public GUITooltips(){}
	
	// set whether to return a message or a null for the tooltips value
	public static void setTooltipsOn(boolean tooltipsOn){
		GUITooltips.tooltipsOn = tooltipsOn;
	}

	/**
	 * return a message or null, depending on tooltipsOn
	 */

	public static String getAbsolutePercentage() {
		return tooltipsOn ? absolutePercentage : null;
	}

	public static String getAddObyOrBinary() {
		return tooltipsOn ? addObyOrBinary : null;
	}

	public static String getAddButton() {
		return tooltipsOn ? addButton : null;
	}

	public static String getBothButton() {
		return tooltipsOn ? bothButton : null;
	}

	public static String getBrowseAnalyseEXEButton() {
		return tooltipsOn ? browseAnalyseEXEButton : null;
	}

	public static String getBrowseDatButton() {
		return tooltipsOn ? browseDatButton : null;
	}

	public static String getBrowseMMCButton() {
		return tooltipsOn ? browseMMCButton : null;
	}

	public static String getBrowseSymbolButton() {
		return tooltipsOn ? browseSymbolButton : null;
	}

	public static String getCallerLoadButton() {
		return tooltipsOn ? callerLoadButton : null;
	}

	public static String getClearSelectedBinariesButton() {
		return tooltipsOn ? clearSelectedBinariesButton : null;
	}

	public static String getCloseTabButton() {
		return tooltipsOn ? closeTabButton : null;
	}

	public static String getConfigTab() {
		return tooltipsOn ? configTab : null;
	}

	public static String getDeeperKernelAnalyseButton() {
		return tooltipsOn ? deeperKernelAnalyseButton : null;
	}

	public static String getEndTextField() {
		return tooltipsOn ? endTextField : null;
	}

	public static String getFileNewButton() {
		return tooltipsOn ? fileNewButton : null;
	}

	public static String getFunctionAnalyseButton() {
		return tooltipsOn ? functionAnalyseButton : null;
	}

	public static String getHeapButton() {
		return tooltipsOn ? heapButton : null;
	}

	public static String getKernelAnalyseButton() {
		return tooltipsOn ? kernelAnalyseButton : null;
	}

	public static String getLinkedFunctionAnalyseButton() {
		return tooltipsOn ? linkedFunctionAnalyseButton : null;
	}
	
	public static String getLoadAlreadyCreatedAnalysisButton() {
		return tooltipsOn ? loadAlreadyCreatedAnalysisButton : null;
	}

	public static String getMinusButton() {
		return tooltipsOn ? minusButton : null;
	}

	public static String getMinusMinusButton() {
		return tooltipsOn ? minusMinusButton : null;
	}

	public static String getNewFileAnalysisButton() {
		return tooltipsOn ? newFileAnalysisButton : null;
	}

	public static String getOpenAllFilesButton() {
		return tooltipsOn ? openAllFilesButton : null;
	}
	
	public static String getOpenCusFileButton() {
		return tooltipsOn ? openCusFileButton : null;
	}	

	public static String getOpenDSPFileButton() {
		return tooltipsOn ? openDSPFileButton : null;
	}

	public static String getOpenGFCFileButton() {
		return tooltipsOn ? openGFCFileButton : null;
	}

	public static String getOpenGPPFileButton() {
		return tooltipsOn ? openGPPFileButton : null;
	}
	
	public static String getOpenISAFileButton() {
		return tooltipsOn ? openISAFileButton : null;
	}

	public static String getOpenITTFileButton() {
		return tooltipsOn ? openITTFileButton : null;
	}
	
	//kernel analysis is not planned
	/*
	public static String getOpenKRNFileButton() {
		return tooltipsOn ? openKRNFileButton : null;
	}*/

	public static String getOpenMEMFileButton() {
		return tooltipsOn ? openMEMFileButton : null;
	}

	public static String getOpenTIPFileButton() {
		return tooltipsOn ? openTIPFileButton : null;
	}

	public static String getPlusButton() {
		return tooltipsOn ? plusButton : null;
	}

	public static String getPlusPlusButton() {
		return tooltipsOn ? plusPlusButton : null;
	}

	public static String getPreviousSelectionButton() {
		return tooltipsOn ? previousSelectionButton : null;
	}

	public static String getReadMMCCardButton() {
		return tooltipsOn ? readMMCCardButton : null;
	}

	public static String getRecolorButton() {
		return tooltipsOn ? recolorButton : null;
	}

	public static String getRecursiveLoadButton() {
		return tooltipsOn ? recursiveLoadButton : null;
	}

	public static String getRemoveObyOrBinary() {
		return tooltipsOn ? removeObyOrBinary : null;
	}

	public static String getRemoveButton() {
		return tooltipsOn ? removeButton : null;
	}

	public static String getRenameTab()
    {
	   return tooltipsOn ? renameTab : null;
    }

	public static String getReportButton() {
		return tooltipsOn ? reportButton : null;
	}

	public static String getRofsSettingsButton() {
		return tooltipsOn ? rofsSettingsButton : null;
	}

	public static String getSaveAnalysisButton() {
		return tooltipsOn ? saveAnalysisButton : null;
	}

	public static String getSaveConfiguration() {
		return tooltipsOn ? saveConfiguration : null;
	}

	public static String getSaveIttSettings() {
		return tooltipsOn ? saveIttSettings : null;
	}

	public static String getSaveListScreenshotButton() {
		return tooltipsOn ? saveListScreenshotButton : null;
	}

	public static String getSaveScreenshotButton() {
		return tooltipsOn ? saveScreenshotButton : null;
	}

	public static String getSelectButton() {
		return tooltipsOn ? selectButton : null;
	}

	public static String getSelectionTabButton() {
		return tooltipsOn ? selectionTabButton : null;
	}

	public static String getSerialAnalyseButton() {
		return tooltipsOn ? serialAnalyseButton : null;
	}

	public static String getSetOriginalThreads() {
		return tooltipsOn ? setOriginalThreads : null;
	}

	public static String getSetReleaseRootLetter() {
		return tooltipsOn ? setReleaseRootLetter : null;
	}

	public static String getShowFunctionInformationButton() {
		return tooltipsOn ? showFunctionInformationButton : null;
	}

	public static String getStackButton() {
		return tooltipsOn ? stackButton : null;
	}

	public static String getStartTextField() {
		return tooltipsOn ? startTextField : null;
	}

	public static String getThreadList() {
		return tooltipsOn ? threadList : null;
	}

	public static String getTotalLoadButton() {
		return tooltipsOn ? totalLoadButton : null;
	}

	public static String getUsePrimarilyItt() {
		return tooltipsOn ? usePrimarilyItt : null;
	}

	public static String getUsePrimarilySymbol() {
		return tooltipsOn ? usePrimarilySymbol : null;
	}

	public static String getVisualiserPanel() {
		return tooltipsOn ? visualiserPanel : null;
	}

	public static String getXminusDeepButton() {
		return tooltipsOn ? xminusDeepButton : null;
	}

	public static String getXplusDeepButton() {
		return tooltipsOn ? xplusDeepButton : null;
	}

	public static String getYminusButton() {
		return tooltipsOn ? yminusButton : null;
	}

	public static String getYminusDeepButton() {
		return tooltipsOn ? yminusDeepButton : null;
	}

	public static String getYminusminusButton() {
		return tooltipsOn ? yminusminusButton : null;
	}

	public static String getYplusButton() {
		return tooltipsOn ? yplusButton : null;
	}

	public static String getYplusDeepButton() {
		return tooltipsOn ? yplusDeepButton : null;
	}

	public static String getYplusplusButton() {
		return tooltipsOn ? yplusplusButton : null;
	}

	/**
	 *  Set tooltip strings
	 */

	public static void setAbsolutePercentage(String absolutePercentage) {
		GUITooltips.absolutePercentage = absolutePercentage;
	}

	public static void setAddObyOrBinary(String addObyOrBinary) {
		GUITooltips.addObyOrBinary = addObyOrBinary;
	}

	public static void setAddButton(String addButton) {
		GUITooltips.addButton = addButton;
	}

	public static void setBothButton(String bothButton) {
		GUITooltips.bothButton = bothButton;
	}

	public static void setBrowseAnalyseEXEButton(String browseAnalyseEXEButton) {
		GUITooltips.browseAnalyseEXEButton = browseAnalyseEXEButton;
	}

	public static void setBrowseDatButton(String browseDatButton) {
		GUITooltips.browseDatButton = browseDatButton;
	}

	public static void setBrowseMMCButton(String browseMMCButton) {
		GUITooltips.browseMMCButton = browseMMCButton;
	}

	public static void setBrowseSymbolButton(String browseSymbolButton) {
		GUITooltips.browseSymbolButton = browseSymbolButton;
	}

	public static void setCallerLoadButton(String callerLoadButton) {
		GUITooltips.callerLoadButton = callerLoadButton;
	}

	public static void setClearSelectedBinariesButton(
			String clearSelectedBinariesButton) {
		GUITooltips.clearSelectedBinariesButton = clearSelectedBinariesButton;
	}

	public static void setCloseTabButton(String closeTabButton) {
		GUITooltips.closeTabButton = closeTabButton;
	}

	public static void setConfigTab(String configTab) {
		GUITooltips.configTab = configTab;
	}

	public static void setDeeperKernelAnalyseButton(
			String deeperKernelAnalyseButton) {
		GUITooltips.deeperKernelAnalyseButton = deeperKernelAnalyseButton;
	}

	public static void setEndTextField(String endTextField) {
		GUITooltips.endTextField = endTextField;
	}

	public static void setFileNewButton(String fileNewButton) {
		GUITooltips.fileNewButton = fileNewButton;
	}

	public static void setFunctionAnalyseButton(String functionAnalyseButton) {
		GUITooltips.functionAnalyseButton = functionAnalyseButton;
	}

	public static void setHeapButton(String heapButton) {
		GUITooltips.heapButton = heapButton;
	}

	public static void setKernelAnalyseButton(String kernelAnalyseButton) {
		GUITooltips.kernelAnalyseButton = kernelAnalyseButton;
	}

	public static void setLinkedFunctionAnalyseButton(String linkedFunctionAnalyseButton) {
		GUITooltips.linkedFunctionAnalyseButton = linkedFunctionAnalyseButton;
	}

	public static void setLoadAlreadyCreatedAnalysisButton(String loadAlreadyCreatedAnalysisButton) {
		GUITooltips.loadAlreadyCreatedAnalysisButton = loadAlreadyCreatedAnalysisButton;
	}

	public static void setMinusButton(String minusButton) {
		GUITooltips.minusButton = minusButton;
	}

	public static void setMinusMinusButton(String minusMinusButton) {
		GUITooltips.minusMinusButton = minusMinusButton;
	}

	public static void setNewFileAnalysisButton(String newFileAnalysisButton) {
		GUITooltips.newFileAnalysisButton = newFileAnalysisButton;
	}

	public static void setOpenAllFilesButton(String openAllFilesButton) {
		GUITooltips.openAllFilesButton = openAllFilesButton;
	}

	public static void setOpenCusFileButton(String openCusFileButton) {
		GUITooltips.openCusFileButton = openCusFileButton;
	}	

	public static void setOpenDSPFileButton(String openDSPFileButton) {
		GUITooltips.openDSPFileButton = openDSPFileButton;
	}

	public static void setOpenGFCFileButton(String openGFCFileButton) {
		GUITooltips.openGFCFileButton = openGFCFileButton;
	}

	public static void setOpenGPPFileButton(String openGPPFileButton) {
		GUITooltips.openGPPFileButton = openGPPFileButton;
	}

	public static void setOpenISAFileButton(String openISAFileButton) {
		GUITooltips.openISAFileButton = openISAFileButton;
	}

	public static void setOpenITTFileButton(String openITTFileButton) {
		GUITooltips.openITTFileButton = openITTFileButton;
	}

	//kernel analysis is not planned
	/*
	public static void setOpenKRNFileButton(String openKRNFileButton) {
		GUITooltips.openKRNFileButton = openKRNFileButton;
	}*/

	public static void setOpenMEMFileButton(String openMEMFileButton) {
		GUITooltips.openMEMFileButton = openMEMFileButton;
	}

	public static void setOpenTIPFileButton(String openTIPFileButton) {
		GUITooltips.openTIPFileButton = openTIPFileButton;
	}

	public static void setPlusButton(String plusButton) {
		GUITooltips.plusButton = plusButton;
	}

	public static void setPlusPlusButton(String plusPlusButton) {
		GUITooltips.plusPlusButton = plusPlusButton;
	}

	public static void setPreviousSelectionButton(String previousSelectionButton) {
		GUITooltips.previousSelectionButton = previousSelectionButton;
	}

	public static void setReadMMCCardButton(String readMMCCardButton) {
		GUITooltips.readMMCCardButton = readMMCCardButton;
	}

	public static void setRecolorButton(String recolorButton) {
		GUITooltips.recolorButton = recolorButton;
	}

	public static void setRecursiveLoadButton(String recursiveLoadButton) {
		GUITooltips.recursiveLoadButton = recursiveLoadButton;
	}

	public static void setRemoveObyOrBinary(String removeObyOrBinary) {
		GUITooltips.removeObyOrBinary = removeObyOrBinary;
	}

	public static void setRemoveButton(String removeButton) {
		GUITooltips.removeButton = removeButton;
	}

	public static void setRenameTab(String renameTab)
    {
        GUITooltips.renameTab = renameTab;
    }

	public static void setReportButton(String reportButton) {
		GUITooltips.reportButton = reportButton;
	}

    public static void setRofsSettingsButton(String rofsSettingsButton) {
		GUITooltips.rofsSettingsButton = rofsSettingsButton;
	}

	public static void setSaveAnalysisButton(String saveAnalysisButton) {
		GUITooltips.saveAnalysisButton = saveAnalysisButton;
	}

	public static void setSaveConfiguration(String saveConfiguration) {
		GUITooltips.saveConfiguration = saveConfiguration;
	}

	public static void setSaveIttSettings(String saveIttSettings) {
		GUITooltips.saveIttSettings = saveIttSettings;
	}

	public static void setSaveListScreenshotButton(String saveListScreenshotButton) {
		GUITooltips.saveListScreenshotButton = saveListScreenshotButton;
	}

	public static void setSaveScreenshotButton(String saveScreenshotButton) {
		GUITooltips.saveScreenshotButton = saveScreenshotButton;
	}

	public static void setSelectButton(String selectButton) {
		GUITooltips.selectButton = selectButton;
	}

	public static void setSelectionTabButton(String selectionTabButton) {
		GUITooltips.selectionTabButton = selectionTabButton;
	}

	public static void setSerialAnalyseButton(String serialAnalyseButton) {
		GUITooltips.serialAnalyseButton = serialAnalyseButton;
	}

	public static void setSetOriginalThreads(String setOriginalThreads) {
		GUITooltips.setOriginalThreads = setOriginalThreads;
	}

	public static void setSetReleaseRootLetter(String setReleaseRootLetter) {
		GUITooltips.setReleaseRootLetter = setReleaseRootLetter;
	}

	public static void setShowFunctionInformationButton(String showFunctionInformationButton) {
		GUITooltips.showFunctionInformationButton = showFunctionInformationButton;
	}

	public static void setStackButton(String stackButton) {
		GUITooltips.stackButton = stackButton;
	}

	public static void setStartTextField(String startTextField) {
		GUITooltips.startTextField = startTextField;
	}

	public static void setThreadList(String threadList) {
		GUITooltips.threadList = threadList;
	}

	public static void setTotalLoadButton(String totalLoadButton) {
		GUITooltips.totalLoadButton = totalLoadButton;
	}

	public static void setUsePrimarilyItt(String usePrimarilyItt) {
		GUITooltips.usePrimarilyItt = usePrimarilyItt;
	}

	public static void setUsePrimarilySymbol(String usePrimarilySymbol) {
		GUITooltips.usePrimarilySymbol = usePrimarilySymbol;
	}

	public static void setVisualiserPanel(String visualiserPanel) {
		GUITooltips.visualiserPanel = visualiserPanel;
	}

	public static void setXminusDeepButton(String xminusDeepButton) {
		GUITooltips.xminusDeepButton = xminusDeepButton;
	}

	public static void setXplusDeepButton(String xplusDeepButton) {
		GUITooltips.xplusDeepButton = xplusDeepButton;
	}

	public static void setYminusButton(String yminusButton) {
		GUITooltips.yminusButton = yminusButton;
	}

	public static void setYminusDeepButton(String yminusDeepButton) {
		GUITooltips.yminusDeepButton = yminusDeepButton;
	}

	public static void setYminusminusButton(String yminusminusButton) {
		GUITooltips.yminusminusButton = yminusminusButton;
	}

	public static void setYplusButton(String yplusButton) {
		GUITooltips.yplusButton = yplusButton;
	}

	public static void setYplusDeepButton(String yplusDeepButton) {
		GUITooltips.yplusDeepButton = yplusDeepButton;
	}

	public static void setYplusplusButton(String yplusplusButton) {
		GUITooltips.yplusplusButton = yplusplusButton;
	}
}
