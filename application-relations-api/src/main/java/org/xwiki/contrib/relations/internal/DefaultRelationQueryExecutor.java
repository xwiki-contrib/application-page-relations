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
package org.xwiki.contrib.relations.internal;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.relations.RelationQueryExecutor;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

/**
 * Implementation of a <tt>RelationsQueryExecutor</tt> component.
 *
 * @version $Id$
 */
@Component
@Singleton
public class DefaultRelationQueryExecutor implements RelationQueryExecutor
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
        //Get inverse relations, querying pages which contain the current subject as a relation either with
        // its full identifier (including the wiki name), or with an identifier that does not contain the wiki name
        // (because the complement is in the same wiki as the subject).
        // We pass the reference as an argument twice other the contextual wiki is used, which is "xwiki", not
        // the one of "reference".
        String relativeIdentifier = compactWikiSerializer.serialize(reference, reference);
        String absoluteIdentifier = defaultSerializer.serialize((reference));
        String solrStatement = "type:DOCUMENT AND ((property.XWiki.Relations.IsRelatedToClass.page:"
            + "\"" + relativeIdentifier + "\" AND wiki:" + wikiId + ")"
            + " OR (property.XWiki.Relations.IsRelatedToClass.page:\"" + absoluteIdentifier + "\"))";
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
