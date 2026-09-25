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
import java.util.Collections;
import java.util.List;

import org.eclipse.collections.api.block.function.Function;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
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
    public void testIteratorToList() {
        ImmutableList<String> result = CollectionUtil.iteratorToList(Arrays.asList("a", "b", "c").iterator());
        assertEquals(Lists.immutable.of("a", "b", "c"), result);
    }

    @Test
    public void testIteratorToListEmpty() {
        ImmutableList<String> result = CollectionUtil.iteratorToList(Collections.<String>emptyIterator());
        assertTrue(result.isEmpty());
    }

    @Test
    public void testIteratorToListPreservesDuplicatesAndNulls() {
        ImmutableList<String> result = CollectionUtil.iteratorToList(Arrays.asList("a", null, "a").iterator());
        assertEquals(Lists.immutable.of("a", null, "a"), result);
    }

    @Test
    public void testReturnOneRichIterable() {
        assertNull(CollectionUtil.returnOne(Lists.immutable.<String>empty(), "msg"));
        assertEquals("a", CollectionUtil.returnOne(Lists.immutable.of("a"), "msg"));
    }

    @Test
    public void testReturnOneRichIterableMultipleElementsThrows() {
        try {
            CollectionUtil.returnOne(Lists.immutable.of("a", "b"), "myContext");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("myContext"));
            assertThat(e.getMessage(), containsString("[a, b]"));
        }
    }

    @Test
    public void testReturnOnlyOne() {
        assertEquals("a", CollectionUtil.returnOnlyOne(Lists.immutable.of("a"), "msg"));
    }

    @Test
    public void testReturnOnlyOneEmptyThrows() {
        try {
            CollectionUtil.returnOnlyOne(Lists.immutable.<String>empty(), "myContext");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("myContext"));
            assertThat(e.getMessage(), containsString("[]"));
        }
    }

    @Test
    public void testReturnOnlyOneMultipleElementsThrows() {
        try {
            CollectionUtil.returnOnlyOne(Lists.immutable.of("a", "b"), "myContext");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("myContext"));
            assertThat(e.getMessage(), containsString("[a, b]"));
        }
    }

    @Test
    public void testReturnOneCollection() {
        assertNull(CollectionUtil.returnOne(Collections.<String>emptyList(), "msg"));
        assertEquals("a", CollectionUtil.returnOne(Collections.singletonList("a"), "msg"));
    }

    @Test
    public void testReturnOneCollectionMultipleElementsThrows() {
        List<String> coll = Arrays.asList("a", "b");
        try {
            CollectionUtil.returnOne(coll, "myContext");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("myContext"));
            assertThat(e.getMessage(), containsString("[a, b]"));
        }
    }

    @Test
    public void testVerifyNoDuplicatesPasses() {
        CollectionUtil.verifyNoDuplicates(Lists.immutable.of("apple", "banana", "cherry"), FIRST_CHAR, "msg");
        CollectionUtil.verifyNoDuplicates(Lists.immutable.<String>empty(), FIRST_CHAR, "msg");
    }

    @Test
    public void testVerifyNoDuplicatesThrows() {
        try {
            CollectionUtil.verifyNoDuplicates(Lists.immutable.of("apple", "avocado", "banana", "blueberry", "cherry"), FIRST_CHAR, "dupes found");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            assertTrue(message, message.startsWith("dupes found;\n"));
            assertThat(message, containsString("--> a"));
            assertThat(message, containsString("--> b"));
            assertThat(message, containsString("apple"));
            assertThat(message, containsString("avocado"));
            assertThat(message, containsString("banana"));
            assertThat(message, containsString("blueberry"));
            assertThat(message, not(containsString("cherry")));
            assertThat(message, not(containsString("--> c")));
        }
    }
}
