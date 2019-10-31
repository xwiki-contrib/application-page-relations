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
package org.xwiki.contrib.ring.internal.listeners;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import aek.ring.RingException;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.event.Event;
import org.xwiki.refactoring.event.DocumentRenamingEvent;

/**
 * Listener updating the inverse vertices of an ringSet destination when the destination gets updated, or rings when a
 * relation page gets updated.
 *
 * @version $Id$
 */
@Component
@Singleton
@Named(DocumentRenamingEventListener.NAME)
public class DocumentRenamingEventListener extends BaseXWikiRingEventListener
{
    /**
     * Listener's name.
     */
    public static final String NAME = "ringSet.page.rename";

    /**
     * Default constructor.
     */
    public DocumentRenamingEventListener()
    {
        super(NAME, new DocumentRenamingEvent());
    }

    public void onEvent(Event event, Object source, Object data)
    {
        DocumentReference originalReference = ((DocumentRenamingEvent) event).getSourceReference();
        DocumentReference newReference = ((DocumentRenamingEvent) event).getTargetReference();
        try {
            // Update all rings using the reference as a destination
            ringSet.updateRingsTo(originalReference, newReference);
            // Update all rings using the reference as a relation, if any
            ringSet.updateRingsWith(originalReference, newReference);
            // No need to update rings origin since it's not stored as such at the moment since all rings
            // are stored as objects attached to their origin vertex document.
        } catch (RingException e) {
            // FIXME: error handling
            logger.error("Error while updating inverse relations of document {}.",
                    serializer.serialize(originalReference), e);
        }
    }

}
