/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.vxquery.xmlquery.query;

import java.util.Iterator;

import org.apache.vxquery.compiler.expression.ExpressionHandle;
import org.apache.vxquery.compiler.rewriter.framework.RulesetDriver;
import org.apache.vxquery.compiler.rewriter.rulesets.DefaultRulesetProviderImpl;
import org.apache.vxquery.context.StaticContext;
import org.apache.vxquery.functions.Function;
import org.apache.vxquery.functions.UserDefinedXQueryFunction;

final class XMLQueryOptimizer {
    static void optimize(Module module, int optimizationLevel) {
        StaticContext sCtx = module.getModuleContext();
        for (Iterator<Function> i = sCtx.listFunctions(); i.hasNext();) {
            Function f = i.next();
            if (Function.FunctionTag.UDXQUERY.equals(f.getTag())) {
                UserDefinedXQueryFunction udf = (UserDefinedXQueryFunction) f;
                optimize(udf.getBody(), optimizationLevel);
            }
        }
        if (module.getBody() != null) {
            optimize(module.getBody(), optimizationLevel);
        }
    }

    private static void optimize(ExpressionHandle handle, int optimizationLevel) {
        RulesetDriver rd = new RulesetDriver();
        rd.rewrite(handle, DefaultRulesetProviderImpl.INSTANCE.createRuleset(), optimizationLevel);
    }
}