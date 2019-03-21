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

package io.r2dbc.mssql.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.r2dbc.mssql.message.tds.TdsFragment;
import org.reactivestreams.Publisher;

/**
 * A message sent from a client to a server.
 *
 * @author Mark Paluch
 */
public interface ClientMessage extends Message {

    /**
     * Encode a message into a {@link ByteBuf data buffer}.
     *
     * @param allocator the {@link ByteBufAllocator} to use to get a {@link ByteBuf data buffer} to write into.
     * @return a {@link Publisher} that produces the {@link ByteBuf} containing the encoded message.
     */
    Publisher<TdsFragment> encode(ByteBufAllocator allocator);

}
