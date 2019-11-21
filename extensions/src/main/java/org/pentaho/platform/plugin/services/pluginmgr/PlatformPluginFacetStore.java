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

import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPlatformPluginFacetStore;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class PlatformPluginFacetStore implements IPlatformPluginFacetStore {

  private final WeakHashMap<IPlatformPlugin, Map<Class, Object>> store;

  public PlatformPluginFacetStore() {
    store = new WeakHashMap<>();
  }

  @Override
  public synchronized <TFacetData> TFacetData get(IPlatformPlugin plugin, Class<TFacetData> facetDataClass) {
    Map<Class, Object> pluginFacetsMap = store.get( plugin );
    if ( pluginFacetsMap == null ) {
      return null;
    }

    // noinspection unchecked
    return (TFacetData) pluginFacetsMap.get( facetDataClass );
  }

  @Override
  public synchronized <TFacetData> void set(IPlatformPlugin plugin, Class<TFacetData> facetDataClass, TFacetData value) {
    store.computeIfAbsent( plugin, k -> new HashMap<>() )
        .put( facetDataClass, value );
  }

  @Override
  public synchronized <TFacetData> void clear(IPlatformPlugin plugin, Class<TFacetData> facetDataClass) {
    Map<Class, Object> pluginFacetsMap = store.get( plugin );
    if ( pluginFacetsMap != null ) {
      pluginFacetsMap.remove( facetDataClass );
    }
  }

  @Override
  public synchronized <TFacetData> boolean contains(IPlatformPlugin plugin, Class<TFacetData> facetDataClass) {
    Map<Class, Object> pluginFacetsMap = store.get( plugin );
    if ( pluginFacetsMap == null ) {
      return false;
    }

    return pluginFacetsMap.containsKey( facetDataClass );
  }
}
