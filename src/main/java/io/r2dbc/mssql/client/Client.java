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

package io.r2dbc.mssql.client;

import io.netty.buffer.ByteBufAllocator;
import io.r2dbc.mssql.message.ClientMessage;
import io.r2dbc.mssql.message.Message;
import io.r2dbc.mssql.message.TransactionDescriptor;
import io.r2dbc.mssql.message.type.Collation;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * An abstraction that wraps the networking part of exchanging {@link Message}s.
 *
 * @author Mark Paluch
 */
public interface Client {

    /**
     * Release any resources held by the {@link Client}.
     *
     * @return a {@link Mono} that indicates that a client has been closed
     */
    Mono<Void> close();

    /**
     * Perform an exchange of messages.
     *
     * @param requests the publisher of outbound messages
     * @return a {@link Flux} of incoming messages that ends with the end of the frame.
     */
    Flux<Message> exchange(Publisher<? extends ClientMessage> requests);

    /**
     * Returns the {@link ByteBufAllocator}.
     *
     * @return the {@link ByteBufAllocator}
     */
    ByteBufAllocator getByteBufAllocator();

    /**
     * Returns the {@link TransactionDescriptor}.
     *
     * @return the {@link TransactionDescriptor} describing the server-side transaction.
     */
    TransactionDescriptor getTransactionDescriptor();

    /**
     * Returns the {@link TransactionStatus}.
     *
     * @return the current {@link TransactionStatus}.
     */
    TransactionStatus getTransactionStatus();

    /**
     * Returns the database {@link Collation}.
     *
     * @return the database {@link Collation}.
     */
    Optional<Collation> getDatabaseCollation();

    /**
     * @return the required {@link Collation} for the current database.
     * @throws IllegalStateException if no {@link Collation} is available.
     */
    default Collation getRequiredCollation() {
        return getDatabaseCollation().orElseThrow(() -> new IllegalStateException("Collation not available"));
    }

    /**
     * Returns whether the server supports column encryption.
     *
     * @return {@literal true} if the server supports column encryption.
     */
    boolean isColumnEncryptionSupported();
}
