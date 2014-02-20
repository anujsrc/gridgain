// @scala.file.header

/*
 * ___    _________________________ ________
 * __ |  / /____  _/__  ___/__  __ \___  __ \
 * __ | / /  __  /  _____ \ _  / / /__  /_/ /
 * __ |/ /  __/ /   ____/ / / /_/ / _  _, _/
 * _____/   /___/   /____/  \____/  /_/ |_|
 *
 */

package org.gridgain.visor.commands.tasks

import org.scalatest._
import matchers._
import org.gridgain.visor._
import VisorTasksCommand._
import org.gridgain.grid._
import scala.collection._
import org.gridgain.grid.events.GridEventType
import GridEventType._
import JavaConversions._
import org.gridgain.grid.compute.{GridComputeJob, GridComputeJobAdapter, GridComputeTaskNoReduceSplitAdapter}

/**
 * Unit test for 'tasks' command.
 *
 * @author @java.author
 * @version @java.version
 */
class VisorTasksCommandSpec extends FlatSpec with ShouldMatchers with BeforeAndAfterAll {
    /**
     * Open visor and execute several tasks before all tests.
     */
    override def beforeAll() {
        GridGain.start(config("grid-1"))
        GridGain.start(config("grid-2"))

        visor.open("-d", false)

        try {
            val compute = visor.grid.compute()
            val fut1 = compute.withName("TestTask1").execute(new TestTask1(), null, 0)
            val fut2 = compute.withName("TestTask1").execute(new TestTask1(), null, 0)
            val fut3 = compute.withName("TestTask1").execute(new TestTask1(), null, 0)
            val fut4 = compute.withName("TestTask2").execute(new TestTask2(), null, 0)
            val fut5 = compute.withName("Test3").execute(new Test3(), null, 0)

            fut1.get
            fut2.get
            fut3.get
            fut4.get
            fut5.get
        }
        catch {
            case _: Exception =>
        }
    }

    /**
     * Creates grid configuration for provided grid host.
     *
     * @param name Grid name.
     * @return Grid configuration.
     */
    private def config(name: String): GridConfiguration = {
        val cfg = new GridConfiguration

        cfg.setGridName(name)
        cfg.setLifeCycleEmailNotification(false)
        cfg.setIncludeEventTypes(EVTS_ALL: _*)

        cfg
    }

    /**
     * Close visor after all tests.
     */
    override def afterAll() {
        visor.close()

        GridGain.stopAll(false)
    }

    behavior of "A 'tasks' visor command"

    it should "print tasks when called w/o arguments" in {
        visor.tasks()
    }

    it should "print error message with incorrect argument" in {
        visor.tasks("-xx")
    }

    it should "print task summary when called for specific task" in {
        visor.tasks("-n=@t1")
    }

    it should "print execution when called for specific execution" in {
        visor.tasks("-e=@e1")
    }

    it should "print all tasks" in {
        visor.tasks("-l")
    }

    it should "print all tasks and executions" in {
        visor.tasks("-l -a")
    }

    it should "print tasks that started during last 5 seconds" in {
        visor.tasks("-l -t=5s")
    }

    it should "print error message about invalid time period" in {
        visor.tasks("-l -t=x2s")
    }

    it should "print error message about negative time period" in {
        visor.tasks("-l -t=-10s")
    }

    it should "print error message about invalid time period specification" in {
        visor.tasks("-l -t=10x")
    }

    it should "print task summary for the first task" in {
        visor.tasks("-n=TestTask1")
    }

    it should "print task summary and executions for the first task" in {
        visor.tasks("-n=TestTask1 -a")
    }

    it should "print list of tasks grouped by nodes" in {
        visor.tasks("-g")
    }

    it should "print list of tasks that started during last 5 minutes grouped by nodes" in {
        visor.tasks("-g -t=5m")
    }

    it should "print list of tasks grouped by hosts" in {
        visor.tasks("-h")
    }

    it should "print list of tasks that started during last 5 minutes grouped by hosts" in {
        visor.tasks("-h -t=5m")
    }

    it should "print list of tasks filtered by substring" in {
        visor.tasks("-s=TestTask")
    }

    it should "print list of tasks and executions filtered by substring" in {
        visor.tasks("-s=TestTask -a")
    }
}

/**
 * Test task 1.
 *
 * @author @java.author
 * @version @java.version
 */
private class TestTask1 extends GridComputeTaskNoReduceSplitAdapter[String] {
    def split(gridSize: Int, arg: String): java.util.Collection[_ <: GridComputeJob] = {
        Iterable.fill(gridSize)(new GridComputeJobAdapter() {
            def execute() = {
                println("Task 1")

                null
            }
        })
    }
}

/**
 * Test task 2.
 *
 * @author @java.author
 * @version @java.version
 */
private class TestTask2 extends GridComputeTaskNoReduceSplitAdapter[String] {
    def split(gridSize: Int, arg: String): java.util.Collection[_ <: GridComputeJob] = {
        Iterable.fill(gridSize)(new GridComputeJobAdapter() {
            def execute() = {
                println("Task 2")

                null
            }
        })
    }
}

/**
 * Test task 3 (w/o 'Task' in host for testing '-s' option).
 *
 * @author @java.author
 * @version @java.version
 */
private class Test3 extends GridComputeTaskNoReduceSplitAdapter[String] {
    def split(gridSize: Int, arg: String): java.util.Collection[_ <: GridComputeJob] = {
        Iterable.fill(gridSize)(new GridComputeJobAdapter() {
            def execute() = {
                println("Task 3")

                null
            }
        })
    }
}