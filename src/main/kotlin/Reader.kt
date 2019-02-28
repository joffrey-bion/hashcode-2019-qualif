import org.hildan.hashcode.utils.reader.HCReader
import java.lang.IllegalStateException

fun HCReader.readProblem(): Problem {
    val nPics = readInt()
    val photos = List(nPics) { readPhoto(it) }
    return Problem(photos)
}

private fun HCReader.readPhoto(id: Int): Photo {
    val orientation = readOrientation()
    val nTags = readInt()
    val tags = HashSet(List(nTags) { readString() })
    return Photo(id, orientation, tags)
}

fun HCReader.readOrientation(): Orientation = when (readString()) {
    "H" -> Orientation.HORIZONTAL
    "V" -> Orientation.VERTICAL
    else -> throw IllegalStateException("expected H or V")
}
