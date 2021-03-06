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

/*  _________        _____ __________________        _____
*  __  ____/___________(_)______  /__  ____/______ ____(_)_______
*  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
*  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
*  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
*/

package org.gridgain.grid.util;

import org.gridgain.grid.util.tostring.*;

/**
 * Synchronization aid to track "busy" state of a subsystem that owns it.
 * <p>
 * Main difference over {@link GridBusyLock} is that this class is implemented
 * over {@link GridSpinReadWriteLock}.
 * <p>
 * For example, there may be a manager that have different threads for some
 * purposes and the manager must not be stopped while at least a single thread
 * is in "busy" state. In this situation each thread must enter to "busy"
 * state calling method {@link #enterBusy()} in critical pieces of code
 * which, i.e. use grid kernal functionality, notifying that the manager
 * and the whole grid kernal cannot be stopped while it's in progress. Once
 * the activity is done, the thread should leave "busy" state calling method
 * {@link #leaveBusy()}. The manager itself, when stopping, should call method
 * {@link #block} that blocks till all activities leave "busy" state.
 *
 * @author @java.author
 * @version @java.version
 * @see GridBusyLock
 * @see GridSpinReadWriteLock
 */
@GridToStringExclude
public class GridSpinBusyLock {
    /** Underlying read-write lock. */
    private final GridSpinReadWriteLock lock = new GridSpinReadWriteLock();

    /**
     * Enters "busy" state.
     *
     * @return {@code true} if entered to busy state.
     */
    public boolean enterBusy() {
        return !lock.writeLockedByCurrentThread() && lock.tryReadLock();
    }

    /**
     * Checks if busy lock was blocked by current thread.
     *
     * @return {@code True} if busy lock was blocked by current thread.
     */
    public boolean blockedByCurrentThread() {
        return lock.writeLockedByCurrentThread();
    }

    /**
     * Leaves "busy" state.
     */
    public void leaveBusy() {
        lock.readUnlock();
    }

    /**
     * Blocks current thread till all activities left "busy" state
     * and prevents them from further entering to "busy" state.
     */
    public void block() {
        lock.writeLock();
    }

    /**
     * Makes possible for activities entering busy state again.
     */
    public void unblock() {
        lock.writeUnlock();
    }
}
