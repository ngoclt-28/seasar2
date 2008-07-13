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
package org.seasar.extension.jdbc.gen.dialect;

import java.math.BigDecimal;
import java.sql.Types;

import javax.persistence.GenerationType;

import org.seasar.extension.jdbc.gen.DataType;
import org.seasar.extension.jdbc.gen.JavaType;

/**
 * Oracleの方言を扱うクラスです。
 * 
 * @author taedium
 */
public class OracleGenDialect extends StandardGenDialect {

    /**
     * インスタンスを構築します。
     */
    public OracleGenDialect() {
        super();
        javaTypeMap.put(Types.DECIMAL, OracleJavaType.DECIMAL);

        dataTypeMap.put(Types.BINARY, OracleDataType.BINARY);
        dataTypeMap.put(Types.BIT, OracleDataType.BIT);
        dataTypeMap.put(Types.BIGINT, OracleDataType.BIGINT);
        dataTypeMap.put(Types.CHAR, OracleDataType.CHAR);
        dataTypeMap.put(Types.DECIMAL, OracleDataType.DECIMAL);
        dataTypeMap.put(Types.DOUBLE, OracleDataType.DOUBLE);
        dataTypeMap.put(Types.INTEGER, OracleDataType.INTEGER);
        dataTypeMap.put(Types.NUMERIC, OracleDataType.NUMERIC);
        dataTypeMap.put(Types.SMALLINT, OracleDataType.SMALLINT);
        dataTypeMap.put(Types.TIME, OracleDataType.TIME);
        dataTypeMap.put(Types.TINYINT, OracleDataType.TINYINT);
        dataTypeMap.put(Types.VARBINARY, OracleDataType.VARBINARY);
        dataTypeMap.put(Types.VARCHAR, OracleDataType.VARCHAR);
    }

    @Override
    public boolean isUserTable(String tableName) {
        return !tableName.contains("$");
    }

    @Override
    public GenerationType getDefaultGenerationType() {
        return GenerationType.SEQUENCE;
    }

    @Override
    public boolean supportsSequence() {
        return true;
    }

    @Override
    public String getSequenceDefinitionFragment(String dataType, int initValue,
            int allocationSize) {
        return "increment by " + allocationSize + " start with " + initValue;
    }

    @Override
    public String getBlockDelimiter() {
        return "/";
    }

    public static class OracleJavaType extends StandardJavaType {

        private static JavaType DECIMAL = new OracleJavaType() {

            @Override
            public Class<?> getJavaClass(int length, int scale,
                    String typeName, boolean nullable) {
                if (scale > 0 || length > 10) {
                    return BigDecimal.class;
                }
                return Integer.class;
            }
        };

        protected OracleJavaType() {
        }

        /**
         * インスタンスを構築します。
         * 
         * @param clazz
         *            クラス
         */
        protected OracleJavaType(Class<?> clazz) {
            super(clazz);
        }
    }

    /**
     * Oracle用の{@link DataType}の実装です。
     * 
     * @author taedium
     */
    public static class OracleDataType extends StandardDataType {

        private static DataType BIGINT = new OracleDataType("number(19,0)");

        private static DataType BINARY = new OracleDataType() {

            @Override
            public String getDefinition(int length, int precision, int scale) {
                return VARBINARY.getDefinition(length, precision, scale);
            }
        };

        private static DataType BIT = new OracleDataType("number(1,0)");

        private static DataType CHAR = new OracleDataType("char(1 char)");

        private static DataType DECIMAL = new OracleDataType() {

            @Override
            public String getDefinition(int length, int presision, int scale) {
                return format("number(%d,%d)", presision, scale);
            }
        };

        private static DataType DOUBLE = new OracleDataType("double precision");

        private static DataType INTEGER = new OracleDataType("number(10,0)");

        private static DataType NUMERIC = new OracleDataType() {

            @Override
            public String getDefinition(int length, int presision, int scale) {
                return format("number(%d,%d)", presision, scale);
            }
        };

        private static DataType SMALLINT = new OracleDataType("number(5,0)");

        private static DataType TIME = new OracleDataType("date");

        private static DataType TINYINT = new OracleDataType("number(3,0)");

        private static DataType VARBINARY = new OracleDataType() {

            @Override
            public String getDefinition(int length, int precision, int scale) {
                if (length > 2000) {
                    return "long raw";
                }
                return format("raw(%d)", length);
            }
        };

        private static DataType VARCHAR = new OracleDataType() {

            @Override
            public String getDefinition(int length, int precision, int scale) {
                if (length > 4000) {
                    return "long";
                }
                return format("varchar2(%d)", length);
            }
        };

        protected OracleDataType() {
        }

        /**
         * インスタンスを構築します。
         * 
         * @param definition
         *            定義
         */
        protected OracleDataType(String definition) {
            super(definition);
        }
    }
}
