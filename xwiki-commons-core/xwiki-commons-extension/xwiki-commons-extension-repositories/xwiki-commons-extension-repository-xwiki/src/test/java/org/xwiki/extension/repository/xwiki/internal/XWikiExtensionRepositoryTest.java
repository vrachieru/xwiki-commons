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
package org.xwiki.extension.repository.xwiki.internal;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.Unmarshaller;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.extension.ExtensionLicenseManager;
import org.xwiki.extension.ExtensionManagerConfiguration;
import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.result.IterableResult;
import org.xwiki.extension.repository.xwiki.model.jaxb.ExtensionVersionSummary;
import org.xwiki.extension.repository.xwiki.model.jaxb.ExtensionVersions;
import org.xwiki.extension.version.Version;
import org.xwiki.extension.version.internal.DefaultVersion;

import com.google.common.collect.Iterators;

/**
 * Unit tests for {@link XWikiExtensionRepository}.
 * 
 * @version $Id$
 */
public class XWikiExtensionRepositoryTest
{
    /**
     * The object being tested.
     */
    private XWikiExtensionRepository repository;

    private Unmarshaller unmarshaller = mock(Unmarshaller.class);

    @Before
    public void setUp() throws Exception
    {
        ExtensionRepositoryDescriptor repositoryDescriptor = mock(ExtensionRepositoryDescriptor.class);
        when(repositoryDescriptor.getURI()).thenReturn(new URI("http://extensions.xwiki.org/xwiki/rest"));

        XWikiExtensionRepositoryFactory repositoryFactory = mock(XWikiExtensionRepositoryFactory.class);
        when(repositoryFactory.getUnmarshaller()).thenReturn(unmarshaller);

        repository =
            new XWikiExtensionRepository(repositoryDescriptor, repositoryFactory, mock(ExtensionLicenseManager.class),
                mock(ExtensionManagerConfiguration.class));
    }

    @Test
    public void resolveVersions() throws Exception
    {
        ExtensionVersionSummary v1 = mock(ExtensionVersionSummary.class);
        when(v1.getVersion()).thenReturn("1.3");

        ExtensionVersionSummary v2 = mock(ExtensionVersionSummary.class);
        when(v2.getVersion()).thenReturn("2.4.1");

        List<ExtensionVersionSummary> versionSummaries = Arrays.asList(v1, v2);
        ExtensionVersions restVersions = mock(ExtensionVersions.class);
        when(unmarshaller.unmarshal(any(InputStream.class))).thenReturn(restVersions);
        when(restVersions.getExtensionVersionSummaries()).thenReturn(versionSummaries);
        when(restVersions.getOffset()).thenReturn(5);
        when(restVersions.getTotalHits()).thenReturn(7);

        IterableResult<Version> result = repository.resolveVersions("foo", 0, -1);
        // The result must take the offset and total hits number from the REST response.
        assertEquals(5, result.getOffset());
        assertEquals(7, result.getTotalHits());
        List<Version> versions = new ArrayList<Version>();
        Iterators.addAll(versions, result.iterator());
        assertEquals(Arrays.asList(new DefaultVersion("1.3"), new DefaultVersion("2.4.1")), versions);
    }
}