/*
 * $Id$
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
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

package io.github.laurentschoelens.tiles.template;

import java.io.IOException;
import java.util.Deque;

import io.github.laurentschoelens.tiles.ListAttribute;
import io.github.laurentschoelens.tiles.autotag.core.runtime.ModelBody;
import io.github.laurentschoelens.tiles.request.Request;

/**
 * <p>
 * <strong>Declare a list that will be pass as an attribute. </strong>
 * </p>
 * <p>
 * Declare a list that will be pass as an attribute . List elements are added
 * using the tag 'addAttribute' or 'addListAttribute'. This tag can only be used
 * inside 'insertTemplate', 'insertDefinition' or 'definition' tag.
 * </p>
 *
 * @version $Rev$ $Date$
 * @since 2.2.0
 */
public class AddListAttributeModel {

    /**
     * Executes the model.
     *
     * @param role The comma-separated list of roles that can use the list attribute.
     * @param request The request.
     * @param modelBody The body.
     * @throws IOException If the body cannot be evaluated.
     */
    public void execute(String role, Request request, ModelBody modelBody) throws IOException {
        Deque<Object> composeStack = ComposeStackUtil.getComposeStack(request);
        ListAttribute listAttribute = new ListAttribute();
        listAttribute.setRole(role);
        composeStack.push(listAttribute);
        modelBody.evaluateWithoutWriting();
        listAttribute = (ListAttribute) composeStack.pop();
        ListAttribute parent = (ListAttribute) composeStack.peek();
        parent.add(listAttribute);
    }
}
