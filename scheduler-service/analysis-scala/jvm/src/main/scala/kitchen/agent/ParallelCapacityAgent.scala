package kitchen.agent

import java.util.Random
import java.util.concurrent.{ExecutorService, Executors}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import CapacityAgent.CandidateEvaluation

/**
 * `CapacityAgent` with a candidate's simulations spread over threads. JVM only, since
 * Scala.js has no threads. Seeds are drawn up front, so a run is reproducible, though
 * not identical to the sequential one.
 */
object ParallelCapacityAgent:

  /** Always `rule.runs` services: with every run dispatched up front, nothing stops early. */
  def evaluate(kitchen: KitchenModel, admissionQuota: Int, assessment: Assessment, rnd: Random)(using
      ec: ExecutionContext,
  ): CandidateEvaluation =
    val runs = assessment.rule.runs
    val caps = assessment.caps
    val seeds = Vector.fill(runs)(rnd.nextLong())
    val futures = seeds.map { seed =>
      Future(ServiceSimulation.simulateService(kitchen, admissionQuota, assessment.slots, new Random(seed)))
    }
    val results = Await.result(Future.sequence(futures), Duration.Inf).map(_.withLeadTime(caps.leadTimeMinutes))
    val averageBreaches = results.count(_.averageDelayMinutes > caps.averageDelayCapMinutes)
    val worstBreaches = results.count(_.worstDelayMinutes > caps.worstDelayCapMinutes)
    CandidateEvaluation(admissionQuota, averageBreaches.toDouble / runs, worstBreaches.toDouble / runs, runs)

  def recommendCapacity(kitchen: KitchenModel, quotas: Range, assessment: Assessment, rnd: Random)(using
      ec: ExecutionContext,
  ): Option[CandidateEvaluation] =
    require(quotas.nonEmpty, "need at least one candidate quota")
    quotas.reverseIterator
      .map(q => evaluate(kitchen, q, assessment, rnd))
      .find(_.meets(assessment.acceptUpTo))

  /** A pool with one thread per core. */
  def newExecutionContext(): (ExecutorService, ExecutionContext) =
    val pool = Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors)
    (pool, ExecutionContext.fromExecutor(pool))
