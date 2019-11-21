/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2019 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.platform.plugin.services.pluginmgr;

import org.dom4j.Element;

// IPlatformPluginFacetXmlReader facet-id=FacetTypeFullyQualifiedName
/**
 * The {@link IPlatformPluginFacetXmlReader} interface supports the  {@link SystemPathXmlPluginProvider} class
 * to enable reading facet data from a plugin definition XML element.
 *
 * The {@link IPlatformPluginFacetXmlReader} interface should be registered as a service
 * having as property {@code facet-id} the fully qualified name of the facet data class.
 */
public interface IPlatformPluginFacetXmlReader {
  /**
   * Reads the facet data from a given plugin definition in XML format.
   *
   * @param pluginDefinition - The root element of the XML plugin definition.
   * @return An instance of the plugin's facet data class (never {@code null}).
   */
  Object read( Element pluginDefinition );
}

