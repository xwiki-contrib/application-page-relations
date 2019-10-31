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
package org.xwiki.contrib.ring.internal.model;

import java.util.Date;

import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;

import com.xpn.xwiki.objects.BaseObject;

public class DateXWikiRing extends DefaultXWikiRing
{
    public final static EntityReference XCLASS_REFERENCE =
            new EntityReference("DateRingClass", EntityType.DOCUMENT, Names.RING_CODE_SPACE_REFERENCE);

    public DateXWikiRing(BaseObject object, EntityReferenceSerializer<String> serializer,
            DocumentReferenceResolver<String> resolver)
    {
        super(object, serializer, resolver);
    }

    public Date getValue()
    {
        return object.getDateValue(Names.HAS_VALUE);
    }

    public void setValue(Object value)
    {
        if (value == null) {
            return;
        }
        if (value != null && value instanceof Date) {
            object.setDateValue(Names.HAS_VALUE, (Date) value);
        } else {
            throw new IllegalArgumentException("Invalid argument passed to DateXWikiRing:setValue: " + value);
        }
    }
}
