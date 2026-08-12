package com.ghostchu.quickshop.buildlogic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.md_5.specialsource.Jar;
import net.md_5.specialsource.JarMapping;
import net.md_5.specialsource.JarRemapper;
import net.md_5.specialsource.provider.JarProvider;
import net.md_5.specialsource.provider.JointProvider;

/** Small command-line bridge around SpecialSource for the Gradle build. */
public final class SpigotRemapper {

  private SpigotRemapper() {
  }

  public static void main(final String[] args) throws Exception {

    if(args.length < 4) {
      throw new IllegalArgumentException("Usage: input output mappings reverse [inheritance jars...]");
    }

    final File inputFile = new File(args[0]);
    final File outputFile = new File(args[1]);
    final File mappingFile = new File(args[2]);
    final boolean reverse = Boolean.parseBoolean(args[3]);
    final File parent = outputFile.getParentFile();
    if(parent != null && !parent.isDirectory() && !parent.mkdirs()) {
      throw new IllegalStateException("Cannot create output directory: " + parent);
    }

    final JarMapping mapping = new JarMapping();
    mapping.loadMappings(mappingFile.getPath(), reverse, false, null, null);

    final List<Jar> inheritanceJars = new ArrayList<>();
    try(Jar inputJar = Jar.init(inputFile)) {
      final JointProvider inheritance = new JointProvider();
      for(int i = 4; i < args.length; i++) {
        final File file = new File(args[i]);
        if(!file.isFile() || file.equals(inputFile)) {
          continue;
        }
        final Jar jar = Jar.init(file);
        inheritanceJars.add(jar);
        inheritance.add(new JarProvider(jar));
      }
      inheritance.add(new JarProvider(inputJar));
      mapping.setFallbackInheritanceProvider(inheritance);
      new JarRemapper(null, mapping, null).remapJar(inputJar, outputFile);
    } finally {
      for(final Jar jar : inheritanceJars) {
        jar.close();
      }
    }
  }
}
