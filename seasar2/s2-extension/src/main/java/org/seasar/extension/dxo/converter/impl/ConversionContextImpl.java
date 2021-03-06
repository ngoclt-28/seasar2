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
package org.seasar.extension.dxo.converter.impl;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.seasar.extension.dxo.DxoConstants;
import org.seasar.extension.dxo.annotation.AnnotationReader;
import org.seasar.extension.dxo.converter.ConversionContext;
import org.seasar.extension.dxo.converter.Converter;
import org.seasar.extension.dxo.converter.ConverterFactory;
import org.seasar.extension.dxo.converter.DatePropertyInfo;
import org.seasar.extension.dxo.converter.NestedPropertyInfo;
import org.seasar.extension.dxo.util.DxoUtil;
import org.seasar.extension.dxo.util.Expression;
import org.seasar.framework.beans.BeanDesc;
import org.seasar.framework.beans.PropertyDesc;
import org.seasar.framework.beans.factory.BeanDescFactory;
import org.seasar.framework.util.CaseInsensitiveMap;
import org.seasar.framework.util.Disposable;
import org.seasar.framework.util.DisposableUtil;
import org.seasar.framework.util.MapUtil;
import org.seasar.framework.util.StringUtil;

/**
 * 変換コンテキストの実装クラスです。
 * 
 * @author koichik
 */
public class ConversionContextImpl implements ConversionContext {

    // constants
    /** javaで始まるパッケージ名のプレフィックスです。 */
    protected static final String JAVA = "java.";

    /** javaxで始まるパッケージのプレフィクスです。 */
    protected static final String JAVAX = "javax.";

    /** キーに対応する値が存在しないことを示すオブジェクトです。 */
    protected static final Object NOT_FOUND = new Object();

    // static fields
    /** クラスが初期化済みであることを示します。 */
    protected static boolean initialized;

    /** コンテキスト情報のキャッシュです。 */
    protected static final Map contextInfoCache = MapUtil.createHashMap(1024);

    /** コンバータのキャッシュです。 */
    protected static Map convertersCache = MapUtil.createHashMap(1024);

    /** ネストしたプロパティ情報のキャッシュです。 */
    protected static final Map nestedPropertyInfoCache = MapUtil
            .createHashMap(1024);

    /** 日時プロパティ情報のキャッシュです。 */
    protected static final Map datePropertyInfoCache = MapUtil
            .createHashMap(1024);

    /** {@link DateFormat}のキャッシュです。 */
    protected static final ThreadLocal dateFormatCache = new ThreadLocal() {
        protected Object initialValue() {
            return new HashMap();
        }
    };

    // instance fields
    /** このコンテキストを実行しているインターフェースまたはクラスです。 */
    protected Class dxoClass;

    /** このコンテキストを実行しているメソッドです。 */
    protected Method method;

    /** コンバータファクトリです。 */
    protected ConverterFactory converterFactory;

    /** アノテーションリーダです。 */
    protected AnnotationReader annotationReader;

    /** コンテキスト情報です。 */
    protected Map contextInfo;

    /** 評価済みのオブジェクトです。 */
    protected Map evaluatedValues = new CaseInsensitiveMap();

    /** 変換済みのオブジェクトです。 */
    protected Map convertedObjects = new IdentityHashMap();

    /** 変換先のJavaBeansに<code>null</code>の値を設定しないことを示します。 */
    protected boolean excludeNull;

    /** 変換先のJavaBeansに空白(スペース，復帰，改行，タブ文字のみ)の値を設定しないことを示します。 */
    protected boolean excludeWhitespace;

    /** 変換元JavaBeansのプロパティのprefixです。 */
    protected String sourcePrefix;

    /** 変換先JavaBeansのプロパティのprefixです。 */
    protected String destPrefix;

    static {
        initialize();
    }

    /**
     * クラスを初期化済みにします。
     */
    public static void initialize() {
        if (initialized) {
            return;
        }
        DisposableUtil.add(new Disposable() {
            public void dispose() {
                destroy();
            }
        });
        initialized = true;
    }

    /**
     * キャッシュを破棄し、クラスを未初期化状態にします。
     * 
     */
    public static void destroy() {
        contextInfoCache.clear();
        convertersCache.clear();
        nestedPropertyInfoCache.clear();
        datePropertyInfoCache.clear();
        initialized = false;
    }

    /**
     * <code>ConversionContextImpl</code>のインスタンスを構築します。
     * 
     * @param dxoClass
     *            Dxoインターフェースまたはクラス
     * @param method
     *            Dxoのメソッド
     * @param converterFactory
     *            コンバータファクトリ
     * @param annotationReader
     *            アノテーションリーダ
     * @param source
     *            変換元のオブジェクト
     */
    public ConversionContextImpl(final Class dxoClass, final Method method,
            final ConverterFactory converterFactory,
            final AnnotationReader annotationReader, final Object source) {
        initialize();
        this.dxoClass = dxoClass;
        this.method = method;
        this.converterFactory = converterFactory;
        this.annotationReader = annotationReader;

        contextInfo = getContextInfo(annotationReader);
        final Expression conversionRule = (Expression) getContextInfo(DxoConstants.CONVERSION_RULE);
        if (conversionRule != null) {
            evaluatedValues = conversionRule.evaluate(source);
        }
        excludeNull = ((Boolean) getContextInfo(DxoConstants.EXCLUDE_NULL))
                .booleanValue();
        excludeWhitespace = ((Boolean) getContextInfo(DxoConstants.EXCLUDE_WHITESPACE))
                .booleanValue();
        sourcePrefix = (String) getContextInfo(DxoConstants.SOURCE_PREFIX);
        destPrefix = (String) getContextInfo(DxoConstants.DEST_PREFIX);
    }

    public ConverterFactory getConverterFactory() {
        return converterFactory;
    }

    public Converter getConverter(final Class destClass,
            final String destPropertyName) {
        final Map cachedConverters = (Map) convertersCache.get(destClass);
        if (cachedConverters != null) {
            return (Converter) cachedConverters.get(destPropertyName);
        }
        final Map converters = annotationReader.getConverters(destClass);
        convertersCache.put(destClass, converters);
        return (Converter) converters.get(destPropertyName);
    }

    public Object getConvertedObject(final Object source) {
        return convertedObjects.get(source);
    }

    public void addConvertedObject(final Object source, final Object dest) {
        convertedObjects.put(source, dest);
    }

    public Object getContextInfo(final String key) {
        return contextInfo.get(key);
    }

    public DateFormat getDateFormat() {
        final String format = (String) contextInfo
                .get(DxoConstants.DATE_PATTERN);
        return format == null ? null : getDateFormat(format);
    }

    public DateFormat getTimeFormat() {
        final String format = (String) contextInfo
                .get(DxoConstants.TIME_PATTERN);
        return format == null ? null : getDateFormat(format);
    }

    public DateFormat getTimestampFormat() {
        final String format = (String) contextInfo
                .get(DxoConstants.TIMESTAMP_PATTERN);
        return format == null ? null : getDateFormat(format);
    }

    public boolean hasEvalueatedValue(final String name) {
        return evaluatedValues.containsKey(name);
    }

    public Object getEvaluatedValue(final String name) {
        return evaluatedValues.get(name);
    }

    public void addEvaluatedValue(final String name, final Object value) {
        evaluatedValues.put(name, value);
    }

    public boolean isIncludeNull() {
        return !excludeNull;
    }

    public boolean isExcludeNull() {
        return excludeNull;
    }

    public boolean isExcludeWhitespace() {
        return excludeWhitespace;
    }

    public boolean isIncludeWhitespace() {
        return !excludeWhitespace;
    }

    public NestedPropertyInfo getNestedPropertyInfo(final Class srcClass,
            final String propertyName) {
        final String key = srcClass.getName() + "::" + propertyName;
        final Object value = nestedPropertyInfoCache.get(key);
        if (value != null) {
            return value == NOT_FOUND ? null : (NestedPropertyInfo) value;
        }
        return createNestedPropertyInfo(srcClass, propertyName, key);
    }

    public DatePropertyInfo getDatePropertyInfo(Class srcClass,
            String propertyName) {
        final String key = srcClass.getName() + "::" + propertyName;
        final Object value = datePropertyInfoCache.get(key);
        if (value != null) {
            return value == NOT_FOUND ? null : (DatePropertyInfo) value;
        }
        return createDatePropertyInfo(srcClass, propertyName + "_", key);
    }

    public DateFormat getDateFormat(final String format) {
        final Map formatters = (Map) dateFormatCache.get();
        DateFormat formatter = (DateFormat) formatters.get(format);
        if (formatter == null) {
            formatter = new SimpleDateFormat(format);
            formatters.put(format, formatter);
        }
        return formatter;
    }

    public String getSourcePrefix() {
        return sourcePrefix;
    }

    public String getSourcePropertyName(final String destPropertyName) {
        String sourcePropertyName = destPropertyName;
        if (!StringUtil.isEmpty(destPrefix)) {
            if (!sourcePropertyName.startsWith(destPrefix)) {
                return null;
            }
            sourcePropertyName = StringUtil.decapitalize(sourcePropertyName
                    .substring(destPrefix.length()));
        }
        if (!StringUtil.isEmpty(sourcePrefix)) {
            if (sourcePrefix.endsWith("_")) {
                sourcePropertyName = sourcePrefix + sourcePropertyName;
            } else {
                sourcePropertyName = sourcePrefix
                        + StringUtil.capitalize(sourcePropertyName);
            }
        }
        return sourcePropertyName;
    }

    /**
     * コンテキスト情報を返します。
     * <p>
     * コンテキスト情報がキャッシュにあればそれを返します。 キャッシュにない場合はコンテキスト情報緒を作成して返します。
     * 
     * @param reader
     *            アノテーションリーダ
     * @return コンテキスト情報
     */
    protected Map getContextInfo(final AnnotationReader reader) {
        final Map contextInfo = (Map) contextInfoCache.get(method);
        if (contextInfo != null) {
            return contextInfo;
        }
        return createContextInfo(reader);
    }

    /**
     * コンテキスト情報を作成して返します。
     * 
     * @param reader
     *            アノテーションリーダ
     * @return コンテキスト情報
     */
    protected Map createContextInfo(final AnnotationReader reader) {
        final Map contextInfo = new HashMap();
        final String datePattern = reader.getDatePattern(dxoClass, method);
        if (!StringUtil.isEmpty(datePattern)) {
            contextInfo.put(DxoConstants.DATE_PATTERN, datePattern);
        }
        final String timePattern = reader.getTimePattern(dxoClass, method);
        if (!StringUtil.isEmpty(timePattern)) {
            contextInfo.put(DxoConstants.TIME_PATTERN, timePattern);
        }
        final String timestampPattern = reader.getTimestampPattern(dxoClass,
                method);
        if (!StringUtil.isEmpty(timestampPattern)) {
            contextInfo.put(DxoConstants.TIMESTAMP_PATTERN, timestampPattern);
        }
        final String conversionRule = reader
                .getConversionRule(dxoClass, method);
        if (!StringUtil.isEmpty(conversionRule)) {
            contextInfo.put(DxoConstants.CONVERSION_RULE, DxoUtil
                    .parseRule(conversionRule));
        }
        contextInfo.put(DxoConstants.EXCLUDE_NULL, Boolean
                .valueOf(annotationReader.isExcludeNull(dxoClass, method)));
        contextInfo.put(DxoConstants.EXCLUDE_WHITESPACE,
                Boolean.valueOf(annotationReader.isExcludeWhitespace(dxoClass,
                        method)));
        contextInfo.put(DxoConstants.SOURCE_PREFIX, annotationReader
                .getSourcePrefix(dxoClass, method));
        contextInfo.put(DxoConstants.DEST_PREFIX, annotationReader
                .getDestPrefix(dxoClass, method));
        contextInfoCache.put(method, contextInfo);
        return contextInfo;
    }

    /**
     * ネストしたプロパティ情報を作成します。
     * <p>
     * 作成されたネストしたプロパティ情報はキャッシュに登録されます。
     * </p>
     * 
     * @param srcClass
     *            変換元のクラス
     * @param propertyName
     *            プロパティ名
     * @param key
     *            ネストしたプロパティ情報のキャッシュのキー
     * @return ネストしたプロパティ情報
     */
    protected NestedPropertyInfo createNestedPropertyInfo(final Class srcClass,
            final String propertyName, final String key) {
        final BeanDesc beanDesc = BeanDescFactory.getBeanDesc(srcClass);
        final int propertyDefSize = beanDesc.getPropertyDescSize();
        for (int i = 0; i < propertyDefSize; ++i) {
            final PropertyDesc propertyDesc = beanDesc.getPropertyDesc(i);
            final Class propertyType = propertyDesc.getPropertyType();
            if (!propertyDesc.isReadable() || isBasicType(propertyType)) {
                continue;
            }

            final BeanDesc nestedBeanDesc = BeanDescFactory
                    .getBeanDesc(propertyType);
            if (!nestedBeanDesc.hasPropertyDesc(propertyName)) {
                continue;
            }
            final PropertyDesc nestedPropertyDesc = nestedBeanDesc
                    .getPropertyDesc(propertyName);
            if (!nestedPropertyDesc.isReadable()) {
                continue;
            }
            final NestedPropertyInfo info = new NestedPropertyInfo(
                    propertyDesc, nestedPropertyDesc);
            nestedPropertyInfoCache.put(key, info);
            return info;
        }
        nestedPropertyInfoCache.put(key, NOT_FOUND);
        return null;
    }

    /**
     * クラスがJavaBeansではない基本的な型か判定します。
     * 
     * @param clazz
     *            クラス
     * @return クラスがJavaBeansではない基本的な型なら<code>true</code>、そうでない場合は<code>false</code>
     */
    protected boolean isBasicType(final Class clazz) {
        if (clazz.isPrimitive() || clazz.isArray()) {
            return true;
        }
        final String className = clazz.getName();
        return className.startsWith(JAVA) || className.startsWith(JAVAX);
    }

    /**
     * 日時プロパティ情報を作成します。
     * <p>
     * 作成された日時プロパティ情報はキャッシュに登録されます。
     * </p>
     * 
     * @param srcClass
     *            変換元のクラス
     * @param prefix
     *            プロパティ名の接頭辞
     * @param key
     *            日時プロパティ情報のキャッシュのキー
     * @return 日時プロパティ情報
     */
    protected DatePropertyInfo createDatePropertyInfo(Class srcClass,
            String prefix, String key) {
        final int pos = prefix.length();
        final BeanDesc beanDesc = BeanDescFactory.getBeanDesc(srcClass);
        final StringBuffer formatBuffer = new StringBuffer();
        final List propertyDescs = new ArrayList();
        final int size = beanDesc.getPropertyDescSize();
        for (int i = 0; i < size; ++i) {
            final PropertyDesc propertyDesc = beanDesc.getPropertyDesc(i);
            if (propertyDesc.getPropertyType() != String.class) {
                continue;
            }
            final String propertyName = propertyDesc.getPropertyName();
            if (!propertyName.startsWith(prefix)) {
                continue;
            }
            formatBuffer.append(propertyName.substring(pos));
            propertyDescs.add(propertyDesc);
        }
        if (formatBuffer.length() == 0) {
            datePropertyInfoCache.put(key, NOT_FOUND);
            return null;
        }
        final String format = new String(formatBuffer);
        final PropertyDesc[] array = (PropertyDesc[]) propertyDescs
                .toArray(new PropertyDesc[propertyDescs.size()]);
        final DatePropertyInfo info = new DatePropertyInfo(this, format, array);
        datePropertyInfoCache.put(key, info);
        return info;
    }

}
