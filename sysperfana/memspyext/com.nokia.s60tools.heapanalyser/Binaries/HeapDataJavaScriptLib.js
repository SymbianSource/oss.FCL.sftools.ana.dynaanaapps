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

var info = document.createElement("div");
var header = document.createElement("div");
var body = document.createElement("div");

function initialize()
{
     info.style.position="absolute";
     info.style.visibility='hidden';

     setGeneralStyle(header);

     header.style.fontWeight='bold';
     header.style.color='#4B7A98';
     header.style.background='#D5EBF9';

     setGeneralStyle(body);

     body.style.borderTop='0px';
     body.style.color='#1B4966';
     body.style.background='#FFFFFF';

     info.appendChild(header);
     info.appendChild(body);

     document.body.appendChild(info);
}

function setGeneralStyle(elm)
{
     elm.style.fontFamily='arial';
     elm.style.fontSize='10';
     elm.style.padding='2';
     elm.style.border='1px solid #A5CFE9';
}

function showInfo(title, text, width)
{
     header.innerHTML = title;
     body.innerHTML = text;
 
     if ( width )
     {
          header.style.width = width + "px";
          body.style.width = width + "px";
     }
     else
     {
          header.style.width = '180px';
          body.style.width = '180px';
     }
 
     info.style.visibility = 'visible';
}

function hideInfo()
{
     info.style.visibility = 'hidden';
}

function mouseMove(e)
{
     if ( info.style.visibility == 'visible' )
     {
          var evt;

          e?evt=e:evt=event;

          var x, y;

          if ( !evt.pageX )
                x = evt.x + document.body.scrollLeft;
          else
                x = evt.pageX;

          info.style.left = x + 15;

          if ( !evt.pageY )
                y  = evt.y + document.body.scrollTop;
          else
                y = evt.pageY;

          info.style.top = y + 15;
      }
}

function showMainFormCell( txt )
{
    var currentURL = parent.MainWindow.location.toString();
    var hashPos = currentURL.indexOf('#');

    if  ( hashPos >= 0 )
    {
        var hashArray = currentURL.split("#");
        currentURL = hashArray[ 0 ];
    }

    var newURL = currentURL + '#Cell_' + txt;
 
    parent.MainWindow.location = newURL;
}