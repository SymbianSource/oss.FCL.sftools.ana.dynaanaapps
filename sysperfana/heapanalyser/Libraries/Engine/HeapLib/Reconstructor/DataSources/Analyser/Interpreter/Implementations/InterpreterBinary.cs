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
using System.Text.RegularExpressions;
using SymbianUtils;
using HeapLib.Reconstructor.Misc;
using HeapLib.Reconstructor.DataSources;
using HeapLib.Reconstructor.DataSources.Analyser.Extractor;

namespace HeapLib.Reconstructor.DataSources.Analyser.Interpreter
{
    internal sealed class InterpreterBinary// : InterpreterBase
    {
        /*
        #region Constructors & destructor
        public InterpreterBinary( DataSource aDataSource )
            : base( aDataSource )
        {
        }
        #endregion

        #region API
        public override void InterpretData( ExtractedData aData )
        {
            // [15:21:32.719] xti2:MCU_ASCII_PRINTF; channel:0xE0; msg:HeapData - EComServer::!ecomserver - HEAP INFO FOR THREAD 'EComServer::!ecomserver'
            // [15:21:32.719] xti2:MCU_ASCII_PRINTF; channel:0xE0; msg:HeapData - EComServer::!ecomserver - ==============================================
            // [15:21:32.719] xti2:MCU_ASCII_PRINTF; channel:0xE0; msg:HeapData - EComServer::!ecomserver - HeapInfo - heapBaseAddress:     0x00700074
            // [15:21:32.719] xti2:MCU_ASCII_PRINTF; channel:0xE0; msg:HeapData - EComServer::!ecomserver - HeapInfo - heapSize:                745352
            // [15:21:32.719] xti2:MCU_ASCII_PRINTF; channel:0xE0; msg:HeapData - EComServer::!ecomserver - HeapInfo - heapChunkSize:           745472
            // ...
            // [15:21:32.719] xti2:MCU_ASCII_PRINTF; channel:0xE0; msg:HeapData - EComServer::!ecomserver - 00700084: 00 00 00 00 03 03 03 03 03 03 03 03 03 03 03 03 ................
            // [15:21:32.719] xti2:MCU_ASCII_PRINTF; channel:0xE0; msg:HeapData - EComServer::!ecomserver - 00700094: 03 03 03 03 03 03 03 03 28 00 00 00 ec 47 28 80 ........(....G(.
            // [15:21:32.719] xti2:MCU_ASCII_PRINTF; channel:0xE0; msg:HeapData - EComServer::!ecomserver - 007000a4: d8 0c 70 00 58 0d 70 00 f8 0c 70 00 03 03 03 03 ..p.X.p...p.....
            // [15:21:32.719] xti2:MCU_ASCII_PRINTF; channel:0xE0; msg:HeapData - EComServer::!ecomserver - 007000b4: 03 03 03 03 03 03 03 03 03 03 03 03 03 03 03 03 ................
            // [15:21:32.719] xti2:MCU_ASCII_PRINTF; channel:0xE0; msg:HeapData - EComServer::!ecomserver - 007000c4: 48 00 00 00 e8 2b 30 80 00 2c 30 80 ec 02 70 00 H....+0..,0...p.
            //
            // or
            //
            // HEAP INFO FOR THREAD 'ecomserver::!ecomserver'
            // ==============================================
            // HeapInfo - heapBaseAddress:     0x0f8c0074
            // HeapInfo - heapSize:                262028
        }
        #endregion

        #region Data members
        #endregion
         */
    }
}
