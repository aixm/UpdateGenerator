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

import java.util.HashMap

class SimpleNamespaceContext : NamespaceContextEx {

    private val prefixMap = HashMap<String, String>()
    private val uriMap = HashMap<String, MutableSet<String>>()

    /**
     * Adds a additional mapping to this namespace context.
     * In the case the uri or prefix is already provided, the input will be ignored.
     *
     * @param prefix
     * The alias of the namespace.
     * @param uri
     * The uri of the namespace.
     */
    fun addNamespaceMapping(prefix: String, uri: String) {
        if (this.prefixMap.containsKey(prefix)) {
            return
        }
        this.prefixMap[prefix] = uri

        var prefixes: MutableSet<String>? = this.uriMap[uri]
        if (prefixes == null) {
            prefixes = java.util.HashSet()
            this.uriMap[uri] = prefixes
        }
        if (!prefixes.contains(prefix)) {
            prefixes.add(prefix)
        }
    }

    override fun getNamespaceURI(prefix: String): String? {
        return this.prefixMap[prefix]
    }

    override fun getPrefix(namespaceURI: String): String? {
        val prefixes = this.uriMap[namespaceURI]
        return if (prefixes != null && prefixes.isNotEmpty()) {
            prefixes.iterator().next()
        } else null
    }

    override fun getPrefixes(namespaceURI: String): Iterator<*> {
        val prefixes = this.uriMap[namespaceURI]
        val clonedPrefixes = prefixes?.let { HashSet(it) } ?: java.util.HashSet()
        return clonedPrefixes.iterator()
    }

    override fun getNamespaceURIs(): Set<String> {
        return HashSet(this.uriMap.keys)
    }
}