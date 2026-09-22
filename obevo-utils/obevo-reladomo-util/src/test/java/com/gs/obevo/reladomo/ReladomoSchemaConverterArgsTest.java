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
package com.gs.obevo.reladomo;

import java.io.File;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ReladomoSchemaConverterArgsTest {
    @Test
    public void defaults() {
        ReladomoSchemaConverterArgs args = new ReladomoSchemaConverterArgs();
        assertNull(args.getInputDir());
        assertNull(args.getOutputDir());
        assertNull(args.getPlatform());
        assertNull(args.getExcludeObjects());
        assertEquals("yourSchema", args.getDbSchema());
        assertFalse(args.isDontGenerateBaseline());
    }

    @Test
    public void setters() {
        ReladomoSchemaConverterArgs args = new ReladomoSchemaConverterArgs();
        args.setInputDir(new File("in"));
        args.setOutputDir(new File("out"));
        args.setPlatform("DB2");
        args.setDbSchema("mySchema");
        args.setExcludeObjects("TABLE~tab1");
        args.setDontGenerateBaseline(true);

        assertEquals(new File("in"), args.getInputDir());
        assertEquals(new File("out"), args.getOutputDir());
        assertEquals("DB2", args.getPlatform());
        assertEquals("mySchema", args.getDbSchema());
        assertEquals("TABLE~tab1", args.getExcludeObjects());
        assertTrue(args.isDontGenerateBaseline());
    }
}
