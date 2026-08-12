package com.ghostchu.quickshop.buildlogic;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

/** Configuration-cache-safe task that runs the two SpecialSource remap stages. */
@CacheableTask
public abstract class SpigotRemapTask extends DefaultTask {

  @InputFile
  @PathSensitive(PathSensitivity.NONE)
  public abstract RegularFileProperty getInputJar();

  @InputFiles
  @PathSensitive(PathSensitivity.NONE)
  public abstract ConfigurableFileCollection getMappings();

  @Classpath
  public abstract ConfigurableFileCollection getInheritanceClasspath();

  @Input
  public abstract Property<Boolean> getReverse();

  @OutputFile
  public abstract RegularFileProperty getOutputJar();

  @TaskAction
  public void remap() throws Exception {

    final List<File> inheritance = new ArrayList<>(getInheritanceClasspath().getFiles());
    inheritance.sort(Comparator.comparing(File::getAbsolutePath));
    final List<String> arguments = new ArrayList<>();
    arguments.add(getInputJar().get().getAsFile().getAbsolutePath());
    arguments.add(getOutputJar().get().getAsFile().getAbsolutePath());
    arguments.add(getMappings().getSingleFile().getAbsolutePath());
    arguments.add(getReverse().get().toString());
    inheritance.stream().filter(File::isFile).map(File::getAbsolutePath).forEach(arguments::add);
    SpigotRemapper.main(arguments.toArray(String[]::new));
  }
}
