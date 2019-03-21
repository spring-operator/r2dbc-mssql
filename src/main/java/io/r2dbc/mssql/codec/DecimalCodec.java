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
import io.netty.buffer.ByteBufAllocator;
import io.r2dbc.mssql.message.tds.Encode;
import io.r2dbc.mssql.message.type.Length;
import io.r2dbc.mssql.message.type.SqlServerType;
import io.r2dbc.mssql.message.type.TdsDataType;
import io.r2dbc.mssql.message.type.TypeInformation;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Codec for fixed floating-point values that are represented as {@link BigDecimal}.
 *
 * <ul>
 * <li>Server types: {@link SqlServerType#NUMERIC} and  {@link SqlServerType#DECIMAL}</li>
 * <li>Java type: {@link BigDecimal}</li>
 * <li>Downcast: none</li>
 * </ul>
 *
 * @author Mark Paluch
 */
final class DecimalCodec extends AbstractCodec<BigDecimal> {

    private final static int MAX_PRECISION = 38;

    static final DecimalCodec INSTANCE = new DecimalCodec();

    private DecimalCodec() {
        super(BigDecimal.class);
    }


    @Override
    Encoded doEncode(ByteBufAllocator allocator, RpcParameterContext context, BigDecimal value) {

        ByteBuf buffer = RpcEncoding.prepareBuffer(allocator, TdsDataType.DECIMALN.getLengthStrategy(), 0x11, SqlServerType.DECIMAL.getMaxLength());

        encodeBigDecimal(buffer, value);
        return new DecimalEncoded(TdsDataType.DECIMALN, buffer, MAX_PRECISION, value.scale());
    }

    @Override
    Encoded doEncodeNull(ByteBufAllocator allocator) {

        ByteBuf buffer = allocator.buffer(4);

        Encode.asByte(buffer, 0x11);
        Encode.asByte(buffer, SqlServerType.DECIMAL.getMaxLength());
        Encode.asByte(buffer, 0); // scale
        Encode.asByte(buffer, 0); // length

        return new DecimalEncoded(TdsDataType.DECIMALN, buffer, MAX_PRECISION, 0);
    }

    @Override
    boolean doCanDecode(TypeInformation typeInformation) {
        return typeInformation.getServerType() == SqlServerType.DECIMAL || typeInformation.getServerType() == SqlServerType.NUMERIC;
    }

    @Override
    BigDecimal doDecode(ByteBuf buffer, Length length, TypeInformation type, Class<? extends BigDecimal> valueType) {

        if (length.isNull()) {
            return null;
        }

        byte signByte = buffer.readByte();
        int sign = (0 == signByte) ? -1 : 1;
        byte[] magnitude = new byte[length.getLength() - 1];

        // read magnitude LE
        for (int i = 0; i < magnitude.length; i++) {
            magnitude[magnitude.length - 1 - i] = buffer.readByte();
        }

        return new BigDecimal(new BigInteger(sign, magnitude), type.getScale());
    }

    private static void encodeBigDecimal(ByteBuf buffer, BigDecimal value) {

        boolean isNegative = (value.signum() < 0);

        BigInteger valueToUse = value.unscaledValue();

        if (isNegative) {
            valueToUse = valueToUse.negate();
        }

        byte[] unscaledBytes = valueToUse.toByteArray();

        Encode.asByte(buffer, value.scale());
        Encode.asByte(buffer, unscaledBytes.length + 1); // data length + sign
        Encode.asByte(buffer, isNegative ? 0 : 1);   // 1 = +ve, 0 = -ve

        for (int i = unscaledBytes.length - 1; i >= 0; i--) {
            Encode.asByte(buffer, unscaledBytes[i]);
        }
    }

    static class DecimalEncoded extends RpcEncoding.HintedEncoded {

        private final int length;

        private final int scale;

        DecimalEncoded(TdsDataType dataType, ByteBuf value, int length, int scale) {
            super(dataType, SqlServerType.DECIMAL, value);
            this.length = length;
            this.scale = scale;
        }

        @Override
        public String getFormalType() {
            return super.getFormalType() + "(" + this.length + "," + this.scale + ")";
        }
    }
}
