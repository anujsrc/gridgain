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

package org.gridgain.grid.spi.securesession;

import org.gridgain.grid.*;
import org.gridgain.grid.spi.*;
import org.gridgain.grid.spi.securesession.noop.*;
import org.gridgain.grid.spi.securesession.rememberme.*;
import org.jetbrains.annotations.*;

/**
 * Secure session SPI allows for session creation and validation, typically after authentication
 * has successfully happened. The main purpose of this SPI is to ensure that remote clients are
 * authenticated only once and upon successful authentication get issued a secure session token
 * to reuse for consequent requests (very much the same way like HTTP sessions work).
 * <p>
 * The default secure session SPI is {@link GridNoopSecureSessionSpi}
 * which permits any request.
 * <p>
 * Gridgain provides the following {@code GridSecureSessionSpi} implementations:
 * <ul>
 * <li>
 *     {@link GridNoopSecureSessionSpi} - permits any request.
 * </li>
 * <li>
 *     {@link GridRememberMeSecureSessionSpi} -
 *     validates client session with remember-me session token.
 * </li>
 * </ul>
 * <p>
 * <b>NOTE:</b> that multiple secure session SPIs may be started on the same grid node. In this case
 * GridGain will differentiate between them based on {@link #supported(GridSecuritySubjectType)}
 * value. The first SPI which returns {@code true} for a given subject type will be used for
 * session validation.
 * <p>
 * <b>NOTE:</b> this SPI (i.e. methods in this interface) should never be used directly. SPIs provide
 * internal view on the subsystem and is used internally by GridGain kernal. In rare use cases when
 * access to a specific implementation of this SPI is required - an instance of this SPI can be obtained
 * via {@link Grid#configuration()} method to check its configuration properties or call other non-SPI
 * methods. Note again that calling methods from this interface on the obtained instance can lead
 * to undefined behavior and explicitly not supported.
 *
 * @author @java.author
 * @version @java.version
 */
public interface GridSecureSessionSpi extends GridSpi, GridSpiJsonConfigurable {
    /**
     * Checks if given subject is supported by this SPI. If not, then next secure session SPI
     * in the list will be checked.
     *
     * @param subjType Subject type.
     * @return {@code True} if subject type is supported, {@code false} otherwise.
     */
    public boolean supported(GridSecuritySubjectType subjType);

    /**
     * Validates a given token and returns a valid {@code "remember-me"} session token.
     * <p>If passed in token is {@code null}, then this is a first time token generation
     * and a new session token will be created.
     * <p>Otherwise, the passed in token is validated and if validation passes, then
     * the next value for session token is returned. If failed, then {@code null} is returned.
     *
     * @param subjType Subject type.
     * @param subjId Unique subject ID such as local or remote node ID, client ID, etc.
     * @param tok If {@code null}, then this is the first request and new session
     * token should be created, otherwise, the token will be validated and next session
     * token will be generated.
     * @param params Additional implementation-specific parameters.
     * @return Session token which may be used to validate consequent requests or
     *         {@code null} if validation failed.
     * @throws GridSpiException If validation resulted in system error. Note that
     * bad credentials should not cause this exception.
     */
    @Nullable public byte[] validate(GridSecuritySubjectType subjType, byte[] subjId, @Nullable byte[] tok,
        @Nullable Object params) throws GridSpiException;
}
