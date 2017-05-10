unmanagedSourceDirectories in Compile := (javaSource in Compile).value :: baseDirectory.value / "gensrc" :: Nil
