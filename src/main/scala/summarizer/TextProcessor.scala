class TextProcessor {
  val stopwords: Set[String] = {
    val source = scala.io.Source.fromResource("stopwords.txt")
    val words = try source.getLines().toSet
    finally source.close()
    words
  }
  def sentenceSegment(text: String): List[String] = {
    val endings = "(?<=[.!?])\\s+"
    text.split(endings)
      .map(_.trim)
      .filter(_.nonEmpty)
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