/*
 * Copyright 2018-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.r2dbc.mssql;

import io.r2dbc.mssql.client.Client;
import io.r2dbc.mssql.client.ReactorNettyClient;
import io.r2dbc.mssql.util.Assert;
import io.r2dbc.spi.ConnectionFactory;
import reactor.core.publisher.Mono;

/**
 * An implementation of {@link ConnectionFactory} for creating connections to a Microsoft SQL Server database.
 *
 * @author Mark Paluch
 */
public final class MssqlConnectionFactory implements ConnectionFactory {

    private final Mono<? extends Client> clientFactory;

    private final MssqlConnectionConfiguration configuration;

    /**
     * Creates a new connection factory.
     *
     * @param configuration the configuration to use connections
     * @throws IllegalArgumentException when {@link MssqlConnectionConfiguration} is {@code null}.
     */
    public MssqlConnectionFactory(MssqlConnectionConfiguration configuration) {
        this(Mono.defer(() -> {
            Assert.requireNonNull(configuration, "configuration must not be null");

            return ReactorNettyClient.connect(configuration.getHost(), configuration.getPort(), configuration.getConnectTimeout()).cast(Client.class);
        }), configuration);
    }

    MssqlConnectionFactory(Mono<? extends Client> clientFactory, MssqlConnectionConfiguration configuration) {
        this.clientFactory = Assert.requireNonNull(clientFactory, "clientFactory must not be null");
        this.configuration = Assert.requireNonNull(configuration, "configuration must not be null");
    }

    @Override
    public Mono<MssqlConnection> create() {

        LoginConfiguration loginConfiguration = this.configuration.getLoginConfiguration();

        return this.clientFactory.delayUntil(client -> {
            return LoginFlow.exchange(client, loginConfiguration)
                .doOnError(e -> client.close().subscribe());
        })
            .map(MssqlConnection::new);
    }

    @Override
    public MssqlConnectionFactoryMetadata getMetadata() {
        return MssqlConnectionFactoryMetadata.INSTANCE;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(getClass().getSimpleName());
        sb.append(" [configuration=").append(this.configuration);
        sb.append(']');
        return sb.toString();
    }
}
