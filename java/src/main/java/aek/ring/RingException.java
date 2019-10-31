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
package aek.ring;

/**
 * Exception that can be thrown during executing operations on terms or rings.
 */
public class RingException extends Exception
{
    Throwable throwable;

    String message;

    public RingException(Throwable e)
    {
        this.throwable = e;
    }

    public RingException(String message) {
        this.message = message;
    }

    public RingException(String message, Throwable e)
    {
        this(e);
        this.message = message;
    }

    public Throwable get()
    {
        return throwable;
    }

    public String getMessage() {
        return message;
    }
}
