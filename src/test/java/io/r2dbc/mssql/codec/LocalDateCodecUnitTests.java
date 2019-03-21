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

package io.r2dbc.mssql.codec;

import io.netty.buffer.ByteBuf;
import io.r2dbc.mssql.message.type.LengthStrategy;
import io.r2dbc.mssql.message.type.SqlServerType;
import io.r2dbc.mssql.message.type.TypeInformation;
import io.r2dbc.mssql.util.EncodedAssert;
import io.r2dbc.mssql.util.HexUtils;
import io.r2dbc.mssql.util.TestByteBufAllocator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static io.r2dbc.mssql.message.type.TypeInformation.builder;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link LocalDateCodec}.
 *
 * @author Mark Paluch
 */
class LocalDateCodecUnitTests {

    static final TypeInformation DATE = builder().withLengthStrategy(LengthStrategy.BYTELENTYPE).withServerType(SqlServerType.DATE).build();

    @Test
    void shouldEncodeDate() {

        LocalDate value = LocalDate.parse("2018-10-23");

        Encoded encoded = LocalDateCodec.INSTANCE.encode(TestByteBufAllocator.TEST, RpcParameterContext.out(), value);

        EncodedAssert.assertThat(encoded).isEqualToHex("03 DD 3E 0B");
        assertThat(encoded.getFormalType()).isEqualTo("date");
    }

    @Test
    void shouldEncodeNull() {

        Encoded encoded = LocalDateCodec.INSTANCE.encodeNull(TestByteBufAllocator.TEST);

        EncodedAssert.assertThat(encoded).isEqualToHex("00");
        assertThat(encoded.getFormalType()).isEqualTo("date");
    }

    @Test
    void shouldDecodeNull() {

        ByteBuf buffer = HexUtils.decodeToByteBuf("00");

        LocalDate decoded = LocalDateCodec.INSTANCE.decode(buffer, ColumnUtil.createColumn(DATE), LocalDate.class);

        assertThat(decoded).isNull();
    }

    @Test
    void shouldDecodeDate() {

        ByteBuf buffer = HexUtils.decodeToByteBuf("03DD3E0B");

        LocalDate decoded = LocalDateCodec.INSTANCE.decode(buffer, ColumnUtil.createColumn(DATE), LocalDate.class);

        assertThat(decoded).isEqualTo("2018-10-23");
    }
}
