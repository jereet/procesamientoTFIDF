class Summarizer {
  def summarize(documents: List[(String, String)], topN: Int = 10): List[String] = {
    val processor = new TextProcessor
    val tfidf = new TFIDF

    val sentences = documents.flatMap { case (_, content) =>
      processor.sentenceSegment(content)
    }

    val sentenceDataList = sentences.map {
      sentence => SentenceData(sentence, processor.tokenize(sentence))
    }
    val idfMap = tfidf.buildIdfMap(sentenceDataList)

    val scored: List[(SentenceData, Double)] = sentenceDataList.map(s => (s, tfidf.score(s, idfMap)))

    scored
      .sortBy(-_._2)
      .take(topN)
      .map(_._1.original)
  }
}