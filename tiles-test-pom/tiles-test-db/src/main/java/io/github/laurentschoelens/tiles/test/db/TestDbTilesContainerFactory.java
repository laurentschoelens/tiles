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

package io.github.laurentschoelens.tiles.test.db;

import java.util.Locale;

import javax.sql.DataSource;

import io.github.laurentschoelens.tiles.definition.LocaleDefinitionsFactory;
import io.github.laurentschoelens.tiles.definition.dao.DefinitionDAO;
import io.github.laurentschoelens.tiles.factory.BasicTilesContainerFactory;
import io.github.laurentschoelens.tiles.locale.LocaleResolver;
import io.github.laurentschoelens.tiles.request.ApplicationContext;


/**
 * Test alternate Tiles container factory that uses a DB to store definitions.
 *
 * @version $Rev$ $Date$
 */
public class TestDbTilesContainerFactory extends BasicTilesContainerFactory {

    /** {@inheritDoc} */
    @Override
    protected DefinitionDAO<Locale> createLocaleDefinitionDao(ApplicationContext applicationContext,
            LocaleResolver resolver) {
        LocaleDbDefinitionDAO definitionDao = new LocaleDbDefinitionDAO();
        definitionDao.setDataSource((DataSource) applicationContext
                .getApplicationScope().get("dataSource"));
        return definitionDao;
    }

    /** {@inheritDoc} */
    @Override
    protected LocaleDefinitionsFactory instantiateDefinitionsFactory(
            ApplicationContext applicationContext, LocaleResolver resolver) {
        return new LocaleDefinitionsFactory();
    }
}
