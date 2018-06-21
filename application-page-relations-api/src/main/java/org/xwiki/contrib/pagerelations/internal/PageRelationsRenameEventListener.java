package org.xwiki.contrib.pagerelations.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.job.Job;
import org.xwiki.job.JobContext;
import org.xwiki.job.event.JobStartedEvent;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.ObservationContext;
import org.xwiki.observation.event.Event;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component
@Named("PageRelationsRenameEventListener")
@Singleton
public class PageRelationsRenameEventListener implements EventListener {

	public static final DocumentReference RESOURCE_CLASS = new DocumentReference("ressources", "USHCode",
			"ResourceClass");

	@Inject
	private Logger logger;

	@Inject
	private ObservationContext observationContext;

	@Inject
	private JobContext jobContext;

	@Inject
	private QueryManager queryManager;

	@Override
	public String getName() {
		return "PageRelationsRenameEventListener";
	}

	@Inject
	@Named("compactwiki")
	private EntityReferenceSerializer<String> compactWikiSerializer;

	@Override
	public List<Event> getEvents() {
		return Arrays.<Event>asList(new DocumentCreatedEvent());
	}

	@Override
	public void onEvent(Event event, Object source, Object data) {

		XWikiDocument currentDocument = (XWikiDocument) source;

		boolean isRenameJob = observationContext.isIn(new JobStartedEvent("refactoring/rename"));

		XWikiContext context = (XWikiContext) data;

		XWiki wiki = context.getWiki();

		Job job = jobContext.getCurrentJob();

		if (isRenameJob) {

			List<DocumentReference> references = job.getRequest().getProperty("entityReferences");

			if (references != null && references.size() > 0) {
				DocumentReference reference = references.get(0);
				try {

					// TODO: make the update generic: update all page fields to the new document
					// name
					Query query = this.queryManager.createQuery(
							"select distinct doc.fullName from Document doc, doc.object(PageRelations.Code.PageRelationClass) as obj where obj.page=:page",
							Query.XWQL);
					String name = reference.toString();
					int idx = name.indexOf(":");
					if (idx > 0) {
						name = name.substring(idx + 1);
					}
					query = query.bindValue("page", name);
					List entries = query.execute();
					for (Object entry : entries) {
						String inverseRelation = entry.toString();
						XWikiDocument inverseRelationDocument = wiki.getDocument(inverseRelation, context);
						DocumentReference classReference = getPageRelationClassReference();
						BaseObject object = inverseRelationDocument.getXObject(classReference, "page", name);
						if (object != null) {
							object.setStringValue("page", currentDocument.getFullName());
							wiki.saveDocument(inverseRelationDocument,
									"Update of relation to \"" + currentDocument.getFullName() + "\"", context);
						}
					}
				} catch (XWikiException | QueryException e) {

					logger.error("Error while updating inverse relations of document "
							+ compactWikiSerializer.serialize(reference), e);
				}
			}
		}

	}

	// TODO: how to define the reference properly
	protected DocumentReference getPageRelationClassReference() {
		ArrayList<String> spaces = new ArrayList<String>();
		spaces.add("PageRelations");
		spaces.add("Code");
		DocumentReference classReference = new DocumentReference("xwiki", spaces, "PageRelationClass");
		return classReference;
	}
}