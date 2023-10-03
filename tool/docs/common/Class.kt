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

package com.vaticle.typedb.client.tool.doc.common

data class Class(
    val name: String,
    val fields: List<Argument> = listOf(),
    val methods: List<Method> = listOf(),
    val description: List<String> = listOf(),
    val examples: List<String> = listOf(),
    val superClasses: List<String> = listOf(),
    val packagePath: String? = null,
    val anchor: String? = null,
) {
    fun toAsciiDoc(language: String): String {
        var result = ""
        result += "[#_${this.anchor ?: this.name}]\n"
        result += "= ${this.name}\n\n"

        if (this.superClasses.isNotEmpty()) {
            result += when (language) {
                "java" -> "*Superinterfaces:*\n\n"
                else -> "*Supertypes:*\n\n"
            }
            result += this.superClasses.map { "* `$it`" }.joinToString("\n")
            result += "\n\n"
        }

        if (this.description.isNotEmpty()) {
            result += "== Description\n\n${this.description.joinToString("\n\n")}\n\n"
        }

        if (this.examples.isNotEmpty()) {
            result += "== Code examples\n\n"
            this.examples.forEach {
                result += "[source,$language]\n----\n$it\n----\n\n"
            }
        }

        if (this.fields.isNotEmpty()) {
            result += when (language) {
                "python" -> "== Properties\n\n"
                else -> "== Fields\n\n"
            }
            this.fields.forEach { result += it.toAsciiDocPage(language) }
        }

        if (this.methods.isNotEmpty()) {
            result += "\n== Methods\n\n"
            this.methods.forEach { result += it.toAsciiDoc(language) }
        }

        return result
    }

    fun toJavaComment(): String {
        var result = ""
        result += "${this.name}\n\n"

        if (this.description.isNotEmpty()) {
            result += "/**\n * ${this.description.map { backquotesToCode(it) }.joinToString("\n * ")}\n"
            if (this.examples.isNotEmpty()) {
                result += " * <h3>Examples</h3>\n"
                result += " * <pre>\n"
                this.examples.forEach {
                    result += " * ${snakeToCamel(it)}\n"
                }
                result += " * </pre>\n"
            }
            result += " */\n\n"
        }

        if (this.fields.isNotEmpty()) {
            this.fields.forEach { result += it.toJavaCommentField() }
        }

        if (this.methods.isNotEmpty()) {
            this.methods.forEach { result += it.toJavaComment() }
        }

        return result
    }

    fun toNodejsComment(): String {
        var result = ""
        result += "${this.name}\n\n"

        if (this.description.isNotEmpty()) {
            result += "/**\n * ${this.description.map { backquotesToCode(it) }.joinToString("\n * ")}\n"
            result += " * \n"
            if (this.examples.isNotEmpty()) {
                result += " * ### Examples\n"
                result += " * \n"
                result += " * ```ts\n"
                this.examples.forEach {
                    result += " * ${snakeToCamel(it)}\n"
                }
                result += " * ```\n"
            }
            result += " */\n\n"
        }

        if (this.fields.isNotEmpty()) {
            this.fields.forEach { result += it.toNodejsCommentField() }
        }

        if (this.methods.isNotEmpty()) {
            this.methods.forEach { result += it.toNodejsComment() }
        }

        return result
    }

    fun toRustComment(): String {
        var result = ""
        result += "${this.name}\n\n"

        if (this.description.isNotEmpty()) {
            result += "/// ${this.description.joinToString("\n/// ")}\n"
        }

        if (this.examples.isNotEmpty()) {
            result += "/// \n"
            result += "/// # Examples\n"
            result += "/// \n"
            result += "/// ```rust\n"
            this.examples.forEach {
                result += "/// $it\n"
            }
            result += "/// ```\n"
        }

        result += "\n"

        if (this.fields.isNotEmpty()) {
            this.fields.forEach { result += it.toRustCommentField() }
        }

        if (this.methods.isNotEmpty()) {
            this.methods.forEach { result += it.toRustComment() }
        }

        return result
    }
}
