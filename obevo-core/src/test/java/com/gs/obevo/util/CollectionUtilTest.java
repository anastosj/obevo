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
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Sets;
import org.junit.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
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
        Iterator<String> iterator = Arrays.asList("a", "b", "c").iterator();

        ImmutableList<String> result = CollectionUtil.iteratorToList(iterator);

        assertEquals(Lists.immutable.of("a", "b", "c"), result);
    }

    @Test
    public void iteratorToListOnEmptyIterator() {
        ImmutableList<Object> result = CollectionUtil.iteratorToList(Collections.emptyIterator());

        assertTrue(result.isEmpty());
    }

    @Test
    public void returnOneRichIterableWithSingleElement() {
        assertEquals("a", CollectionUtil.returnOne(Lists.immutable.of("a"), "msg"));
    }

    @Test
    public void returnOneRichIterableWithNoElements() {
        assertNull(CollectionUtil.returnOne(Lists.immutable.<String>empty(), "msg"));
    }

    @Test
    public void returnOneRichIterableWithMultipleElements() {
        try {
            CollectionUtil.returnOne(Lists.immutable.of("a", "b"), "my context");
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("my context"));
            assertThat(e.getMessage(), containsString("a"));
            assertThat(e.getMessage(), containsString("b"));
        }
    }

    @Test
    public void returnOneCollectionWithSingleElement() {
        Collection<String> coll = Collections.singletonList("a");

        assertEquals("a", CollectionUtil.returnOne(coll, "msg"));
    }

    @Test
    public void returnOneCollectionWithNoElements() {
        Collection<String> coll = Collections.emptyList();

        assertNull(CollectionUtil.returnOne(coll, "msg"));
    }

    @Test
    public void returnOneCollectionWithMultipleElements() {
        Collection<String> coll = Arrays.asList("a", "b");

        try {
            CollectionUtil.returnOne(coll, "my context");
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("my context"));
        }
    }

    @Test
    public void returnOnlyOneWithSingleElement() {
        assertEquals("a", CollectionUtil.returnOnlyOne(Sets.immutable.of("a"), "msg"));
    }

    @Test
    public void returnOnlyOneWithNoElements() {
        try {
            CollectionUtil.returnOnlyOne(Lists.immutable.<String>empty(), "my context");
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("my context"));
        }
    }

    @Test
    public void returnOnlyOneWithMultipleElements() {
        try {
            CollectionUtil.returnOnlyOne(Lists.immutable.of("a", "b"), "my context");
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("my context"));
        }
    }

    @Test
    public void verifyNoDuplicatesPassesWhenKeysAreUnique() {
        CollectionUtil.verifyNoDuplicates(Lists.immutable.of("apple", "banana", "cherry"), FIRST_CHAR, "dupes found");
    }

    @Test
    public void verifyNoDuplicatesPassesOnEmptyInput() {
        CollectionUtil.verifyNoDuplicates(Lists.immutable.<String>empty(), FIRST_CHAR, "dupes found");
    }

    @Test
    public void verifyNoDuplicatesFailsOnDuplicateKeys() {
        try {
            CollectionUtil.verifyNoDuplicates(Lists.immutable.of("apple", "avocado", "banana", "blueberry", "cherry"), FIRST_CHAR, "dupes found");
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            assertThat(message, containsString("dupes found"));
            assertThat(message, containsString("--> a"));
            assertThat(message, containsString("--> b"));
            assertThat(message, containsString("apple"));
            assertThat(message, containsString("avocado"));
            assertThat(message, containsString("banana"));
            assertThat(message, containsString("blueberry"));
            assertThat(message.contains("cherry"), equalTo(false));
        }
    }
}
