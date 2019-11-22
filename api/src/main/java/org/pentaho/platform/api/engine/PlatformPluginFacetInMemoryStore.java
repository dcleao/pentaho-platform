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

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * This class supports the implementation of the {@link IPlatformPlugin} interface's default methods
 * related with plugin facets.
 * @see IPlatformPluginFacet
 */
class PlatformPluginFacetInMemoryStore {

  private static PlatformPluginFacetInMemoryStore instance = new PlatformPluginFacetInMemoryStore();

  public static PlatformPluginFacetInMemoryStore getInstance() {
    return instance;
  }

  private final WeakHashMap<IPlatformPlugin, Map<Class<?>, Object>> store;

  private PlatformPluginFacetInMemoryStore() {
    store = new WeakHashMap<>();
  }

  /**
   * Gets the value of the facet of a given type for a given plugin.
   *
   * @param plugin - The platform plugin.
   * @param facetDataClass - The facet data class.
   * @param <TFacetBase> The base class of the facet data.
   * @param <TFacetValue> The class of the facet value.
   * @return The facet data.
   */
  @SuppressWarnings( "unchecked" )
  public synchronized <TFacetBase, TFacetValue extends TFacetBase> TFacetValue get(
      IPlatformPlugin plugin,
      Class<TFacetBase> facetDataClass ) {

    Map<Class<?>, Object> pluginFacetsMap = store.get( plugin );
    if ( pluginFacetsMap == null ) {
      return null;
    }

    // Suppressing warning unchecked due to this cast.
    return (TFacetValue) pluginFacetsMap.get( facetDataClass );
  }

  /**
   * Sets the value of the facet of a given type for a given plugin.
   *
   * @param plugin - The platform plugin.
   * @param facetDataClass - The facet data class.
   * @param value - The facet data.
   * @param <TFacetBase> The base class of the facet data.
   * @param <TFacetValue> The class of the facet value.
   */
  public synchronized <TFacetBase, TFacetValue extends TFacetBase> void set(
      IPlatformPlugin plugin,
      Class<TFacetBase> facetDataClass,
      TFacetValue value ) {

    store.computeIfAbsent( plugin, pluginWithoutProps -> new HashMap<>() )
        .put( facetDataClass, value );
  }

  /**
   * Clears the value of the facet of a given type for a given plugin.
   *
   * @param plugin - The platform plugin.
   * @param facetDataClass - The facet data class.
   * @param <TFacetBase> The base class of the facet data.
   */
  public synchronized <TFacetBase> void clear( IPlatformPlugin plugin, Class<TFacetBase> facetDataClass ) {
    Map<Class<?>, Object> pluginFacetsMap = store.get( plugin );
    if ( pluginFacetsMap != null ) {
      pluginFacetsMap.remove( facetDataClass );
    }
  }
}
