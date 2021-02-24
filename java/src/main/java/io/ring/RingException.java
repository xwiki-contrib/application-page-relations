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
package io.ring;

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
