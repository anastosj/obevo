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
package com.gs.obevo.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.collections.api.block.function.Function;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CollectionUtilTest {
    private static final Function<String, Object> FIRST_CHAR = new Function<String, Object>() {
        @Override
        public Object valueOf(String s) {
            return s.charAt(0);
        }
    };

    @Test
    public void iteratorToListPreservesOrder() {
        ImmutableList<String> list = CollectionUtil.iteratorToList(Arrays.asList("a", "b", "c").iterator());
        assertEquals(Lists.immutable.of("a", "b", "c"), list);
    }

    @Test
    public void iteratorToListOnEmptyIterator() {
        ImmutableList<String> list = CollectionUtil.iteratorToList(Collections.<String>emptyIterator());
        assertTrue(list.isEmpty());
    }

    @Test
    public void iteratorToListRetainsDuplicatesAndNulls() {
        ImmutableList<String> list = CollectionUtil.iteratorToList(Arrays.asList("a", null, "a").iterator());
        assertEquals(Lists.immutable.of("a", null, "a"), list);
    }

    @Test
    public void returnOneRichIterable() {
        assertEquals("a", CollectionUtil.returnOne(Lists.immutable.of("a"), "msg"));
        assertNull(CollectionUtil.returnOne(Lists.immutable.<String>empty(), "msg"));
    }

    @Test
    public void returnOneRichIterableFailsOnMultiple() {
        try {
            CollectionUtil.returnOne(Lists.immutable.of("a", "b"), "myMessage");
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("myMessage"));
            assertThat(e.getMessage(), containsString("[a, b]"));
        }
    }

    @Test
    public void returnOneCollection() {
        Collection<String> one = Arrays.asList("a");
        assertEquals("a", CollectionUtil.returnOne(one, "msg"));
        assertNull(CollectionUtil.returnOne(Collections.<String>emptyList(), "msg"));
    }

    @Test
    public void returnOneCollectionFailsOnMultiple() {
        try {
            CollectionUtil.returnOne(Arrays.asList("a", "b"), "myMessage");
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("myMessage"));
        }
    }

    @Test
    public void returnOnlyOne() {
        assertEquals("a", CollectionUtil.returnOnlyOne(Lists.immutable.of("a"), "msg"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void returnOnlyOneFailsOnEmpty() {
        CollectionUtil.returnOnlyOne(Lists.immutable.<String>empty(), "msg");
    }

    @Test(expected = IllegalArgumentException.class)
    public void returnOnlyOneFailsOnMultiple() {
        CollectionUtil.returnOnlyOne(Lists.immutable.of("a", "b"), "msg");
    }

    @Test
    public void verifyNoDuplicatesPassesOnUniqueKeys() {
        CollectionUtil.verifyNoDuplicates(Lists.immutable.of("apple", "banana", "cherry"), FIRST_CHAR, "dupes");
    }

    @Test
    public void verifyNoDuplicatesPassesOnEmpty() {
        CollectionUtil.verifyNoDuplicates(Lists.immutable.<String>empty(), FIRST_CHAR, "dupes");
    }

    @Test
    public void verifyNoDuplicatesFailsOnDuplicateKeys() {
        MutableList<String> list = Lists.mutable.of("apple", "avocado", "banana", "blueberry", "cherry");
        try {
            CollectionUtil.verifyNoDuplicates(list, FIRST_CHAR, "myErrorMessage");
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("myErrorMessage"));
            assertThat(e.getMessage(), containsString("--> a"));
            assertThat(e.getMessage(), containsString("--> b"));
            assertThat(e.getMessage(), containsString("apple"));
            assertThat(e.getMessage(), containsString("avocado"));
            assertThat(e.getMessage(), containsString("banana"));
            assertThat(e.getMessage(), containsString("blueberry"));
            assertThat(e.getMessage(), not(containsString("cherry")));
        }
    }

    @Test
    public void verifyNoDuplicatesFailsOnIdenticalElements() {
        try {
            CollectionUtil.verifyNoDuplicates(Lists.immutable.of("apple", "apple"), FIRST_CHAR, "dupes");
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("--> a"));
        }
    }
}
