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

package org.pentaho.platform.api.engine;

import java.io.Closeable;

/**
 * The {@link IPlatformPluginFacetLoader} interface holds the knowledge to load and unload a
 * facet of a plugin in a given Pentaho system.
 *
 * The {@link IPlatformPluginFacetLoader} interface should be registered as a service
 * having as property {@code facet-id} the fully qualified name of the facet data class.
 */
public interface IPlatformPluginFacetLoader {
  /**
   * Loads the facet of a given plugin, in a given Pentaho system.
   *
   * If needed, the plugin's {@link ClassLoader} can be obtained from:
   * <pre>{@code
   * pentahoSystemObjectFactory
   *   .get(ClassLoader.class, null, Collections.singletonMap("plugin-id", plugin.getId()))
   * }</pre>
   *
   * Likewise, if needed, the plugin's Spring {@code GenericApplicationContext} can be obtained from:
   * <pre>{@code
   * pentahoSystemObjectFactory
   *   .get(GenericApplicationContext.class, null, Collections.singletonMap("plugin-id", plugin.getId()))
   * }</pre>
   *
   * @param plugin - The platform plugin.
   * @param facet - The plugin facet being loaded.
   * @param pentahoSystemObjectFactory - The object factory of the Pentaho system.
   *
   * @return A closeable instance which, when closed, allows unloading the plugin facet
   * from the given Pentaho system.
   */
  Closeable load(
      IPlatformPlugin plugin,
      IPlatformPluginFacet facet,
      IPentahoRegistrableObjectFactory pentahoSystemObjectFactory
  );
}
