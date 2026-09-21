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
package com.gs.obevo.util.vfs;

import org.apache.commons.vfs2.FileFilter;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileFilterUtilsTest {
    private static final FileFilter TRUE = TrueFileFilter.INSTANCE;
    private static final FileFilter FALSE = new FileFilter() {
        @Override
        public boolean accept(FileSelectInfo fileInfo) {
            return false;
        }
    };

    private static FileSelectInfo fileInfo(String baseName, FileType type) throws FileSystemException {
        FileName fileName = mock(FileName.class);
        when(fileName.getBaseName()).thenReturn(baseName);
        FileObject file = mock(FileObject.class);
        when(file.getName()).thenReturn(fileName);
        when(file.getType()).thenReturn(type);
        FileSelectInfo info = mock(FileSelectInfo.class);
        when(info.getFile()).thenReturn(file);
        return info;
    }

    private static FileSelectInfo folder(String baseName) throws FileSystemException {
        return fileInfo(baseName, FileType.FOLDER);
    }

    private static FileSelectInfo file(String baseName) throws FileSystemException {
        return fileInfo(baseName, FileType.FILE);
    }

    @Test
    public void testNot() throws Exception {
        assertFalse(FileFilterUtils.not(TRUE).accept(file("a")));
        assertTrue(FileFilterUtils.not(FALSE).accept(file("a")));
    }

    @Test
    public void testOr() throws Exception {
        FileSelectInfo info = file("a");
        assertTrue(FileFilterUtils.or(TRUE, TRUE).accept(info));
        assertTrue(FileFilterUtils.or(TRUE, FALSE).accept(info));
        assertTrue(FileFilterUtils.or(FALSE, TRUE).accept(info));
        assertFalse(FileFilterUtils.or(FALSE, FALSE).accept(info));
    }

    @Test
    public void testAnd() throws Exception {
        FileSelectInfo info = file("a");
        assertTrue(FileFilterUtils.and(TRUE, TRUE).accept(info));
        assertFalse(FileFilterUtils.and(TRUE, FALSE).accept(info));
        assertFalse(FileFilterUtils.and(FALSE, TRUE).accept(info));
        assertFalse(FileFilterUtils.and(FALSE, FALSE).accept(info));
    }

    @Test
    public void testAndVarargs() throws Exception {
        FileSelectInfo info = file("a");
        assertTrue(FileFilterUtils.and(TRUE, TRUE, TRUE, TRUE).accept(info));
        assertFalse(FileFilterUtils.and(TRUE, TRUE, TRUE, FALSE).accept(info));
        assertTrue(FileFilterUtils.and(TRUE, TRUE, new FileFilter[0]).accept(info));
    }

    @Test
    public void testDirectory() throws Exception {
        assertTrue(FileFilterUtils.directory().accept(folder("dir")));
        assertFalse(FileFilterUtils.directory().accept(file("file.txt")));
        assertFalse(FileFilterUtils.directory().accept(fileInfo("x", FileType.IMAGINARY)));
    }

    @Test
    public void testVcsAwareExcludesVcsDirectoriesOnly() throws Exception {
        FileFilter filter = FileFilterUtils.vcsAware();
        assertFalse(filter.accept(folder(".svn")));
        assertFalse(filter.accept(folder("CVS")));

        assertTrue(filter.accept(folder("src")));
        assertTrue(filter.accept(folder(".git")));
        assertTrue(filter.accept(folder("cvs")));
        assertTrue(filter.accept(file(".svn")));
        assertTrue(filter.accept(file("CVS")));
        assertTrue(filter.accept(file("file.txt")));
    }

    @Test
    public void testMakeVcsAwareRequiresBothVcsDirectoryNames() throws Exception {
        // the composed filter only passes when the input filter passes AND the file is a folder named both CVS and
        // .svn, which no single file can satisfy
        FileFilter filter = FileFilterUtils.makeVcsAware(TRUE);
        assertFalse(filter.accept(folder("CVS")));
        assertFalse(filter.accept(folder(".svn")));
        assertFalse(filter.accept(folder("src")));
        assertFalse(filter.accept(file("file.txt")));

        assertFalse(FileFilterUtils.makeVcsAware(FALSE).accept(folder("CVS")));
    }
}
