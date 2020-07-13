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
import java.util.function.Consumer
import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.XMLGregorianCalendar

object XMLTool {

    private val datatypeFactory = DatatypeFactory.newInstance()

    /**
     * This method parses XMLDateTime instance from the given text in [dateTime].
     */
    @Throws(IllegalArgumentException::class)
    fun parseXMLDateTime(dateTime: String) : XMLGregorianCalendar = datatypeFactory.newXMLGregorianCalendar(dateTime)
}

/**
 * This extension method removes all attributes from the Node
 * @receiver An instance of Node
 */
fun Node.removeAllAttributes() {
    val attributes = this.attributes
    if (attributes.length > 0) {
        val range = 0 until attributes.length
        val list = range.map(attributes::item)
        list.forEach(Consumer { t -> attributes.removeNamedItemNS(t.namespaceURI, t.localName) })
    }
}