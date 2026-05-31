@main
def main(): Unit = {
  print("Ingrese el path del directorio: ")
  val dirPath = scala.io.StdIn.readLine()

  val loader = new FileLoader
  val documents = loader.loadFiles(dirPath)

  val summarizer = new Summarizer
  val summary = summarizer.summarize(documents)

  println(s"\nRESUMEN EXTRACTIVO:")
  println(s"Archivos procesados: ${documents.size}")
  println(s"Oraciones seleccionadas: ${summary.size}\n")

  summary.zipWithIndex.foreach { case (sentence, index) =>
    println(s"${index + 1}. $sentence")
  }
}

