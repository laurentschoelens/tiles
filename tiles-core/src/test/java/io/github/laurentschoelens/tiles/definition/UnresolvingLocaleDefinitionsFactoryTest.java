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

package io.github.laurentschoelens.tiles.definition;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Locale;

import io.github.laurentschoelens.tiles.Definition;
import io.github.laurentschoelens.tiles.definition.dao.DefinitionDAO;
import io.github.laurentschoelens.tiles.locale.LocaleResolver;
import io.github.laurentschoelens.tiles.request.Request;
import org.junit.Test;

/**
 * Tests {@link UnresolvingLocaleDefinitionsFactory}.
 *
 * @version $Rev$ $Date$
 */
public class UnresolvingLocaleDefinitionsFactoryTest {

    /**
     * Test method for {@link UnresolvingLocaleDefinitionsFactory#getDefinition(String, Request)}.
     */
    @Test
    public void testGetDefinition() {
        DefinitionDAO<Locale> dao = createMock(DefinitionDAO.class);
        LocaleResolver localeResolver = createMock(LocaleResolver.class);
        UnresolvingLocaleDefinitionsFactory factory = new UnresolvingLocaleDefinitionsFactory();
        Request request = createMock(Request.class);
        Definition definition = createMock(Definition.class);
        Locale locale = Locale.ITALY;

        expect(localeResolver.resolveLocale(request)).andReturn(locale);
        expect(dao.getDefinition("myDefinition", locale)).andReturn(definition);

        replay(dao, localeResolver, request, definition);
        factory.setDefinitionDAO(dao);
        factory.setLocaleResolver(localeResolver);
        assertEquals(definition, factory.getDefinition("myDefinition", request));
        verify(dao, localeResolver, request, definition);
    }

}
