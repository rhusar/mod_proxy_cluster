/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.modcluster.container.tomcat;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Iterator;
import java.util.Set;

import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.jboss.modcluster.container.Engine;
import org.jboss.modcluster.container.Host;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link TomcatHost}.
 *
 * @author Paul Ferraro
 */
public class HostTestCase {
    protected final TomcatRegistry registry = mock(TomcatRegistry.class);
    protected final org.apache.catalina.Host host = mock(org.apache.catalina.Host.class);
    protected Engine engine;
    protected Host catalinaHost;

    @Before
    public void setup() {
        Service serviceMock = mock(Service.class);
        when(serviceMock.getServer()).thenReturn(mock(Server.class));

        org.apache.catalina.Engine engineMock = mock(org.apache.catalina.Engine.class);
        when(engineMock.getService()).thenReturn(serviceMock);

        when(this.host.getParent()).thenReturn(engineMock);

        engine = new TomcatEngine(registry, engineMock);
        catalinaHost = new TomcatHost(this.registry, this.host);
    }

    @Test
    public void getAliases() {
        when(this.host.getName()).thenReturn("host");
        when(this.host.findAliases()).thenReturn(new String[] { "alias" });

        Set<String> result = this.catalinaHost.getAliases();

        assertEquals(2, result.size());

        Iterator<String> aliases = result.iterator();
        assertEquals("host", aliases.next());
        assertEquals("alias", aliases.next());
    }

    @Test
    public void getEngine() {
        Engine result = this.catalinaHost.getEngine();

        assertEquals(this.engine, result);
    }

    @Test
    public void getName() {
        String expected = "name";

        when(this.host.getName()).thenReturn(expected);

        String result = this.catalinaHost.getName();

        assertSame(expected, result);
    }
}
