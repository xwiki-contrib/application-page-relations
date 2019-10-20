
- Possibly add a displayer property to each Ring class for custom display
- Check difference between ContextualAuthorizationManager and AuthorizationManager
- Add extension point to ElmSheet to load content dynamically
- Rename getDirectPredecessors to getRingsTo? and add method to RingTraverser and to RingScriptService?
- Rename removeRingsWith to removeRingsInvolving, or removeRingsThrough?
- Script service:
    Add method getRingsWith
    Add method getRingsTo, and check implementation of method getRings
- Rename identifier into reference, denotation?
- Check access rights, and protection of resources such as Ring:object, and dangerous RRing methods
- Add events RingEvent RingAddedEvent, TermAddedEvent, RingRemovedEvent, TermRemovedEvent, TermUpdatedEvent, RingUpdatedEvent, ...
- Check and translate the labels cf xar-handler / ApplicationResources
- Check with multilingual documents
- Add a cache of relations
- The setters of Relation, Ring should be protected to specific users because it allows direct change, and such objects are availble from the script service
- Make it easy to delete Solr index of all pages with an ringSet and recreate it (simple HQL query)
- When deleting document from Solr index from the XWiki administration via HQL query, it seems the index refererring to translated documents are not remove, eg "kuava:XWiki.RRing.Type_en" remains present in the index until we request a deletion from all
- In the Solr admin console, add ability to enter specify the documents to be removed via a query or a list of Solr identifiers
- When launching Solr reindex, it seems the index is not deleted first
- When importing a XAR with some pages with language="en", they get imported with no language set, while when creating a page, a default language is set.
- {@link org.xwiki.search.solr.internal.reference.DefaultSolrReferenceResolver#getResover(EntityReference)} should be "getResolver" (make the old method deprecated and add a new one?)
- In an existing wiki, check if there are documents with content that have a Solr identifier that does not end with a locale id. This could be a bug: Solr identifiers ending with "_" or anything that is not a locale identifier should only relate to technical documents (which are supposed to have no language, even though they actually have one: the comments in the code use a language, and so do the variable names.
- It seems Solr delete does not empty completely the index, but only the ones of correct documents that can be resolved properly (i.e. from which an EntityReference can be built). If an EntityReference cannot get created (which can happen if the index was wrongly created), the wrong index entry will remain.
- Issue when restoring a deleted vertex: the index is not correctly restored.
- the property "property.XWiki.RRing.IsConnectedTo:[PageA]" should remain present in the index until there is no ringSet any more invovling PageA.
- Imagine how a full implementation based on a ringSet database will work: Neo4jGraph, DgraphGraph, etc.
- Create document, delete it, the Solr index still contains an entry about it
- Move the SolrConsole to the admin tools?
- See also:
    giraph.apache.org: org.apache.giraph.graph, org.apache.giraph
    jgrapht
    jung
- See org.xwiki.index.tree.internal.nestedpages.ObjectTreeNode / instantion per lookup and inject context etc. However we should check that the obtained Term and rings are equal whatever the context is? We don't want one set of Vertices, rings, Relations per context.
- Make sure that the image / domain relations are checked when adding and saving an ringSet
- Handle other types for ringSet values: Long, Double, etc.
- User interface to enter / edit scalar values
- Security aspects: Ring encapsulate a BaseObject which require programming rights. The general idea is to make sure that 1) the access to these restricted objects is restricted by programming right check, 2) the operations offered by Ring on these encapsulated objects either are not dangerous or they check the programming rights.

