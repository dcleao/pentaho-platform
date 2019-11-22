/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * Copyright (c) 2019 Hitachi Vantara. All rights reserved.
 */
package org.pentaho.platform.engine.core.system;

import org.osgi.framework.BundleContext;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPentahoObjectRegistration;
import org.pentaho.platform.api.engine.IPentahoRegistrableObjectFactory;
import org.pentaho.platform.engine.core.system.objfac.AggregateObjectFactory;
import org.pentaho.platform.engine.core.system.objfac.OSGIRuntimeObjectFactory;

public class PentahoSystemObjectFactory extends AggregateObjectFactory implements IPentahoRegistrableObjectFactory {

  private OSGIRuntimeObjectFactory runtimeObjectFactory;

  public PentahoSystemObjectFactory() {
    reset();
  }

  private void reset() {
    runtimeObjectFactory = new OSGIRuntimeObjectFactory();
    registerObjectFactory( runtimeObjectFactory );
  }

  @Override
  public void clear() {
    super.clear();
    reset();
  }

  public void setBundleContext( BundleContext context ) {
    runtimeObjectFactory.setBundleContext( context );
  }

  // region IPentahoRegistrableObjectFactory
  @Override
  public <T> IPentahoObjectRegistration registerObject( T obj ) {
    return runtimeObjectFactory.registerObject( obj );
  }

  @Override
  public <T> IPentahoObjectRegistration registerObject( T obj, Types types ) {
    return runtimeObjectFactory.registerObject( obj, types );
  }

  @Override
  public <T> IPentahoObjectRegistration registerObject( T obj, Class<?>... classes ) {
    return runtimeObjectFactory.registerObject( obj, classes );
  }

  @Override
  public <T> IPentahoObjectRegistration registerReference( IPentahoObjectReference<T> reference ) {
    return runtimeObjectFactory.registerReference( reference );
  }

  @Override
  public <T> IPentahoObjectRegistration registerReference( IPentahoObjectReference<T> reference, Types types ) {
    return runtimeObjectFactory.registerReference( reference, types );
  }

  @Override
  public <T> IPentahoObjectRegistration registerReference( IPentahoObjectReference<T> reference, Class<?>... classes ) {
    return runtimeObjectFactory.registerReference( reference, classes );
  }
  // endregion IPentahoRegistrableObjectFactory
}
