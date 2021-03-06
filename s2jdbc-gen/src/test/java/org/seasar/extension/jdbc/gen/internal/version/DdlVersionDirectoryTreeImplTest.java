/*
 * Copyright 2004-2014 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.extension.jdbc.gen.internal.version;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author taedium
 * 
 */
public class DdlVersionDirectoryTreeImplTest {

    private DdlVersionDirectoryTreeImpl tree;

    /**
     * 
     */
    @Before
    public void setUp() {
        tree = new DdlVersionDirectoryTreeImpl(new File("aaa"),
                new File("bbb"), "0000", null);
    }

    /**
     * 
     */
    @Test
    public void testGetCurrentVersionDir() {
        File dir = tree.getCurrentVersionDirectory().asFile();
        assertEquals("aaa", dir.getParentFile().getName());
        assertEquals("0000", dir.getName());
    }

    /**
     * 
     */
    @Test
    public void testGetNextVersionDir() {
        File dir = tree.getNextVersionDirectory().asFile();
        assertEquals("aaa", dir.getParentFile().getName());
        assertEquals("0001", dir.getName());
    }

    /**
     * 
     */
    @Test
    public void testGetVersionDir() {
        File dir = tree.getVersionDirectory(10).asFile();
        assertEquals("aaa", dir.getParentFile().getName());
        assertEquals("0010", dir.getName());
    }

    /**
     * 
     */
    @Test
    public void testGetDdlVersion() {
        assertNotNull(tree.getDdlInfoFile());
    }

}
