/* 
 Copyright (C) GridGain Systems. All Rights Reserved.
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

/*
 * ________               ______                    ______   _______
 * __  ___/_____________ ____  /______ _________    __/__ \  __  __ \
 * _____ \ _  ___/_  __ `/__  / _  __ `/__  ___/    ____/ /  _  / / /
 * ____/ / / /__  / /_/ / _  /  / /_/ / _  /        _  __/___/ /_/ /
 * /____/  \___/  \__,_/  /_/   \__,_/  /_/         /____/_(_)____/
 *
 */

package org.gridgain.scalar

import java.net.URL
import org.springframework.context.ApplicationContext
import org.jetbrains.annotations.Nullable
import java.util.UUID
import org.gridgain.grid._
import org.gridgain.grid.cache._
import org.gridgain.grid.{GridGain => G}
import org.gridgain.grid.util.{GridUtils => U}
import query._
import annotation.target.field
import org.gridgain.grid.dataload.GridDataLoader

/**
 * {{{
 * ________               ______                    ______   _______
 * __  ___/_____________ ____  /______ _________    __/__ \  __  __ \
 * _____ \ _  ___/_  __ `/__  / _  __ `/__  ___/    ____/ /  _  / / /
 * ____/ / / /__  / /_/ / _  /  / /_/ / _  /        _  __/___/ /_/ /
 * /____/  \___/  \__,_/  /_/   \__,_/  /_/         /____/_(_)____/
 *
 * }}}
 *
 * ==Overview==
 * `scalar` is the main object that encapsulates Scalar DSL. It includes global functions
 * on "scalar" keyword, helper converters as well as necessary implicit conversions. `scalar` also
 * mimics many methods in `GridFactory` class from Java side.
 *
 * The idea behind Scalar DSL - '''zero additional logic and only conversions''' implemented
 * using Scala "Pimp" pattern. Note that most of the Scalar DSL development happened on Java
 * side of GridGain 3.0 product line - Java APIs had to be adjusted quite significantly to
 * support natural adaptation of functional APIs. That basically means that all functional
 * logic must be available on Java side and Scalar only provides conversions from Scala
 * language constructs to Java constructs. Note that currently GridGain supports Scala 2.8
 * and up only.
 *
 * This design approach ensures that Java side does not starve and usage paradigm
 * is mostly the same between Java and Scala - yet with full power of Scala behind.
 * In other words, Scalar only adds Scala specifics, but not greatly altering semantics
 * of how GridGain APIs work. Most of the time the code in Scalar can be written in
 * Java in almost the same number of lines.
 *
 * ==Suffix '$' In Names==
 * Symbol `$` is used in names when they conflict with the names in the base Java class
 * that Scala pimp is shadowing or with Java package name that your Scala code is importing.
 * Instead of giving two different names to the same function we've decided to simply mark
 * Scala's side method with `$` suffix.
 *
 * ==Importing==
 * Scalar needs to be imported in a proper way so that necessary objects and implicit
 * conversions got available in the scope:
 * <pre name="code" class="scala">
 * import org.gridgain.scalar._
 * import scalar._
 * </pre>
 * This way you import object `scalar` as well as all methods declared or inherited in that
 * object as well.
 *
 * ==Examples==
 * Here are few short examples of how Scalar can be used to program routine distributed
 * task. All examples below use default GridGain configuration and default grid. All these
 * examples take an implicit advantage of auto-discovery and failover, load balancing and
 * collision resolution, zero deployment and many other underlying technologies in the
 * GridGain - while remaining absolutely distilled to the core domain logic.
 *
 * This code snippet prints out full topology:
 * <pre name="code" class="scala">
 * scalar {
 *     grid$ foreach (n => println("Node: " + n.id8))
 * }
 * </pre>
 * The obligatory example - cloud enabled `Hello World!`. It splits the phrase
 * into multiple words and prints each word on a separate grid node:
 * <pre name="code" class="scala">
 * scalar {
 *     grid$ *< (SPREAD, (for (w <- "Hello World!".split(" ")) yield () => println(w)))
 * }
 * </pre>
 * This example broadcasts message to all nodes:
 * <pre name="code" class="scala">
 * scalar {
 *     grid$ *< (BROADCAST, () => println("Broadcasting!!!"))
 * }
 * </pre>
 * This example "greets" remote nodes only (note usage of Java-side closure):
 * <pre name="code" class="scala">
 * scalar {
 *     val me = grid$.localNode.id
 *     grid$.remoteProjection() *< (BROADCAST, F.println("Greetings from: " + me))
 * }
 * </pre>
 *
 * Next example creates a function that calculates lengths of the string
 * using MapReduce type of processing by splitting the input string into
 * multiple substrings, calculating each substring length on the remote
 * node and aggregating results for the final length of the original string:
 * <pre name="code" class="scala">
 * def count(msg: String) =
 *     grid$ @< (SPREAD, for (w <- msg.split(" ")) yield () => w.length, (s: Seq[Int]) => s.sum)
 * </pre>
 * This example shows a simple example of how Scalar can be used to work with in-memory data grid:
 * <pre name="code" class="scala">
 * scalar {
 *     val t = cache$[Symbol, Double]("partitioned")
 *     t += ('symbol -> 2.0)
 *     t -= ('symbol)
 * }
 * </pre>
 *
 * @author @java.author
 * @version @java.version
 */
object scalar extends ScalarConversions {
    /** Visor copyright blurb. */
    private val COPYRIGHT = /*@scala.copyright*/"Copyright (C) 2013 GridGain Systems."

    /** Visor version number. */
    private val VER = /*@scala.version*/"x.x.xx"

    /** Visor build number. */
    private val BUILD = /*@scala.build*/"DDMMYYYY"

    /** Type alias for `GridCacheQuerySqlField`. */
    type ScalarCacheQuerySqlField = GridCacheQuerySqlField @field

    /** Type alias for `GridCacheQueryTextField`. */
    type ScalarCacheQueryTextField = GridCacheQueryTextField @field

    /**
     * Prints Scalar ASCII-logo.
     */
    def logo() {
        val NL = System getProperty "line.separator"

        val s =
            " ________               ______                 " + NL +
            " __  ___/_____________ ____  /______ _________ " + NL +
            " _____ \\ _  ___/_  __ `/__  / _  __ `/__  ___/ " + NL +
            " ____/ / / /__  / /_/ / _  /  / /_/ / _  /     " + NL +
            " /____/  \\___/  \\__,_/  /_/   \\__,_/  /_/      " + NL + NL +
            " GRIDGAIN SCALAR" +
            " " + COPYRIGHT + NL

        println(s)
    }

    /**
     * Prints help for REPL.
     */
    def replHelp() {
        val t = ScalarTextTable()

        t #= ("Command", "Description")

        t += ("scalar start", "Starts GridGain with the default configurtaion.")
        t += ("scalar stop", "Stops GridGain.")
        t += ("scalar start(\"path/to/my/config.xml\")", "Starts GridGain with the provided configurtaion file.")
        t += ("grid$", "Global grid projection (including all nodes).")
        t += ("cache$", "Default cache.")
        t += ("scalar ?", "Prints this help.")

        println("Some of the key Scalar command:")
        t.render()
        println()
        println("Example:")
        println("    scala> scalar start")
        println("    scala> grid$ *< (BROADCAST, () => println(\"Howdy!\"))")
    }

    /**
     * Help shortcut.
     */
    def `?` {
        replHelp()
    }

    /**
     * Note that grid instance will be stopped with cancel flat set to `true`.
     *
     * @param g Grid instance.
     * @param body Closure with grid instance as body's parameter.
     */
    private def init[T](g: Grid, body: Grid => T): T = {
        assert(g != null, body != null)

        try {
            body(g)
        }
        finally {
            GridGain.stop(g.name, true)
        }
    }

    /**
     * Note that grid instance will be stopped with cancel flat set to `true`.
     *
     * @param g Grid instance.
     * @param body Passed by name body.
     */
    private def init0[T](g: Grid, body: => T): T = {
        assert(g != null)

        try {
            body
        }
        finally {
            GridGain.stop(g.name, true)
        }
    }

    /**
     * Executes given closure within automatically managed default grid instance.
     * If default grid is already started the passed in closure will simply
     * execute.
     *
     * @param body Closure to execute within automatically managed default grid instance.
     */
    def apply(body: Grid => Unit) {
        if (!isStarted) init(GridGain.start, body) else body(grid$)
    }

    /**
     * Executes given closure within automatically managed default grid instance.
     * If default grid is already started the passed in closure will simply
     * execute.
     *
     * @param body Closure to execute within automatically managed default grid instance.
     */
    def apply[T](body: Grid => T): T =
        if (!isStarted) init(GridGain.start, body) else body(grid$)

    /**
     * Executes given closure within automatically managed default grid instance.
     * If default grid is already started the passed in closure will simply
     * execute.
     *
     * @param body Closure to execute within automatically managed default grid instance.
     */
    def apply[T](body: => T): T =
        if (!isStarted) init0(GridGain.start, body) else body

    /**
     * Executes given closure within automatically managed default grid instance.
     * If default grid is already started the passed in closure will simply
     * execute.
     *
     * @param body Closure to execute within automatically managed grid instance.
     */
    def apply(body: => Unit) {
        if (!isStarted) init0(GridGain.start, body) else body
    }

    /**
     * Executes given closure within automatically managed grid instance.
     *
     * @param springCfgPath Spring XML configuration file path or URL.
     * @param body Closure to execute within automatically managed grid instance.
     */
    def apply(springCfgPath: String)(body: => Unit) {
        init0(GridGain.start(springCfgPath), body)
    }

    /**
     * Executes given closure within automatically managed grid instance.
     *
     * @param cfg Grid configuration instance.
     * @param body Closure to execute within automatically managed grid instance.
     */
    def apply(cfg: GridConfiguration)(body: => Unit) {
        init0(GridGain.start(cfg), body)
    }

    /**
     * Executes given closure within automatically managed grid instance.
     *
     * @param cfg Spring XML configuration file path or URL.
     * @param springCtx Optional Spring application context.
     * @param body Closure to execute within automatically managed grid instance.
     */
    def apply(cfg: GridConfiguration, springCtx: ApplicationContext)(body: => Unit) {
        init0(GridGain.start(cfg, springCtx), body)
    }

    /**
     * Executes given closure within automatically managed grid instance.
     *
     * @param springCfgPath Spring XML configuration file path or URL.
     * @param springCtx Optional Spring application context.
     * @param body Closure to execute within automatically managed grid instance.
     */
    def apply(springCfgPath: String, springCtx: ApplicationContext)(body: => Unit) {
        init0(GridGain.start(springCfgPath, springCtx), body)
    }

    /**
     * Executes given closure within automatically managed grid instance.
     *
     * @param springCtx Optional Spring application context.
     * @param body Closure to execute within automatically managed grid instance.
     */
    def apply(springCtx: ApplicationContext)(body: => Unit) {
        init0(GridGain.start(springCtx), body)
    }

    /**
     * Executes given closure within automatically managed grid instance.
     *
     * @param springCtx Optional Spring application context.
     * @param springCfgUrl Spring XML configuration file URL.
     * @param body Closure to execute within automatically managed grid instance.
     */
    def apply(springCfgUrl: URL, springCtx: ApplicationContext)(body: => Unit) {
        init0(GridGain.start(springCfgUrl, springCtx), body)
    }

    /**
     * Executes given closure within automatically managed grid instance.
     *
     * @param springCfgUrl Spring XML configuration file URL.
     * @param body Closure to execute within automatically managed grid instance.
     */
    def apply(springCfgUrl: URL)(body: => Unit) {
        init0(GridGain.start(springCfgUrl), body)
    }

    /**
     * Gets default cache.
     *
     * Note that you always need to provide types when calling
     * this function - otherwise Scala will create `GridCache[Nothing, Nothing]`
     * typed instance that cannot be used.
     */
    @inline def cache$[K, V]: Option[GridCache[K, V]] =
        Option(GridGain.grid.cache[K, V](null))

    /**
     * Gets named cache from default grid.
     *
     * @param cacheName Name of the cache to get.
     */
    @inline def cache$[K, V](@Nullable cacheName: String): Option[GridCache[K, V]] =
        Option(GridGain.grid.cache(cacheName))

    /**
     * Gets named cache from specified grid.
     *
     * @param gridName Name of the grid.
     * @param cacheName Name of the cache to get.
     */
    @inline def cache$[K, V](@Nullable gridName: String, @Nullable cacheName: String): Option[GridCache[K, V]] =
        grid$(gridName) match {
            case Some(g) => Option(g.cache(cacheName))
            case None => None
        }

    /**
     * Gets a new instance of data loader associated with given cache name.
     *
     * @param cacheName Cache name (`null` for default cache).
     * @param bufSize Per node buffer size.
     * @return New instance of data loader.
     */
    @inline def dataLoader$[K, V](
        @Nullable cacheName: String,
        bufSize: Int): GridDataLoader[K, V] = {
        val dl = grid$.dataLoader[K, V](cacheName)

        dl.perNodeBufferSize(bufSize)

        dl
    }

    /**
     * Gets default grid instance.
     */
    @inline def grid$: Grid = GridGain.grid

    /**
     * Gets node ID as ID8 string.
     */
    def nid8$(node: GridNode) = node.id().toString.take(8).toUpperCase

    /**
     * Gets named grid.
     *
     * @param name Grid name.
     */
    @inline def grid$(@Nullable name: String): Option[Grid] =
        try {
            Option(GridGain.grid(name))
        }
        catch {
            case _: IllegalStateException => None
        }

    /**
     * Gets grid for given node ID.
     *
     * @param locNodeId Local node ID for which to get grid instance option.
     */
    @inline def grid$(locNodeId: UUID): Option[Grid] = {
        assert(locNodeId != null)

        try {
            Option(GridGain.grid(locNodeId))
        }
        catch {
            case _: IllegalStateException => None
        }
    }

    /**
     * Tests if specified grid is started.
     *
     * @param name Gird name.
     */
    def isStarted(@Nullable name: String) =
        GridGain.state(name) == GridGainState.STARTED

    /**
     * Tests if specified grid is stopped.
     *
     * @param name Gird name.
     */
    def isStopped(@Nullable name: String) =
        GridGain.state(name) == GridGainState.STOPPED

    /**
     * Tests if default grid is started.
     */
    def isStarted =
        GridGain.state == GridGainState.STARTED

    /**
     * Tests if default grid is stopped.
     */
    def isStopped =
        GridGain.state == GridGainState.STOPPED

    /**
     * Stops given grid and specified cancel flag.
     * If specified grid is already stopped - it's no-op.
     *
     * @param name Grid name to cancel.
     * @param cancel Whether or not to cancel all currently running jobs.
     */
    def stop(@Nullable name: String, cancel: Boolean) =
        if (isStarted(name))
            GridGain.stop(name, cancel)

    /**
     * Stops default grid with given cancel flag.
     * If default grid is already stopped - it's no-op.
     *
     * @param cancel Whether or not to cancel all currently running jobs.
     */
    def stop(cancel: Boolean) =
        if (isStarted) GridGain.stop(cancel)

    /**
     * Stops default grid with cancel flag set to `true`.
     * If default grid is already stopped - it's no-op.
     */
    def stop() =
        if (isStarted) GridGain.stop(true)

    /**
     * Sets daemon flag to grid factory. Note that this method should be called
     * before grid instance starts.
     *
     * @param f Daemon flag to set.
     */
    def daemon(f: Boolean) {
        GridGain.setDaemon(f)
    }

    /**
     * Gets daemon flag set in the grid factory.
     */
    def isDaemon =
        GridGain.isDaemon

    /**
     *  Starts default grid. It's no-op if default grid is already started.
     *
     *  @return Started grid.
     */
    def start(): Grid = {
        if (!isStarted) GridGain.start else grid$
    }

    /**
     * Starts grid with given parameter(s).
     *
     * @param springCfgPath Spring XML configuration file path or URL.
     * @return Started grid. If Spring configuration contains multiple grid instances,
     *      then the 1st found instance is returned.
     */
    def start(@Nullable springCfgPath: String): Grid = {
        GridGain.start(springCfgPath)
    }

    /**
     * Starts grid with given parameter(s).
     *
     * @param cfg Grid configuration. This cannot be `null`.
     * @return Started grid.
     */
    def start(cfg: GridConfiguration): Grid = {
        GridGain.start(cfg)
    }

    /**
     * Starts grid with given parameter(s).
     *
     * @param cfg Grid configuration. This cannot be `null`.
     * @param ctx Optional Spring application context.
     * @return Started grid.
     */
    def start(cfg: GridConfiguration, @Nullable ctx: ApplicationContext): Grid = {
        GridGain.start(cfg, ctx)
    }

    /**
     * Starts grid with given parameter(s).
     *
     * @param springCfgPath Spring XML configuration file path or URL.
     * @param ctx Optional Spring application context.
     * @return Started grid.
     */
    def start(@Nullable springCfgPath: String = null, @Nullable ctx: ApplicationContext): Grid = {
        GridGain.start(springCfgPath, ctx)
    }

    /**
     * Starts grid with given parameter(s).
     *
     * @param ctx Optional Spring application context.
     * @return Started grid.
     */
    def start(@Nullable ctx: ApplicationContext): Grid = {
        GridGain.start(ctx)
    }

    /**
     * Starts grid with given parameter(s).
     *
     * @param springCfgUrl Spring XML configuration file URL.
     * @param ctx Optional Spring application context.
     * @return Started grid.
     */
    def start(springCfgUrl: URL, @Nullable ctx: ApplicationContext): Grid = {
        GridGain.start(springCfgUrl, ctx)
    }

    /**
     * Starts grid with given parameter(s).
     *
     * @param springCfgUrl Spring XML configuration file URL.
     * @return Started grid.
     */
    def start(springCfgUrl: URL): Grid = {
        GridGain.start(springCfgUrl)
    }
}
