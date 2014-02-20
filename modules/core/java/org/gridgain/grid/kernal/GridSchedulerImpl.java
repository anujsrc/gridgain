// @java.file.header

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.grid.kernal;

import org.gridgain.grid.*;
import org.gridgain.grid.scheduler.*;
import org.gridgain.grid.util.typedef.internal.*;
import org.jetbrains.annotations.*;

import java.util.concurrent.*;

/**
 * {@link GridScheduler} implementation.
 *
 * @author @java.author
 * @version @java.version
 */
public class GridSchedulerImpl implements GridScheduler {
    /** */
    private final GridKernalContext ctx;

    /**
     * @param ctx Kernal context.
     */
    public GridSchedulerImpl(GridKernalContext ctx) {
        this.ctx = ctx;
    }

    /** {@inheritDoc} */
    @Override public GridFuture<?> runLocal(@Nullable Runnable r) {
        A.notNull(r, "r");

        guard();

        try {
            return ctx.closure().runLocalSafe(r, false);
        }
        finally {
            unguard();
        }
    }

    /** {@inheritDoc} */
    @Override public <R> GridFuture<R> callLocal(@Nullable Callable<R> c) {
        A.notNull(c, "c");

        guard();

        try {
            return ctx.closure().callLocalSafe(c, false);
        }
        finally {
            unguard();
        }
    }

    /** {@inheritDoc} */
    @Override public GridSchedulerFuture<?> scheduleLocal(@Nullable Runnable c, String ptrn) throws GridException {
        guard();

        try {
            return ctx.schedule().schedule(c, ptrn);
        }
        finally {
            unguard();
        }
    }

    /** {@inheritDoc} */
    @Override public <R> GridSchedulerFuture<R> scheduleLocal(@Nullable Callable<R> c, String ptrn)
        throws GridException {
        guard();

        try {
            return ctx.schedule().schedule(c, ptrn);
        }
        finally {
            unguard();
        }
    }

    /**
     * <tt>ctx.gateway().readLock()</tt>
     */
    private void guard() {
        ctx.gateway().readLock();
    }

    /**
     * <tt>ctx.gateway().readUnlock()</tt>
     */
    private void unguard() {
        ctx.gateway().readUnlock();
    }
}