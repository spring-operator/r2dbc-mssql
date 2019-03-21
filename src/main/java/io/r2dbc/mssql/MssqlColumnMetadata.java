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

import io.r2dbc.mssql.codec.Codecs;
import io.r2dbc.mssql.message.token.Column;
import io.r2dbc.mssql.message.type.TypeInformation;
import io.r2dbc.mssql.util.Assert;
import io.r2dbc.spi.ColumnMetadata;
import io.r2dbc.spi.Nullability;

import javax.annotation.Nonnull;

/**
 * Microsoft SQL Server-specific {@link ColumnMetadata} based on {@link Column}.
 *
 * @author Mark Paluch
 */
public final class MssqlColumnMetadata implements ColumnMetadata {

    private final Column column;

    private final Codecs codecs;

    /**
     * Creates a new {@link MssqlColumnMetadata}.
     *
     * @param column the column.
     * @param codecs the {@link Codecs codec registry}
     */
    MssqlColumnMetadata(Column column, Codecs codecs) {
        this.column = Assert.requireNonNull(column, "Column must not be null");
        this.codecs = Assert.requireNonNull(codecs, "Codecs must not be null");
    }

    @Override
    public String getName() {
        return this.column.getName();
    }

    @Override
    public Integer getPrecision() {
        return getNativeTypeMetadata().getPrecision();
    }

    @Override
    public Integer getScale() {
        return getNativeTypeMetadata().getScale();
    }

    @Override
    public Nullability getNullability() {
        return getNativeTypeMetadata().isNullable() ? Nullability.NULLABLE : Nullability.NON_NULL;
    }

    @Override
    public Class<?> getJavaType() {
        return codecs.getJavaType(getNativeTypeMetadata());
    }

    @Override
    @Nonnull
    public TypeInformation getNativeTypeMetadata() {
        return this.column.getType();
    }
}
