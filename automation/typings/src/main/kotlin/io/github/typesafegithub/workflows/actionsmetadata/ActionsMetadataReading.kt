package io.github.typesafegithub.workflows.actionsmetadata

import com.charleskorn.kaml.Yaml
import io.github.typesafegithub.workflows.actionsmetadata.model.ActionCoords
import io.github.typesafegithub.workflows.actionsmetadata.model.ActionTypes
import io.github.typesafegithub.workflows.actionsmetadata.model.TypingsSource
import io.github.typesafegithub.workflows.actionsmetadata.model.WrapperRequest
import io.github.typesafegithub.workflows.actionsmetadata.model.prettyPrint
import kotlinx.serialization.decodeFromString
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.relativeTo
import kotlin.streams.asSequence

private val rootProject = File(".").canonicalFile.let {
    when (it.name) {
        "github-workflows-kt" -> it
        "" -> it // Root directory in Docker container.
        else -> it.parentFile.parentFile
    }
}

val actionsDirectory: Path = rootProject.resolve("actions").toPath()

internal fun readActionsMetadata(): List<WrapperRequest> =
    readLocalActionTypings()
        .addDeprecationInfo()
        .sortedBy { it.actionCoords.prettyPrint.lowercase() }

private fun readLocalActionTypings(): List<WrapperRequest> {
    return Files.walk(actionsDirectory)
        .asSequence()
        .filter { it.isRegularFile() }
        .filter { it.name !in setOf("commit-hash.txt") }
        .map {
            val pathParts = it.relativeTo(actionsDirectory).invariantSeparatorsPathString.split("/")
            val owner = pathParts[0]
            val name = pathParts[1]
            val version = pathParts[2]
            val subname = pathParts.subList(3, pathParts.size - 1).joinToString("/")
            val file = pathParts.last()
            WrapperRequest(
                actionCoords = ActionCoords(
                    owner = owner,
                    name = listOfNotNull(name, subname.ifEmpty { null }).joinToString("/"),
                    version = version,
                    deprecatedByVersion = null, // It's set in postprocessing.
                ),
                typingsSource = buildTypingsSource(
                    path = it,
                    fileName = file,
                    actionTypingsDirectory = actionsDirectory,
                ),
            )
        }.toList()
}

private fun buildTypingsSource(
    path: Path,
    fileName: String,
    actionTypingsDirectory: Path,
) = when (fileName) {
    "action-types.yml" -> {
        val typings = try {
            myYaml.decodeFromString<ActionTypes>(path.readText())
        } catch (e: Exception) {
            println("There was a problem parsing action typing: $path")
            throw e
        }
        TypingsSource.WrapperGenerator(inputTypings = typings.toTypesMap())
    }
    "typings-hosted-by-action" -> TypingsSource.ActionTypes
    else -> error("An unexpected file found in $actionTypingsDirectory: '$fileName'")
}

private val myYaml = Yaml(
    configuration = Yaml.default.configuration.copy(
        // Don't allow any unknown keys, to keep the YAMLs minimal.
        strictMode = true,
    ),
)
