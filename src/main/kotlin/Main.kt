import kotlinx.coroutines.runBlocking
import org.hildan.hashcode.utils.solveHCFilesInParallel
import java.util.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

fun main(args: Array<String>) = runBlocking {
    solveHCFilesInParallel(*args) {
        readProblem().solve()
    }
}

enum class Orientation { HORIZONTAL, VERTICAL }

class Problem(
    val photos: List<Photo>
) {
    val hPhotos = photos.filter { it.orientation == Orientation.HORIZONTAL }.sortedByDescending { it.tags.size }
    val vPhotos = photos.filter { it.orientation == Orientation.VERTICAL }.sortedByDescending { it.tags.size }

    suspend fun solve(): List<String> = coroutineScope {
        val vSlides = pairVPics(vPhotos)
        println("${vSlides.size} vertical slides")
        val slides = hPhotos.map { HSlide(it) } + vSlides
        println("${slides.size} slides")

        val slideshows = slides.chunked(1000) {
            val chunk = ArrayList(it)
            async {
                buildSlideShow(chunk)
            }
        }.map { it.await() }
        val slideshow = slideshows.flatten()

        println("Slideshow built")

        val lines = mutableListOf<String>()
        lines.add("${slideshow.size}")
        lines.addAll(slideshow.map { it.toOutput() })
        lines
    }

    private fun buildSlideShow(slides: List<Slide>): MutableList<Slide> {
        println("Computing points for pairs...")
        val pointsPerSlidePair = computePairPoints(slides)
        println("Points computed for pairs")

        println("Building slideshow...")
        val slideshow = mutableListOf<Slide>()

        val firstSlide = slides.first()
        firstSlide.used = true
        slideshow.add(firstSlide)

        while (true) {
            val s = slideshow.last()
            val next = getBestNext(pointsPerSlidePair[s]!!) ?: break
            next.slide.used = true
            slideshow.add(next.slide)
        }
        return slideshow
    }

    private fun getBestNext(
        queue: PriorityQueue<EvaluatedSlide>
    ): EvaluatedSlide? {
        while (queue.isNotEmpty()) {
            val s = queue.poll()
            if (!s.slide.used) {
                return s
            }
        }
        return null
    }

    private fun computePairPoints(slides: List<Slide>): Map<Slide, PriorityQueue<EvaluatedSlide>> {
        val comparator = compareByDescending<EvaluatedSlide> { it.points }
        val pointsPerSlidePair: MutableMap<Slide, PriorityQueue<EvaluatedSlide>> = mutableMapOf()
        for (i1 in slides.indices) {
            val s1 = slides[i1]
            for (i2 in (i1 + 1) until slides.size) {
                val s2 = slides[i2]
                val s1Queue = pointsPerSlidePair.getOrPut(s1) { PriorityQueue(comparator) }
                val s2Queue = pointsPerSlidePair.getOrPut(s2) { PriorityQueue(comparator) }
                val points = computePoints(s1, s2)
                s1Queue.add(EvaluatedSlide(s2,  points))
                s2Queue.add(EvaluatedSlide(s1,  points))
            }
        }
        return pointsPerSlidePair
    }

    data class EvaluatedSlide(val slide: Slide, val points: Int)

    private fun computePoints(s1: Slide, s2: Slide): Int {
        val intersect = s1.tags.intersect(s2.tags)
        val uniqueS1 = s1.tags.size - intersect.size
        val uniqueS2 = s2.tags.size - intersect.size
        return intersect.size.coerceAtMost(uniqueS1).coerceAtMost(uniqueS2)
    }
}

private fun pairVPics(vPics: List<Photo>): List<VSlide> {
    val takenPics = HashSet<Int>()
    val vSlides = mutableListOf<VSlide>()
    for (p in vPics) {
        if (p.id in takenPics) {
            continue
        }
        takenPics.add(p.id)
        val bestSlide = pairUp(p, vPics, takenPics)
        takenPics.add(bestSlide.pic2.id)
        vSlides.add(bestSlide)
    }
    return vSlides
}

private fun pairUp(p: Photo, vPics: List<Photo>, takenPics: Set<Int>): VSlide {
    var bestSize = 0
    var bestMate: Photo? = null
    var bestTags: Set<String>? = null
    for (it in vPics) {
        if (it.id in takenPics) {
            continue
        }

        val unionTags = (p.tags + it.tags)
        if (unionTags.size == p.tags.size + it.tags.size) {
            return VSlide(p, it, unionTags)
        }
        if (unionTags.size > bestSize) {
            bestSize = unionTags.size
            bestMate = it
            bestTags = unionTags
        }
    }
    return VSlide(p, bestMate!!, bestTags!!)
}

interface Slide {
    val tags: Set<String>
    var used: Boolean
    fun toOutput(): String
}

data class VSlide(
    val pic1: Photo,
    val pic2: Photo,
    override val tags: Set<String>
): Slide {
    override var used: Boolean = false

    override fun toOutput(): String = "${pic1.id} ${pic2.id}"
}

data class HSlide(
    val pic: Photo
): Slide {
    override val tags
        get() = pic.tags
    override var used: Boolean = false

    override fun toOutput(): String = "${pic.id}"
}

data class Photo(
    val id: Int,
    val orientation: Orientation,
    val tags: Set<String>
)
