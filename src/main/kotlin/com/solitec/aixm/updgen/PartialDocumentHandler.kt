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

import org.w3c.dom.DocumentFragment
import org.w3c.dom.Element
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.SAXParserFactory

class PartialDocumentHandler(private val receiver: PartialHandler) : DefaultHandler() {

    private val document = DocumentBuilderFactory.newInstance().run {
        isNamespaceAware = true
        newDocumentBuilder().newDocument()
    }

    internal enum class State {
        STATE_NOTHING,
        STATE_IN_SEPARATOR_NODE,
        STATE_IN_FEATURE_NODE
    }

    companion object {
        fun parse(inputSource : InputSource, receiver: PartialHandler) {
            val saxParserFactory = SAXParserFactory.newInstance()
            saxParserFactory.isNamespaceAware = true
            val saxParser = saxParserFactory.newSAXParser()
            saxParser.parse(inputSource, PartialDocumentHandler(receiver))
        }
    }

    private var state = State.STATE_NOTHING
    private var depth = 0
    private var fragment: DocumentFragment = document.createDocumentFragment()

    private val characters = StringBuilder()
    private val namespaceContext = SimpleNamespaceContext()
    private val elementStack = ArrayDeque<Element>()

    /**
     * This method adds an element to the stack implementation
     *
     * @param uri
     *      The namespace uri of the element.
     * @param qName
     *      The fully qualified name of the element.
     * @param attributes
     *      The attributes of the element.
     */
    private fun addElement(uri: String?, qName: String?, attributes: Attributes?) {

        val element = document.createElementNS(uri, qName)

        if (!fragment.hasChildNodes()) {
            fragment.appendChild(element)
        }

        for (i in 0 until attributes!!.length) {
            element.setAttributeNS(attributes.getURI(i), attributes.getQName(i), attributes.getValue(i))
        }

        elementStack.peek()?.appendChild(element)
        this.elementStack.push(element)
    }

    /**
     * do some finalizing tasks, e.g. set text node, if text is available.
     */
    private fun finalizeElement() {
        val element = this.elementStack.pop()
        val text = this.characters.toString().trim { it <= ' ' }
        if (text.isNotEmpty()) {
            element.textContent = text
        }
    }

    private fun startPartial() {
        fragment = document.createDocumentFragment()
    }

    @Throws(SAXException::class)
    override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
        characters.clear()
        depth += 1

        when (state) {

            State.STATE_NOTHING -> {
                if (depth == 1) {
                    receiver.rootElement(uri, localName, qName, attributes, namespaceContext)
                }
                if (receiver.isSeparator(uri, localName)) {
                    state = State.STATE_IN_SEPARATOR_NODE
                }
            }

            State.STATE_IN_SEPARATOR_NODE -> if (receiver.skipTag(uri, localName)) {
                state = State.STATE_NOTHING
            } else {
                state = State.STATE_IN_FEATURE_NODE
                startPartial()
                addElement(uri, qName, attributes)
            }

            State.STATE_IN_FEATURE_NODE -> addElement(uri, qName, attributes)
        }
    }

    @Throws(SAXException::class)
    override fun endElement(uri: String, localName: String, qName: String) {
        try {
            if (state != State.STATE_NOTHING) {
                if (receiver.isSeparator(uri, localName)) {
                    state = State.STATE_NOTHING
                    elementStack.clear()
                    receiver.handlePartial(fragment, namespaceContext)
                }
            }
            if (state == State.STATE_IN_FEATURE_NODE) {
                finalizeElement()
            }
        } finally {
            characters.setLength(0)
            depth -= 1
        }
    }

    @Throws(SAXException::class)
    override fun characters(ch: CharArray?, start: Int, length: Int) {
        characters.append(ch, start, length)
    }

    @Throws(SAXException::class)
    override fun startPrefixMapping(prefix: String, uri: String) {
        namespaceContext.addNamespaceMapping(prefix, uri)
    }

    override fun endDocument() {
        receiver.documentFinished()
    }
}