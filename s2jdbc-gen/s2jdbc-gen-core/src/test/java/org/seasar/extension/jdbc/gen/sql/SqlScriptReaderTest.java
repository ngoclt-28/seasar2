/*
 * Copyright 2004-2008 the Seasar Foundation and the Others.
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
package org.seasar.extension.jdbc.gen.sql;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;
import org.seasar.extension.jdbc.gen.SqlScriptReader;

import static org.junit.Assert.*;

/**
 * @author taedium
 * 
 */
public class SqlScriptReaderTest {

    @Test
    public void testReadSql() throws Exception {
        SqlScriptReader reader = new SqlScriptReaderImpl(new File("dummy"),
                "UTF-8", new SqlScriptTokenizerImpl(';', "go")) {

            @Override
            protected BufferedReader createBufferedReader() throws IOException {
                StringBuilder buf = new StringBuilder();
                buf.append("aaa;\n");
                buf.append("bbb\n");
                buf.append("go\n");
                buf.append("ccc\n");
                buf.append("ddd\n");
                StringReader reader = new StringReader(buf.toString());
                return new BufferedReader(reader);
            }
        };
        assertEquals("aaa", reader.readSql());
        assertEquals("bbb", reader.readSql());
        assertEquals("ccc ddd", reader.readSql());
        assertNull(reader.readSql());
    }
}
