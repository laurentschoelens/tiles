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
package io.github.laurentschoelens.tiles.test.preparer;

import io.github.laurentschoelens.tiles.Attribute;
import io.github.laurentschoelens.tiles.AttributeContext;
import io.github.laurentschoelens.tiles.preparer.ViewPreparer;
import io.github.laurentschoelens.tiles.request.Request;

/**
 * A <code>ViewPreparer</code> that stores an attribute.
 *
 * @version $Rev$ $Date$
 */
public class AttributeViewPreparer implements ViewPreparer {

    /** {@inheritDoc} */
    public void execute(Request tilesContext,
            AttributeContext attributeContext) {
        attributeContext.putAttribute(
            "body",
            new Attribute("This is the value added by the AttributeViewPreparer"));
    }
}
