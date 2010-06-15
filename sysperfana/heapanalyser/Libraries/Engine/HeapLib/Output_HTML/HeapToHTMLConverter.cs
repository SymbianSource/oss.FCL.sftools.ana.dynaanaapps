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
using System.Text;
using System.Collections.Generic;
using System.ComponentModel;
using SymbianUtils;
using HeapLib.Reconstructor;

namespace HeapLib
{
	public class HeapToHTMLConverter
    {
        #region Enumerations
        public enum TEvent
        {
            EEventStarted = 0,
            EEventProgress,
            EEventComplete
        }
        #endregion

        #region Delegates & events
        public delegate void EventHandler( TEvent aEvent );
        public event EventHandler Observer;
        #endregion

        #region Constructors
        public HeapToHTMLConverter( HeapReconstructor aReconstructor, string aBaseOutputPath )
		{
			iReconstructor = aReconstructor;
			iBaseOutputPath = aBaseOutputPath;
			
			// Make sure output base path exists
			DirectoryInfo dirInfo = new DirectoryInfo( aBaseOutputPath );
			if	( dirInfo.Exists == false )
			{
				dirInfo.Create();
			}
        }
		#endregion

        #region API
        public void Convert( TSynchronicity aSynchronicity )
        {
            switch ( aSynchronicity )
            {
            default:
            case TSynchronicity.EAsynchronous:
                CreateWorkers();
                break;
            case TSynchronicity.ESynchronous:
                throw new NotSupportedException();
            }
        }

        public static string PageFileNameEnsuringPathExists( string aName, string aBasePath, string aSubDir )
        {
            return PageFileNameEnsuringPathExists( aName, aBasePath, aSubDir, KHtmlFileExtension );
        }

        public static string PageFileNameEnsuringPathExists( string aName, string aBasePath, string aSubDir, string aExtension )
        {
            string directory = PageDirectoryNameEnsuringPathExists( aBasePath, aSubDir );
            string fileName = fileName = Path.Combine( directory, aName + aExtension );

            // Make sure filename does not exist
            FileInfo fileInfo = new FileInfo( fileName );
            if ( fileInfo.Exists )
            {
                fileInfo.Delete();
            }
            //
            return fileName;
        }

        public static string PageDirectoryNameEnsuringPathExists( string aBasePath, string aSubDir )
        {
            string directory = aBasePath;
            if ( aSubDir != string.Empty )
            {
                directory = Path.Combine( directory, aSubDir );
            }

            // Make sure directory exists
            DirectoryInfo dirInfo = new DirectoryInfo( directory );
            if ( dirInfo.Exists == false )
            {
                dirInfo.Create();
            }

            return directory;
        }
        #endregion

        #region Properties
        public int Progress
        {
            get
            {
                long pos = 0;
                //
                pos += iPageIndex.Position;
                pos += iPageEntireListingByAddress.Position;
                pos += iPageEntireListingByType.Position;
                pos += iPageEntireListingByLength.Position;
                pos += iPageHeapCellManager.Position;
                pos += iPageJavaScriptManager.Position;
                //
                long size = Size;
                float progress = (((float) pos / (float) size ) ) * 100.0f;
                //
                return (int) progress;
            }
        }
        #endregion

        #region Internal properties
        private long Size
		{
			get
			{
				long size = 0;
				//
				size += iPageIndex.Size;
				size += iPageEntireListingByAddress.Size;
				size += iPageEntireListingByType.Size;
				size += iPageEntireListingByLength.Size;
				size += iPageHeapCellManager.Size;
				size += iPageJavaScriptManager.Size;
				//
				return size;
			}
		}

        private bool IsComplete
        {
            get
            {
                int count = iCompletionStatuses.Count;

                // Count number of finished writers
                int completeCount = 0;
                foreach ( KeyValuePair<AsyncTextWriterBase, bool> entry in iCompletionStatuses )
                {
                    if ( entry.Value == KWriterIsComplete )
                    {
                        ++completeCount;
                    }
                }

                return ( completeCount == count );
            }
        }
		#endregion

		#region Internal methods
        private void CreateWorkers()
		{
            // Create all the writers first
			iPageIndex = new HeapToHTMLPageIndex( iReconstructor, PageFileNameEnsuringPathExists( "Index" ) );
            iPageIndex.iObserver += new AsyncTextWriterBase.Observer( WriterObserver );
            //
			string entireListingFileNameByAddress = HeapToHTMLPageEntireListing.PageFileName( HeapToHTMLPageEntireListing.TSortType.ESortTypeByAddress );
			entireListingFileNameByAddress = PageFileNameEnsuringPathExists( entireListingFileNameByAddress, iBaseOutputPath, string.Empty, string.Empty );
			iPageEntireListingByAddress = new HeapToHTMLPageEntireListing( iReconstructor, HeapToHTMLPageEntireListing.TSortType.ESortTypeByAddress, entireListingFileNameByAddress );
            iPageEntireListingByAddress.iObserver += new AsyncTextWriterBase.Observer( WriterObserver );
			//
			string entireListingFileNameByType = HeapToHTMLPageEntireListing.PageFileName( HeapToHTMLPageEntireListing.TSortType.ESortTypeByType );
			entireListingFileNameByType = PageFileNameEnsuringPathExists( entireListingFileNameByType, iBaseOutputPath, string.Empty, string.Empty );
			iPageEntireListingByType = new HeapToHTMLPageEntireListing( iReconstructor, HeapToHTMLPageEntireListing.TSortType.ESortTypeByType, entireListingFileNameByType );
            iPageEntireListingByType.iObserver += new AsyncTextWriterBase.Observer( WriterObserver );
			//
			string entireListingFileNameByLength = HeapToHTMLPageEntireListing.PageFileName( HeapToHTMLPageEntireListing.TSortType.ESortTypeByLength );
			entireListingFileNameByLength = PageFileNameEnsuringPathExists( entireListingFileNameByLength, iBaseOutputPath, string.Empty, string.Empty );
			iPageEntireListingByLength = new HeapToHTMLPageEntireListing( iReconstructor, HeapToHTMLPageEntireListing.TSortType.ESortTypeByLength, entireListingFileNameByLength );
            iPageEntireListingByLength.iObserver += new AsyncTextWriterBase.Observer( WriterObserver );
			//
			iPageHeapCellManager = new HeapToHTMLPageHeapCellManager( iReconstructor, iBaseOutputPath );
            iPageHeapCellManager.iObserver += new AsyncTextWriterBase.Observer( WriterObserver );
			//
			iPageJavaScriptManager = new HeapToHTMLPageJavaScriptManager( iReconstructor, iBaseOutputPath );
            iPageJavaScriptManager.iObserver += new AsyncTextWriterBase.Observer( WriterObserver );

            // Reset their completion statuses so that we're ready to start anew
            ResetCompletionStatuses();

            // Now it's safe to start all the writers running.
            iPageIndex.AsyncWrite();
            iPageEntireListingByAddress.AsyncWrite();
            iPageEntireListingByType.AsyncWrite();
            iPageEntireListingByLength.AsyncWrite();
            iPageHeapCellManager.AsyncWrite();
            iPageJavaScriptManager.AsyncWrite();
        }

        private void ResetCompletionStatuses()
        {
            SetCompletionState( iPageIndex, false );
            SetCompletionState( iPageEntireListingByAddress, false );
            SetCompletionState( iPageEntireListingByType, false );
            SetCompletionState( iPageEntireListingByLength, false );
            SetCompletionState( iPageHeapCellManager, false );
            SetCompletionState( iPageJavaScriptManager, false );
        }

        private void WriterObserver( AsyncTextWriterBase.TEvent aEvent, AsyncTextWriterBase aObject )
        {
            if ( aEvent == AsyncTextWriterBase.TEvent.EWritingComplete )
            {
                SetCompletionState( aObject, KWriterIsComplete );
                CheckForCompletion();
            }
            else if ( aEvent == AsyncTextWriterBase.TEvent.EWritingStarted )
            {
                SetCompletionState( aObject, KWriterIsIncomplete );
            }
            else
            {
                NotifyEvent( TEvent.EEventProgress );
            }
        }

        private void NotifyEvent( TEvent aEvent )
        {
            if ( Observer != null )
            {
                Observer( aEvent );
            }
        }
    
        private void CheckForCompletion()
        {
            // If all writers are completed, then notify "ready" event
            bool complete = IsComplete;
            if ( complete )
            {
                NotifyEvent( TEvent.EEventComplete );
            }
        }

        private void SetCompletionState( AsyncTextWriterBase aObject, bool aComplete )
        {
            if ( !iCompletionStatuses.ContainsKey( aObject ) )
            {
                iCompletionStatuses.Add( aObject, aComplete );
            }
            else
            {
                iCompletionStatuses[ aObject ] = aComplete;
            }
        }

        private string PageFileNameEnsuringPathExists( string aName )
		{
			return PageFileNameEnsuringPathExists( aName, string.Empty );
		}

		private string PageFileNameEnsuringPathExists( string aName, string aSubDir )
		{
			return PageFileNameEnsuringPathExists( aName, iBaseOutputPath, aSubDir );
		}
		#endregion

		#region Constants
		private const string KHtmlFileExtension = ".html";
        private const bool KWriterIsComplete = true;
        private const bool KWriterIsIncomplete = false;
		#endregion

		#region Data members
		private readonly HeapReconstructor iReconstructor;
		private readonly string iBaseOutputPath;
        private Dictionary<AsyncTextWriterBase, bool> iCompletionStatuses = new Dictionary<AsyncTextWriterBase, bool>();
		private HeapToHTMLPageIndex iPageIndex;
		private HeapToHTMLPageEntireListing iPageEntireListingByAddress;
		private HeapToHTMLPageEntireListing iPageEntireListingByType;
		private HeapToHTMLPageEntireListing iPageEntireListingByLength;
		private HeapToHTMLPageHeapCellManager iPageHeapCellManager;
		private HeapToHTMLPageJavaScriptManager iPageJavaScriptManager;
		#endregion
	}
}
