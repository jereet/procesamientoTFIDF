# Plan de Programación: Resumen Extractivo con TF-IDF en Scala

## Objetivo
Implementar una aplicación de consola en Scala que lea archivos `.txt` de un directorio y produzca un resumen extractivo usando TF-IDF. La lógica de procesamiento debe estar completamente separada de la entrada/salida.

---

## Estructura de Archivos a Crear

```
tfidf-summarizer/
├── build.sbt
├── project/
│   └── build.properties
└── src/
    └── main/
        └── scala/
            ├── Main.scala
            └── summarizer/
                ├── FileLoader.scala
                ├── TextProcessor.scala
                ├── TFIDFCalculator.scala
                └── Summarizer.scala
```

---

## build.sbt

- `scalaVersion := "2.13.x"` (o 3.x)
- No se requieren dependencias externas
- `name := "tfidf-summarizer"`

---

## Módulo 1: `FileLoader.scala` — objeto `FileLoader`

**Función principal:**
```
loadFiles(dirPath: String): List[(String, String)]
```
- Usar `java.io.File(dirPath)` para acceder al directorio
- Filtrar solo archivos con extensión `.txt`
- Leer cada archivo con `scala.io.Source.fromFile(...).mkString`
- Retornar lista de tuplas `(nombreArchivo, contenido)`
- Manejar errores con `scala.util.Try`: si el directorio no existe o un archivo no es legible, imprimir advertencia y continuar

---

## Módulo 2: `TextProcessor.scala` — objeto `TextProcessor`

### Constante interna
```
val STOPWORDS: Set[String]
```
Lista de stopwords en inglés:
`"a", "an", "the", "is", "it", "in", "on", "at", "to", "for", "of", "and", "or", "but", "with", "as", "by", "from", "that", "this", "was", "are", "be", "been", "has", "have", "had", "not", "he", "she", "his", "her", "they", "their", "its", "which", "who", "also", "into", "than", "more", "were", "can", "about", "after", "before", "between", "under", "over", "such", "when", "where", "while", "how", "what", "if", "all", "no", "so", "do", "did", "would", "could", "will", "may", "one", "two", "three"`

### Función 1
```
sentenceSegment(text: String): List[String]
```
- Dividir el texto usando regex `(?<=[.!?])\\s+`
- Alternativamente: split por `". "`, `"! "`, `"? "`
- Filtrar oraciones con menos de 5 palabras (muy cortas para ser útiles)
- Retornar lista de strings con el texto original de cada oración

### Función 2
```
tokenize(sentence: String): List[String]
```
- Convertir a minúsculas: `sentence.toLowerCase`
- Eliminar puntuación con regex: reemplazar `[^a-z0-9\\s]` por `""`
- Dividir por espacios: `split("\\s+")`
- Filtrar tokens que estén en `STOPWORDS`
- Filtrar tokens de longitud menor a 2
- Filtrar tokens puramente numéricos (regex `\\d+`)
- Retornar lista de strings limpios

---

## Módulo 3: `TFIDFCalculator.scala`

### Tipo de datos (case class dentro del objeto o en archivo aparte)
```scala
case class SentenceData(original: String, tokens: List[String])
```

### Función 1
```
tf(token: String, sentence: SentenceData): Double
```
- Si `sentence.tokens` está vacío, retornar `0.0`
- Contar cuántas veces aparece `token` en `sentence.tokens`
- Retornar `count.toDouble / sentence.tokens.size`

### Función 2
```
buildIdfMap(sentences: List[SentenceData]): Map[String, Double]
```
- `N` = total de oraciones = `sentences.size`
- Para cada token único en todas las oraciones:
  - `df(t)` = cantidad de oraciones en las que aparece el token (al menos una vez)
  - `idf(t)` = `math.log(N.toDouble / (1 + df(t)))`
- Retornar `Map[String, Double]` con todos los tokens y sus valores IDF
- Implementar con `groupBy` o `foldLeft` sobre la lista de oraciones

### Función 3
```
score(sentence: SentenceData, idfMap: Map[String, Double]): Double
```
- Para cada token único en `sentence.tokens`:
  - Obtener `tf = tf(token, sentence)`
  - Obtener `idf = idfMap.getOrElse(token, 0.0)`
  - Acumular `tf * idf`
- Retornar la suma total

---

## Módulo 4: `Summarizer.scala` — objeto `Summarizer`

### Función principal
```
summarize(documents: List[(String, String)], topN: Int = 10): List[String]
```

**Pipeline paso a paso:**

1. **Extraer todas las oraciones** de todos los documentos:
   - Para cada `(_, content)` en `documents`, llamar `TextProcessor.sentenceSegment(content)`
   - Concatenar en una única `List[String]`

2. **Crear lista de `SentenceData`**:
   - Para cada oración cruda, crear `SentenceData(original = oración, tokens = TextProcessor.tokenize(oración))`
   - Filtrar aquellas cuya lista de tokens tenga menos de 3 elementos

3. **Calcular el mapa IDF** una sola vez:
   - `val idfMap = TFIDFCalculator.buildIdfMap(sentenceDataList)`

4. **Calcular puntaje de cada oración**:
   - `val scored: List[(SentenceData, Double)] = sentenceDataList.map(s => (s, TFIDFCalculator.score(s, idfMap)))`

5. **Ordenar y seleccionar**:
   - Ordenar por puntaje descendente: `.sortBy(-_._2)`
   - Tomar las primeras `topN`: `.take(topN)`

6. **Retornar** la lista de textos originales: `.map(_._1.original)`

---

## Módulo 5: `Main.scala` — único módulo con efectos I/O

```
object Main extends App
```

**Flujo:**

1. Leer el path del directorio:
   - Si `args` no está vacío, usar `args(0)`
   - Si está vacío, solicitar al usuario con `scala.io.StdIn.readLine("Ingrese el path del directorio: ")`

2. Llamar `FileLoader.loadFiles(path)` y mostrar cuántos archivos fueron cargados

3. Si la lista está vacía, mostrar mensaje de error y terminar

4. Llamar `Summarizer.summarize(documents)`

5. Imprimir el resultado:
```
=== RESUMEN EXTRACTIVO ===
Archivos procesados: N
Oraciones seleccionadas: M

1. <oración>
2. <oración>
...
```

---

## Orden de Implementación

1. `TextProcessor.scala` — sin dependencias, testeable solo
2. `TFIDFCalculator.scala` — depende solo de los tipos definidos
3. `Summarizer.scala` — orquesta los dos anteriores
4. `FileLoader.scala` — I/O simple e independiente
5. `Main.scala` — conecta todo al final

---

## Notas Adicionales

- Las oraciones del resumen se presentan **ordenadas por puntaje** (mayor relevancia primero), no por orden de aparición
- Si dos oraciones tienen el mismo puntaje, el orden entre ellas es indistinto
- El valor de `topN` por defecto es `10`; si hay menos de 10 oraciones válidas, retornar todas
- No se requieren librerías externas; usar solo la biblioteca estándar de Scala/Java
