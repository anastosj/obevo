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
import java.util.Iterator;

import org.eclipse.collections.api.block.function.Function;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Sets;
import org.junit.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
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
        Iterator<String> iterator = Arrays.asList("a", "b", "c").iterator();
        ImmutableList<String> list = CollectionUtil.iteratorToList(iterator);
        assertEquals(Lists.immutable.of("a", "b", "c"), list);
    }

    @Test
    public void iteratorToListHandlesEmptyIterator() {
        ImmutableList<String> list = CollectionUtil.iteratorToList(Collections.<String>emptyIterator());
        assertTrue(list.isEmpty());
    }

    @Test
    public void iteratorToListExhaustsIterator() {
        Iterator<Integer> iterator = Arrays.asList(1, 2).iterator();
        CollectionUtil.iteratorToList(iterator);
        assertTrue(!iterator.hasNext());
    }

    @Test
    public void returnOneRichIterableWithSingleElement() {
        assertThat(CollectionUtil.returnOne(Lists.immutable.of("x"), "msg"), equalTo("x"));
    }

    @Test
    public void returnOneRichIterableWithNoElements() {
        assertThat(CollectionUtil.returnOne(Lists.immutable.<String>empty(), "msg"), nullValue());
    }

    @Test
    public void returnOneRichIterableWithMultipleElementsThrows() {
        try {
            CollectionUtil.returnOne(Lists.immutable.of("x", "y"), "my context");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("my context"));
            assertThat(e.getMessage(), containsString("x"));
            assertThat(e.getMessage(), containsString("y"));
        }
    }

    @Test
    public void returnOneCollectionWithSingleElement() {
        Collection<String> coll = Collections.singletonList("x");
        assertThat(CollectionUtil.returnOne(coll, "msg"), equalTo("x"));
    }

    @Test
    public void returnOneCollectionWithNoElements() {
        Collection<String> coll = Collections.emptyList();
        assertThat(CollectionUtil.returnOne(coll, "msg"), nullValue());
    }

    @Test
    public void returnOneCollectionWithMultipleElementsThrows() {
        Collection<String> coll = Arrays.asList("x", "y");
        try {
            CollectionUtil.returnOne(coll, "my context");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("my context"));
        }
    }

    @Test
    public void returnOnlyOneWithSingleElement() {
        assertThat(CollectionUtil.returnOnlyOne(Sets.immutable.of("x"), "msg"), equalTo("x"));
    }

    @Test
    public void returnOnlyOneWithNoElementsThrows() {
        try {
            CollectionUtil.returnOnlyOne(Lists.immutable.<String>empty(), "my context");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("my context"));
        }
    }

    @Test
    public void returnOnlyOneWithMultipleElementsThrows() {
        try {
            CollectionUtil.returnOnlyOne(Lists.immutable.of("x", "y"), "my context");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("my context"));
        }
    }

    @Test
    public void verifyNoDuplicatesPassesWhenKeysAreUnique() {
        MutableList<String> list = Lists.mutable.of("apple", "banana", "cherry");
        CollectionUtil.verifyNoDuplicates(list, FIRST_CHAR, "dupes found");
    }

    @Test
    public void verifyNoDuplicatesPassesOnEmptyInput() {
        CollectionUtil.verifyNoDuplicates(Lists.mutable.<String>empty(), FIRST_CHAR, "dupes found");
    }

    @Test
    public void verifyNoDuplicatesThrowsWhenKeysCollide() {
        MutableList<String> list = Lists.mutable.of("apple", "avocado", "banana", "blueberry", "cherry");
        try {
            CollectionUtil.verifyNoDuplicates(list, FIRST_CHAR, "dupes found");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            assertThat(message, containsString("dupes found"));
            assertThat(message, containsString("Duplicate keys:"));
            assertThat(message, containsString("--> a"));
            assertThat(message, containsString("--> b"));
            assertThat(message, containsString("apple"));
            assertThat(message, containsString("avocado"));
            assertThat(message, containsString("banana"));
            assertThat(message, containsString("blueberry"));
            assertThat(message, not(containsString("cherry")));
        }
    }
}
