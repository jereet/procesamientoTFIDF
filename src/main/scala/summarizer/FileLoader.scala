class FileLoader {

  def loadFiles(dirPath: String): List[(String, String)] = {
    val dir = new java.io.File (dirPath)
    if (! dir.exists () || ! dir.isDirectory) {
      throw new IllegalArgumentException (s"El directorio $dirPath no existe o no es un directorio.")
    }

    val files = dir.listFiles()
    val filesTxt = files.filter(f => f.isFile && f.getName.endsWith(".txt"))
    val filesList = filesTxt.toList

    filesList.map {file =>
      val source = scala.io.Source.fromFile (file)
      val content = try source.mkString finally source.close ()
      (file.getName, content)
    }
  }
}