class TFIDF {
  private def tf(token: String, sentence: SentenceData): Double = {
    val tCount = sentence.tokens.count(_ == token)
    if(sentence.tokens.nonEmpty) tCount.toDouble/sentence.tokens.size
    else 0.0
  }

  def buildIdfMap(sentences: List[SentenceData]): Map[String, Double] = {
    val N = sentences.size.toDouble
    val dfMap = sentences
      .flatMap(s => s.tokens.distinct)
      .groupBy(identity)                
      .map { case (token, list) => token -> list.size }

    dfMap.map { case (token, df) =>
      token -> math.log(N / (1 + df))
    }
  }
  def score(sentence: SentenceData, idfMap: Map[String, Double]): Double = {
    sentence.tokens.map { token =>
      val tfValue = tf(token, sentence)
      val idfValue = idfMap.getOrElse(token, 0.0)
      tfValue * idfValue
    }.sum
  }
}