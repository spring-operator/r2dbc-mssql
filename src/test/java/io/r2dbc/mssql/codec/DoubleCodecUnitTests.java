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

import static io.r2dbc.mssql.message.type.TypeInformation.builder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

/**
 * Unit tests for {@link DoubleCodec}.
 *
 * @author Mark Paluch
 */
class DoubleCodecUnitTests {

    @Test
    void shouldEncodeDouble() {

        Encoded encoded = DoubleCodec.INSTANCE.encode(TestByteBufAllocator.TEST, RpcParameterContext.in(), 11344.554);

        EncodedAssert.assertThat(encoded).isEqualToHex("08 08 FE D4 78 E9 46 28 C6 40");
        assertThat(encoded.getFormalType()).isEqualTo("float");
    }

    @Test
    void shouldEncodeNull() {

        Encoded encoded = DoubleCodec.INSTANCE.encodeNull(TestByteBufAllocator.TEST);

        EncodedAssert.assertThat(encoded).isEqualToHex("08 00");
        assertThat(encoded.getFormalType()).isEqualTo("float");
    }

    @Test
    void shouldBeAbleToDecode() {

        TypeInformation floatType =
            builder().withServerType(SqlServerType.FLOAT).build();

        TypeInformation intType =
            builder().withServerType(SqlServerType.INTEGER).build();

        assertThat(DoubleCodec.INSTANCE.canDecode(ColumnUtil.createColumn(floatType), Double.class)).isTrue();
        assertThat(DoubleCodec.INSTANCE.canDecode(ColumnUtil.createColumn(intType), Double.class)).isFalse();
    }

    @Test
    void shouldDecodeFloat() {

        TypeInformation type = builder().withLengthStrategy(LengthStrategy.BYTELENTYPE).withServerType(SqlServerType.FLOAT).withMaxLength(8).build();

        ByteBuf buffer = HexUtils.decodeToByteBuf("08FED478E94628C640");

        assertThat(DoubleCodec.INSTANCE.decode(buffer, ColumnUtil.createColumn(type), Double.class)).isCloseTo(11344.554, offset(0.01));
    }

    @Test
    void shouldDecodeReal() {

        TypeInformation type = builder().withLengthStrategy(LengthStrategy.BYTELENTYPE).withServerType(SqlServerType.REAL).withMaxLength(4).build();

        ByteBuf buffer = HexUtils.decodeToByteBuf("0437423146");

        assertThat(DoubleCodec.INSTANCE.decode(buffer, ColumnUtil.createColumn(type), Double.class)).isCloseTo(11344.554, offset(0.01));
    }
}
