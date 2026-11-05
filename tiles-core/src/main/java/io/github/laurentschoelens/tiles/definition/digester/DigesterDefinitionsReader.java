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

package io.github.laurentschoelens.tiles.definition.digester;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import io.github.laurentschoelens.tiles.Attribute;
import io.github.laurentschoelens.tiles.Definition;
import io.github.laurentschoelens.tiles.Expression;
import io.github.laurentschoelens.tiles.ListAttribute;
import io.github.laurentschoelens.tiles.definition.DefinitionsFactoryException;
import io.github.laurentschoelens.tiles.definition.DefinitionsReader;

/**
 * Reads {@link Definition} objects from an XML InputStream
 * using the DOM API.
 *
 * <p>This implementation builds a Map of Definition objects
 * from a Tiles definitions XML document.</p>
 *
 * <p>The parser supports local DTD registration and optional
 * DTD validation.</p>
 *
 * <p>The Definition objects are stored internally in a Map.
 * Instances of this class are therefore not thread-safe.</p>
 */
public class DigesterDefinitionsReader implements DefinitionsReader {

    /**
     * XML validation parameter name.
     */
    public static final String PARSER_VALIDATE_PARAMETER_NAME =
            "io.github.laurentschoelens.tiles.definition.digester."
                    + "DigesterDefinitionsReader.PARSER_VALIDATE";

    /**
     * Local DTD registrations.
     *
     * There MUST be an even number of Strings in this array.
     */
    protected String[] registrations;

    /**
     * Stores Definition objects.
     */
    private Map<String, Definition> definitions;

    /**
     * Index used to generate unique names for anonymous definitions.
     */
    private int anonymousDefinitionIndex = 1;

    /**
     * Factory used to create XML document builders.
     */
    private final DocumentBuilderFactory xmlInputFactory;

    /**
     * Creates a new instance of DigesterDefinitionsReader.
     */
    public DigesterDefinitionsReader() {
        xmlInputFactory = DocumentBuilderFactory.newInstance();
        xmlInputFactory.setNamespaceAware(true);

        configureXmlSecurity();
    }

    /**
     * Configures XML parser security.
     *
     * <p>External entities are disabled. External DTDs can only
     * be resolved through the registered local resources.</p>
     */
    private void configureXmlSecurity() {
        try {
            xmlInputFactory.setFeature(
                    XMLConstants.FEATURE_SECURE_PROCESSING, true);

            xmlInputFactory.setFeature(
                    "http://xml.org/sax/features/external-general-entities",
                    false);

            xmlInputFactory.setFeature(
                    "http://xml.org/sax/features/external-parameter-entities",
                    true);

            xmlInputFactory.setFeature(
                    "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                    true);

            xmlInputFactory.setAttribute(
                    XMLConstants.ACCESS_EXTERNAL_DTD, "");

            xmlInputFactory.setAttribute(
                    XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        } catch (ParserConfigurationException | IllegalArgumentException e) {
            throw new IllegalStateException(
                    "Unable to configure XML parser security.", e);
        }
    }

    /**
     * Sets the validation of XML files.
     *
     * @param validating true to enable DTD validation.
     */
    public void setValidating(boolean validating) {
        xmlInputFactory.setValidating(validating);
    }

    /**
     * Reads Definition objects from a source.
     *
     * @param source the InputStream containing the XML definitions.
     * @return a Map of Definition objects.
     * @throws DefinitionsFactoryException if an error occurs while reading
     *         definitions.
     */
    @Override
    public Map<String, Definition> read(Object source) {

        if (source == null) {
            return null;
        }

        if (!(source instanceof InputStream)) {
            throw new DefinitionsFactoryException(
                    "Invalid source type. Requires java.io.InputStream.");
        }

        definitions = new LinkedHashMap<>();

        try {
            DocumentBuilder builder = xmlInputFactory.newDocumentBuilder();

            builder.setErrorHandler(new ThrowingErrorHandler());
            builder.setEntityResolver(new LocalEntityResolver());

            Document document = builder.parse((InputStream) source);

            Element root = document.getDocumentElement();

            parseDocument(root);

            return definitions;

        } catch (DefinitionsFactoryException e) {
            throw e;

        } catch (ParserConfigurationException | SAXException e) {
            throw new DefinitionsFactoryException(
                    "XML error reading definitions.", e);

        } catch (IOException e) {
            throw new DefinitionsFactoryException(
                    "I/O error reading definitions.", e);
        }
    }

    /**
     * Parses the XML document.
     *
     * <p>This method can be overridden by subclasses to support
     * additional document root elements.</p>
     *
     * @param root the document root element.
     */
    protected void parseDocument(Element root) {

        if (root == null || !"tiles-definitions".equals(getElementName(root))) {
            throw new DefinitionsFactoryException(
                    "Invalid XML document. Expected tiles-definitions root element.");
        }

        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE
                    && "definition".equals(getElementName(node))) {

                parseDefinition((Element) node, true);
            }
        }
    }

    /**
     * Parses a definition.
     *
     * @param elem the XML definition element.
     * @param isRoot true if this is a root definition.
     */
    protected void parseDefinition(Element elem, boolean isRoot) {

        Definition definition = new Definition();

        definition.setName(getAttributeValue(elem, "name"));
        definition.setPreparer(getAttributeValue(elem, "preparer"));

        String extendsAttribute = getAttributeValue(elem, "extends");
        definition.setExtends(extendsAttribute);

        // Template attribute

        String template = getAttributeValue(elem, "template");

        Attribute templateAttribute =
                Attribute.createTemplateAttribute(template);

        templateAttribute.setExpressionObject(
                Expression.createExpressionFromDescribedExpression(
                        getAttributeValue(elem, "templateExpression")));

        templateAttribute.setRole(getAttributeValue(elem, "role"));

        String templateType = getAttributeValue(elem, "templateType");

        if (templateType != null) {
            templateAttribute.setRenderer(templateType);
        } else if (extendsAttribute != null) {
            templateAttribute.setRenderer(null);
        }

        definition.setTemplateAttribute(templateAttribute);

        // Definition attributes

        parseDefinitionChildren(definition, elem);

        // Register the definition.

        addDefinition(definition);
    }

    /**
     * Parses the children of a definition.
     *
     * <p>This method can be overridden by subclasses to support
     * additional child elements.</p>
     *
     * @param definition the definition being populated.
     * @param elem the XML definition element.
     */
    protected void parseDefinitionChildren(
            Definition definition, Element elem) {

        NodeList children = elem.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {

            Node node = children.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element child = (Element) node;
            String tagName = getElementName(child);

            if ("put-attribute".equals(tagName)) {

                Attribute attribute = parseAttribute(child);

                String name = getAttributeValue(child, "name");

                boolean cascade =
                        "true".equals(getAttributeValue(child, "cascade"));

                definition.putAttribute(name, attribute, cascade);

            } else if ("put-list-attribute".equals(tagName)) {

                ListAttribute attribute = parseListAttribute(child);

                String name = getAttributeValue(child, "name");

                boolean cascade =
                        "true".equals(getAttributeValue(child, "cascade"));

                definition.putAttribute(name, attribute, cascade);
            }
        }
    }

    /**
     * Parses an attribute.
     *
     * <p>Supports nested definitions inside put-attribute and
     * add-attribute.</p>
     *
     * @param elem the XML attribute element.
     * @return the created Attribute.
     */
    protected Attribute parseAttribute(Element elem) {

        Attribute attribute = new Attribute();

        attribute.setValue(getAttributeValue(elem, "value"));

        String expression = getAttributeValue(elem, "expression");

        attribute.setExpressionObject(
                Expression.createExpressionFromDescribedExpression(expression));

        attribute.setRole(getAttributeValue(elem, "role"));
        attribute.setRenderer(getAttributeValue(elem, "type"));

        // Nested definitions

        NodeList children = elem.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {

            Node node = children.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element child = (Element) node;

            if (!"definition".equals(getElementName(child))) {
                continue;
            }

            String name = getAttributeValue(child, "name");

            if (name == null) {
                name = getNextUniqueDefinitionName(definitions);
            }

            attribute.setValue(name);
            attribute.setRenderer("definition");

            // Parse and register the nested definition.

            if (getAttributeValue(child, "name") == null) {
                child.setAttribute("name", name);
            }

            parseDefinition(child, false);
        }

        return attribute;
    }

    /**
     * Parses a list attribute.
     *
     * @param elem the XML list attribute element.
     * @return the created ListAttribute.
     */
    protected ListAttribute parseListAttribute(Element elem) {

        ListAttribute listAttribute = new ListAttribute();

        listAttribute.setInherit(
                Boolean.parseBoolean(getAttributeValue(elem, "inherit")));

        NodeList children = elem.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {

            Node node = children.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element child = (Element) node;
            String tagName = getElementName(child);

            if ("add-attribute".equals(tagName)) {

                Attribute attribute = parseAttribute(child);
                listAttribute.add(attribute);

            } else if ("add-list-attribute".equals(tagName)) {

                ListAttribute nestedList = parseListAttribute(child);
                listAttribute.add(nestedList);
            }
        }

        return listAttribute;
    }

    /**
     * Returns an XML element name.
     *
     * @param node the XML node.
     * @return the local element name.
     */
    protected String getElementName(Node node) {
        String localName = node.getLocalName();

        return localName != null ? localName : node.getNodeName();
    }

    /**
     * Returns an XML attribute value.
     *
     * @param elem the XML element.
     * @param name the attribute name.
     * @return the attribute value, or null if absent.
     */
    protected String getAttributeValue(Element elem, String name) {
        return elem.hasAttribute(name) ? elem.getAttribute(name) : null;
    }

    /**
     * Adds a Definition to the internal Map.
     *
     * @param definition the Definition to add.
     */
    public void addDefinition(Definition definition) {

        String name = definition.getName();

        if (name == null) {
            throw new DigesterDefinitionsReaderException(
                    "A root definition has been defined with no name");
        }

        definitions.put(name, definition);
    }

    /**
     * Resolves registered local DTDs.
     */
    private class LocalEntityResolver implements EntityResolver {

        @Override
        public InputSource resolveEntity(String publicId, String systemId)
                throws SAXException, IOException {

            String[] regs = getRegistrations();

            for (int i = 0; i < regs.length; i += 2) {

                if (regs[i].equals(publicId)) {

                    URL url = DigesterDefinitionsReader.class
                            .getResource(regs[i + 1]);

                    if (url != null) {
                        InputSource source = new InputSource(url.openStream());
                        source.setPublicId(publicId);
                        source.setSystemId(url.toExternalForm());
                        return source;
                    }
                }
            }

            // Do not allow external network or filesystem resolution.

            InputSource emptySource =
                    new InputSource(new StringReader(""));

            emptySource.setPublicId(publicId);
            emptySource.setSystemId(systemId);

            return emptySource;
        }
    }

    /**
     * Error handler that throws every parsing exception.
     */
    private static class ThrowingErrorHandler implements ErrorHandler {

        @Override
        public void warning(SAXParseException exception)
                throws SAXException {
            throw exception;
        }

        @Override
        public void error(SAXParseException exception)
                throws SAXException {
            throw exception;
        }

        @Override
        public void fatalError(SAXParseException exception)
                throws SAXException {
            throw exception;
        }
    }

    /**
     * Returns the registrations for local DTDs.
     *
     * @return an array containing public identifiers and resource paths.
     */
    protected String[] getRegistrations() {

        if (registrations == null) {
            registrations = new String[] {
                    "-//Apache Software Foundation//DTD Tiles Configuration 3.0//EN",
                    "/io/github/laurentschoelens/tiles/resources/tiles-config_3_0.dtd"
            };
        }

        return registrations;
    }

    /**
     * Creates a unique name for an anonymous definition.
     *
     * @param definitions the already created definitions.
     * @return a unique definition name.
     */
    protected String getNextUniqueDefinitionName(
            Map<String, Definition> definitions) {

        String candidate;

        do {
            candidate = "$anonymousDefinition" + anonymousDefinitionIndex;
            anonymousDefinitionIndex++;
        } while (definitions.containsKey(candidate));

        return candidate;
    }
}
