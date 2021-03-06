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
package org.seasar.framework.container.factory.intertype;

import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.InterTypeDef;
import org.seasar.framework.container.annotation.tiger.InterType;
import org.seasar.framework.container.factory.AnnotationHandler;
import org.seasar.framework.container.factory.IntertypeDefBuilder;
import org.seasar.framework.container.impl.InterTypeDefImpl;
import org.seasar.framework.container.ognl.OgnlExpression;

/**
 * {@link InterType}アノテーションを読み取り{@link InterTypeDef}を作成するコンポーネントの実装クラスです。
 * 
 * @author koichik
 */
public class S2IntertypeDefBuilder implements IntertypeDefBuilder {

    public void appendIntertypeDef(final AnnotationHandler annotationHandler,
            final ComponentDef componentDef) {
        final Class<?> componentClass = componentDef.getComponentClass();
        if (componentClass == null) {
            return;
        }

        final InterType interType = componentClass
                .getAnnotation(InterType.class);
        if (interType != null) {
            for (String interTypeName : interType.value()) {
                final InterTypeDef interTypeDef = new InterTypeDefImpl();
                interTypeDef.setExpression(new OgnlExpression(interTypeName));
                componentDef.addInterTypeDef(interTypeDef);
            }
        }
    }

}
