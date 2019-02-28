import org.hildan.hashcode.utils.solveHCProblemAndWriteFile
import java.nio.file.Paths

fun main(args: Array<String>) {
    val inputPath = Paths.get("inputs/problem1.in")
    val outputPath = Paths.get("problem1.out")
    solveHCProblemAndWriteFile(inputPath, outputPath) {
        readProblem().solve()
    }
}

class Photo(
    val id: Int,
    val orientation: Orientation,
    val tags: Set<String>
)

enum class Orientation { HORIZONTAL, VERTICAL }

class Problem(
    val photos: List<Photo>
) {
    val hPhotos = photos.filter { it.orientation == Orientation.HORIZONTAL }
    val vPhotos = photos.filter { it.orientation == Orientation.VERTICAL }

    fun solve(): List<String> {
        val vSlides = pairVPics(vPhotos)

        // solve the problem here

        // write solution into lines (this is problem-specific)
        val lines = mutableListOf<String>()
        lines.add("output line 0")
        lines.add("output line 1")
        return lines
    }
}

private fun pairVPics(vPics: List<Photo>): List<VSlide> {
    val takenPics = HashSet<Int>()
    val vSlides = mutableListOf<VSlide>()
    for (p in vPics) {
        takenPics.add(p.id)
        val bestSlide = pairUp(p, vPics, takenPics)
        if (bestSlide != null) {
            takenPics.add(bestSlide.pic2.id)
            vSlides.add(bestSlide)
        }
    }
    return vSlides
}

private fun pairUp(p: Photo, vPics: List<Photo>, takenPics: Set<Int>): VSlide? {
    var bestSize = 0
    var bestMate: Photo? = null
    var bestTags: Set<String>? = null
    vPics.filterNot { it.id in takenPics }.forEach {
        val unionTags = (p.tags + it.tags)
        if (unionTags.size > bestSize) {
            bestSize = unionTags.size
            bestMate = it
            bestTags = unionTags
        }
    }
    if (bestMate == null || bestTags == null) {
        return null
    }
    return VSlide(p, bestMate!!, bestTags!!)
}

data class VSlide(val pic1: Photo, val pic2: Photo, val tags: Set<String>)
