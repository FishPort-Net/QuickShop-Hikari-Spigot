package com.ghostchu.quickshop.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipInputStream
import javax.inject.Inject

@CacheableTask
abstract class StageSanitizedDependencyJar : DefaultTask() {

    @get:Classpath
    abstract val inputJars: ConfigurableFileCollection

    @get:Input
    abstract val excludedPrefixes: ListProperty<String>

    @get:Input
    abstract val includedPrefixes: ListProperty<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Inject
    abstract val fileSystemOperations: FileSystemOperations

    init {
        includedPrefixes.convention(emptyList())
    }

    @TaskAction
    fun stage() {
        val input = inputJars.singleFile
        val outputRoot = outputDirectory.get().asFile.toPath()
        fileSystemOperations.delete { delete(outputRoot) }
        Files.createDirectories(outputRoot)

        ZipInputStream(input.inputStream().buffered()).use { zin ->
            val included = includedPrefixes.get()
            var entry = zin.nextEntry
            while (entry != null) {
                if ((included.isEmpty() || included.any(entry.name::startsWith))
                    && excludedPrefixes.get().none(entry.name::startsWith)) {
                    val relativePath = outputRoot.fileSystem.getPath(entry.name).normalize()
                    require(!relativePath.isAbsolute && !relativePath.startsWith("..")) {
                        "Unsafe ZIP entry in ${input.name}: ${entry.name}"
                    }
                    val target = outputRoot.resolve(relativePath)
                    if (entry.isDirectory) {
                        Files.createDirectories(target)
                    } else {
                        Files.createDirectories(target.parent)
                        Files.copy(zin, target, StandardCopyOption.REPLACE_EXISTING)
                    }
                }
                zin.closeEntry()
                entry = zin.nextEntry
            }
        }
    }
}
