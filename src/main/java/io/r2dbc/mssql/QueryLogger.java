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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Query logger to log queries.
 *
 * @author Mark Paluch
 */
final class QueryLogger {

    private static final Logger QUERY_LOGGER = LoggerFactory.getLogger("io.r2dbc.mssql.QUERY");

    static void logQuery(String query) {
        QUERY_LOGGER.debug("Executing query: {}", query);
    }
}
