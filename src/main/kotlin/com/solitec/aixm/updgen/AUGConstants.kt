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

object AUGConstants {
    const val AIXM51_NS_URI = "http://www.aixm.aero/schema/5.1"
    const val AIXM51_NS_ALIAS = "aixm"
    const val AIXM51_MESSAGE_METADATA = "messageMetadata"
    const val AIXM51_BM_NS_URI = "http://www.aixm.aero/schema/5.1/message"
    const val AIXM51_BM_NS_ALIAS = "message"
    const val AIXM51_BM_SEPARATOR_LOCAL = "hasMember"
    const val GML_NS_URI = "http://www.opengis.net/gml/3.2"
    const val GML_NS_ALIAS = "gml"
    const val GMD_NS_URI = "http://www.isotc211.org/2005/gmd"
    const val GMD_NS_ALIAS = "gmd"
    const val GCO_NS_URI = "http://www.isotc211.org/2005/gco"
    const val GCO_NS_ALIAS = "gco"
    const val XLINK_NS_URI = "http://www.w3.org/1999/xlink"
    const val XLINK_NS_ALIAS = "xlink"
    const val XSI_NS_URI = "http://www.w3.org/2001/XMLSchema-instance"
    const val XSI_NS_ALIAS = "xsi"

    val DEFAULT_NAMESPACE_CONTEXT = SimpleNamespaceContext().apply {
        addNamespaceMapping(
            AIXM51_BM_NS_ALIAS,
            AIXM51_BM_NS_URI
        )
        addNamespaceMapping(
            AIXM51_NS_ALIAS,
            AIXM51_NS_URI
        )
        addNamespaceMapping(
            GML_NS_ALIAS,
            GML_NS_URI
        )
        addNamespaceMapping(
            GMD_NS_ALIAS,
            GMD_NS_URI
        )
        addNamespaceMapping(
            GCO_NS_ALIAS,
            GCO_NS_URI
        )
        addNamespaceMapping(
            XSI_NS_ALIAS,
            XSI_NS_URI
        )
    }
}