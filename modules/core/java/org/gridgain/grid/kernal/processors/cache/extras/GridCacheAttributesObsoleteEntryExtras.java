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

package org.gridgain.grid.kernal.processors.cache.extras;

import org.gridgain.grid.kernal.processors.cache.*;
import org.gridgain.grid.util.*;
import org.gridgain.grid.util.typedef.internal.*;
import org.jetbrains.annotations.*;

/**
 * Extras where attributes and obsolete version are set.
 *
 * @author @java.author
 * @version @java.version
 */
public class GridCacheAttributesObsoleteEntryExtras<K> extends GridCacheEntryExtrasAdapter<K> {
    /** Attributes data. */
    private GridLeanMap<String, Object> attrData;

    /** Obsolete version. */
    private GridCacheVersion obsoleteVer;

    /**
     * Constructor.
     *
     * @param attrData Attributes data.
     * @param obsoleteVer Obsolete version.
     */
    public GridCacheAttributesObsoleteEntryExtras(GridLeanMap<String, Object> attrData, GridCacheVersion obsoleteVer) {
        assert attrData != null;
        assert obsoleteVer != null;

        this.attrData = attrData;
        this.obsoleteVer = obsoleteVer;
    }

    /** {@inheritDoc} */
    @Override public GridLeanMap<String, Object> attributesData() {
        return attrData;
    }

    /** {@inheritDoc} */
    @Override public GridCacheEntryExtras<K> attributesData(@Nullable GridLeanMap<String, Object> attrData) {
        if (attrData != null) {
            this.attrData = attrData;

            return this;
        }
        else
            return new GridCacheObsoleteEntryExtras<>(obsoleteVer);
    }

    /** {@inheritDoc} */
    @Override public GridCacheEntryExtras<K> mvcc(GridCacheMvcc<K> mvcc) {
        return mvcc != null ? new GridCacheAttributesMvccObsoleteEntryExtras<>(attrData, mvcc, obsoleteVer) : this;
    }

    /** {@inheritDoc} */
    @Override public GridCacheVersion obsoleteVersion() {
        return obsoleteVer;
    }

    /** {@inheritDoc} */
    @Override public GridCacheEntryExtras<K> obsoleteVersion(GridCacheVersion obsoleteVer) {
        if (obsoleteVer != null) {
            this.obsoleteVer = obsoleteVer;

            return this;
        }
        else
            return new GridCacheAttributesEntryExtras<>(attrData);
    }

    /** {@inheritDoc} */
    @Override public GridCacheEntryExtras<K> ttlAndExpireTime(long ttl, long expireTime) {
        return ttl != 0 ? new GridCacheAttributesObsoleteTtlEntryExtras<K>(attrData, obsoleteVer, ttl, expireTime) :
            this;
    }

    /** {@inheritDoc} */
    @Override public int size() {
        return 16;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(GridCacheAttributesObsoleteEntryExtras.class, this);
    }
}
