/**
 * Copyright 2026 Jonathan Anastos.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.gs.obevo.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.block.function.Function;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.Assert;
import org.junit.Test;

public class CollectionUtilTest {
    private static final Function<String, Object> FIRST_CHAR = new Function<String, Object>() {
        @Override
        public Object valueOf(String object) {
            return object.substring(0, 1);
        }
    };

    @Test
    public void testIteratorToListEmpty() {
        ImmutableList<String> list = CollectionUtil.iteratorToList(Collections.<String>emptyList().iterator());
        Assert.assertEquals(Lists.immutable.<String>empty(), list);
    }

    @Test
    public void testIteratorToListSingle() {
        ImmutableList<String> list = CollectionUtil.iteratorToList(Arrays.asList("a").iterator());
        Assert.assertEquals(Lists.immutable.of("a"), list);
    }

    @Test
    public void testIteratorToListMultiple() {
        ImmutableList<String> list = CollectionUtil.iteratorToList(Arrays.asList("a", "b", "c").iterator());
        Assert.assertEquals(Lists.immutable.of("a", "b", "c"), list);
    }

    @Test
    public void testReturnOneRichIterableEmpty() {
        RichIterable<String> coll = Lists.immutable.empty();
        Assert.assertNull(CollectionUtil.returnOne(coll, "myMessage"));
    }

    @Test
    public void testReturnOneRichIterableSingle() {
        RichIterable<String> coll = Lists.immutable.of("a");
        Assert.assertEquals("a", CollectionUtil.returnOne(coll, "myMessage"));
    }

    @Test
    public void testReturnOneRichIterableMultiple() {
        RichIterable<String> coll = Lists.immutable.of("a", "b");
        try {
            CollectionUtil.returnOne(coll, "myMessage");
            Assert.fail("Expected an exception");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage(), e.getMessage().contains("myMessage"));
        }
    }

    @Test
    public void testReturnOneCollectionEmpty() {
        Collection<String> coll = Collections.emptyList();
        Assert.assertNull(CollectionUtil.returnOne(coll, "myMessage"));
    }

    @Test
    public void testReturnOneCollectionSingle() {
        Collection<String> coll = Arrays.asList("a");
        Assert.assertEquals("a", CollectionUtil.returnOne(coll, "myMessage"));
    }

    @Test
    public void testReturnOneCollectionMultiple() {
        Collection<String> coll = Arrays.asList("a", "b");
        try {
            CollectionUtil.returnOne(coll, "myMessage");
            Assert.fail("Expected an exception");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage(), e.getMessage().contains("myMessage"));
        }
    }

    @Test
    public void testReturnOnlyOneSingle() {
        RichIterable<String> coll = Lists.immutable.of("a");
        Assert.assertEquals("a", CollectionUtil.returnOnlyOne(coll, "myMessage"));
    }

    @Test
    public void testReturnOnlyOneEmpty() {
        RichIterable<String> coll = Lists.immutable.empty();
        try {
            CollectionUtil.returnOnlyOne(coll, "myMessage");
            Assert.fail("Expected an exception");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage(), e.getMessage().contains("myMessage"));
        }
    }

    @Test
    public void testReturnOnlyOneMultiple() {
        RichIterable<String> coll = Lists.immutable.of("a", "b");
        try {
            CollectionUtil.returnOnlyOne(coll, "myMessage");
            Assert.fail("Expected an exception");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage(), e.getMessage().contains("myMessage"));
        }
    }

    @Test
    public void testVerifyNoDuplicatesEmpty() {
        CollectionUtil.verifyNoDuplicates(Lists.immutable.<String>empty(), FIRST_CHAR, "errorMessage");
    }

    @Test
    public void testVerifyNoDuplicatesSingle() {
        CollectionUtil.verifyNoDuplicates(Lists.immutable.of("abc"), FIRST_CHAR, "errorMessage");
    }

    @Test
    public void testVerifyNoDuplicatesMultipleDistinctKeys() {
        CollectionUtil.verifyNoDuplicates(Lists.immutable.of("abc", "bcd", "cde"), FIRST_CHAR, "errorMessage");
    }

    @Test
    public void testVerifyNoDuplicatesWithDuplicateKey() {
        try {
            CollectionUtil.verifyNoDuplicates(Lists.immutable.of("abc", "axy", "bcd"), FIRST_CHAR, "errorMessage");
            Assert.fail("Expected an exception");
        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            Assert.assertTrue(message, message.contains("errorMessage"));
            Assert.assertTrue(message, message.contains("abc"));
            Assert.assertTrue(message, message.contains("axy"));
            Assert.assertFalse(message, message.contains("bcd"));
        }
    }

    @Test
    public void testVerifyNoDuplicatesOnMutableList() {
        List<String> values = Arrays.asList("abc", "bcd");
        CollectionUtil.verifyNoDuplicates(Lists.mutable.withAll(values), FIRST_CHAR, "errorMessage");
    }
}
