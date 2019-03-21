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

import io.r2dbc.mssql.client.TestClient;
import io.r2dbc.mssql.codec.Codecs;
import io.r2dbc.mssql.codec.DefaultCodecs;
import io.r2dbc.mssql.message.TransactionDescriptor;
import io.r2dbc.mssql.message.tds.Encode;
import io.r2dbc.mssql.message.tds.ServerCharset;
import io.r2dbc.mssql.message.token.Column;
import io.r2dbc.mssql.message.token.ColumnMetadataToken;
import io.r2dbc.mssql.message.token.DataToken;
import io.r2dbc.mssql.message.token.DoneToken;
import io.r2dbc.mssql.message.token.RowToken;
import io.r2dbc.mssql.message.token.RowTokenFactory;
import io.r2dbc.mssql.message.token.SqlBatch;
import io.r2dbc.mssql.message.token.Tabular;
import io.r2dbc.mssql.message.type.LengthStrategy;
import io.r2dbc.mssql.message.type.SqlServerType;
import io.r2dbc.mssql.message.type.TypeInformation;
import io.r2dbc.spi.ColumnMetadata;
import io.r2dbc.spi.Result;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import reactor.util.annotation.Nullable;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.r2dbc.mssql.message.type.TypeInformation.Builder;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link MssqlResult}.
 *
 * @author Mark Paluch
 */
class SimpleMssqlStatementUnitTests {

    static final List<Column> COLUMNS = Arrays.asList(createColumn(0, "employee_id", SqlServerType.TINYINT, 1, LengthStrategy.FIXEDLENTYPE, null),
        createColumn(1, "last_name", SqlServerType.NVARCHAR, 100, LengthStrategy.USHORTLENTYPE, ServerCharset.UNICODE.charset()),

        createColumn(2, "first_name", SqlServerType.VARCHAR, 50, LengthStrategy.USHORTLENTYPE, ServerCharset.CP1252.charset()),

        createColumn(3, "salary", SqlServerType.MONEY, 8, LengthStrategy.BYTELENTYPE, null));

    static final Codecs CODECS = new DefaultCodecs();

    @Test
    void shouldReportNumberOfAffectedRows() {

        SqlBatch batch = SqlBatch.create(1, TransactionDescriptor.empty(), "SELECT * FROM foo");

        ColumnMetadataToken columns = ColumnMetadataToken.create(COLUMNS);

        Tabular tabular = Tabular.create(columns, DoneToken.create(1));

        TestClient client = TestClient.builder().expectRequest(batch).thenRespond(tabular.getTokens().toArray(new DataToken[0])).build();

        SimpleMssqlStatement statement = new SimpleMssqlStatement(client, CODECS, "SELECT * FROM foo");

        statement.execute()
            .flatMap(Result::getRowsUpdated)
            .as(StepVerifier::create)
            .expectNext(1)
            .verifyComplete();
    }

    @Test
    void shouldReturnColumnData() {

        SqlBatch batch = SqlBatch.create(1, TransactionDescriptor.empty(), "SELECT * FROM foo");

        ColumnMetadataToken columns = ColumnMetadataToken.create(COLUMNS);

        RowToken rowToken = RowTokenFactory.create(columns, buffer -> {

            Encode.asByte(buffer, 1);
            Encode.uString(buffer, "paluch", ServerCharset.UNICODE.charset());
            Encode.uString(buffer, "mark", ServerCharset.CP1252.charset());

            //money/salary
            Encode.asByte(buffer, 8);
            Encode.money(buffer, new BigDecimal("50.0000").unscaledValue());
        });

        Tabular tabular = Tabular.create(columns, rowToken, DoneToken.create(1));

        TestClient client = TestClient.builder().expectRequest(batch).thenRespond(tabular.getTokens().toArray(new DataToken[0])).build();

        SimpleMssqlStatement statement = new SimpleMssqlStatement(client, CODECS, "SELECT * FROM foo");

        statement.execute()
            .flatMap(result -> result.map((row, md) -> {

                Map<String, Object> rowData = new HashMap<>();
                for (ColumnMetadata column : md.getColumnMetadatas()) {
                    rowData.put(column.getName(), row.get(column.getName()));

                }

                return rowData;
            }))
            .as(StepVerifier::create)
            .consumeNextWith(actual -> {

                assertThat(actual).containsEntry("first_name", "mark").containsEntry("last_name", "paluch");

            })
            .verifyComplete();
    }

    private static Column createColumn(int index, String name, SqlServerType serverType, int length, LengthStrategy lengthStrategy, @Nullable Charset charset) {

        Builder builder = TypeInformation.builder().withServerType(serverType).withMaxLength(length).withLengthStrategy(lengthStrategy);
        if (charset != null) {
            builder.withCharset(charset);
        }
        TypeInformation type = builder.build();

        return new Column(index, name, type, null);
    }
}
