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
using System.Collections.Generic;
using System.Text;
using System.Reflection;

namespace HeapLib.Reconstructor.DataSources.Analyser.Extractor
{
    internal sealed class ExtractorFactory
    {
        #region Constructors & destrutor
        public ExtractorFactory()
        {
            Type extractorType = typeof( Extractor );
            Type[] types = Assembly.GetExecutingAssembly().GetTypes();
            foreach ( Type t in types )
            {
                if ( t.IsClass && extractorType.IsAssignableFrom( t ) && !t.IsAbstract )
                {
                    iExtractorTypes.Add( t );
                }
            }
        }
        #endregion

        #region API
        public Extractor FindSuitableExtractor( string aLine )
        {
            Extractor ret = null;
            //
            foreach ( Type extractorType in iExtractorTypes )
            {
                Extractor ext = CreateExtractor( extractorType );
                try
                {
                    bool canExtract = ext.CanExtractFrom( aLine );
                    if ( canExtract )
                    {
                        System.Diagnostics.Debug.Assert( ext.PrimaryLine != string.Empty );
                        System.Diagnostics.Debug.Assert( ext.Hash != string.Empty );
                        ret = ext;
                        break;
                    }
                }
                catch ( Exception )
                {
                }
            }
            //
            return ret;
        }
        #endregion

        #region Internal methods
        private Extractor CreateExtractor( Type aType )
        {
            object[] parameters = new object[] { };
            Extractor ret = (Extractor) Activator.CreateInstance( aType, parameters );
            return ret;
        }
        #endregion

        #region Data members
        private List<Type> iExtractorTypes = new List<Type>();
        #endregion
    }
}
