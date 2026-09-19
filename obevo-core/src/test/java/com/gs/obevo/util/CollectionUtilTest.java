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
import java.util.List;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.block.function.Function;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CollectionUtilTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private static final Function<String, Object> FIRST_CHAR = new Function<String, Object>() {
        @Override
        public Object valueOf(String value) {
            return value.substring(0, 1);
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
    public void testIteratorToListMultiple() {
        Iterator<String> iterator = Arrays.asList("a", "b", "a").iterator();
        ImmutableList<String> list = CollectionUtil.iteratorToList(iterator);
        Assert.assertEquals(Lists.immutable.with("a", "b", "a"), list);
    }

    @Test
    public void testReturnOneRichIterableEmpty() {
        RichIterable<String> coll = Lists.mutable.empty();
        Assert.assertNull(CollectionUtil.returnOne(coll, "myMessage"));
    }

    @Test
    public void testReturnOneRichIterableSingle() {
        RichIterable<String> coll = Lists.mutable.with("a");
        Assert.assertEquals("a", CollectionUtil.returnOne(coll, "myMessage"));
    }

    @Test
    public void testReturnOneRichIterableMultiple() {
        RichIterable<String> coll = Lists.mutable.with("a", "b");
        this.thrown.expect(IllegalArgumentException.class);
        this.thrown.expectMessage("myMessage");
        CollectionUtil.returnOne(coll, "myMessage");
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
        this.thrown.expect(IllegalArgumentException.class);
        this.thrown.expectMessage("myMessage");
        CollectionUtil.returnOne(coll, "myMessage");
    }

    @Test
    public void testReturnOnlyOneSingle() {
        RichIterable<String> coll = Lists.mutable.with("a");
        Assert.assertEquals("a", CollectionUtil.returnOnlyOne(coll, "myMessage"));
    }

    @Test
    public void testReturnOnlyOneEmpty() {
        RichIterable<String> coll = Lists.mutable.empty();
        this.thrown.expect(IllegalArgumentException.class);
        this.thrown.expectMessage("myMessage");
        CollectionUtil.returnOnlyOne(coll, "myMessage");
    }

    @Test
    public void testReturnOnlyOneMultiple() {
        RichIterable<String> coll = Lists.mutable.with("a", "b");
        this.thrown.expect(IllegalArgumentException.class);
        this.thrown.expectMessage("myMessage");
        CollectionUtil.returnOnlyOne(coll, "myMessage");
    }

    @Test
    public void testVerifyNoDuplicatesEmpty() {
        CollectionUtil.verifyNoDuplicates(Lists.mutable.<String>empty(), FIRST_CHAR, "myError");
    }

    @Test
    public void testVerifyNoDuplicatesSingle() {
        CollectionUtil.verifyNoDuplicates(Lists.mutable.with("abc"), FIRST_CHAR, "myError");
    }

    @Test
    public void testVerifyNoDuplicatesMultipleDistinctKeys() {
        CollectionUtil.verifyNoDuplicates(Lists.mutable.with("abc", "bcd", "cde"), FIRST_CHAR, "myError");
    }

    @Test
    public void testVerifyNoDuplicatesWithDuplicates() {
        List<String> list = Arrays.asList("abc", "axy", "bcd");
        this.thrown.expect(IllegalArgumentException.class);
        this.thrown.expectMessage("myError");
        CollectionUtil.verifyNoDuplicates(Lists.mutable.withAll(list), FIRST_CHAR, "myError");
    }
}
