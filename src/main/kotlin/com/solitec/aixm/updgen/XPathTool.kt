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
import org.w3c.dom.NodeList
import java.util.*
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathExpressionException
import javax.xml.xpath.XPathFactory

object XPathTool {

    val xpathFactory = XPathFactory.newInstance()

    private val xpathCache = TreeMap<String,XPathExpression>()

    /**
     * This method creates an [XPathExpression] for the given [expression]
     */
    @Throws (XPathExpressionException::class)
    fun buildXPathExpression(expression: String, namespaceContext: SimpleNamespaceContext = AUGConstants.DEFAULT_NAMESPACE_CONTEXT) : XPathExpression {
        val xpathExpression = xpathFactory.newXPath()
        xpathExpression.namespaceContext = namespaceContext
        return xpathExpression.compile(expression)
    }

    fun extractText(node: Node, xPathExpression: XPathExpression) : String {
        return xPathExpression.evaluate(node)
    }

    fun ensureXpathExpression(expression: String) : XPathExpression {
        val _xpathExpression = xpathCache[expression]
        return if (_xpathExpression != null) {
            _xpathExpression
        } else {
            val newExpression = buildXPathExpression(expression)
            xpathCache[expression] = newExpression
            newExpression
        }
    }

    fun extractText(node: Node, expression: String) : String {
        val xpathExpression = ensureXpathExpression(expression)
        return xpathExpression.evaluate(node)
    }

    fun extractNode(node: Node, expression: String) : Node? {
        val xpathExpression = ensureXpathExpression(expression)
        return xpathExpression.evaluate(node, XPathConstants.NODE) as Node?
    }

    fun extractNodeList(node: Node, expression: String) : List<Node> {
        val xpathExpression = ensureXpathExpression(expression)
        val nodeList = xpathExpression.evaluate(node, XPathConstants.NODESET) as NodeList?
        val nodeListSize = nodeList?.length ?: 0
        return MutableList(nodeListSize) { nodeList!!.item(it)}
    }

    /**
     * Helper method to extract an integer value.
     *
     * @param node
     * The node to query.
     * @param expression
     * The [XPathExpression] to query
     * @return
     * the value of the expression or 0 if not found.
     * @throws XPathExpressionException
     * In the case of an error.
     */
    @Throws(XPathExpressionException::class)
    fun extractInteger(node: Node, expression: XPathExpression): Int? {
        val text = expression.evaluate(node)
        return if (text != null && text.isNotEmpty()) Integer.parseInt(text) else null
    }

}