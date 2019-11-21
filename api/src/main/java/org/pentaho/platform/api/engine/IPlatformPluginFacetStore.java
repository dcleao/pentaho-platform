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

/**
 * The {@link IPlatformPluginFacetStore} interface represents a store of facet data, of varied types,
 * for instances of {@link IPlatformPlugin}.
 */
public interface IPlatformPluginFacetStore {
  /**
   * Gets the value of the facet of a given type for a given plugin.
   *
   * @param plugin - The platform plugin.
   * @param facetDataClass - The facet data class.
   * @param <TFacetData> The facet data class.
   * @return The facet data.
   */
  <TFacetData> TFacetData get( IPlatformPlugin plugin, Class<TFacetData> facetDataClass );

  /**
   * Sets the value of the facet of a given type for a given plugin.
   *
   * @param plugin - The platform plugin.
   * @param facetDataClass - The facet data class.
   * @param value - The facet data.
   * @param <TFacetData> The facet data class.
   */
  <TFacetData> void set( IPlatformPlugin plugin, Class<TFacetData> facetDataClass, TFacetData value );

  /**
   * Clears the value of the facet of a given type for a given plugin.
   *
   * @param plugin - The platform plugin.
   * @param facetDataClass - The facet data class.
   * @param <TFacetData> The facet data class.
   */
  <TFacetData> void clear( IPlatformPlugin plugin, Class<TFacetData> facetDataClass );

  /**
   * Gets a value that indicates if there is facet data of a given type associated with a given plugin.
   *
   * @param plugin - The platform plugin.
   * @param facetDataClass - The facet data class.
   * @param <TFacetData> The facet data class.
   */
  <TFacetData> boolean contains( IPlatformPlugin plugin, Class<TFacetData> facetDataClass );
}
