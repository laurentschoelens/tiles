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
package io.github.laurentschoelens.tiles.web.startup;

import static org.easymock.EasyMock.*;

import java.util.Enumeration;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;

import io.github.laurentschoelens.tiles.request.servlet.ServletApplicationContext;
import io.github.laurentschoelens.tiles.startup.TilesInitializer;
import org.junit.Test;

/**
 * Tests {@link AbstractTilesInitializerServlet}.
 *
 * @version $Rev$ $Date$
 */
public class AbstractTilesInitializerServletTest {

    /**
     * Test method for {@link AbstractTilesInitializerServlet#init()}.
     * @throws ServletException If something goes wrong.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testInit() throws ServletException {
        AbstractTilesInitializerServlet servlet = createMockBuilder(AbstractTilesInitializerServlet.class).createMock();
        TilesInitializer initializer = createMock(TilesInitializer.class);
        ServletConfig config = createMock(ServletConfig.class);
        ServletContext servletContext = createMock(ServletContext.class);
        Enumeration<String> names = createMock(Enumeration.class);

        expect(servlet.createTilesInitializer()).andReturn(initializer);
        expect(config.getServletContext()).andReturn(servletContext);
        expect(servletContext.getInitParameterNames()).andReturn(names);
        expect(config.getInitParameterNames()).andReturn(names);
        expect(config.getInitParameter(HttpServlet.LEGACY_DO_HEAD)).andReturn(Boolean.TRUE.toString());
        expect(names.hasMoreElements()).andReturn(false).times(2);
        initializer.initialize(isA(ServletApplicationContext.class));
        initializer.destroy();

        replay(servlet, initializer, config, servletContext, names);
        servlet.init(config);
        servlet.destroy();
        verify(servlet, initializer, config, servletContext, names);
    }
}
