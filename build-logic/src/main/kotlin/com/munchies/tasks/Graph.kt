package com.munchies.tasks

import kotlin.collections.mapKeys
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.NONE
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

private class Graph(
  private val root: Project,
  private val dependencies: MutableMap<Project, Set<Pair<Configuration, Project>>> = mutableMapOf(),
  private val plugins: MutableMap<Project, PluginType> = mutableMapOf(),
  private val seen: MutableSet<String> = mutableSetOf(),
) {

  operator fun invoke(project: Project = root): Graph {
    if (project.path in seen) return this
    seen += project.path
    plugins.putIfAbsent(project, PluginType.Unknown)
    dependencies.compute(project) { _, u -> u.orEmpty() }
    project.configurations
      .map { it to it.dependencies.withType<ProjectDependency>().ifEmpty { null } }
      .filter { (_, dep) -> dep != null }
      .flatMap { (c, value) -> value!!.map { dep -> c to project.project(dep.path) } }
      .filter { (c, dep) -> c.toString().contains(regex = Regex(".*(?:implementation|api).*")) }
      .forEach { (configuration: Configuration, projectDependency: Project) ->
        val newPair = (configuration to projectDependency)
        dependencies.compute(project) { _, u -> u.orEmpty() + newPair }
        invoke(projectDependency)
      }
    return this
  }

  fun dependencies(): Map<String, Set<Pair<String, String>>> = dependencies
    .mapKeys { it.key.path }
    .mapValues { it.value.mapTo(mutableSetOf()) { (c, p) -> c.name to p.path } }

  fun plugins() = plugins.mapKeys { it.key.path }
}

internal enum class PluginType(val id: String, val ref: String, val style: String) {
  Unknown(
    id = "?",
    ref = "unknown",
    style = "fill:#FFADAD,stroke:#000,stroke-width:2px,color:#000",
  ),
}

fun Project.configureGraphTasks() {
  if (!buildFile.exists()) return

  val dumpTask = tasks.register<GraphDumpTask>("graphDump") {
    val graph = Graph(this@configureGraphTasks).invoke()
    projectPath = this@configureGraphTasks.path
    dependencies = graph.dependencies()
    plugins = graph.plugins()
    output = this@configureGraphTasks.layout.buildDirectory.file("mermaid/graph.txt")
  }
  tasks.register<AppendGraphTask>("graphUpdate") {
    group = "munchies"
    projectPath = this@configureGraphTasks.path
    input = dumpTask.flatMap { it.output }
    output = this@configureGraphTasks.layout.projectDirectory.file("README.md")
    dependsOn(tasks.named("graphDump"))
  }
}

@CacheableTask
private abstract class GraphDumpTask : DefaultTask() {

  @get:Input
  abstract val projectPath: Property<String>

  @get:Input
  abstract val dependencies: MapProperty<String, Set<Pair<String, String>>>

  @get:Input
  abstract val plugins: MapProperty<String, PluginType>

  @get:OutputFile
  abstract val output: RegularFileProperty

  override fun getDescription() = "Dumps project dependencies to a mermaid file."

  @TaskAction
  operator fun invoke() {
    output.get().asFile.writeText(mermaid())
    logger.lifecycle(output.get().asFile.toPath().toUri().toString())
  }

  private fun mermaid() = buildString {
    val dependencies: Set<Dependency> = dependencies.get()
      .flatMapTo(mutableSetOf()) { (project, entries) -> entries.map { it.toDependency(project) } }
    // FrontMatter configuration (not supported yet on GitHub.com)
    appendLine(
      // language=YAML
      """
            ---
            config:
              layout: elk
              elk:
                nodePlacementStrategy: SIMPLE
            ---
      """.trimIndent(),
    )
    // Graph declaration
    appendLine("graph TB")
    // Nodes and subgraphs
    val (rootProjects, nestedProjects) = dependencies
      .map { listOf(it.project, it.dependency) }.flatten().toSet()
      .plus(projectPath.get()) // Special case when this specific module has no other dependency
      .groupBy { it.substringBeforeLast(":") }
      .entries.partition { it.key.isEmpty() }

    val orderedGroups = nestedProjects.groupBy {
      if (it.key.count { char -> char == ':' } > 1) it.key.substringBeforeLast(":") else ""
    }

    orderedGroups.forEach { (outerGroup, innerGroups) ->
      if (outerGroup.isNotEmpty()) {
        appendLine("  subgraph $outerGroup")
        appendLine("    direction TB")
      }
      innerGroups.sortedWith(
        compareBy(
          { (group, _) ->
            dependencies.filter { dep ->
              val toGroup = dep.dependency.substringBeforeLast(":")
              toGroup == group && dep.project.substringBeforeLast(":") != group
            }.count()
          },
          { -it.value.size },
        ),
      ).forEach { (group, projects) ->
        val indent = if (outerGroup.isNotEmpty()) 4 else 2
        appendLine(" ".repeat(indent) + "subgraph $group")
        appendLine(" ".repeat(indent) + "  direction TB")
        projects.sorted().forEach {
          appendLine(it.alias(indent = indent + 2, plugins.get().getValue(it)))
        }
        appendLine(" ".repeat(indent) + "end")
      }
      if (outerGroup.isNotEmpty()) {
        appendLine("  end")
      }
    }

    rootProjects.flatMap { it.value }.sortedDescending().forEach {
      appendLine(it.alias(indent = 2, plugins.get().getValue(it)))
    }
    // Links
    if (dependencies.isNotEmpty()) appendLine()
    dependencies
      .sortedWith(compareBy({ it.project }, { it.dependency }, { it.configuration }))
      .forEach { appendLine(it.link(indent = 2)) }
    // Classes
    appendLine()
    appendLine(PluginType.Unknown.classDef())
  }

  private class Dependency(val project: String, val configuration: String, val dependency: String)

  private fun Pair<String, String>.toDependency(project: String) =
    Dependency(project, configuration = first, dependency = second)

  private fun String.alias(indent: Int, pluginType: PluginType): String = buildString {
    append(" ".repeat(indent))
    append(this@alias)
    append("[").append(substringAfterLast(":")).append("]:::")
    append(pluginType.ref)
  }

  private fun Dependency.link(indent: Int) = buildString {
    append(" ".repeat(indent))
    append(project).append(" ")
    append(
      when (configuration) {
        "api" -> "-->"
        "implementation" -> "-.->"
        else -> "-.->|$configuration|"
      },
    )
    append(" ").append(dependency)
  }

  private fun PluginType.classDef() = "classDef $ref $style;"
}

@CacheableTask
private abstract class AppendGraphTask : DefaultTask() {
  @get:Input
  abstract val projectPath: Property<String>

  @get:InputFile
  @get:PathSensitive(NONE)
  abstract val input: RegularFileProperty

  @get:OutputFile
  abstract val output: RegularFileProperty

  override fun getDescription() = "Updates Markdown file with the corresponding dependency graph."

  @TaskAction
  operator fun invoke() = with(output.get().asFile) {
    if (!exists()) {
      createNewFile()

      writeText(
        """
                # `${projectPath.get()}`

                ## Module dependency graph

                <!--region graph--> <!--endregion-->

        """.trimIndent(),
      )
    }
    val mermaid = input.get().asFile.readText().trimTrailingNewLines()
    val regex = """(<!--region graph-->)(.*?)(<!--endregion-->)""".toRegex(
      RegexOption.DOT_MATCHES_ALL,
    )
    val text = readText().replace(regex) { match ->
      val (start, _, end) = match.destructured
      """
            |$start
            |```mermaid
            |$mermaid
            |```
            |$end
      """.trimMargin()
    }
    writeText(text)
  }

  private fun String.trimTrailingNewLines() = lines()
    .dropLastWhile(String::isBlank)
    .joinToString(System.lineSeparator())
}
