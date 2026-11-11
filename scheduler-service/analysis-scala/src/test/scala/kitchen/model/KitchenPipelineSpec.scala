package kitchen.model

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import u07.utils.MSet
import u07.modeling.SPN
import u07.modeling.CTMCSimulation.*
import java.util.Random
import KitchenPipeline.*

class KitchenPipelineSpec extends AnyFunSuite:

  private def marking(places: Place*): MSet[Place] = MSet.ofList(places.toList)

  private val prepThenCook =
    Config(Route.PrepThenCook, prepWorkers = 1, cookStationCapacities = List(3), Map(1 -> 1.0))
  private val prepThenCookCtmc = SPN.toCTMC(KitchenPipeline.spn(prepThenCook, KitchenPipeline.Rates(1.0, 1.0, 1.0, 1.0)))

  test("admission is not offered once the span's quota is exhausted"):
    val m = marking(WorkerFree, Room(0), Room(0), Room(0))
    prepThenCookCtmc.transitions(m) shouldBe empty

  test("admission consumes the quota, routes to the route's first stage, and records Admitted"):
    val m = KitchenPipeline.initialMarking(prepThenCook, admissionQuota = 3)
    val actions = prepThenCookCtmc.transitions(m)
    actions.map(_.state).exists { s =>
      s(Admission) == 2 && s(AwaitingPrep) == 1 && s(Admitted) == 1
    } shouldBe true

  test("prep starts only once a worker is free and something is awaiting prep"):
    val m = marking(AwaitingPrep, WorkerFree)
    val actions = prepThenCookCtmc.transitions(m)
    actions.map(_.state) should contain(marking(WorkerPrepping))

  test("prep completion rate scales with occupancy and routes to the configured output"):
    val m = marking(WorkerPrepping, WorkerPrepping)
    val actions = prepThenCookCtmc.transitions(m)
    actions.exists(a => a.rate == 2.0 && a.state == marking(WorkerPrepping, WorkerFree, AwaitingCook)) shouldBe true

  test("loading starts only once a worker is free, something is awaiting cook, and this station's own slot and room are free"):
    val m = marking(AwaitingCook, WorkerFree, StationLoadFree(0), Room(0))
    val actions = prepThenCookCtmc.transitions(m)
    // Starting a session places the first item immediately, as part of the same step,
    // and the item takes its place in the oven.
    actions.map(_.state) should contain(marking(StationLoading(0), Baking(0, 0)))

  test("loading never even starts at a station with no room left, however much demand or staffing there is"):
    val m = marking(AwaitingCook, WorkerFree, StationLoadFree(0))
    val actions = prepThenCookCtmc.transitions(m)
    actions.map(_.state).exists(_(StationLoading(0)) > 0) shouldBe false

  test("a worker holds the station across several items, one real-time step each, instead of releasing in between"):
    val m = marking(StationLoading(0), AwaitingCook, Room(0))
    val actions = prepThenCookCtmc.transitions(m)
    // The worker and the slot are never released here: the session just continues.
    actions.exists(a => a.rate == 1.0 && a.state == marking(StationLoading(0), Baking(0, 0))) shouldBe true

  test("a session releases the worker and the slot, instantly, once there's no more demand"):
    val m = marking(StationLoading(0))
    val actions = prepThenCookCtmc.transitions(m)
    actions.exists(a => a.rate == 1e6 && a.state == marking(WorkerFree, StationLoadFree(0))) shouldBe true

  test("a session releases the worker and the slot, instantly, once there's no more room, even with demand left"):
    val m = marking(StationLoading(0), AwaitingCook, AwaitingCook)
    val actions = prepThenCookCtmc.transitions(m)
    actions.exists(a => a.rate == 1e6 && a.state == marking(AwaitingCook, AwaitingCook, WorkerFree, StationLoadFree(0))) shouldBe true

  test("a worker busy prepping doesn't block or inflate the rate of a station's independent loading session"):
    val m = marking(WorkerPrepping, StationLoading(0), AwaitingCook, Room(0))
    val actions = cookThenPrepCtmc.transitions(m)
    actions.exists(a => a.rate == 1.0 && a.state == marking(StationLoading(0), AwaitingCook, Room(0), WorkerFree, Exit)) shouldBe true
    actions.exists(a => a.rate == 1.0 && a.state == marking(WorkerPrepping, StationLoading(0), Baking(0, 0))) shouldBe true

  test("prepping and starting to load race for the same shared worker pool, not separate ones"):
    val m = marking(AwaitingPrep, AwaitingCook, WorkerFree, StationLoadFree(0), Room(0))
    val actions = prepThenCookCtmc.transitions(m)
    // Either duty can claim the single free worker; once it does, the other is left waiting.
    actions.map(_.state) should contain(marking(AwaitingCook, StationLoadFree(0), Room(0), WorkerPrepping))
    actions.map(_.state) should contain(marking(AwaitingPrep, StationLoading(0), Baking(0, 0)))

  test("a station's loading slot stays exclusive even with spare workers free"):
    // Station 0 already has a load in progress; a second free worker and more demand
    // don't open a second one at the *same* station -- there's only one slot.
    val m = marking(StationLoading(0), AwaitingCook, WorkerFree, WorkerFree)
    val actions = prepThenCookCtmc.transitions(m)
    actions.map(_.state).exists(_(StationLoading(0)) >= 2) shouldBe false

  test("loading at different stations runs independently, so it can happen in parallel"):
    val twoStations =
      Config(Route.PrepThenCook, prepWorkers = 2, cookStationCapacities = List(3, 3), Map(1 -> 1.0))
    val ctmc = SPN.toCTMC(KitchenPipeline.spn(twoStations, KitchenPipeline.Rates(1.0, 1.0, 1.0, 1.0)))
    val m = marking(
      AwaitingCook, AwaitingCook, WorkerFree, WorkerFree, StationLoadFree(0), StationLoadFree(1), Room(0), Room(1),
    )
    val actions = ctmc.transitions(m)
    actions.map(_.state) should contain(
      marking(AwaitingCook, WorkerFree, StationLoadFree(1), Room(1), StationLoading(0), Baking(0, 0)),
    )
    actions.map(_.state) should contain(
      marking(AwaitingCook, WorkerFree, StationLoadFree(0), Room(0), StationLoading(1), Baking(1, 0)),
    )

  test("every place bakes independently: the completion rate scales with the items in the oven"):
    val m = marking(Baking(0, 0), Baking(0, 0), Baking(0, 0))
    val actions = prepThenCookCtmc.transitions(m)
    // One phase (the default): a bake completes in one step, freeing its place.
    actions.exists(a => a.rate == 3.0 && a.state == marking(Baking(0, 0), Baking(0, 0), Room(0), Exit)) shouldBe true

  private val erlangBake =
    Config(Route.PrepThenCook, prepWorkers = 1, cookStationCapacities = List(3), Map(1 -> 1.0), bakePhases = 3)
  private val erlangBakeCtmc = SPN.toCTMC(KitchenPipeline.spn(erlangBake, KitchenPipeline.Rates(1.0, 1.0, 1.0, 1.0)))

  test("with several phases, a bake moves through each at phases times the cook rate before freeing its place"):
    erlangBakeCtmc.transitions(marking(Baking(0, 0), Baking(0, 0))).toList.map(a => (a.rate, a.state)) shouldBe
      List((6.0, marking(Baking(0, 0), Baking(0, 1))))
    erlangBakeCtmc.transitions(marking(Baking(0, 2))).toList.map(a => (a.rate, a.state)) shouldBe
      List((3.0, marking(Room(0), Exit)))

  test("an Erlang bake keeps the mean bake time but cuts its spread to 1 / sqrt(phases)"):
    val phases = 4
    val cookRate = 0.2
    val config = Config(Route.PrepThenCook, prepWorkers = 1, cookStationCapacities = List(1), Map(1 -> 1.0), phases)
    val ctmc = SPN.toCTMC(KitchenPipeline.spn(config, KitchenPipeline.Rates(1.0, 1.0, 1.0, cookRate)))
    val rnd = new Random(11)
    val bakes = (1 to 2000).map { _ =>
      ctmc.newSimulationTrace(marking(Baking(0, 0)), rnd).find(_.state(Exit) == 1).get.time
    }
    val mean = bakes.sum / bakes.size
    val sd = math.sqrt(bakes.map(b => (b - mean) * (b - mean)).sum / (bakes.size - 1))
    mean shouldBe (5.0 +- 0.2)
    (sd / mean) shouldBe (0.5 +- 0.05)

  private val cookThenPrep =
    Config(Route.CookThenPrep, prepWorkers = 1, cookStationCapacities = List(1), Map(1 -> 1.0))
  private val cookThenPrepCtmc = SPN.toCTMC(KitchenPipeline.spn(cookThenPrep, KitchenPipeline.Rates(1.0, 1.0, 1.0, 1.0)))

  test("for a CookThenPrep route, admission routes to AwaitingCook, loading puts it in the oven, cook delivers to AwaitingPrep, prep delivers to Exit"):
    val admitted = KitchenPipeline.initialMarking(cookThenPrep, admissionQuota = 1)
    cookThenPrepCtmc.transitions(admitted).map(_.state).exists(_(AwaitingCook) == 1) shouldBe true

    val awaitingCook = marking(AwaitingCook, WorkerFree, StationLoadFree(0), Room(0))
    // Starting the session puts the first item in the oven immediately, in the same step.
    cookThenPrepCtmc.transitions(awaitingCook).map(_.state) should contain(marking(StationLoading(0), Baking(0, 0)))

    val baking = marking(Baking(0, 0))
    cookThenPrepCtmc.transitions(baking).map(_.state) should contain(marking(Room(0), AwaitingPrep))

    val prepping = marking(WorkerPrepping)
    cookThenPrepCtmc.transitions(prepping).map(_.state) should contain(marking(WorkerFree, Exit))

  test("end to end: an admitted order reaches Exit for a PrepThenCook route"):
    val rnd = new Random(7)
    val m0 = KitchenPipeline.initialMarking(prepThenCook, admissionQuota = 1)
    val trace = prepThenCookCtmc.newSimulationTrace(m0, rnd).takeWhile(_.time <= 5.0)
    trace.exists(_.state(Exit) >= 1) shouldBe true

  test("end to end: an admitted order reaches Exit for a CookThenPrep route"):
    val rnd = new Random(7)
    val m0 = KitchenPipeline.initialMarking(cookThenPrep, admissionQuota = 1)
    val trace = cookThenPrepCtmc.newSimulationTrace(m0, rnd).takeWhile(_.time <= 5.0)
    trace.exists(_.state(Exit) >= 1) shouldBe true

  test("resetAdmission overwrites the quota, discarding leftover, and leaves everything else untouched"):
    val m = marking(Admission, Admission, WorkerFree, Room(0))
    val reset = KitchenPipeline.resetAdmission(m, quota = 5)
    reset(Admission) shouldBe 5
    reset(WorkerFree) shouldBe 1
    reset(Room(0)) shouldBe 1

  test("places hash by content, so a seed gives the same simulation in any process"):
    // An identity hash code would essentially never equal the hash of the case's name.
    Admission.hashCode shouldBe "Admission".hashCode
    Baking(0, 1).hashCode shouldBe Baking(0, 1).hashCode
    Baking(0, 1).hashCode should not be Baking(0, 2).hashCode
