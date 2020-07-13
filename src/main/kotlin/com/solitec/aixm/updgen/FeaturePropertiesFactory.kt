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
 Created:      07.07.20
 Author:       Manfred Odenstein, SOLITEC Software Solutions G.m.b.H.

*/
package com.solitec.aixm.updgen

import java.util.*

/**
 * A container containing the list of properties of feature types in the correct order
 */
object FeaturePropertiesFactory {

    private val features : Map<String, FeatureProperties>

    init {
        val properties = Properties()
        properties.load(FeaturePropertiesFactory.javaClass.getResourceAsStream("/feature.properties"))
        val stringKeys = properties.keys.filterIsInstance<String>().toList()
        features = stringKeys
            .map { key ->
                key to FeatureProperties(properties.getProperty(key as String?).split(","))
            }.toMap()
    }

    private val emptyFeatureProperties = FeatureProperties(listOf())

    /**
     * This method returns a helper object to determine the position of a given first level property
     *
     * @param name  The localName of the feature
     * @return a helper object to determine the position of given first level property.
     */
    fun propertiesFor(name: String): FeatureProperties {
        return features.getOrElse(name) { emptyFeatureProperties }
    }

}

/**
 * helper class
 */
class FeatureProperties(propertyNames: List<String>) {

    private val properties =
        propertyNames
            .mapIndexed { index, s -> FeatureProperty(s, index) }
            .map { it.name to it.ordinal }
            .toMap()
    private val annotationOrdinal = properties.getOrElse("annotation") { Int.MAX_VALUE}

    /**
     * This method checks if the given local-name of a first level property is positioned after the annotation property.
     */
    fun isAfterAnnotation(localName : String) : Boolean {
        if (localName == "extension") return true
        return properties.getOrElse(localName) { 0 } > annotationOrdinal
    }
}

data class FeatureProperty(val name: String, val ordinal: Int)
