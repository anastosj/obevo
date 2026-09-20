/**
 * Copyright 2017 Goldman Sachs.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
// Portions copyright Jonathan Anastos. Licensed under Apache 2.0 license
*/
package com.gs.obevo.db.impl.platforms.mssql;

import java.util.Arrays;

import com.gs.obevo.api.appdata.ChangeInput;
import com.gs.obevo.db.impl.platforms.sqltranslator.UnparsedSqlTranslator;
import com.gs.obevo.db.sqlparser.tokenparser.SqlTokenParser;
import com.gs.obevo.db.sqlparser.tokenparser.SqlTokenType;
import com.gs.obevo.impl.text.CommentRemover;

public abstract class MsSqlDomainSqlTranslator implements UnparsedSqlTranslator {
    @Override
    public String handleRawFullSql(String sql, ChangeInput change) {
        sql = CommentRemover.removeComments(sql, "sybase sp_addtype conversion").trim();
        if (sql.startsWith("sp_addtype")) {
            // all the params are in string literals, e.g. 'param'. Let's extract it out
            var allParts = new SqlTokenParser().parseTokens(sql);
            var parts = allParts.select(it -> it.getTokenType().equals(SqlTokenType.STRING));

            String domainName = null;
            String domainType = null;
            String domainProps = null;
            for (int i = 0; i < parts.size(); i++) {
                String part = this.stripAddTypeParam(parts.get(i).getText().trim());
                if (i == 0) {
                    domainName = part;
                } else if (i == 1) {
                    domainType = part;
                } else if (i == 2) {
                    domainProps = part;
                } else {
                    throw new IllegalArgumentException("Not expecting more than 3 args here, but got: " +
                            Arrays.asList(parts));
                }
            }

            return this.createDomainSql(domainName, domainType, domainProps);
        } else {
            return sql;
        }
    }

    protected abstract String createDomainSql(String domainName, String domainType, String domainProps);

    private String stripAddTypeParam(String param) {
        var firstQuoteIndex = param.indexOf('\'');
        var lastQuoteIndex = param.lastIndexOf('\'');
        return param.substring(firstQuoteIndex + 1, lastQuoteIndex);
    }
}
