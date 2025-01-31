/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

package org.wildfly.extension.clustering.singleton;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.jboss.msc.value.Value;
import org.wildfly.clustering.service.Builder;
import org.wildfly.clustering.service.IdentityServiceConfigurator;

/**
 * Builds an alias to another service.
 * @author Paul Ferraro
 * @param <T> the type of the target service
 * @deprecated Replaced by {@link IdentityServiceConfigurator}.
 */
@Deprecated
public class AliasServiceBuilder<T> implements Builder<T> {

    private final InjectedValue<T> value = new InjectedValue<>();
    private final ServiceName name;
    private final ServiceName targetName;
    private final Class<T> targetClass;

    /**
     * Constructs a new builder
     * @param name the target service name
     * @param targetName the target service
     * @param targetClass the target service class
     */
    public AliasServiceBuilder(ServiceName name, ServiceName targetName, Class<T> targetClass) {
        this.name = name;
        this.targetName = targetName;
        this.targetClass = targetClass;
    }

    @Override
    public ServiceName getServiceName() {
        return this.name;
    }

    @Override
    public ServiceBuilder<T> build(ServiceTarget target) {
        return (ServiceBuilder<T>)target.addService(this.name)
                .setInstance(new ValueService<T>(this.value))
                .addDependency(this.targetName, this.targetClass, this.value)
                .setInitialMode(ServiceController.Mode.PASSIVE);
    }

    private static final class ValueService<T> implements Service<T> {
        private final Value<T> value;
        private volatile T valueInstance;

        public ValueService(final Value<T> value) {
            this.value = value;
        }

        public void start(final StartContext context) {
            valueInstance = value.getValue();
        }

        public void stop(final StopContext context) {
            valueInstance = null;
        }

        public T getValue() throws IllegalStateException {
            final T value = valueInstance;
            if (value == null) {
                throw new IllegalStateException();
            }
            return value;
        }
    }

}
