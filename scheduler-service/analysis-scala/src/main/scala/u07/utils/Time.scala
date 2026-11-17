// Vendored verbatim from ../../../asmd-public-models (src/main/scala/u07/utils/Time.scala,
// commit 3297344). Not modified — see analysis-scala/README.md for the reuse rationale.
// Used to time CapacityAdvisor.adviseCapacity in kitchen/JsApi.scala.
package u07.utils

object Time {

  // facility to track time, just embed the computation in the input
  def timed[A](v: =>A):A = {
    val t0 = java.lang.System.nanoTime
    try{ v } finally println("Timed op (msec): "+(java.lang.System.nanoTime-t0)/1000000)
  }
}
