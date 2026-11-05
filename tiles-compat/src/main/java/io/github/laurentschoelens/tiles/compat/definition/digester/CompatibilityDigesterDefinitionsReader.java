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

package io.github.laurentschoelens.tiles.compat.definition.digester;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.github.laurentschoelens.tiles.Attribute;
import io.github.laurentschoelens.tiles.Definition;
import io.github.laurentschoelens.tiles.ListAttribute;
import io.github.laurentschoelens.tiles.beans.SimpleMenuItem;
import io.github.laurentschoelens.tiles.definition.DefinitionsFactoryException;
import io.github.laurentschoelens.tiles.definition.digester.DigesterDefinitionsReader;

/**
 * Reads Tiles 1.1, 1.2, 1.3, 1.4, 2.0 and 3.0 definitions.
 *
 * <p>This reader supports both the modern Tiles definitions syntax
 * and the legacy component definitions and instances syntaxes.</p>
 */
public class CompatibilityDigesterDefinitionsReader
        extends DigesterDefinitionsReader {

    /**
     * Legacy document syntax.
     */
    private enum Syntax {
        /**
         * Indicates the legacy Tiles definitions document syntax.
         */
        TILES_DEFINITIONS,
        /**
         * Legacy document syntax for component definition declarations.
         */
        COMPONENT_DEFINITIONS,
        /**
         * Legacy document syntax for component instance declarations.
         */
        COMPONENT_INSTANCES
    }

    /**
     * Legacy document syntax currently being parsed.
     */
    private Syntax syntax;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void parseDocument(Element root) {

        if (root == null) {
            throw new DefinitionsFactoryException(
                    "Invalid XML document: missing root element.");
        }

        String rootName = getElementName(root);

        switch (rootName) {
            case "tiles-definitions":
                syntax = Syntax.TILES_DEFINITIONS;
                break;

            case "component-definitions":
                syntax = Syntax.COMPONENT_DEFINITIONS;
                break;

            case "component-instances":
                syntax = Syntax.COMPONENT_INSTANCES;
                break;

            default:
                throw new DefinitionsFactoryException(
                        "Unsupported XML root element: " + rootName);
        }

        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {

            Node node = children.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element element = (Element) node;
            String name = getElementName(element);

            if (("definition".equals(name)
                    && syntax != Syntax.COMPONENT_INSTANCES)
                    || ("instance".equals(name)
                    && syntax == Syntax.COMPONENT_INSTANCES)) {

                parseDefinition(element, true);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Adds support for legacy put, putAttribute and putList elements.
     *
     * @param definition the definition
     * @param elem the XML element
     */
    @Override
    protected void parseDefinitionChildren(
            Definition definition, Element elem) {

        // First process the modern Tiles syntax.
        super.parseDefinitionChildren(definition, elem);

        NodeList children = elem.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {

            Node node = children.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element child = (Element) node;
            String tagName = getElementName(child);

            switch (tagName) {

                case "put":
                case "putAttribute":
                    putAttribute(definition, child);
                    break;

                case "putList":
                    putLegacyListAttribute(definition, child);
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * Processes a legacy put or putAttribute element.
     *
     * @param definition the definition
     * @param elem the XML element
     */
    private void putAttribute(Definition definition, Element elem) {

        Attribute attribute = parseAttribute(elem);

        definition.putAttribute(
                getAttributeValue(elem, "name"),
                attribute,
                "true".equals(getAttributeValue(elem, "cascade")));
    }

    /**
     * Processes a legacy putList element.
     * <p>
     * Tiles component definitions and Tiles definitions use ListAttribute.
     * Tiles component instances historically use Attribute.
     *
     * @param definition the definition
     * @param elem the XML element
     */
    private void putLegacyListAttribute(
            Definition definition, Element elem) {

        Attribute attribute;

        if (syntax == Syntax.COMPONENT_INSTANCES) {
            attribute = parseLegacyInstanceList(elem);
        } else {
            attribute = parseLegacyList(elem);
        }

        definition.putAttribute(
                getAttributeValue(elem, "name"),
                attribute,
                "true".equals(getAttributeValue(elem, "cascade")));
    }

    /**
     * Parses a legacy list represented by ListAttribute.
     *
     * @param elem the XML element
     * @return the parsed list attribute
     */
    private ListAttribute parseLegacyList(Element elem) {

        ListAttribute list = new ListAttribute();

        list.setInherit(
                Boolean.parseBoolean(getAttributeValue(elem, "inherit")));

        NodeList children = elem.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {

            Node node = children.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element child = (Element) node;
            String tagName = getElementName(child);

            switch (tagName) {

                case "add":
                    list.add(parseAttribute(child));
                    break;

                case "putList":
                    list.add(parseLegacyList(child));
                    break;

                case "item":
                case "bean":
                    list.add(parseBeanAttribute(child));
                    break;

                default:
                    break;
            }
        }

        return list;
    }

    /**
     * Parses a legacy component instance list represented by Attribute.
     *
     * @param elem the XML element
     * @return the parsed legacy component instance list
     */
    private Attribute parseLegacyInstanceList(Element elem) {

        Attribute list = new Attribute();

        setBeanProperties(list, elem);

        NodeList children = elem.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {

            Node node = children.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element child = (Element) node;
            String tagName = getElementName(child);

            switch (tagName) {

                case "add":
                    addToList(list, parseAttribute(child));
                    break;

                case "item":
                case "bean":
                    addToList(list, parseBeanAttribute(child));
                    break;

                default:
                    break;
            }
        }

        return list;
    }

    /**
     * Creates an Attribute containing a bean or menu item.
     * <p>
     * The default class is SimpleMenuItem. The classtype XML attribute
     * can specify another implementation.
     *
     * @param elem the XML element
     * @return the parsed bean attribute
     */
    private Attribute parseBeanAttribute(Element elem) {

        String className = getAttributeValue(elem, "classtype");

        Class<?> beanClass;

        try {
            if (className == null || className.isEmpty()) {
                beanClass = SimpleMenuItem.class;
            } else {
                ClassLoader classLoader =
                        Thread.currentThread().getContextClassLoader();

                if (classLoader == null) {
                    classLoader = getClass().getClassLoader();
                }

                beanClass = classLoader.loadClass(className);
            }

            Object bean = beanClass.getDeclaredConstructor().newInstance();

            setBeanProperties(bean, elem);

            // Apply explicit set-property elements.
            NodeList children = elem.getChildNodes();

            for (int i = 0; i < children.getLength(); i++) {

                Node node = children.item(i);

                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                Element child = (Element) node;

                if ("set-property".equals(getElementName(child))) {
                    setBeanProperty(
                            bean,
                            getAttributeValue(child, "property"),
                            getAttributeValue(child, "value"));
                }
            }

            return new Attribute(bean);

        } catch (ReflectiveOperationException e) {
            throw new DefinitionsFactoryException(
                    "Unable to create bean for element "
                            + getElementName(elem) + ".", e);
        }
    }

    /**
     * Applies XML attributes to JavaBean properties.
     * <p>
     * The classtype attribute is reserved for object creation.
     *
     * @param bean the bean
     * @param elem the XML element
     */
    private void setBeanProperties(Object bean, Element elem) {

        org.w3c.dom.NamedNodeMap attributes = elem.getAttributes();

        for (int i = 0; i < attributes.getLength(); i++) {

            Node attribute = attributes.item(i);
            String name = attribute.getNodeName();

            if ("classtype".equals(name)) {
                continue;
            }

            setBeanProperty(bean, name, attribute.getNodeValue());
        }
    }

    /**
     * Sets a JavaBean property using its setter.
     *
     * @param bean the bean
     * @param propertyName the property name
     * @param value the value
     */
    private void setBeanProperty(
            Object bean, String propertyName, String value) {

        if (propertyName == null || value == null) {
            return;
        }

        try {
            for (PropertyDescriptor descriptor : Introspector.getBeanInfo(bean.getClass()).getPropertyDescriptors()) {

                if (!descriptor.getName().equals(propertyName)) {
                    continue;
                }

                Method setter = descriptor.getWriteMethod();

                if (setter == null) {
                    return;
                }

                Class<?> type = setter.getParameterTypes()[0];

                setter.invoke(bean, convertValue(value, type));
                return;
            }

            // Digester's SetPropertiesRule ignores unmatched properties.

        } catch (Exception e) {
            throw new DefinitionsFactoryException(
                    "Unable to set property '" + propertyName
                            + "' on " + bean.getClass().getName(), e);
        }
    }

    /**
     * Converts a string to a JavaBean property type.
     *
     * @param value the value
     * @param type the type
     * @return the converted value
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object convertValue(String value, Class<?> type) {

        if (type == String.class) {
            return value;
        }

        if (type == boolean.class || type == Boolean.class) {
            return Boolean.valueOf(value);
        }

        if (type == int.class || type == Integer.class) {
            return Integer.valueOf(value);
        }

        if (type == long.class || type == Long.class) {
            return Long.valueOf(value);
        }

        if (type == double.class || type == Double.class) {
            return Double.valueOf(value);
        }

        if (type == float.class || type == Float.class) {
            return Float.valueOf(value);
        }

        if (type == short.class || type == Short.class) {
            return Short.valueOf(value);
        }

        if (type == byte.class || type == Byte.class) {
            return Byte.valueOf(value);
        }

        if (type == char.class || type == Character.class) {
            if (value.length() != 1) {
                throw new IllegalArgumentException(
                        "Expected a single character: " + value);
            }
            return value.charAt(0);
        }

        if (type.isEnum()) {
            return Enum.valueOf((Class<? extends Enum>) type, value);
        }

        if (type == Class.class) {
            try {
                return Class.forName(value);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        }

        // Allow standard Java types exposing a String constructor.
        try {
            return type.getConstructor(String.class).newInstance(value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(
                    "Unsupported property type: " + type.getName(), e);
        }
    }

    /**
     * Adds an attribute to a legacy list.
     *
     * @param parent the parent
     * @param child the child
     */
    private void addToList(Attribute parent, Attribute child) {

        try {
            Method add = parent.getClass().getMethod("add", Attribute.class);
            add.invoke(parent, child);

        } catch (ReflectiveOperationException e) {
            throw new DefinitionsFactoryException(
                    "Unable to add an element to legacy attribute list.", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Registers the local DTDs for all supported Tiles versions.
     */
    @Override
    protected String[] getRegistrations() {

        if (registrations == null) {
            registrations = new String[] {
                    "-//Apache Software Foundation//DTD Tiles Configuration 3.0//EN",
                    "/io/github/laurentschoelens/tiles/resources/tiles-config_3_0.dtd",

                    "-//Apache Software Foundation//DTD Tiles Configuration 2.0//EN",
                    "/io/github/laurentschoelens/tiles/compat/resources/tiles-config_2_0.dtd",

                    "-//Apache Software Foundation//DTD Tiles Configuration 2.1//EN",
                    "/io/github/laurentschoelens/tiles/compat/resources/tiles-config_2_1.dtd",

                    "-//Apache Software Foundation//DTD Tiles Configuration 1.1//EN",
                    "/io/github/laurentschoelens/tiles/compat/resources/tiles-config_1_1.dtd",

                    "-//Apache Software Foundation//DTD Tiles Configuration 1.3//EN",
                    "/io/github/laurentschoelens/tiles/compat/resources/tiles-config_1_3.dtd",

                    "-//Apache Software Foundation//DTD Tiles Configuration 1.4//EN",
                    "/io/github/laurentschoelens/tiles/compat/resources/tiles-config_1_4.dtd"
            };
        }

        return registrations;
    }
}
