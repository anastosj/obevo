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
package com.gs.obevo.dbmetadata.impl.dialects;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.gs.obevo.api.appdata.PhysicalSchema;
import com.gs.obevo.dbmetadata.api.DaRoutine;
import com.gs.obevo.dbmetadata.api.DaRoutineType;
import com.gs.obevo.dbmetadata.api.DaRule;
import com.gs.obevo.dbmetadata.api.DaRuleImpl;
import com.gs.obevo.dbmetadata.api.DaSchema;
import com.gs.obevo.dbmetadata.api.DaUserType;
import com.gs.obevo.dbmetadata.api.DaUserTypeImpl;
import com.gs.obevo.dbmetadata.api.RuleBinding;
import com.gs.obevo.dbmetadata.impl.DaRoutinePojoImpl;
import com.gs.obevo.dbmetadata.impl.ExtraIndexInfo;
import com.gs.obevo.dbmetadata.impl.ExtraRerunnableInfo;
import com.gs.obevo.dbmetadata.impl.RuleBindingImpl;
import com.gs.obevo.dbmetadata.impl.SchemaByCatalogStrategy;
import com.gs.obevo.dbmetadata.impl.SchemaStrategy;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.collection.MutableCollection;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.impl.block.factory.Comparators;
import org.eclipse.collections.impl.collection.mutable.CollectionAdapter;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.list.mutable.ListAdapter;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.RoutineType;
import schemacrawler.schema.Schema;
import schemacrawler.schemacrawler.LimitOptionsBuilderFixed;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.schemacrawler.SchemaRetrievalOptionsBuilder;

/**
 * Metadata dialect for Sybase ASE.
 *
 * See here for information on the metadata tables: http://infocenter.sybase.com/archive/index.jsp?topic=/com.sybase.help.ase_15.0.tables/html/tables/tables25.htm
 *
 * TODO Add support for Sybase DEFAULTs?
 * But, we don't have a way yet to distinguish between pre-defined defaults (i.e. Omni) and regular defaults
 * SELECT 10 ord, 'DEFAULT' objtype, s1.name objname, 'DROP DEFAULT ' +  s1.name sqlstatement
 * FROM ${schemaName}..sysobjects s1
 * WHERE s1.type = 'D'
 *
 * For information on group/user creation: http://infocenter.sybase.com/help/index.jsp?topic=/com.sybase.infocenter.dc36274.1600/doc/html/san1393052489790.html
 */
public class SybaseAseMetadataDialect extends AbstractMetadataDialect {
    @Override
    public SchemaRetrievalOptionsBuilder getDbSpecificOptionsBuilder(Connection conn, PhysicalSchema physicalSchema, boolean searchAllTables) throws IOException {
        // no implementation is available in SchemaCrawler; hence, we start with a blank implementation
        return SchemaRetrievalOptionsBuilder.builder();
    }

    @Override
    public void updateLimitOptionsBuilder(LimitOptionsBuilderFixed options) {
        // Sybase driver supports SP metadata, but not functions. As a result, we must disable SchemaCrawler's own
        // lookups entirely and use our own query. (SchemaCrawler's inherent behavior for the SQL only adds to existing
        // routine data, not loading in entire new ones).
        options.routineTypes(Lists.mutable.<RoutineType>empty());
    }

    @Override
    public void updateSchemaInfoLevelBuilder(SchemaInfoLevelBuilder schemaInfoLevelBuilder) {
        schemaInfoLevelBuilder.setRetrieveDatabaseInfo(false);  // Fails for Sybase when connections are retrieved using Sybase's native JDBC pools
    }

    @Override
    public void setSchemaOnConnection(Connection conn, PhysicalSchema physicalSchema) {
        executeUpdate(conn, "use " + physicalSchema.getPhysicalName());
    }

    @Override
    public String getSchemaExpression(PhysicalSchema physicalSchema) {
        String subschema = ObjectUtils.defaultIfNull(physicalSchema.getSubschema(), "dbo");
        return physicalSchema.getPhysicalName() + "\\." + subschema;
    }

    @Override
    public void validateDatabase(Catalog database, final PhysicalSchema physicalSchema) {
        MutableCollection<Schema> schemasWithIncorrectCatalog = CollectionAdapter.adapt(database.getSchemas())
                .reject(each -> each.getCatalogName().equals(physicalSchema.getPhysicalName()));

        if (schemasWithIncorrectCatalog.notEmpty()) {
            throw new IllegalArgumentException("Returned ASE schemas should be in " + physicalSchema.getPhysicalName() + " catalog; however, these were not: " + schemasWithIncorrectCatalog);
        }
    }

    @Override
    public ImmutableCollection<RuleBinding> getRuleBindings(DaSchema schema, Connection conn) {
        String schemaName = schema.getName();
        // return the bindings to columns and bindings to domains
        String sql = """
                select tab.name 'object', rul.name 'rule', 'sp_bindrule ' || rul.name || ', ''' || tab.name || '.' || col.name || '''' 'sql'
                from %1$s..syscolumns col, %1$s..sysobjects rul, %1$s..sysobjects tab
                    , %1$s..sysusers sch
                where col.domain = rul.id and col.id = tab.id and tab.type='U' and col.domain <> 0
                    and tab.uid = sch.uid and sch.name = '%2$s'
                union
                select obj.name 'object', rul.name 'rule', 'sp_bindrule ' || rul.name || ', ' || obj.name 'sql'
                from %1$s..systypes obj, %1$s..sysobjects rul
                    , %1$s..sysusers sch
                where obj.domain = rul.id and obj.domain <> 0
                    and obj.uid = sch.uid and sch.name = '%2$s'
                """.formatted(schemaName, schema.getSubschemaName());

        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            MutableList<RuleBinding> ruleBindings = Lists.mutable.empty();
            while (rs.next()) {
                var ruleBinding = new RuleBindingImpl();
                ruleBinding.setObject(rs.getString("object"));
                ruleBinding.setRule(rs.getString("rule"));
                ruleBinding.setSql(rs.getString("sql"));
                ruleBindings.add(ruleBinding);
            }
            return ruleBindings.toImmutable();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ImmutableCollection<ExtraIndexInfo> searchExtraConstraintIndices(DaSchema schema, String tableName, Connection conn) throws SQLException {
        // Do not use ANSI JOIN as it does not work in Sybase 11.x - the SQL below works across all versions
        String tableClause = tableName == null ? "" : " AND tab.name = '" + tableName + "'";
        ImmutableList<Map<String, Object>> maps = ListAdapter.adapt(jdbc.query(conn,
                "select tab.name TABLE_NAME, ind.name INDEX_NAME, status2 & 8 IS_CONSTRAINT, status2 & 512 IS_CLUSTERED " +
                        "from " + schema.getName() + "..sysindexes ind" +
                        ", " + schema.getName() + "..sysobjects tab " +
                        ", " + schema.getName() + "..sysusers sch " +
                        "where ind.id = tab.id " +
                        "and tab.uid = sch.uid and sch.name = '" + schema.getSubschemaName() + "'\n" +
                        tableClause,
                new MapListHandler()
        )).toImmutable();

        return maps.collect(map -> new ExtraIndexInfo(
                (String) map.get("TABLE_NAME"),
                (String) map.get("INDEX_NAME"),
                (Integer) map.get("IS_CONSTRAINT") != 0,
                (Integer) map.get("IS_CLUSTERED") != 0
        ));
    }

    @Override
    public ImmutableCollection<ExtraRerunnableInfo> searchExtraViewInfo(DaSchema schema, String tableName, Connection conn) throws SQLException {
        String query = """
                select obj.name name, com.number number, colid2 colid2, colid colid, text text
                from %1$s..syscomments com
                , %1$s..sysobjects obj
                    , %1$s..sysusers sch
                where com.id = obj.id
                and com.texttype = 0
                and obj.type in ('V')
                and obj.uid = sch.uid and sch.name = '%2$s'
                order by com.id, number, colid2, colid
                """.formatted(schema.getName(), schema.getSubschemaName());

        ImmutableList<Map<String, Object>> maps = ListAdapter.adapt(jdbc.query(conn, query, new MapListHandler())).toImmutable();

        ImmutableList<ExtraRerunnableInfo> viewInfos = maps.collect(object -> new ExtraRerunnableInfo(
                (String) object.get("name"),
                null,
                (String) object.get("text"),
                null,
                (Integer) object.get("colid2"),
                (Integer) object.get("colid")
        ));

        return viewInfos.groupBy(ExtraRerunnableInfo.TO_NAME).multiValuesView().collect(objectInfos -> {
            var sortedInfos = objectInfos.toSortedList(Comparators.fromFunctions(ExtraRerunnableInfo.TO_ORDER2, ExtraRerunnableInfo.TO_ORDER1));
            StringBuilder definitionString = sortedInfos.injectInto(new StringBuilder(), (StringBuilder sb, ExtraRerunnableInfo rerunnableInfo) -> sb.append(rerunnableInfo.getDefinition()));
            return new ExtraRerunnableInfo(
                    sortedInfos.get(0).getName(),
                    null,
                    definitionString.toString()
            );
        }).toList().toImmutable();
    }

    @Override
    public ImmutableCollection<DaRule> searchRules(final DaSchema schema, Connection conn) throws SQLException {
        // Do not use ANSI JOIN as it does not work in Sybase 11.x - the SQL below works across all versions
        ImmutableList<Map<String, Object>> maps = ListAdapter.adapt(jdbc.query(conn,
                "SELECT rul.name as RULE_NAME\n" +
                        "FROM " + schema.getName() + "..sysobjects rul\n" +
                        "    , " + schema.getName() + "..sysusers sch\n" +
                        "WHERE rul.type = 'R'\n" +
                        "    and rul.uid = sch.uid and sch.name = '" + schema.getSubschemaName() + "' " +
                        "and not exists (\n" +
                        "\t-- Ensure that the entry is not attached to a table; otherwise, it is a regular table constraint, and will already be dropped when the table is dropped\n" +
                        "\tselect 1 from " + schema.getName() + "..sysconstraints c\n" +
                        "\twhere c.constrid = rul.id\n" +
                        ")\n",
                new MapListHandler()
        )).toImmutable();

        return maps.collect(map -> new DaRuleImpl((String) map.get("RULE_NAME"), schema));
    }

    @Override
    public ImmutableCollection<DaUserType> searchUserTypes(final DaSchema schema, Connection conn) throws SQLException {
        ImmutableList<Map<String, Object>> maps = ListAdapter.adapt(jdbc.query(conn,
                "SELECT s1.name as USER_TYPE_NAME\n" +
                        "FROM " + schema.getName() + "..systypes s1\n" +
                        "    , " + schema.getName() + "..sysusers sch\n" +
                        "WHERE s1.usertype>100 " +
                        "AND s1.uid = sch.uid and sch.name = '" + schema.getSubschemaName() + "' "
                , new MapListHandler()
        )).toImmutable();

        return maps.collect(map -> new DaUserTypeImpl((String) map.get("USER_TYPE_NAME"), schema));
    }

    @Override
    public ImmutableCollection<DaRoutine> searchExtraRoutines(final DaSchema schema, String procedureName, Connection conn) throws SQLException {
        String nameClause = procedureName != null ? "and obj.name = '" + procedureName + "'\n" : "";

        String query = """
                select obj.name name, obj.type type, com.number number, colid2 colid2, colid colid, text text
                from %1$s..syscomments com
                , %1$s..sysobjects obj
                    , %1$s..sysusers sch
                where com.id = obj.id
                and com.texttype = 0
                and obj.uid = sch.uid and sch.name = '%2$s'
                and obj.type in ('SF', 'P')
                %3$sorder by com.id, number, colid2, colid
                """.formatted(schema.getName(), schema.getSubschemaName(), nameClause);

        ImmutableList<Map<String, Object>> maps = ListAdapter.adapt(jdbc.query(conn, query, new MapListHandler())).toImmutable();

        ImmutableList<ExtraRerunnableInfo> routineInfos = maps.collect(object -> {
            var basename = (String) object.get("name");
            int number = (Integer) object.get("number");
            String specificName = number > 1 ? basename + ";" + number : basename;
            return new ExtraRerunnableInfo(
                    basename,
                    specificName,
                    (String) object.get("text"),
                    ((String) object.get("type")).trim(),
                    (Integer) object.get("colid2"),
                    (Integer) object.get("colid")
            );
        });

        return routineInfos.groupBy(ExtraRerunnableInfo.TO_SPECIFIC_NAME).multiValuesView().collect(objectInfos -> {
            var sortedInfos = objectInfos.toSortedList(Comparators.fromFunctions(ExtraRerunnableInfo.TO_ORDER2, ExtraRerunnableInfo.TO_ORDER1));
            StringBuilder definitionString = sortedInfos.injectInto(new StringBuilder(), (StringBuilder sb, ExtraRerunnableInfo rerunnableInfo) -> sb.append(rerunnableInfo.getDefinition()));
            return (DaRoutine) new DaRoutinePojoImpl(
                    sortedInfos.get(0).getName(),
                    schema,
                    sortedInfos.get(0).getType().equals("P") ? DaRoutineType.procedure : DaRoutineType.function,
                    sortedInfos.get(0).getSpecificName(),
                    definitionString.toString()
            );
        }).toList().toImmutable();
    }

    @Override
    public SchemaStrategy getSchemaStrategy() {
        // Sybase stores the "database"/catalog first, then the schema. schema is usually meaningless for ASE, i.e. dbo value
        return SchemaByCatalogStrategy.INSTANCE;
    }

    @Override
    public ImmutableSet<String> getGroupNamesOptional(Connection conn, PhysicalSchema physicalSchema) throws SQLException {
        return ListAdapter.adapt(jdbc.query(conn, physicalSchema.getPhysicalName() + "..sp_helpgroup", new ColumnListHandler<String>("Group_name"))).toSet().toImmutable();
    }

    @Override
    public ImmutableSet<String> getUserNamesOptional(Connection conn, PhysicalSchema physicalSchema) throws SQLException {
        return ListAdapter.adapt(jdbc.query(conn, physicalSchema.getPhysicalName() + "..sp_helpuser", new ColumnListHandler<String>("Users_name"))).toSet().toImmutable();
    }
}
