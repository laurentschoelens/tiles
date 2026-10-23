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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import io.github.laurentschoelens.tiles.AttributeContext;
import io.github.laurentschoelens.tiles.ListAttribute;
import io.github.laurentschoelens.tiles.TilesContainer;
import io.github.laurentschoelens.tiles.access.TilesAccess;
import io.github.laurentschoelens.tiles.autotag.core.runtime.ModelBody;
import io.github.laurentschoelens.tiles.request.ApplicationContext;
import io.github.laurentschoelens.tiles.request.Request;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link PutListAttributeModel}.
 *
 * @version $Rev$ $Date$
 */
public class PutListAttributeModelTest {

    /**
     * The model to test.
     */
    private PutListAttributeModel model;

    /**
     * Sets up the test.
     */
    @Before
    public void setUp() {
        model = new PutListAttributeModel();
    }

    /**
     * Test method for {@link PutListAttributeModel
     * #execute(String, String, boolean, boolean, Request, ModelBody)}.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void testExecute() throws IOException {
        TilesContainer container = createMock(TilesContainer.class);
        AttributeContext attributeContext = createMock(AttributeContext.class);
        Request request = createMock(Request.class);
        Deque<Object> composeStack = new ArrayDeque<Object>();
        Map<String, Object> requestScope = new HashMap<String, Object>();
        requestScope.put(ComposeStackUtil.COMPOSE_STACK_ATTRIBUTE_NAME, composeStack);
        requestScope.put(TilesAccess.CURRENT_CONTAINER_ATTRIBUTE_NAME, container);
        ApplicationContext applicationContext = createMock(ApplicationContext.class);
        ModelBody modelBody = createMock(ModelBody.class);

        modelBody.evaluateWithoutWriting();
        expect(request.getApplicationContext()).andReturn(applicationContext);
        expect(request.getContext("request")).andReturn(requestScope).anyTimes();
        expect(container.getAttributeContext(request)).andReturn(attributeContext);
        attributeContext.putAttribute(eq("myName"), isA(ListAttribute.class), eq(false));

        replay(container, attributeContext, request, modelBody);
        model.execute("myName", "myRole", false, false, request, modelBody);
        assertEquals(0, composeStack.size());
        verify(container, attributeContext, request, modelBody);
    }

}
