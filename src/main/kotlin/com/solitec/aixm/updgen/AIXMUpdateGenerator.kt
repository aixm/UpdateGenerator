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

import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import java.util.function.Consumer
import javax.xml.datatype.XMLGregorianCalendar
import javax.xml.parsers.DocumentBuilderFactory

class AIXMUpdateGenerator(private val outputStream: OutputStream, private val params: GeneratorParams) : PartialHandler {

    companion object {
        fun execute(inputStream: InputStream, outputStream: OutputStream, params: GeneratorParams) {
            val aixmUpdateGenerator = AIXMUpdateGenerator(outputStream, params)
            PartialDocumentHandler.parse(InputSource(inputStream), aixmUpdateGenerator)
            aixmUpdateGenerator.partialWriter?.endDocument()
        }
    }

    private var partialWriter : PartialXMLWriter? = null
    private val document = DocumentBuilderFactory.newInstance().run {
        isNamespaceAware = true
        newDocumentBuilder().newDocument()
    }

    internal enum class State {
        STATE_NOTHING,
        STATE_MESSAGE_META,
        STATE_FEATURE
    }
    internal data class Element(val uri: String, val localName: String) : Comparable<Element> {
        override fun compareTo(other: Element): Int {
            return compareValuesBy(this, other, { it.uri }, { it.localName })
        }
    }

    private var state = State.STATE_NOTHING
    private val separatorMap = TreeMap<Element, State>().apply {
        put(Element(AUGConstants.AIXM51_NS_URI, AUGConstants.AIXM51_MESSAGE_METADATA), State.STATE_MESSAGE_META)
        put(Element(AUGConstants.AIXM51_BM_NS_URI, AUGConstants.AIXM51_BM_SEPARATOR_LOCAL), State.STATE_FEATURE)
    }

    override fun isSeparator(uri: String, localName: String): Boolean {
        val tag = Element(uri, localName)
        if (separatorMap.containsKey(tag)) {
            state = separatorMap[tag]!!
            return true
        }
        return false
    }

    override fun handlePartial(partial: Node, namespaceContext: NamespaceContextEx) {
        partialWriter = partialWriter ?: PartialXMLWriter(
            outputStream,
            "utf-8",
            namespaceContext
        ).apply {
            startDocument(document.firstChild)
            changeSeparator(
                AUGConstants.AIXM51_BM_NS_URI,
                AUGConstants.AIXM51_BM_SEPARATOR_LOCAL
            )
        }

        when(state) {

            State.STATE_MESSAGE_META -> {
                handleMessageMetadata(partial)
            }

            State.STATE_FEATURE -> {
                handleFeature(partial)
            }
            else ->
                throw IllegalStateException("Invalid state in handlePartial")
        }

    }

    /**
     * This function is a placeholder for managing message meta data.
     *
     * @param partial   The metadata node.
     */
    private fun handleMessageMetadata(partial: Node) {
        // TODO("Not yet implemented")
        println("skipping Metadata")
    }

    /**
     * This function handles feature nodes, duplicates time slices, etc.
     *
     * @param partial   The parent of the feature node.
     */
    private fun handleFeature(partial: Node) {
        val feature = partial.firstChild

        // clone timeSlice
        val updateTimeSlice = XPathTool.extractNode(feature, """aixm:timeSlice""")
        val cancelTimeSlice = updateTimeSlice?.cloneNode(true)

        feature.insertBefore(cancelTimeSlice, updateTimeSlice)

        // "cancelation" timeSlice
        XPathTool.extractNode(cancelTimeSlice!!, """descendant::gml:validTime/gml:TimePeriod/gml:endPosition""")?.also {
            it.removeAllAttributes()
            it.textContent = params.effectiveDate.toXMLFormat()
        }
        XPathTool.extractNode(cancelTimeSlice, """descendant::aixm:correctionNumber""")?.incrementContent()

        // "update" timeSlice
        regenerateGmlIds(updateTimeSlice)
        XPathTool.extractNode(updateTimeSlice!!, """descendant::gml:validTime/gml:TimePeriod/gml:beginPosition""")?.also {
            it.textContent = params.effectiveDate.toXMLFormat()
        }

        XPathTool.extractNode(updateTimeSlice, """descendant::aixm:sequenceNumber""")?.incrementContent()
        XPathTool.extractNode(updateTimeSlice, """descendant::aixm:correctionNumber""")?.textContent = "0"

        if (params.remark != null) {
            val fTimeSlice = updateTimeSlice.firstChild
            val emptyAnnotation = XPathTool.extractNode(fTimeSlice!!, """aixm:annotation[@xsi:nil = "true"]""")
            if (emptyAnnotation != null) {
                fTimeSlice.removeChild(emptyAnnotation)
            }
            val annotation = createAnnotation(params.remark, fTimeSlice.ownerDocument)
            placeAnnotation(feature.localName, fTimeSlice, annotation)
        }

        feature.also {
            document.adoptNode(it)
            partialWriter!!.streamElement(it)
        }

    }

    /**
     * This method places the [annotation] on the correct place as child in the given [timeSlice]
     * The [name] attribute is the local-name of the feature.
     */
    private fun placeAnnotation(name : String, timeSlice: Node, annotation : Node) {
        val elements = convertToList(timeSlice.childNodes)
            .filter { it.nodeType == Node.ELEMENT_NODE }.toList()
        // getFirstNodeGreaterThanAnno, if found insertBefore, else appendChild
        val featureProperties = FeaturePropertiesFactory.propertiesFor(name)
        val afterAnnotation = elements.filter { node -> featureProperties.isAfterAnnotation(node.localName) }

        if (afterAnnotation.isNotEmpty()) {
            timeSlice.insertBefore(annotation, afterAnnotation.first())
        } else {
            timeSlice.appendChild(annotation)
        }

    }

    /**
     * This method converts the ugly [nodeList] to a collection.
     */
    private fun convertToList(nodeList : NodeList): List<Node> {
        val list = mutableListOf<Node>()
        for (i in 0 until nodeList.length) {
            list.add(nodeList.item(i))
        }
        return list
    }

    /**
     * This function assignes new values to the "gml:id" attributes in the given [node] descendants.
     *
     * @param node  The node to be traversed.
     */
    private fun regenerateGmlIds(node: Node) {
        val list = XPathTool.extractNodeList(node, """descendant::node()/@gml:id""")
        list.forEach(Consumer { n ->
            n.textContent = "uuid.${UUID.randomUUID()}"
        })
    }

    /**
     * This extension function increments the numerical content of the [Node].
     */
    private fun Node.incrementContent() {
        val number = this.textContent.toInt()
        this.textContent = number.inc().toString()
    }

    /**
     * This function creates a new annotation element tree, with the purpose of "REMARK" and the content of [text]
     *
     * @param text  The content of the annotation.
     * @param doc   The "element factory".
     */
    private fun createAnnotation(text: String, doc: Document) : Node {
        val annotation = doc.createElementNS(AUGConstants.AIXM51_NS_URI, "aixm:annotation")

        val note = doc.createElementNS(AUGConstants.AIXM51_NS_URI, "aixm:Note")
        note.setAttributeNS(AUGConstants.GML_NS_URI, "gml:id", "uuid.${UUID.randomUUID()}")

        val purpose = doc.createElementNS(AUGConstants.AIXM51_NS_URI, "aixm:purpose")
        purpose.textContent = "REMARK"
        note.appendChild(purpose)

        val translatedNote = doc.createElementNS(AUGConstants.AIXM51_NS_URI, "aixm:translatedNote")
        val linguisticNode = doc.createElementNS(AUGConstants.AIXM51_NS_URI, "aixm:LinguisticNote")
        linguisticNode.setAttributeNS(AUGConstants.GML_NS_URI, "gml:id", "uuid.${UUID.randomUUID()}")
        val innerNote = doc.createElementNS(AUGConstants.AIXM51_NS_URI, "aixm:note")
        innerNote.textContent = text
        linguisticNode.appendChild(innerNote)
        translatedNote.appendChild(linguisticNode)
        note.appendChild(translatedNote)

        annotation.appendChild(note)
        return annotation
    }

    override fun rootElement(
        uri: String,
        localName: String,
        qName: String,
        attributes: Attributes,
        namespaceContext: NamespaceContextEx
    ) {
        val rootElement = document.createElementNS(uri, qName)
        for (i in 0 until attributes.length) {
            rootElement.setAttributeNS(attributes.getURI(i), attributes.getQName(i), attributes.getValue(i))
        }
        document.appendChild(rootElement)
    }

}

/**
 * This data class encapsulate parameters for the generator.
 *
 * @param effectiveDate The new effective date.
 * @param remark        The optional remark.
 */
data class GeneratorParams(val effectiveDate: XMLGregorianCalendar, val remark: String?)
