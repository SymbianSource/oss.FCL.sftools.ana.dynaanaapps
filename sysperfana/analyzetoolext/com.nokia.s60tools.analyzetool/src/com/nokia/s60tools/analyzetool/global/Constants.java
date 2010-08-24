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
 * Description:  Definitions for the class Constants
 *
 */

package com.nokia.s60tools.analyzetool.global;

/**
 * Contains all the defined constants
 * 
 * @author kihe
 * 
 */
public class Constants {

	public static enum ACTIONS {
		RUN_VIEW_MEM_LEAKS, RUN_BUILD, RUN_CLEAN
	}

	public static enum Operation {
		PCS, PCE, ALH, ALF, FRH, FRF, RAH, RAF, DLL, DLU, TDS, TDE, VER, TSS, TSE, DEVINFO, NOVALUE;

		public static Operation toOperation(String str) {
			try {
				return valueOf(str);
			} catch (Exception ex) {
				return NOVALUE;
			}
		}
	}

	public static final String ATOOL_LIBS_OK = "atool_libs_ok";

	/** AnalyzeTool specific constants */
	public final static String ANALYZE_TOOL_TITLE = "AnalyzeTool";
	public final static String ANALYZE_TOOL_TITLE_WITH_VERSION = "AnalyzeTool v%s";
	public final static String MAIN_TAB_TITLE = "Results";
	public final static String ANALYZE_TOOL_VIEW_ID = "com.nokia.s60tools.analyzetool.ui.MainView";
	public final static String ANALYZE_TOOL_PREFS_ID = "com.nokia.s60tools.analyzetool.preferences.AnalyzeToolPreferencePage";
	public final static String DBGHELPDLL_IS_UP_TO_DATE = "dbghelp.dll is up to date";
	/**
	 * Progress monitor text displayed when AnalyzeTool processing some action
	 * long time
	 */
	public final static String PROGRESSDIALOG_TITLE = "Processing analysis data";
	public final static String PROGRESSDIALOG_ATOOL = "Executing AnalyzeTool";
	public final static String PROGRESSDIALOG_CLEAR_CHANGES = "Cleaning changes made by AnalyzeTool";

	public final static String PROGRESSDIALOG_CLEAN_COMPLETE = "Changes made by AnalyzeTool cleaned";
	public static final String PROGRESSDIALOG_ANALYZE_COMPLETE = "Analysis complete";
	/** AnalyzeTool toolbar actions text */
	public final static String ACTION_AT_BUILD_ACTIVE = "Deactivate AnalyzeTool build - currently activated";
	public final static String ACTION_AT_BUILD_DEACTIVE = "Activate AnalyzeTool build - currently deactivated";
	public final static String ACTION_CLEAR_CHANGES = "Clean AnalyzeTool changes";
	public final static String ACTION_CLEAR_CHANGES_TOOLTIP = "Clean all changes made by AnalyzeTool (delete atool_temp folder(s) etc.)";
	public final static String ACTION_CHANGE_REPORT_LEVEL = "Change report detail level";
	public final static String ACTION_CHANGE_REPORT_LEVEL_ALL = "Report detail level: All";
	public final static String ACTION_CHANGE_REPORT_LEVEL_KNOWN = "Report detail level: Known";
	public final static String ACTION_CHANGE_REPORT_LEVEL_TOPMOST = "Report detail level: Topmost";
	public final static String ACTION_CHANGE_LOGGING_MODE = "Change data output mode";

	public static final String PREFS_EXT_FAST = "Output to trace (recommended)";
	public static final String PREFS_EXT_FAST_TOOLTIP = "Output data to the host computer through external connection.";
	public static final String PREFS_S60 = "Output to file system";
	public static final String PREFS_S60_TOOLTIP = "Output data to a file in the target device.";
	public static final String PREFS_ASK_ALWAYS = "Ask always";
	public static final String PREFS_ASK_ALWAYS_TOOLTIP = "Ask data output mode from the user every time when building.";

	public final static String ACTION_CHANGE_LOGGING_MODE_TOOLTIP_FAST = "Current mode: "
			+ PREFS_EXT_FAST;
	public final static String ACTION_CHANGE_LOGGING_MODE_TOOLTIP_S60 = "Current mode: "
			+ PREFS_S60;
	public final static String ACTION_CHANGE_LOGGING_MODE_TOOLTIP_ASK = "Current mode: "
			+ PREFS_ASK_ALWAYS;

	public final static String ACTION_SAVE = "Save raw test run data or memory leak results";
	public final static String ACTION_SAVE_REPORT = "Save memory leak results";
	public final static String ACTION_SAVE_DATA = "Save raw test run data";
	public final static String ACTION_START_SUBTEST = "Start subtest";
	public final static String ACTION_STOP_SUBTEST = "Stop subtest";
	public static final String ACTION_START_TRACE = "Start tracing";
	public static final String ACTION_STOP_TRACE = "Stop tracing";
	public static final String ACTION_OPEN = "Open and analyze data file";
	public static final String ACTION_OPEN_DOTS = "Open and analyze data file...";
	public static final String ACTION_CLEAR_RESULTS = "Clear project results";
	public static final String ACTION_RE_ANALYZE = "Re-analyze results";
	public static final String ACTION_RE_ANALYZE_TOOLTIP = "You can re-analyze recently opened file.";
	public static final String ACTION_COPY = "Copy to clipboard";
	public static final String ACTION_OPEN_PREFS = "Open AnalyzeTool preferences";
	public static final String ACTION_OPEN_PREFS_TOOLTIP = "Open AnalyzeTool preferences";
	/** Dialog related text */
	// File save dialog title when user saves project results (XML file)
	public final static String DIALOG_SAVE_REPORT = "Save memory leak results";

	// File save dialog title when user save test run data ( data file )
	public final static String DIALOG_SAVE_TRACE = "Save raw test run data";

	// Selection dialog title when user builds project with AnalyzeTool and
	// logging mode is set to "Ask always"
	public final static String DIALOG_SELECT_LOGGING_MODE = "Select data output mode for AnalyzeTool";

	// generic title for all dialogs, information notes
	public final static String DIALOG_TITLE = "AnalyzeTool";

	// File selection dialog title when user wants to open and analyze data file
	public static final String DIALOG_SELECT_DATA_FILE = "Select data file";

	// Input dialog title when user inputs data file name
	public static final String DIALOG_INPUT_DATA_FILE_NAME = "Enter device data file name";

	/**
	 * Information dialog text in these dialogs user has possible to press just
	 * "Ok"
	 */

	// when trying to save project results
	public static final String INFO_NO_RESULTS_FILE = "No results file found in the current project.";

	// when trying to save test run data
	public static final String INFO_NO_DATA_FILE = "No data file found in the current project.";

	// after the file is saved
	public static final String INFO_SAVE_SUCCESS = "The file successfully saved to: ";

	// information text when user try to use actions and the atool.exe can not
	// be executed
	public static final String INFO_ATOOL_NOT_AVAILABLE = "\n\nCommand Line Engine not available. \nCheck AnalyzeTool preferences.";
	public static final String ERROR_ATOOL_NOT_AVAILABLE = "Command Line Engine not available. Check AnalyzeTool preferences.";

	// info to user if AnalyzeTool for some reason can create results
	public static final String INFO_FILE_INVALID = "Can not analyze. \nNo data to be analyzed.";

	// when user try use AnalyzeTool without selection project first
	public static final String NO_PROJ_SELECT = "No project selected. \nPlease select a project.";

	// if user try to open data file for the project and AnalyzeTool already
	// processing another data file.
	public static final String INFO_ALLREADY_RUNNING = "AnalyzeTool is already processing previously selected file. Try again later.";

	/** Confirmation dialog */
	// when user wants to clean AnalyzeTool made changes
	public static final String CONFIRM_DELETE_ALL = "This option deletes all temporary files created by AnalyzeTool. \n\n Do you want to continue?";

	// when starting the trace and there are data file available
	public static final String CONFIRM_OVERWRITE_FILE = "The project already contains a data file that will be overwritten. \nDo you want to save it first?";

	// when user select folder which does not contain atool.exe via AnalyzeTool
	// preference pages.
	public static final String CONFIRM_DIR_DOES_NOT_CONTAIN_ATOOL = "Directory does not contain atool.exe. \n\nDo you want to continue?";

	/** Tree model text */
	// when no results are opened/available
	public static final String INFO_NO_DATA_FILE_AVAILABLE = "No data file opened or trace data captured.";
	/** Preference page title */
	public static final String ATOOL_DESC = "AnalyzeTool Carbide extension configuration";
	/** Trace capturing related text */
	public static final String INFO_NO_DATA = "No data";
	public static final String SUBTEST_INPUT_NAME = "Enter subtest name.";
	public static final String SUBTEST_NO_PROCESSES = "No processes started, could not start a subtest.";
	public static final String SUBTEST_SELECT_TARGET = "Select the target.";
	public static final String SUBTEST_RUNNING_PROCESSES_INFO = "Running processes";
	public static final String SUBTEST_NO_SUBTESTS = "No subtests started.";
	public static final String SUBTEST_SELECT_SUBTEST_TO_STOP = "Select which subtest should be stopped.";
	public static final String SUBTEST_ALLREADY_RUNNING = "The given subtest already started for the active process.";
	public static final String SUBTEST_STARTED = "Subtest started: ";
	public static final String SUBTEST_ENDED = "Subtest ended: ";
	public static final String INFO_TRACE_START = "Trace started.";
	public static final String INFO_TRACE_STOP = "Trace stopped.";
	public static final String INFO_TRACE_FROM_TARGET_START = "Trace from %s started.";
	public static final String INFO_TRACE_FROM_EMULATOR = "emulator";
	public static final String INFO_TRACE_FROM_DEVICE = "device";
	public static final String INFO_ALLOCATED_MEM = "Number of memory allocations: ";
	public static final String NO_OPENED_FILES = "No recently opened files.";
	public static final String STARTING_TRACE = "Starting trace capture";
	/** TraceViewer error information */
	public static final String TRACE_ALLREADY_CONNECTED = "TraceViewer connection reserved for another plug-in.";
	public static final String TRACE_GENERAL_ERROR = "General TraceViewer error.";
	public static final String TRACE_CON_SET_ERROR = "Invalid TraceViewer connection settings.";
	public static final String TRACE_CANT_FIND_DATAPR = "Cannot find dataprocessor. \nPlease contact the support team.";
	public static final String TRACE_ERROR_NONE = "";
	public static final String TRACE_DISC_ERROR = "Error while disconnecting TraceViewer.";
	public static final String TRACE_NOT_FOUND = "Could not load TraceViewer plugin.";

	/** Main view tree model related constants */
	public static final String HANDLE_LEAK_MODULES_TITLE = "Modules with handle leaks";
	public static final String MEMORY_LEAK_MODULES_TITLE = "Modules with memory leaks";
	public static final String TEST_RUNS_TREE_TITLE = "Test runs";
	public static final String TREE_TITLE = "runs";
	public static final String RUN_TREE_RUN = "Run: ";
	public static final String RUN_TREE_RUN_MEM_LEAKS = " Memory Leaks: ";
	public static final String RUN_TREE_RUN_HANDLE_LEAKS = " Handle leaks: ";
	public static final String RUN_TREE_START_TIME = " Start time: ";
	public static final String RUN_TREE_PROCESS_NAME = " Process name: ";
	public static final String RUN_TREE_BUILD_TARGET = " Build target: ";
	public static final String RUN_TREE_FILTERED = " filtered)";
	public static final String MODULE_TREE_MEM_LEAKS = " memory leaks";
	public static final String MODULE_TREE_HANDLE_LEAKS = " handle leaks";
	public static final String ITEM_TREE_MEM_LEAKS = "Memory leak ";
	public static final String SUBTEST_TREE_TITLE = "Subtest: ";
	public static final String NO_MEM_LEAKS_CURRENT_LEVEL = "No memory leaks info available for current detail level. Try to change report detail level.";
	public static final String RUN_FAILED = " FAILED ";
	public static final String RUN_ABNORMAL = "Abnormal process end";
	public static final String RUN_NO_LEAKS = "No memory leaks.";

	public static final String CANCELLED = "Cancelled.";

	/** Error information if AnalyzeTool libraries are not installed */
	public static final String CAN_NOT_FIND_LIBRARIES_SUPPORT = "You can build AnalyzeTool from sources or contact AnalyzeTool development team to get AnalyzeTool binaries.";

	public static final String CAN_NOT_FIND_LIBRARIES_MARKER = "Can not find AnalyzeTool library files from current SDK:";

	/** If user try to build with unsupported platform */
	public static final String PLATFORM_NOT_SUPPORTED = "AnalyzeTool supports only ARMV5, GCCE and WINSCW build platforms. \n\nDo you want to continue?";

	public static final String TOO_OLD_ENGINE = "Current version of AnalyzeTool Engine is too old. \nRequired version must be 1.6.0 or higher.\n\nCheck AnalyzeTool preferences.";

	public static final String MIN_CLE_SUPPORTED = "1.10.0";
	public static final String CLE_OLDER_THAN_MIN = "Command Line Engine older than {0}. Check AnalyzeTool preferences.";
	public static final String CLE_VERSION_MISMATCH = "Version mismatch";

	public static final String AT_BINARIES_VERSION_MISMATCH = "Version mismatch between the AnalyzeTool binaries ({0}) in the device and AnalyzeTool headers in the SDK ({1}).\nPlease make sure that those match otherwise results may be unknown.";

	public static final String UNSUPPORTED_FORMAT_TITLE = "Unsupported format";
	public static final String UNSUPPORTED_FORMAT_MESSAGE = "Symbian side components are not up to date. Please see help for more information.";

	public static final String TRACE_FORMAT_VERSION_IS_HIGHER = "Carbide extension is older than Symbian components. Please see help for more information.";

	/**
	 * Error text when trying to import unknown module to the workspace and
	 * module could not be found
	 */
	public static final String CAN_NOT_FIND_MODULE = "Can not find module from the active SDK";

	public static final String BUILD_STATE_CHANGED = "Project build state changed. \nDo you want to re-analyze results?";

	public static final String BUILD_CANCELLED = "\n\nAnalyzeTool build cancelled.";

	public static final String BUILD_AND_INSTRUMENT = "Instrument and build with AnalyzeTool";

	public static final String COMPLETE = "Complete";

	public static final String OUTPUT_READER_TITLE = "AnalyzeTool - emulator output reader";

	public static final String INPUT_ILLEGAL = "Illegal character";

	public static final String INPUT_TOO_LONG = "Data file name is too long.";

	public static final String INPUT_NO_SPACES_ALLOWED = "No spaces allowed";

	public static final String MAIN_CAN_NOT_COPY = "Cannot copy: \n";

	/**
	 * Preference page constants
	 */
	public static final String PREFS_USER_SPEC = "Name from the user";
	public static final String PREFS_USER_SPEC_TOOLTIP = "Ask the file name from the user when building.";
	public static final String PREFS_USE_PROCESS_NAME = "Name from the process";
	public static final String PREFS_USE_PROCESS_NAME_TOOLTIP = "Use the process name as the name of the file.";
	public static final String PREFS_ATOOL_VER_NOT_FOUND = "Not available";
	public static final String PREFS_ATOOL_GROUP_TITLE = "AnalyzeTool Engine";
	public static final String PREFS_USE_INTERNAL_TITLE = "Use internal command line engine";
	public static final String PREFS_USE_EXTERNAL_TITLE = "Use external command line engine";
	public static final String PREFS_SELECT_FOLDER = "Choose the atool.exe directory";
	public static final String PREFS_ATOOL_PATH = "&Atool.exe path:";
	public static final String PREFS_BROWSE = "Browse...";
	public static final String PREFS_VERBOSE = "Verbose output";
	public static final String PREFS_VERBOSE_TOOLTIP = "Verbose output to Console View.";
	public static final String PREFS_ENGINE_VERSION = "Engine version: ";
	public static final String PREFS_ADVANCED = "Advanced settings";
	public static final String PREFS_USE_ROM_SYMBOL = "Use rom symbol file";
	public static final String PREFS_USE_ROM_SYMBOL_TOOLTIP = "Use rom symbol file to pinpoint rom locations.";
	public static final String PREFS_ROM_SYMBOL_PATH = "Rom symbol file:";
	public static final String PREFS_ROM_SYMBOL_PATH_TOOLTIP = "Define which rom symbol file to use.";
	public static final String PREFS_SELECT_ROM_SYMBOL = "Choose the rom symbol file.";
	public static final String PREFS_REFRESH_VERSION = "Refresh version";
	public static final String PREFS_REPORT_LEVEL = "Report level";
	public static final String PREFS_SHOW_EVERY = "&Show every detail";
	public static final String PREFS_SHOW_KNOWN = "Show only known code lines (default)";
	public static final String PREFS_SHOW_TOPMOST = "Show only topmost memory allocation code line";
	public static final String PREFS_SELECT_DIR = "Select folder";
	public static final String PREFS_CSSIZE_TITLE = "Callstack size";
	public static final String PREFS_ZERO_BUTTON = "No callstack stored";
	public static final String PREFS_FORTY_BUTTON = "40 items";
	public static final String PREFS_HUNDRED_BUTTON = "100 items (Slows down test run a lot)";
	public static final String PREFS_CUSTOM_BUTTON = "Custom size (0-255)";
	public static final String PREFS_CS_SIZE_DISABLED_TOOLTIP = "Command line engine version is too old, version must be 1.7.4 or higher";
	public static final String PREFS_CLE_NOT_AVAILABLE = "Command Line Engine not available.";
	public static final String PREFS_CLE_OLDER_THAN_MIN = "Command Line Engine older than {0}.";

	/** Statistics view constants */
	public static final String STATISTICS_TAB_TITLE = "Top allocation locations";
	public static final String STATISTICS_SELECT_RUN = "Select run";
	public static final String STATISTICS_NODE_FILE = "File";
	public static final String STATISTICS_NODE_FUNCTION = "Function";
	public static final String STATISTICS_NODE_LINE = "Line";
	public static final String STATISTICS_NODE_ALLOCS = "Allocations";
	public static final String STATISTICS_NODE_TIME = "Time";
	public static final String STATISTICS_NODE_SIZE = "Size";
	public static final String STATISTICS_GENERATING = "Generating statistics. Please wait...";
	public static final String STATISTICS_GENERATING_PROG_TITLE = "Generating statistics";
	public static final String GRAPH_GENERATING_PROG_TITLE = "Generating graph model";
	public static final String GRAPH_LOAD_JOB_TITLE = "AnalyzeTool Loading Graph Data...";
	public static final String STATISTICS_NO_STATS = "No statistics available.";

	public static final String FIND_COMP_JOB_TITLE = "Finding component locations";
	public static final String FIND_COMP_JOB_SELECT_MODULE = "Select module";
	// UI RELATED CONSTANTS END HERE

	public static final String SOURCE_NOT_FOUND = "Source file not found from any project.";

	public static final String SOURCE_FILE_EDITOR_ID = "org.eclipse.jdt.ui.SourceView";
	/** Plug-in id */
	public static final String PLUGINID = "com.nokia.s60tools.analyzetool";

	/** Parser id's to command launcher */
	public static final String[] atoolParserIds = new String[] { "com.nokia.s60tools.analyzetool" };

	/** File name which is used when saving captured data thru TraceViewer */
	public static final String FILENAME = "AtoolDataFile.dat";

	/** File name which is used when generating memory analysis results */
	public static final String FILENAME_CARBIDE = "AtoolFileToCarbide.xml";

	/** Prefix of the old data trace format */
	public final static String PREFIX_OLD = "PCSS";

	/** Prefix to find */
	public final static String PREFIX = "<AT>";

	/** Process start string */
	public static final String PCS = "PCS";

	/** Process end string */
	public static final String PCE = "PCE";

	/** Allocation */
	public static final String ALH = "ALH";
	public static final String ALF = "ALF";

	/** Free */
	public static final String FRH = "FRH";
	public static final String FRF = "FRF";

	/** Reallocation */
	public static final String RAH = "RAH";
	public static final String RAF = "RAF";

	/** DLL */
	public static final String DLL = "DLL";
	public static final String DLU = "DLU";

	/** Thread */
	public static final String TDS = "TDS";
	public static final String TDE = "TDE";

	/** Version */
	public static final String VER = "VER";

	/** Subtests */
	public static final String TSS = "TSS";
	public static final String TSE = "TSE";

	/** Device info */
	public static final String DEVINFO = "DEVINFO";

	/** Memory allocation deallocation flag definitions */
	public static final int TYPE_ALLOC = 0;
	public static final int TYPE_FREE = 1;
	public static final int TYPE_ALLOCH = 2;
	public static final int TYPE_FREEH = 3;

	/** Button icon definitions */
	public static final String BUTTON_RUN = "icons/btn_record.png";
	public static final String BUTTON_STOP = "icons/btn_terminate.png";
	public static final String BUTTON_BUILD = "icons/btn_build.png";
	public static final String BUTTON_CLEAN = "icons/btn_clear.png";
	public static final String BUTTON_COMPUTER = "icons/btn_computer.png";
	public static final String BUTTON_COMPUTER_FAST = "icons/btn_fast.png";
	public static final String BUTTON_CELLURAR = "icons/btn_cellular.png";
	public static final String BUTTON_ASK = "icons/btn_ask.png";
	public static final String BUTTON_OPEN = "icons/btn_open.png";
	public static final String BUTTON_DETAILS_ALL = "icons/btn_details_all.png";
	public static final String BUTTON_DETAILS_KNOWN = "icons/btn_details_known.png";
	public static final String BUTTON_DETAILS_TOPMOST = "icons/btn_details_topmost.png";
	public static final String BUTTON_SAVE = "icons/btn_save.png";
	public static final String BUTTON_START_SUBTEST = "icons/btn_start_subtest.png";
	public static final String BUTTON_STOP_SUBTEST = "icons/btn_stop_subtest.png";
	public static final String BUTTON_OPEN_PREFS = "icons/open_prefs.png";
	public static final String ICON_OUTSIDE = "icons/module_outside.png";
	public static final String ICON_NOT_BUILD = "icons/module_not_build.png";
	public static final String ICON_BUILD = "icons/module_build.png";

	/** Data file definitions */
	public static final int DATAFILE_LOG = 0;
	public static final int DATAFILE_TRACE = 1;
	public static final int DATAFILE_XML = 2;
	public static final int DATAFILE_BINARY = 3;

	public static final int DATAFILE_INVALID = -1;
	public static final int DATAFILE_EMPTY = -2;
	public static final int DATAFILE_OLD_FORMAT = -3;
	public static final int DATAFILE_UNSUPPORTED_TRACE_FORMAT = -4;

	public static final String DATAFILE_VERSION = "DATA_FILE_VERSION";
	public static final String BINARY_FILE_VERSION = "BINARY_FILE_VERSION";
	public static final String ATOOL_TEMP = "atool_temp";
	public static final String ATOOL_FOLDER = "atool_folder";
	public static final String LOGGING_MODE = "logging_mode";
	public static final String S60_LOG_FILE_MODE = "s60_log_file_mode";
	public static final String USER_SELECTED_FOLDER = "user_selected";
	public static final String CREATE_STATISTIC = "create_stats";
	public static final String USE_ROM_SYMBOL = "use_rom_symbol";
	public static final String USE_ROM = "use_rom";
	public static final String ROM_LOC = "rom_loc";
	public static final String USE_ROM_SYMBOL_LOCATION = "rom_symbol_location";
	public static final String CALLSTACK_SIZE = "callstack_size";
	public static final String USE_CALLSTACK_SIZE = "use_user_define_cs_size";
	public static final String REPORT_LEVEL = "report_level";
	public static final String ATOOL_VERBOSE = "verbose_atool";

	public static final String DEVICE_LOG_FILE_PATH = "device_log_file_path";
	public static final String DEVICE_LOG_FILE_NAME = "device_log_file_name";

	/** Logging mode preference values */
	public static final String LOGGING_EXT_FAST = "EXT_FAST";
	public static final String LOGGING_S60 = "S60";
	public static final String LOGGING_ASK_ALLWAYS = "ask_always";
	public static final String LOGGING_FAST_ENABLED = "logging_fast_enabled";

	/** report level preference values */
	public static final String REPORT_EVERY = "every_details";

	public static final String REPORT_KNOWN = "known_lines";
	public static final String REPORT_TOPMOST = "topmost_lines";

	/** S60 logging mode data file name */
	public static final String LOGGING_S60_PROCESS_NAME = "process_name";
	public static final String LOGGING_S60_USER_SPECIFIED = "user_secified";

	/** Save report file types */
	public static final int SAVE_REPORT_FILE_DATA = 0;
	public static final int SAVE_REPORT_FILE_XML = 1;

	public static final String BUILD_TARGET_WINSCW = "WINSCW";
	public static final String BUILD_TARGET_ARMV5 = "ARMV5";
	public static final String BUILD_TARGET_GCEE = "GCCE";

	/** Default preference values */
	public static final String DEFAULT_ATOOL_FOLDER = "c:\\apps\\atool\\";

	public static final String DEFAULT_LOGGING_MODE = Constants.LOGGING_EXT_FAST;
	public static final String DEFAULT_REPORT_LEVEL = Constants.REPORT_KNOWN;
	public static final String PREFS_LOGGING_MODE_TITLE = "Output mode";

	public static final int ANALYZE_ASK_FOR_USER = 0;
	public static final int ANALYZE_USE_DATA_FILE = 1;

	public static final String ANALYZE_CONSOLE_ID = "Memory Analysis ID";
	public static final String ENRULE = " - ";

	public static final int SAVE_DATA_FILE = 0;
	public static final int SAVE_DATA_FILE_NO = 1;
	public static final int SAVE_DATA_FILE_CANCEL = 2;

	public static final String ATOOL_INST = "-inst";
	public static final String ATOOL_INST_EF = "-instrument_ef";
	public static final String ATOOL_INST_I = "-instrument_i";

	public static final String ATOOL_UNINST = "-uninstrument";
	public static final String ATOOL_USE_VARIANT = "-variant";
	public static final String ATOOL_UNINST_FAILED = "-uninstrument_failed";
	public static final String ATOOL_SHOW_DEBUG = "-show_debug";
	public static final String USE_INTERNAL = "use_internal";
	public static final String CALLSTACK_SIZE_OPTION = "-acs";

	public static final int MAX_LENGTH_OF_USER_INPUT = 50;

	public static final int HISTORY_LEVEL = 5;

	// version number comparison constants
	public static final int VERSION_NUMBERS_INVALID = -1;
	public static final int VERSION_NUMBERS_FIRST = 0;
	public static final int VERSION_NUMBERS_SECOND = 1;
	public static final int VERSION_NUMBERS_EQUALS = 2;

	public static final String UNKNOWN = "Unknown";

	public static final String PREFS_KEEP_IN_SYNC = "keepFilesSync";
	public static final String PREFS_PROMPT_MMP = "promptMMPChanges";
	public static final String PREFS_MANAGE_DEPS = "manageDeps";
	public static final String PREFS_CONC_BUILD = "useConcBuild";
	public static final String PREFS_BUILD_CANCELLED = "buildCancelled";

	// List of libraries what AnalyzeTool needs when compiled applications on
	// armv5 platform
	public static final String atoolLibs[] = {
			"epoc32\\RELEASE\\armv5\\LIB\\AToolMemoryHook.lib",
			"epoc32\\RELEASE\\armv5\\udeb\\AtoolStaticLib.lib",
			"epoc32\\RELEASE\\armv5\\urel\\AtoolStaticLib.lib" };

	// List of libraries what AnalyzeTool needs when compiled applications on
	// armv5 platform (using sbs2 / ABIV2 binaries)
	public static final String atoolLibsSbs2[] = {
			"epoc32\\RELEASE\\armv5\\LIB\\AToolMemoryHook.dso",
			"epoc32\\RELEASE\\armv5\\udeb\\AtoolStaticLib.lib",
			"epoc32\\RELEASE\\armv5\\urel\\AtoolStaticLib.lib" };

	// List of libraries what AnalyzeTool needs when compiled applications on
	// winscw platform
	public static final String atoolLibsWinscw[] = {
			"epoc32\\RELEASE\\winscw\\udeb\\AToolMemoryHook.lib",
			"epoc32\\RELEASE\\winscw\\udeb\\AtoolStaticLib.lib",
			"epoc32\\RELEASE\\winscw\\urel\\AtoolStaticLib.lib" };

	public static enum COMMAND_LINE_ERROR_CODE {
		EXECUTE_ERROR(-1), OK(0),
		/* instrument errors */
		INVALID_ARGUMENT_ERROR(1), CANNOT_FIND_EPOCROOT(3), MAKEFILE_ERROR(5), NO_SUPPORTED_MODULES_ERROR(
				8),
		/* Analyze errors */
		WRONG_DATA_FILE_VERSION(10), ANALYZE_ERROR(12), EMPTY_DATA_FILE(13), SYMBOL_FILE_ERROR(
				14), DATA_FILE_EMPTY(31), DATA_FILE_INVALID(32), DATA_FILE_OLD_FORMAT(
				33), DATA_FILE_UNSUPPORTED_TRACE_FORMAT(34),
		/* building&releasing errors */
		RELEASABLES_ERROR(20), RESTORE_MODULES_ERROR(21), CREATING_TEMP_CPP_ERROR(
				22), CLEANING_TEMP_ERROR(23), READ_MAKEFILE_ERROR(24), MODIFY_MODULES_ERROR(
				25), INVALID_MMP_DEFINED(27),

		/* User issued exit */
		UNKNOWN_ERROR(999);
		private final int code;

		COMMAND_LINE_ERROR_CODE(int c) {
			code = c;
		}

		public int getCode() {
			return code;
		}
	}

	static final String AT_CORE_INCLUDE_FILE_WITH_VERSION_NUMBER[] = {
			"epoc32\\include\\domain\\osextensions\\analyzetool\\analyzetool.h",
			"epoc32\\include\\oem\\analyzetool\\analyzetool.h",
			"epoc32\\include\\platform\\analyzetool\\analyzetool.h" };

	static final String AT_CORE_VERSION_NUMBER_TAG = "ANALYZETOOL_CORE_VERSION_FOR_CLE";
}
