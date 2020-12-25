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

package com.git.ifly6.nsapi.ctelegram.io.permcache;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Holds elements in the cache permanently. This does not extend {@link com.git.ifly6.nsapi.ctelegram.io.cache.CommCache}
 * because it uses a different creation mechanism: this creates based on a {@link Supplier} rather than on the key
 * itself.
 * @param <T> to cache
 */
public class CommPermanentCache<T> {

    Map<Integer, T> cache = new HashMap<>();

    private void add(int key, T instance) {
        cache.put(key, instance);
    }

    /**
     * Gets object by the key if present; puts it into the cache using the supplier if not present.
     * @param key      to get
     * @param supplier to create with
     * @return instance if present; supplied object otherwise
     */
    public T getOrCreate(Object[] key, Supplier<T> supplier) {
        int hashCode = Objects.hash(key);
        if (cache.containsKey(hashCode))
            return cache.get(hashCode);

        this.add(hashCode, supplier.get());
        return cache.get(hashCode);
    }

    /**
     * Creates a {@code Object[]} from provided input.
     * @param objects put into array
     * @return array
     */
    public static Object[] createKey(Object... objects) {
        return objects;
    }
}
