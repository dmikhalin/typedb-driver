/*
 *  Copyright (C) 2022 Vaticle
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.vaticle.typedb.client.tool.doc.nodejs

import java.io.File
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import com.vaticle.typedb.client.tool.doc.common.Argument
import com.vaticle.typedb.client.tool.doc.common.Class
import com.vaticle.typedb.client.tool.doc.common.EnumConstant
import com.vaticle.typedb.client.tool.doc.common.Method
import com.vaticle.typedb.client.tool.doc.common.mergeClasses
import com.vaticle.typedb.client.tool.doc.common.removeAllTags
import com.vaticle.typedb.client.tool.doc.common.replaceCodeTags
import com.vaticle.typedb.client.tool.doc.common.replaceEmTags
import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {
    val inputDirectoryName = args[0]
    val outputDirectoryName = args[1]

    val docsDir = Paths.get(outputDirectoryName)
    Files.createDirectory(docsDir)

    val parsedClasses: HashMap<String, Class> = hashMapOf()
    File(inputDirectoryName).walkTopDown().filter {
        it.toString().contains("/classes/") || it.toString().contains("/interfaces/")
                || it.toString().contains("/modules/")
    }.forEach {
        val html = it.readText(Charsets.UTF_8)
        val parsed = Jsoup.parse(html)
        val title = parsed.select(".tsd-page-title h1")
        val parsedClass = if (!title.isNullOrEmpty() && (title.text().contains("Class") || title.text().contains("Interface"))) {
            parseClass(parsed)
        } else {
            parseNamespace(parsed)
        }
        val parsedClassName = parsedClass.name

        parsedClasses[parsedClass.name] = if (parsedClasses.contains(parsedClassName)) {
            mergeClasses(parsedClasses[parsedClass.name]!!, parsedClass)
        } else {
            parsedClass
        }

        val parsedClassAsciiDoc = parsedClasses[parsedClass.name]!!.toAsciiDoc("nodejs")
        val outputFile = docsDir.resolve("$parsedClassName.adoc").toFile()
        outputFile.createNewFile()
        outputFile.writeText(parsedClassAsciiDoc)
    }
}

fun parseClass(document: Element): Class {
    val className = document.selectFirst(".tsd-page-title h1")!!.text().split(" ")[1]
    val classDescr = document.select(".tsd-page-title + section.tsd-comment div.tsd-comment p").map {
        reformatTextWithCode(it.html())
    }

    val superClasses = document.select("ul.tsd-hierarchy li:has(ul.tsd-hierarchy span.target)").map {
        it.child(0).text()
    }

    val propertiesElements = document.select("section.tsd-member-group:contains(Properties)")
    val properties = propertiesElements.select("section.tsd-member:not(.tsd-is-private)").map {
        parseProperty(it)
    }

    val methodsElements = document.select("section.tsd-member-group:contains(Constructors), " +
            "section.tsd-member-group:contains(Method)")
    val methods = methodsElements.select("section.tsd-member > .tsd-signatures > .tsd-signature").map {
        parseMethod(it)
    }.filter {
        it.name != "proto"
    } + document.select("section.tsd-member-group:contains(Accessors)")
        .select("section.tsd-member > .tsd-signatures > .tsd-signature").map {
            parseAccessor(it)
        }

    return Class(
        name = className,
        description = classDescr,
        methods = methods,
        fields = properties,
        superClasses = superClasses,
    )
}

fun parseNamespace(document: Element): Class {
    val className = document.selectFirst(".tsd-page-title h1")!!.text().split(" ")[1]
    val classDescr = document.select(".tsd-page-title + section.tsd-comment div.tsd-comment p").map {
        reformatTextWithCode(it.html())
    }

    val variables = document.select(".tsd-index-heading:contains(Variables) + .tsd-index-list a").map {
        EnumConstant(name = it.text())
    }

    return Class(
        name = className,
        description = classDescr,
        enumConstants = variables,
    )
}

fun parseMethod(element: Element): Method {
    val methodSignature = element.text()
    val methodName = element.selectFirst(".tsd-kind-call-signature, .tsd-kind-constructor-signature")!!.text()
    val descrElement = element.nextElementSibling()
    val methodReturnType = descrElement!!.select(".tsd-returns-title > *")
        .joinToString("") { it.text() }
    val methodDescr = descrElement.select(".tsd-description > .tsd-comment p").map { reformatTextWithCode(it.html()) }
    val methodExamples = descrElement.select(".tsd-description > .tsd-comment > :has(a[href*=examples]) + pre > :not(button)")
        .map { it.text() }

    val methodArgs = descrElement.select(".tsd-description .tsd-parameters .tsd-parameter-list > li").map {
        Argument(
            name = it.selectFirst(".tsd-kind-parameter")!!.text(),
            type = it.selectFirst(".tsd-signature-type")?.text(),
            description = it.selectFirst(".tsd-comment")?.let { reformatTextWithCode(it.html()) },
        )
    }

    return Method(
        name = methodName,
        signature = methodSignature,
        description = methodDescr,
        args = methodArgs,
        returnType = methodReturnType,
        examples = methodExamples,
    )
}

fun parseAccessor(element: Element): Method {
    val methodSignature = element.text()
    val methodName = element.selectFirst(".tsd-signature")!!.textNodes().first()!!.text()
    val descrElement = element.nextElementSibling()
    val methodReturnType = descrElement!!.select(".tsd-returns-title > *")
        .joinToString("") { it.text() }
    val methodDescr = descrElement.select(".tsd-description > .tsd-comment p").map { reformatTextWithCode(it.html()) }
    val methodExamples = descrElement.select(".tsd-description > .tsd-comment > :has(a[href*=examples]) + pre > :not(button)")
        .map { it.text() }

    return Method(
        name = methodName,
        signature = methodSignature,
        description = methodDescr,
        returnType = methodReturnType,
        examples = methodExamples,
    )
}

fun parseProperty(element: Element): Argument {
    val name = element.selectFirst(".tsd-signature span.tsd-kind-property")!!.text()
    val type = element.selectFirst(".tsd-signature .tsd-signature-type")?.text()
    val descr = element.selectFirst(".tsd-signature + .tsd-comment")?.text()
    return Argument(
        name = name,
        type = type,
        description = descr,
    )
}

fun reformatTextWithCode(html: String): String {
    return removeAllTags(replaceEmTags(replaceCodeTags(html)))
}
