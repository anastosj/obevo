/**
 * Copyright 2017 Goldman Sachs.
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

/*
// Portions copyright Jonathan Anastos. Licensed under Apache 2.0 license
*/

package com.gs.obevo.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
        Iterator<String> iterator = Collections.<String>emptyList().iterator();
        Assert.assertEquals(Lists.immutable.<String>empty(), CollectionUtil.iteratorToList(iterator));
    }

    @Test
    public void testIteratorToListSingle() {
        Iterator<String> iterator = Collections.singletonList("a").iterator();
        Assert.assertEquals(Lists.immutable.with("a"), CollectionUtil.iteratorToList(iterator));
    }

    @Test
    public void testIteratorToListMultiplePreservesOrderAndDuplicates() {
        Iterator<String> iterator = Arrays.asList("a", "b", "a").iterator();
        ImmutableList<String> result = CollectionUtil.iteratorToList(iterator);
        Assert.assertEquals(Lists.immutable.with("a", "b", "a"), result);
    }

    @Test
    public void testReturnOneRichIterableEmpty() {
        Assert.assertNull(CollectionUtil.returnOne(Lists.immutable.<String>empty(), "myMessage"));
    }

    @Test
    public void testReturnOneRichIterableSingle() {
        Assert.assertEquals("a", CollectionUtil.returnOne(Lists.immutable.with("a"), "myMessage"));
    }

    @Test
    public void testReturnOneRichIterableMultiple() {
        try {
            CollectionUtil.returnOne(Lists.immutable.with("a", "b"), "myMessage");
            Assert.fail("Expected an exception due to multiple elements");
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
        Collection<String> coll = Collections.singletonList("a");
        Assert.assertEquals("a", CollectionUtil.returnOne(coll, "myMessage"));
    }

    @Test
    public void testReturnOneCollectionMultiple() {
        Collection<String> coll = Arrays.asList("a", "b");
        try {
            CollectionUtil.returnOne(coll, "myMessage");
            Assert.fail("Expected an exception due to multiple elements");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage(), e.getMessage().contains("myMessage"));
        }
    }

    @Test
    public void testReturnOnlyOneSingle() {
        Assert.assertEquals("a", CollectionUtil.returnOnlyOne(Lists.immutable.with("a"), "myMessage"));
    }

    @Test
    public void testReturnOnlyOneEmpty() {
        try {
            CollectionUtil.returnOnlyOne(Lists.immutable.<String>empty(), "myMessage");
            Assert.fail("Expected an exception due to an empty collection");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage(), e.getMessage().contains("myMessage"));
        }
    }

    @Test
    public void testReturnOnlyOneMultiple() {
        try {
            CollectionUtil.returnOnlyOne(Lists.immutable.with("a", "b"), "myMessage");
            Assert.fail("Expected an exception due to multiple elements");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage(), e.getMessage().contains("myMessage"));
        }
    }

    @Test
    public void testVerifyNoDuplicatesEmpty() {
        CollectionUtil.verifyNoDuplicates(Lists.immutable.<String>empty(), FIRST_CHAR, "myError");
    }

    @Test
    public void testVerifyNoDuplicatesSingle() {
        CollectionUtil.verifyNoDuplicates(Lists.immutable.with("abc"), FIRST_CHAR, "myError");
    }

    @Test
    public void testVerifyNoDuplicatesMultipleDistinctKeys() {
        CollectionUtil.verifyNoDuplicates(Lists.immutable.with("abc", "bcd", "cde"), FIRST_CHAR, "myError");
    }

    @Test
    public void testVerifyNoDuplicatesWithDuplicateKeys() {
        List<String> values = Arrays.asList("abc", "azz", "bcd");
        try {
            CollectionUtil.verifyNoDuplicates(Lists.immutable.withAll(values), FIRST_CHAR, "myError");
            Assert.fail("Expected an exception due to duplicate keys");
        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            Assert.assertTrue(message, message.contains("myError"));
            Assert.assertTrue(message, message.contains("abc"));
            Assert.assertTrue(message, message.contains("azz"));
            Assert.assertFalse(message, message.contains("bcd"));
        }
    }
}
