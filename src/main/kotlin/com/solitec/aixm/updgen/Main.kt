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

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import java.io.BufferedInputStream
import java.io.BufferedOutputStream

class AixmUpdateGenCLI : CliktCommand(name = "aixm-update-gen", help = """
    This command cancels timeslices and creates a new one with an optional annotation (--remark) for the given <EFFECTIVE-DATE>.
    
    Example:
    ```
    aixm-update-gen --remark "new slice" "2022-12-24T00:00:00Z" input.xml output.xml
    ```
""".trimIndent()) {

    private val effectiveDate by argument("<EFFECTIVE-DATE>", help = """The new effective date, e.g. "2022-12-24T00:00:00Z".""").convert {
        XMLTool.parseXMLDateTime(it)
    }
    private val remark by option("-r", "--remark", help = "This text will be placed in the annotation element.")
    private val flagOmitCorrection by option("-c", "--omit-correction", help = "This instructs the program to not create the correction timeslices.").flag()
    private val inputFile by argument("<INPUT-FILE>", help = "An AIXM 5.1 Basic Message file as input.").file(mustExist = true)
    private val outputFile by argument("<OUTPUT-FILE>", help = "The output file.").file()

    
    override fun run() {
        val inputStream = BufferedInputStream(inputFile.inputStream())
        val outputStream = BufferedOutputStream(outputFile.outputStream())
        val params = GeneratorParams(effectiveDate, remark, flagOmitCorrection)
        try {
            outputStream.use {
                AIXMUpdateGenerator.execute(inputStream, outputStream, params)
            }
        } finally {
            // to be defined
        }
    }


}

fun main(args: Array<String>) = AixmUpdateGenCLI().main(args)
