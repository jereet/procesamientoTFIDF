class TextProcessor {
  private val stopwords: Set[String] = {
    val source = scala.io.Source.fromFile("src\\main\\resources\\stopwords-es.txt")
    val words = try source.getLines().toSet
    finally source.close()
    words
  }
  def sentenceSegment(text: String): List[String] = {
    val endings = "(?<=[.!?])\\s+|\\n+"
    text.split(endings)
      .map(_.trim)
      .filter(s => s.nonEmpty && s.split("\\s+").length >= 5)
      .toList
  }

  def tokenize(sentence: String): List[String] = {
    sentence.toLowerCase
      .replaceAll("[^a-z0-9\\s]", "")
      .split("\\s+")
      .filter(_.nonEmpty)
      .filterNot(stopwords.contains)
      .toList

  }

}