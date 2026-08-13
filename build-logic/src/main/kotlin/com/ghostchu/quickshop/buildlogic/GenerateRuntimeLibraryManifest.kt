package com.ghostchu.quickshop.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.security.MessageDigest
import java.util.Base64

@CacheableTask
abstract class GenerateRuntimeLibraryManifest : DefaultTask() {

    @get:Classpath
    abstract val inputJars: ConfigurableFileCollection

    @get:Input
    abstract val coordinatesByFileName: MapProperty<String, String>

    @get:Input
    abstract val excludedModules: SetProperty<String>

    @get:Input
    abstract val relocations: ListProperty<String>

    @get:Input
    abstract val repositoryRoutes: MapProperty<String, String>

    @get:Input
    abstract val defaultRepository: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val coordinates = coordinatesByFileName.get()
        val excluded = excludedModules.get()
        val libraries = inputJars.files.map { file ->
            val coordinate = requireNotNull(coordinates[file.name]) {
                "Missing Maven coordinate for ${file.name}"
            }
            val parts = coordinate.split(':', limit = 3)
            require(parts.size == 3) { "Invalid Maven coordinate: $coordinate" }
            val (group, artifact, version) = parts
            require(file.name == "$artifact-$version.jar") {
                "Runtime dependency classifiers are not supported: $coordinate (${file.name})"
            }
            RuntimeLibrary(group, artifact, version, file.name, sha256(file))
        }.filterNot { "${it.group}:${it.artifact}" in excluded }
            .sortedWith(compareBy(RuntimeLibrary::group, RuntimeLibrary::artifact))

        val target = outputFile.get().asFile
        target.parentFile.mkdirs()
        target.bufferedWriter().use { writer ->
            writer.appendLine("format=1")
            writer.appendLine("library.count=${libraries.size}")
            libraries.forEachIndexed { index, library ->
                val repository = repositoryFor(library.group)
                val path = library.group.replace('.', '/') + "/${library.artifact}/${library.version}/${library.fileName}"
                writer.appendLine("library.$index.group=${library.group}")
                writer.appendLine("library.$index.artifact=${library.artifact}")
                writer.appendLine("library.$index.version=${library.version}")
                writer.appendLine("library.$index.url=${repository.trimEnd('/')}/$path")
                writer.appendLine("library.$index.sha256-base64=${Base64.getEncoder().encodeToString(library.checksum)}")
            }
            val rules = relocations.get()
            writer.appendLine("relocation.count=${rules.size}")
            rules.forEachIndexed { index, rule ->
                val separator = rule.indexOf('=')
                require(separator > 0) { "Invalid relocation rule: $rule" }
                writer.appendLine("relocation.$index.source=${rule.substring(0, separator)}")
                writer.appendLine("relocation.$index.target=${rule.substring(separator + 1)}")
            }
        }
    }

    private fun repositoryFor(group: String): String = repositoryRoutes.get().entries
        .filter { (prefix, _) -> group == prefix || group.startsWith("$prefix.") }
        .maxByOrNull { (prefix, _) -> prefix.length }
        ?.value ?: defaultRepository.get()

    private fun sha256(library: java.io.File): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        library.inputStream().buffered().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var read = input.read(buffer)
            while (read >= 0) {
                digest.update(buffer, 0, read)
                read = input.read(buffer)
            }
        }
        return digest.digest()
    }

    private data class RuntimeLibrary(
        val group: String,
        val artifact: String,
        val version: String,
        val fileName: String,
        val checksum: ByteArray
    )
}
