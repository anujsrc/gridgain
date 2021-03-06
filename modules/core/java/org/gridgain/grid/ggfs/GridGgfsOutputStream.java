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

package org.gridgain.grid.ggfs;

import java.io.*;

/**
 * {@code GGFS} output stream to write data into the file system.
 *
 * @author @java.author
 * @version @java.version
 */
public abstract class GridGgfsOutputStream extends OutputStream {
    /**
     * Transfers specified amount of bytes from data input to this output stream.
     * This method is optimized to avoid unnecessary temporal buffer creation and byte array copy.
     *
     * @param in Data input to copy bytes from.
     * @param len Data length to copy.
     * @throws IOException If write failed, read from input failed or there is no enough data in data input.
     */
    public abstract void transferFrom(DataInput in, int len) throws IOException;
}
