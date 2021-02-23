/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.pagerelations.internal;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.pagerelations.PageRelationsService;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

/**
 * Implementation of a <tt>PageRelationsService</tt> component.
 *
 * @version $Id$
 */
@Component
@Singleton
public class DefaultPageRelationsService implements PageRelationsService
{
    @Inject
    @Named("compactwiki")
    protected EntityReferenceSerializer<String> compactWikiSerializer;

    @Inject
    @Named("default")
    protected EntityReferenceSerializer<String> defaultSerializer;

    @Inject
    protected DocumentReferenceResolver<SolrDocument> solrDocumentReferenceResolver;

    @Inject
    protected QueryManager queryManager;

    @Override
    // NB: the SolrExecutor includes performs some rights check. TODO: check
    // NB: abusively, we call "relations" both a page that is related to another, and the page objects storing
    // the link itself betweent the two pages.
    // TODO: allow also using PageReference
    // TODO: use SolrFieldEntitySerializer to compute the 'property.PageRelations.xxx' parameter name
    public List<DocumentReference> getIncomingRelations(DocumentReference reference) throws QueryException
    {
        String wikiId = reference.getWikiReference().getName();
        // We pass the reference as an argument twice other the contextual wiki is used, which is "xwiki", not
        // the one of "reference".
        String relativeIdentifier = compactWikiSerializer.serialize(reference, reference);
        String absoluteIdentifier = defaultSerializer.serialize((reference));
        String solrStatement = "type:DOCUMENT AND ((property.PageRelations.Code.PageRelationClass.page:"
            + "\"" + relativeIdentifier + "\" AND wiki:" + wikiId + ")"
            + " OR (property.PageRelations.Code.PageRelationClass.page:\"" + absoluteIdentifier + "\"))";
        Query query = this.queryManager.createQuery(solrStatement, "solr");
        List<Object> response = query.execute();
        List<DocumentReference> relations = new ArrayList<>();
        if (response != null && response.size() > 0) {
            QueryResponse searchResponse = (QueryResponse) response.get(0);
            SolrDocumentList results = searchResponse.getResults();
            for (SolrDocument document : results) {
                DocumentReference documentReference = this.solrDocumentReferenceResolver.resolve(document);
                relations.add(documentReference);
            }
            return relations;
        }
        return relations;
    }
}
