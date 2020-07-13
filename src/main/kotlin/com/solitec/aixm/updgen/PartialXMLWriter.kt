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

import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.OutputStream
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamException

class PartialXMLWriter(
    private val outputStream: OutputStream,
    val encoding: String,
    private val namespaceContext: NamespaceContextEx
) {

    private lateinit var separatorLocalName: String
    private lateinit var separatorNamespaceURI: String

    private val xmlStreamWriter = XMLOutputFactory.newInstance().run {
        setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true)
        createXMLStreamWriter(outputStream, encoding)
    }

    init {
        xmlStreamWriter.namespaceContext = namespaceContext
        val defaultNamespaceUri = namespaceContext.getNamespaceURI("")
        if (defaultNamespaceUri != null) {
            xmlStreamWriter.setDefaultNamespace(defaultNamespaceUri)
        }
    }

    /**
     * This method changes the separator, generated around partials.
     *
     * @param namespaceURI The namespace uri of the separator.
     * @param localName The local name of the separator.
     */
    fun changeSeparator(namespaceURI: String, localName: String) {
        separatorNamespaceURI = namespaceURI
        separatorLocalName = localName
    }

    /**
     * This method initiate the start of the document.
     *
     * @param rootNode  The provided root node is taken as a base for generating the document element.
     * @throws XMLStreamException In the case of an error.
     */
    @Throws(XMLStreamException::class)
    fun startDocument(rootNode: Node) {
        xmlStreamWriter.writeStartDocument(encoding, "1.0")
        xmlStreamWriter.writeStartElement(rootNode.namespaceURI, rootNode.localName)
        writeAttributesOf(rootNode)
        namespaceContext.getNamespaceURIs().forEach { uri ->
            val prefix = namespaceContext.getPrefix(uri)
            if ("" != prefix) {
                xmlStreamWriter.writeNamespace(prefix, uri)
            }
        }
    }

    /**
     * This method finalizes the generated document, also the underlying stream will be closed.
     *
     * @throws XMLStreamException In the case of an error.
     */
    @Throws(XMLStreamException::class)
    fun endDocument() {
        // flush and close the document
        xmlStreamWriter.apply {
            writeEndElement() // the document element !
            writeEndDocument()
            flush()
            close()
        }
    }

    /**
     * This method render an element into the document, surrounded by the separator.
     *
     * @param element The element to be rendered.
     * @throws XMLStreamException In the case of an error.
     */
    @Throws(XMLStreamException::class)
    fun streamElement(element: Node) {
        require(element is Element) { String.format("Parameter not instance of Element. [%s]", element.javaClass) }
        xmlStreamWriter.writeStartElement(separatorNamespaceURI, separatorLocalName)
        writeElement(element)
        xmlStreamWriter.writeEndElement()
    }

    /**
     * The method renders an element, without surrounding separator.
     *
     * @param element The element to be rendered.
     * @throws XMLStreamException In the case of an error.
     */
    @Throws(XMLStreamException::class)
    private fun writeElement(element: Node) {
        val childNodes = element.childNodes
        if (childNodes.length > 0) {
            xmlStreamWriter.writeStartElement(element.namespaceURI, element.localName)
            writeAttributesOf(element)
            for (i in 0 until childNodes.length) {
                val item = childNodes.item(i)
                when (item.nodeType) {
                    Node.ELEMENT_NODE -> writeElement(item)
                    Node.TEXT_NODE -> xmlStreamWriter.writeCharacters(item.nodeValue)
                }
            }
            xmlStreamWriter.writeEndElement()
        } else {
            xmlStreamWriter.writeEmptyElement(element.namespaceURI, element.localName)
            writeAttributesOf(element)
        }
    }

    /**
     * This method renders the attribute of the given element node.
     *
     * @param element The [Element] node.
     * @throws XMLStreamException In the case of an error.
     */
    @Throws(XMLStreamException::class)
    private fun writeAttributesOf(element: Node) {
        val attributes = element.attributes
        if (attributes != null) {
            for (i in 0 until attributes.length) {
                val item = attributes.item(i)
                if (item.namespaceURI != null) {
                    xmlStreamWriter.writeAttribute(item.namespaceURI, item.localName, item.nodeValue)
                } else {
                    xmlStreamWriter.writeAttribute(item.localName, item.nodeValue)
                }
            }
        }
    }
}