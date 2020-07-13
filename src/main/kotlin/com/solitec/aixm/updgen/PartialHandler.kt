/*
BSD 2-Clause License

Copyright (c) 2020, EUROCONTROL
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 Project:      aixm-update-gen
 Created:      24.03.20
 Author:       Manfred Odenstein, SOLITEC Software Solutions G.m.b.H.

*/
package com.solitec.aixm.updgen

import org.w3c.dom.Node
import org.xml.sax.Attributes
import javax.xml.namespace.NamespaceContext

interface NamespaceContextEx : NamespaceContext {
    fun getNamespaceURIs(): Set<String>
}

interface PartialHandler {
    /**
     * This method returns true in the case the given tag is a separator, e.g. "hasMember".
     *
     * @param uri
     * The namespace uri of the tag
     * @param localName
     * The local name of the tag
     * @return
     * true, if the tag is a separator
     */
    fun isSeparator(uri: String, localName: String): Boolean

    /**
     * This method returns true in the case the given tag shall be skipped, the splitter proceeds to the next separator.
     *
     * @param uri
     * The namespace uri of the tag
     * @param localName
     * The local name of the tag
     * @return
     * true if the given tag shall be skipped.
     */
    fun skipTag(uri: String, localName: String): Boolean {
        return false
    }

    /**
     *
     * @param partial
     * The partial document to be handled.
     * @param namespaceContext
     * [NamespaceContextEx] implementing object, containing the current namespace mappings.
     */
    fun handlePartial(partial: Node, namespaceContext: NamespaceContextEx)

    /**
     * This method is called if the root element arrived by the SAX content handler
     *
     * @param uri
     * The namespace uri of the tag
     * @param localName
     * The local name of the tag
     * @param qName
     * The fully qualified name (inclusive namespace prefix) of the element.
     * @param attributes
     * The attribute collection of the element, see [Attributes]
     * @param namespaceContext
     * [NamespaceContextEx] implementing object, containing the current namespace mappings.
     */
    fun rootElement(
        uri: String,
        localName: String,
        qName: String,
        attributes: Attributes,
        namespaceContext: NamespaceContextEx
    ) {} // default implementation does nothing

    /**
     * This method just signals that the end of the parsing document is reached.
     */
    fun documentFinished()
    {}
}