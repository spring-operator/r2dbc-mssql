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

package io.r2dbc.mssql.message.token;

import io.netty.buffer.ByteBuf;
import io.r2dbc.mssql.message.tds.Encode;
import io.r2dbc.mssql.util.EncodedAssert;
import io.r2dbc.mssql.util.TestByteBufAllocator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link AllHeaders}.
 *
 * @author Mark Paluch
 */
class AllHeadersUnitTests {

    @Test
    void shouldEncodeProperly() {

        byte tx[] = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};

        AllHeaders header = AllHeaders.transactional(tx, 1);

        ByteBuf buffer = TestByteBufAllocator.TEST.buffer();

        header.encode(buffer);

        assertThat(buffer.readableBytes()).isEqualTo(header.getLength());

        EncodedAssert.assertThat(buffer).isEncodedAs(expected -> {

            Encode.asInt(expected, 22); // all length inclusive
            Encode.asInt(expected, 18); // header length inclusive
            Encode.uShort(expected, 2); // Tx header
            expected.writeBytes(tx); // Tx descriptor
            Encode.dword(expected, 1); // outstanding requests
        });
    }
}
