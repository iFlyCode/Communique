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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class NSNationQueryBuilder {

    private List<Entry<NSNationShard, Integer>> censusShards = new ArrayList<>(8);
    private List<NSNationShard> queryShards = new ArrayList<>(8);

    private StringBuilder builder = new StringBuilder();

    public NSNationQueryBuilder(String nationName) {
        builder.append(NSConnection.API_PREFIX);
        builder.append(NSNationShard.NATION);
        builder.append(nationName);
        builder.append(NSConnection.QUERY_PREFIX);
    }

    public NSNationQueryBuilder addQuery(NSNationShard query) {
        addQuery(query, -1);    // -1 is a dummy var if not specified
        return this;
    }

    public NSNationQueryBuilder addQuery(NSNationShard query, int censusId) {
        // for non-census shards, ignore censusId
        if (query == NSNationShard.CENSUS && censusId != -1)
            censusShards.add(new AbstractMap.SimpleEntry<>(query, censusId));
        else queryShards.add(query);
        return this;
    }

    public NSNationQueryBuilder addQueries(NSNationShard[] queries) {
        for (NSNationShard query : queries) addQuery(query);
        return this;
    }

    /**
     * Returns the relevant NS API {@code String} that can be used to get the relevant URL which can then call the
     * document desired.
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        // For each query element, append to builder
        StringBuilder output = new StringBuilder(builder);
        queryShards.forEach(s -> output.append(s.toString()).append("+"));

        if (censusShards.size() > 0) {
            output.append("census;mode=score;scale=");
            // For each census element, append to builder
            censusShards.stream()
                    .map(Entry::getValue)
                    .forEach(i -> output.append(i.toString()).append("+"));
        }

        String outString = output.toString();
        return outString.endsWith("+") ? outString.substring(0, outString.length() - 1) : outString;
    }

}
