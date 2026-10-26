package kitchen.agent

/**
 * Not part of the vendored course material (see u08.modeling.CTMCExperiment's header
 * for why): the PRISM-style confidence bound for a statistic sampled over many runs,
 * bounded in [0,1] (a probability, or — via Hoeffding's inequality, which holds for any
 * iid sample in [0,1], not just Bernoulli outcomes — a fraction of time). Same formula
 * PRISM's own statistical model checking uses: N = ceil(ln(2/delta) / (2*epsilon^2)),
 * delta = 1 - confidence (08-StochasticAnalysis, "Approximate Modelchecking").
 */
object Statistics:

  def requiredRuns(epsilon: Double, confidence: Double): Int =
    val delta = 1 - confidence
    math.ceil(math.log(2 / delta) / (2 * epsilon * epsilon)).toInt

  def epsilonFor(runs: Int, confidence: Double): Double =
    val delta = 1 - confidence
    math.sqrt(math.log(2 / delta) / (2 * runs))
