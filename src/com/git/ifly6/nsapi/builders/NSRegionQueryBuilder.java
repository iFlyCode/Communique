/*
 * Copyright (c) 2020 ifly6
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this class file and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.git.ifly6.nsapi.builders;

import com.git.ifly6.nsapi.NSConnection;

public class NSRegionQueryBuilder {

    private StringBuilder builder = new StringBuilder();

    /**
     * Constructs query builder.
     * @param regionRef is the reference name of the region
     */
    public NSRegionQueryBuilder(String regionRef) {
        builder.append(NSConnection.API_PREFIX);
        builder.append(NSRegionShard.REGION);
        builder.append(regionRef);
        builder.append(NSConnection.QUERY_PREFIX);
    }

    /**
     * Adds a shard to the query
     * @param query is the shard to query
     * @return the builder
     */
    public NSRegionQueryBuilder addQuery(NSRegionShard query) {
        if (!builder.toString().endsWith("+")) builder.append("+");
        builder.append(query.toString());
        return this;
    }

    /**
     * Adds multiple shards to the query
     * @param queries to add
     * @return the builder
     */
    public NSRegionQueryBuilder addQueries(NSRegionShard[] queries) {
        for (NSRegionShard query : queries)
            addQuery(query);
        return this;
    }

    /**
     * Returns the relevant NS API <code>String</code> that can be used to create the relevant URL which can then call
     * the document desired.
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return builder.toString();
    }

}
