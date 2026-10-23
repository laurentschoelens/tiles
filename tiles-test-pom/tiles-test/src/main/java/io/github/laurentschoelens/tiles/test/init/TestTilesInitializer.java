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

package io.github.laurentschoelens.tiles.test.init;

import jakarta.servlet.ServletContext;

import io.github.laurentschoelens.tiles.factory.AbstractTilesContainerFactory;
import io.github.laurentschoelens.tiles.request.ApplicationContext;
import io.github.laurentschoelens.tiles.request.servlet.wildcard.WildcardServletApplicationContext;
import io.github.laurentschoelens.tiles.startup.AbstractTilesInitializer;
import io.github.laurentschoelens.tiles.test.factory.TestTilesContainerFactory;

/**
 * Test Tiles initializer for Tiles initialization of the default container.
 *
 * @version $Rev$ $Date$
 */
public class TestTilesInitializer extends AbstractTilesInitializer {

    /** {@inheritDoc} */
    @Override
    protected AbstractTilesContainerFactory createContainerFactory(
            ApplicationContext context) {
        return new TestTilesContainerFactory();
    }

    /** {@inheritDoc} */
    @Override
    protected ApplicationContext createTilesApplicationContext(
            ApplicationContext preliminaryContext) {
        return new WildcardServletApplicationContext(
                (ServletContext) preliminaryContext.getContext());
    }
}
