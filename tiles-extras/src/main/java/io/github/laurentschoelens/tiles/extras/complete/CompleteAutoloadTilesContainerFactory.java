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

package io.github.laurentschoelens.tiles.extras.complete;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import jakarta.el.ArrayELResolver;
import jakarta.el.BeanELResolver;
import jakarta.el.CompositeELResolver;
import jakarta.el.ELResolver;
import jakarta.el.ListELResolver;
import jakarta.el.MapELResolver;
import jakarta.el.ResourceBundleELResolver;

import ognl.OgnlException;
import ognl.OgnlRuntime;
import ognl.PropertyAccessor;

import io.github.laurentschoelens.tiles.TilesContainer;
import io.github.laurentschoelens.tiles.compat.definition.digester.CompatibilityDigesterDefinitionsReader;
import io.github.laurentschoelens.tiles.context.TilesRequestContextHolder;
import io.github.laurentschoelens.tiles.definition.DefinitionsReader;
import io.github.laurentschoelens.tiles.definition.pattern.DefinitionPatternMatcherFactory;
import io.github.laurentschoelens.tiles.definition.pattern.PatternDefinitionResolver;
import io.github.laurentschoelens.tiles.definition.pattern.PrefixedPatternDefinitionResolver;
import io.github.laurentschoelens.tiles.definition.pattern.regexp.RegexpDefinitionPatternMatcherFactory;
import io.github.laurentschoelens.tiles.definition.pattern.wildcard.WildcardDefinitionPatternMatcherFactory;
import io.github.laurentschoelens.tiles.el.ELAttributeEvaluator;
import io.github.laurentschoelens.tiles.el.JspExpressionFactoryFactory;
import io.github.laurentschoelens.tiles.el.ScopeELResolver;
import io.github.laurentschoelens.tiles.el.TilesContextBeanELResolver;
import io.github.laurentschoelens.tiles.el.TilesContextELResolver;
import io.github.laurentschoelens.tiles.evaluator.AttributeEvaluatorFactory;
import io.github.laurentschoelens.tiles.evaluator.BasicAttributeEvaluatorFactory;
import io.github.laurentschoelens.tiles.factory.BasicTilesContainerFactory;
import io.github.laurentschoelens.tiles.factory.TilesContainerFactoryException;
//import io.github.laurentschoelens.tiles.freemarker.TilesSharedVariableFactory;
import io.github.laurentschoelens.tiles.impl.mgmt.CachingTilesContainer;
import io.github.laurentschoelens.tiles.locale.LocaleResolver;
import io.github.laurentschoelens.tiles.mvel.MVELAttributeEvaluator;
import io.github.laurentschoelens.tiles.mvel.ScopeVariableResolverFactory;
import io.github.laurentschoelens.tiles.mvel.TilesContextBeanVariableResolverFactory;
import io.github.laurentschoelens.tiles.mvel.TilesContextVariableResolverFactory;
import io.github.laurentschoelens.tiles.ognl.AnyScopePropertyAccessor;
import io.github.laurentschoelens.tiles.ognl.DelegatePropertyAccessor;
import io.github.laurentschoelens.tiles.ognl.NestedObjectDelegatePropertyAccessor;
import io.github.laurentschoelens.tiles.ognl.OGNLAttributeEvaluator;
import io.github.laurentschoelens.tiles.ognl.PropertyAccessorDelegateFactory;
import io.github.laurentschoelens.tiles.ognl.ScopePropertyAccessor;
import io.github.laurentschoelens.tiles.ognl.TilesApplicationContextNestedObjectExtractor;
import io.github.laurentschoelens.tiles.ognl.TilesContextPropertyAccessorDelegateFactory;
import io.github.laurentschoelens.tiles.request.ApplicationContext;
import io.github.laurentschoelens.tiles.request.ApplicationResource;
import io.github.laurentschoelens.tiles.request.Request;
//import io.github.laurentschoelens.tiles.request.freemarker.render.FreemarkerRenderer;
//import io.github.laurentschoelens.tiles.request.freemarker.render.FreemarkerRendererBuilder;
//import io.github.laurentschoelens.tiles.request.freemarker.servlet.SharedVariableLoaderFreemarkerServlet;
import io.github.laurentschoelens.tiles.request.mustache.MustacheRenderer;
import io.github.laurentschoelens.tiles.request.render.BasicRendererFactory;
import io.github.laurentschoelens.tiles.request.render.ChainedDelegateRenderer;
import io.github.laurentschoelens.tiles.request.render.Renderer;
//import io.github.laurentschoelens.tiles.request.velocity.render.VelocityRenderer;
//import io.github.laurentschoelens.tiles.request.velocity.render.VelocityRendererBuilder;
import org.mvel2.integration.VariableResolverFactory;

/**
 * Tiles container factory that:
 * <ul>
 * <li>create supporting objects for Velocity and FreeMarker;</li>
 * <li>create renderers for Velocity, FreeMarker, and Mustache templates;</li>
 * <li>allows using EL, MVEL and OGNL as attribute expressions;</li>
 * <li>allows using Wildcards and Regular Expressions in definition names;</li>
 * <li>loads Tiles 1.x definition files;</li>
 * <li>loads all the definition files that have the "tiles*.xml" pattern under
 * <code>/WEB-INF</code> directory (and subdirectories) and under
 * <code>META-INF</code> directories (and subdirectories) in every jar.</li>
 * </ul>
 *
 * @version $Rev$ $Date$
 * @since 2.2.0
 */
public class CompleteAutoloadTilesContainerFactory extends BasicTilesContainerFactory {

    /**
     * The freemarker renderer name.
     */
    private static final String FREEMARKER_RENDERER_NAME = "freemarker";

    /**
     * The velocity renderer name.
     */
    private static final String VELOCITY_RENDERER_NAME = "velocity";

    /**
     * The mustache renderer name.
     */
    private static final String MUSTACHE_RENDERER_NAME = "mustache";

    /** {@inheritDoc} */
    @Override
    public TilesContainer createDecoratedContainer(TilesContainer originalContainer,
            ApplicationContext applicationContext) {
        return new CachingTilesContainer(originalContainer);
    }

    /** {@inheritDoc} */
    @Override
    protected void registerAttributeRenderers(
            final BasicRendererFactory rendererFactory,
            final ApplicationContext applicationContext,
            final TilesContainer container,
            final AttributeEvaluatorFactory attributeEvaluatorFactory) {
        super.registerAttributeRenderers(rendererFactory, applicationContext, container, attributeEvaluatorFactory);

        // FIXME : freemarker unavailable
//        FreemarkerRenderer freemarkerRenderer = FreemarkerRendererBuilder
//                .createInstance()
//                .setApplicationContext(applicationContext)
//                .setParameter("TemplatePath", "/")
//                .setParameter("NoCache", "true")
//                .setParameter("ContentType", "text/html")
//                .setParameter("template_update_delay", "0")
//                .setParameter("default_encoding", "ISO-8859-1")
//                .setParameter("number_format", "0.##########")
//                .setParameter(SharedVariableLoaderFreemarkerServlet.CUSTOM_SHARED_VARIABLE_FACTORIES_INIT_PARAM,
//                        "tiles," + TilesSharedVariableFactory.class.getName()).build();
//        rendererFactory.registerRenderer(FREEMARKER_RENDERER_NAME, freemarkerRenderer);

        // FIXME : velocity unavailable
//        VelocityRenderer velocityRenderer = VelocityRendererBuilder.createInstance()
//                .setApplicationContext(applicationContext).build();
//        rendererFactory.registerRenderer(VELOCITY_RENDERER_NAME, velocityRenderer);

        MustacheRenderer mustacheRenderer = new MustacheRenderer();
        mustacheRenderer.setAcceptPattern(Pattern.compile(".+\\.mustache"));
        rendererFactory.registerRenderer(MUSTACHE_RENDERER_NAME, mustacheRenderer);
    }

    /** {@inheritDoc} */
    @Override
    protected Renderer createDefaultAttributeRenderer(BasicRendererFactory rendererFactory,
            ApplicationContext applicationContext, TilesContainer container,
            AttributeEvaluatorFactory attributeEvaluatorFactory) {

        ChainedDelegateRenderer retValue = new ChainedDelegateRenderer();
        retValue.addAttributeRenderer(rendererFactory.getRenderer(DEFINITION_RENDERER_NAME));
//        retValue.addAttributeRenderer(rendererFactory.getRenderer(VELOCITY_RENDERER_NAME));
//        retValue.addAttributeRenderer(rendererFactory.getRenderer(FREEMARKER_RENDERER_NAME));
        retValue.addAttributeRenderer(rendererFactory.getRenderer(MUSTACHE_RENDERER_NAME));
        retValue.addAttributeRenderer(rendererFactory.getRenderer(TEMPLATE_RENDERER_NAME));
        retValue.addAttributeRenderer(rendererFactory.getRenderer(STRING_RENDERER_NAME));
        return retValue;
    }

    /** {@inheritDoc} */
    @Override
    protected AttributeEvaluatorFactory createAttributeEvaluatorFactory(ApplicationContext applicationContext,
            LocaleResolver resolver) {
        BasicAttributeEvaluatorFactory attributeEvaluatorFactory = new BasicAttributeEvaluatorFactory(
                createELEvaluator(applicationContext));
        attributeEvaluatorFactory.registerAttributeEvaluator("MVEL", createMVELEvaluator());
        attributeEvaluatorFactory.registerAttributeEvaluator("OGNL", createOGNLEvaluator());

        return attributeEvaluatorFactory;
    }

    /** {@inheritDoc} */
    @Override
    protected <T> PatternDefinitionResolver<T> createPatternDefinitionResolver(Class<T> customizationKeyClass) {
        DefinitionPatternMatcherFactory wildcardFactory = new WildcardDefinitionPatternMatcherFactory();
        DefinitionPatternMatcherFactory regexpFactory = new RegexpDefinitionPatternMatcherFactory();
        PrefixedPatternDefinitionResolver<T> resolver = new PrefixedPatternDefinitionResolver<T>();
        resolver.registerDefinitionPatternMatcherFactory("WILDCARD", wildcardFactory);
        resolver.registerDefinitionPatternMatcherFactory("REGEXP", regexpFactory);
        return resolver;
    }

    /** {@inheritDoc} */
    @Override
    protected List<ApplicationResource> getSources(ApplicationContext applicationContext) {
        Collection<ApplicationResource> webINFSet = applicationContext.getResources("/WEB-INF/**/tiles*.xml");
        Collection<ApplicationResource> metaINFSet = applicationContext
                .getResources("classpath*:META-INF/**/tiles*.xml");

        List<ApplicationResource> filteredResources = new ArrayList<ApplicationResource>();
        if (webINFSet != null) {
            for (ApplicationResource resource : webINFSet) {
                if (Locale.ROOT.equals(resource.getLocale())) {
                    filteredResources.add(resource);
                }
            }
        }
        if (metaINFSet != null) {
            for (ApplicationResource resource : metaINFSet) {
                if (Locale.ROOT.equals(resource.getLocale())) {
                    filteredResources.add(resource);
                }
            }
        }
        return filteredResources;
    }

    /** {@inheritDoc} */
    @Override
    protected DefinitionsReader createDefinitionsReader(ApplicationContext applicationContext) {
        return new CompatibilityDigesterDefinitionsReader();
    }

    /**
     * Creates the EL evaluator.
     *
     * @param applicationContext The Tiles application context.
     * @return The EL evaluator.
     */
    private ELAttributeEvaluator createELEvaluator(ApplicationContext applicationContext) {
        ELAttributeEvaluator evaluator = new ELAttributeEvaluator();
        JspExpressionFactoryFactory efFactory = new JspExpressionFactoryFactory();
        efFactory.setApplicationContext(applicationContext);
        evaluator.setExpressionFactory(efFactory.getExpressionFactory());
        ELResolver elResolver = new CompositeELResolver() {
            {
                BeanELResolver beanElResolver = new BeanELResolver(false);
                add(new ScopeELResolver());
                add(new TilesContextELResolver(beanElResolver));
                add(new TilesContextBeanELResolver());
                add(new ArrayELResolver(false));
                add(new ListELResolver(false));
                add(new MapELResolver(false));
                add(new ResourceBundleELResolver());
                add(beanElResolver);
            }
        };
        evaluator.setResolver(elResolver);
        return evaluator;
    }

    /**
     * Creates the MVEL evaluator.
     *
     * @return The MVEL evaluator.
     */
    private MVELAttributeEvaluator createMVELEvaluator() {
        TilesRequestContextHolder requestHolder = new TilesRequestContextHolder();
        VariableResolverFactory variableResolverFactory = new ScopeVariableResolverFactory(requestHolder);
        variableResolverFactory.setNextFactory(new TilesContextVariableResolverFactory(requestHolder));
        variableResolverFactory.setNextFactory(new TilesContextBeanVariableResolverFactory(requestHolder));
        MVELAttributeEvaluator mvelEvaluator = new MVELAttributeEvaluator(requestHolder, variableResolverFactory);
        return mvelEvaluator;
    }

    /**
     * Creates the OGNL evaluator.
     *
     * @return The OGNL evaluator.
     */
    private OGNLAttributeEvaluator createOGNLEvaluator() {
        try {
            PropertyAccessor objectPropertyAccessor = OgnlRuntime.getPropertyAccessor(Object.class);
            PropertyAccessor applicationContextPropertyAccessor = new NestedObjectDelegatePropertyAccessor<Request>(
                    new TilesApplicationContextNestedObjectExtractor(), objectPropertyAccessor);
            PropertyAccessor anyScopePropertyAccessor = new AnyScopePropertyAccessor();
            PropertyAccessor scopePropertyAccessor = new ScopePropertyAccessor();
            PropertyAccessorDelegateFactory<Request> factory = new TilesContextPropertyAccessorDelegateFactory(
                    objectPropertyAccessor, applicationContextPropertyAccessor, anyScopePropertyAccessor,
                    scopePropertyAccessor);
            PropertyAccessor tilesRequestAccessor = new DelegatePropertyAccessor<Request>(factory);
            OgnlRuntime.setPropertyAccessor(Request.class, tilesRequestAccessor);
            return new OGNLAttributeEvaluator();
        } catch (OgnlException e) {
            throw new TilesContainerFactoryException("Cannot initialize OGNL evaluator", e);
        }
    }
}
