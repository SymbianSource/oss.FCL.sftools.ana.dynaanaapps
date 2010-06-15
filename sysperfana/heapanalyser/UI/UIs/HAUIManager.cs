/*
* Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies).
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* - Redistributions of source code must retain the above copyright notice,
*   this list of conditions and the following disclaimer.
* - Redistributions in binary form must reproduce the above copyright notice,
*   this list of conditions and the following disclaimer in the documentation
*   and/or other materials provided with the distribution.
* - Neither the name of Nokia Corporation nor the names of its contributors
*   may be used to endorse or promote products derived from this software
*   without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
* POSSIBILITY OF SUCH DAMAGE.
* 
* Initial Contributors:
* Nokia Corporation - initial contribution.
*
* Contributors:
*
* Description: 
*
*/

using System;
using System.IO;
using System.Drawing;
using System.Collections;
using System.ComponentModel;
using System.Windows.Forms;
using System.Data;
using System.Text;
using SymbianUtils.Tracer;
using SymbianUtils.Settings;
using SymbianUtils.FileSystem.Utilities;
using SymbianUtils.PluginManager;
using SymbianUtilsUi.Dialogs;
using HeapLib;
using HeapUiLib.Forms;
using HeapAnalyser.Engine;
using HeapAnalyser.Engine.Types;
using HeapAnalyser.Exceptions;

namespace HeapAnalyser.UIs
{
    public class HAUIManager : ApplicationContext, ITracer
	{
		#region Constructors
        public HAUIManager( string[] aArgs )
		{
			// Create settings
            iSettings = new XmlSettings( KHASettingsFileName );
			iSettings.Restore();

			// Create engine
            iEngine = new HeapWizardEngine( Application.StartupPath, aArgs );

            // Find UIs from this assembly
            iUIs.LoadFromCallingAssembly( new object[] { aArgs, iSettings, iEngine, this } );
        
            // Listen to when the application exits so that we can save the settings
            Application.ApplicationExit += new EventHandler( Application_ApplicationExit );
        }
		#endregion

		#region API
        public int Run()
        {
            int err = HAUIException.KErrCommandLineNone;
            //
            try
            {
                // Find appropriate UI to run.
                HAUI uiToRun = null;
                //
                foreach ( HAUI ui in iUIs )
                {
                    bool isHandler = ui.IsAppropriateUI;
                    if ( isHandler )
                    {
                        uiToRun = ui;
                        break;
                    }
                }

                // We must have one UI
                if ( uiToRun == null )
                {
                    throw new HAUIException( "Unable to find UI", HAUIException.KErrCommandLineUINotAvailable );
                }

                iRunningUI = uiToRun;

                // Request that the UI create a form.
                Form mainForm = iRunningUI.PrepareInitialForm();
                base.MainForm = mainForm;

                // Start our message loop if we received a main form
                if ( base.MainForm != null )
                {
                    Application.Run( this );
                }
            }
            catch ( HAUIException cmdLineException )
            {
                err = cmdLineException.ErrorCode;
                //
                Trace( "[HA Cmd] " + cmdLineException.Message + " " + cmdLineException.StackTrace );
            }
            catch ( Exception generalException )
            {
                err = HAUIException.KErrCommandLineGeneral;
                //
                Trace( "[HA Cmd] " + generalException.Message + " " + generalException.StackTrace );
            }
            //
            return err;
        }
		#endregion

		#region Event handlers
		protected override void OnMainFormClosed( object aSender, EventArgs aArgs )
		{
            SymbianUtils.SymDebug.SymDebugger.Assert( iRunningUI != null );
            
            // Inform "UI" that another form has been closed. This returns us another
            // new form if one is needed
            base.MainForm = iRunningUI.HandleFormClosed( aSender, aArgs );

            // Show the next form if needed
            if ( base.MainForm != null )
            {
                base.MainForm.Show();
            }
            else
            {
                // Otherwise exit
                base.OnMainFormClosed( aSender, aArgs );
                Application.Exit();
            }
		}

        private void Application_ApplicationExit( object aSender, EventArgs aArgs )
        {
            SaveSettings();
        }
        #endregion

        #region Internal constants
        private const string KHASettingsFileName = "HeapWizard.xml";
        #endregion

        #region Internal methods
		private void SaveSettings()
		{
			try
			{
				if	( iSettings != null )
				{
					iSettings.Store();
				}
			}
			catch( Exception )
			{
			}
		}
        #endregion

        #region ITracer Members
        public void Trace( string aMessage )
        {
            System.Diagnostics.Debug.WriteLine( aMessage );
        }

        public void Trace( string aFormat, params object[] aParams )
        {
            string text = string.Format( aFormat, aParams );
            Trace( text );
        }
        #endregion

        #region Data members
        private readonly XmlSettings iSettings;
		private readonly HeapWizardEngine iEngine;
        private PluginManager<HAUI> iUIs = new PluginManager<HAUI>( 2 );
        private HAUI iRunningUI = null;
		#endregion
	}
}
