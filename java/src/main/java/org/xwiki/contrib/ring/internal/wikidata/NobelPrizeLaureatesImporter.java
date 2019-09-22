/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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
package org.xwiki.contrib.ring.internal.wikidata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.ring.XWikiRingSet;
import org.xwiki.contrib.ring.XWikiTermFactory;
import org.xwiki.contrib.ring.internal.model.Names;
import org.xwiki.contrib.ring.internal.services.SolrRingIndexer;
import org.xwiki.environment.Environment;
import io.ring.RingException;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.text.StringUtils;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
@Singleton
@Named("nobel-prize-laureates")
public class NobelPrizeLaureatesImporter implements WikidataImporter
{
    @Inject
    protected Logger logger;

    @Inject
    @Named("current")
    protected DocumentReferenceResolver<String> resolver;

    @Inject
    @Named("local")
    private EntityReferenceSerializer<String> serializer;

    @Inject
    protected XWikiRingSet graph;

    @Inject
    protected XWikiTermFactory factory;

    @Inject
    protected Provider<XWikiContext> contextualizer;

    @Inject
    protected Environment environment;

    public HashMap<String, List<String>> getVerticesMetadataByKey(String pairs, String targetSpaceName)
    {

        HashMap<String, List<String>> mapping = new HashMap<>();
        String[] entries = pairs.split("\n");
        for (String entry : entries) {
            List<String> vertexMetadata = new ArrayList<>();
            entry = entry.trim();
            String[] vertexMetadataString = entry.split("\\s");
            int idx = vertexMetadataString[0].indexOf(":");
            String name = vertexMetadataString[0].substring(0, idx);
            String identifier = targetSpaceName + "." + vertexMetadataString[0].substring(idx + 1).trim();
            vertexMetadata.add(identifier);
            if (vertexMetadataString.length > 1) {
                vertexMetadata.add(vertexMetadataString[1].trim());
            }
            mapping.put(name, vertexMetadata);
        }
        return mapping;
    }

    public void importData(String json, String mapping, boolean withImages, String targetSpaceName, int max)
            throws IOException, RingException
    {
        XWikiContext context = contextualizer.get();
        XWiki xwiki = context.getWiki();
        SpaceReference spaceReference = new SpaceReference(targetSpaceName, context.getWikiReference());
        List<Laureate> entities = parse(json);
        HashMap<String, List<String>> verticesByKey = createTypesAndRelations(mapping, targetSpaceName);
        // Create target space home if not exists already
        DocumentReference reference = new DocumentReference("WebHome", spaceReference);
        if (!xwiki.exists(reference, context)) {
            graph.addTerm(reference, "Wiki");
        }
        // Create Wikidata page
        reference = resolver.resolve(spaceReference.getName() + ".wikidata");
        if (!xwiki.exists(reference, context)) {
            graph.addTerm(reference, "Wikidata");
        }
        logger.debug("Found {} in JSON", entities.size());
        for (int i = 0; i < entities.size(); i++) {
            if (max == 0 || i < max) {
                try {
                    Laureate laureate = entities.get(i);
                    maybeCreatePage(laureate, withImages, spaceReference, verticesByKey);
                } catch (Exception e) {
                    logger.error("importData", e);
                }
            }
        }
    }

    private HashMap<String, List<String>> createTypesAndRelations(String mapping, String targetSpaceName)
            throws RingException
    {
        XWikiContext context = contextualizer.get();
        XWiki xwiki = context.getWiki();
        HashMap<String, List<String>> verticesByKey = getVerticesMetadataByKey(mapping, targetSpaceName);

        for (Map.Entry<String, List<String>> entry : verticesByKey.entrySet()) {
            String title = StringUtils.capitalize(entry.getKey().replaceAll("-", " "));
            DocumentReference reference = resolver.resolve(entry.getValue().get(0));
            // Don't create vertex if it exists already
            if (!xwiki.exists(reference, context)) {
                if (entry.getKey().startsWith("has")) {
                    graph.addTerm(reference, title, factory.getIdentifier(Names.RELATION_TERM_NAME));
                    if (entry.getValue().size() > 1) {
                        // An "accept" constraint is then expected, related to the accepted type
                        String image = entry.getValue().get(1);
                        if (!image.equals("string")) {
                            image =
                                    SolrRingIndexer.PROPERTY_GRAPH_PREFIX + serializer
                                            .serialize(factory.getIdentifier(Names.IS_A_RELATION_NAME))
                                            + ":\"" + targetSpaceName + "." + image + "\"";
                        }
                        graph.addRingOnce(reference, factory.getIdentifier(Names.HAS_IMAGE_RELATION_NAME), image);
                    }
                } else {
                    graph.addTerm(reference, title, factory.getIdentifier(Names.TYPE_TERM_NAME));
                }
            }
        }
        return verticesByKey;
    }

    public DocumentReference maybeCreatePage(Entity entity, boolean withImages, SpaceReference spaceReference,
            HashMap<String, List<String>> verticesMetadataByKey) throws IOException, RingException, XWikiException
    {
        logger.debug("Maybe create page {}", entity);
        XWikiContext context = contextualizer.get();
        XWiki xwiki = context.getWiki();
        String label = entity.getEntityLabel();
        DocumentReference vertex = toDocumentReference(label, spaceReference);
        if (!xwiki.exists(vertex, context)) {
            if (entity instanceof Laureate) {
                Laureate laureate = (Laureate) entity;
                List<String> vertexMetadata = verticesMetadataByKey.get(
                        WikidataNames.PERSON_TYPE_NAME);
                if (StringUtils.isNotEmpty(laureate.getTypeLabel()) && !laureate.getTypeLabel().equals("human")) {
                    vertexMetadata = verticesMetadataByKey.get(
                            WikidataNames.ORGANIZATION_TYPE_NAME);
                }
                graph.addTerm(vertex, label, resolver.resolve(vertexMetadata.get(0)));
            }

            XWikiDocument page = xwiki.getDocument(vertex, context).clone();
            page.setAuthorReference(context.getUserReference());
            page.setTitle(label);
            page.setContent(entity.getEntityDescription());
            // Make sure to save the page before creating rings originating from it
            xwiki.saveDocument(page, "", false, context);
            graph.addRingOnce(vertex, resolver.resolve(verticesMetadataByKey.get(
                    WikidataNames.HAS_WIKIDATA_ID).get(0)),
                    entity.getWikidataId());

            if (entity instanceof Laureate) {
                Laureate laureate = (Laureate) entity;

                if (withImages && StringUtils.isNotEmpty(laureate.getImageUrl())) {
                    File temporaryDirectory = environment.getTemporaryDirectory();
                    maybeDownloadImage(laureate.getImageUrl(), temporaryDirectory);
                    page.setAttachment(laureate.getImageUrl(),
                            new FileInputStream("images/" + ((Laureate) entity).getImageUrl()), context);
                }
            }
        }
        // The blocks below should be executed even if page is not new, because a laureate can be linked
        // to several countries or prizes
        if (entity instanceof Laureate) {
            Laureate laureate = (Laureate) entity;
            DocumentReference country = toDocumentReference(laureate.getCountryLabel(), spaceReference);
            if (country != null) {
                if (!xwiki.exists(country, context)) {
                    graph.addTerm(country, laureate.getCountryLabel(),
                            resolver.resolve(verticesMetadataByKey.get(
                                    WikidataNames.COUNTRY_TYPE_NAME).get(0)));
                }
                graph.addRingOnce(vertex, resolver.resolve(verticesMetadataByKey.get(
                        WikidataNames.HAS_COUNTRY).get(0)),
                        country);
            }
            DocumentReference nobelPrize = toDocumentReference(laureate.getPrizeLabel(), spaceReference);
            if (nobelPrize != null) {
                if (!xwiki.exists(nobelPrize, context)) {
                    graph.addTerm(nobelPrize, laureate.getPrizeLabel(),
                            resolver.resolve(verticesMetadataByKey.get(
                                    WikidataNames.AWARD_TYPE_NAME).get(0)));
                }

                graph.addRingOnce(vertex, resolver.resolve(verticesMetadataByKey.get(
                        WikidataNames.HAS_AWARD).get(0)),
                        nobelPrize);
            }
        }

        return vertex;
    }

    public File maybeDownloadImage(String imageUrl, File rootDirectory) throws IOException
    {
        if (imageUrl == null) {
            return null;
        }
        int idx = imageUrl.lastIndexOf("/");
        String imageName = imageUrl.substring(idx + 1);
        File file = new File(rootDirectory, "images/" + imageName);
        if (!file.exists()) {
            //URL url = computeWikimediaCommonsImageUrl(imageName);
            logger.debug("Download image {}", imageName);
            FileUtils.copyURLToFile(new URL(imageUrl), file);
            logger.debug("Image was downloaded");
        }
        return file;
    }

    public List<Laureate> parse(String json) throws IOException
    {
        Gson gson = new Gson();
        JsonReader jsonReader = new JsonReader(new StringReader(json));
        List<Laureate> data = gson.fromJson(jsonReader,
                WikidataNames.LAUREATE); // contains the whole reviews list
        jsonReader.close();
        return data;
    }

    private DocumentReference toDocumentReference(String label, SpaceReference spaceReference)
    {
        if (label == null) {
            return null;
        }
        String name = Normalizer.normalize(label.toLowerCase(), Normalizer.Form.NFD);
        name = name.replaceAll("[\\p{M}]", "");
        name = name.replaceAll("\\s", "-");
        return new DocumentReference(name, spaceReference);
    }
}
