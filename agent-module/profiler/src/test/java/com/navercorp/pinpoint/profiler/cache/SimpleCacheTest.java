/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.cache;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author emeroad
 */
public class SimpleCacheTest {

    @Test
    public void startKey0() {
        SimpleCache<String, Integer> cache = SimpleCache.newIdCache();
        Result<Integer> test1 = cache.put("test1");
        Result<Integer> test2 = cache.put("test2");

        Assertions.assertEquals(1, test1.getId());
        Assertions.assertEquals(2, test2.getId());
    }


    @Test
    public void put() {
        SimpleCache<String, Integer> cache = SimpleCache.newIdCache();;
        Result<Integer> test = cache.put("test");
        Assertions.assertEquals(1, test.getId());
        Assertions.assertTrue(test.isNewValue());

        Result<Integer> recheck = cache.put("test");
        Assertions.assertEquals(test.getId(), recheck.getId());
        Assertions.assertFalse(recheck.isNewValue());

        Result<Integer> newValue = cache.put("new");
        Assertions.assertEquals(2, newValue.getId());
        Assertions.assertTrue(newValue.isNewValue());

    }
}
