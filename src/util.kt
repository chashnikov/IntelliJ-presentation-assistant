/**
 * @author nik
 */
//todo[nik] move to Kotlin std lib?

public inline fun <T: Any, R: Comparable<R>> Array<out T>.minBy(f: (T) -> R): T? {
    var minElem: T? = null
    var minValue: R? = null
    for (e in this) {
        val v = f(e)
        if (minValue == null || v.compareTo(minValue!!) < 0) {
           minElem = e
           minValue = v
        }
    }
    return minElem
}

public inline fun Iterable<Int>.sum(): Int = this.fold(0, {a,b->a+b})