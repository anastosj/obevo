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
package com.gs.obevo.db.impl.platforms.hsql;

import java.io.File;
import java.sql.SQLException;

import com.gs.obevo.api.factory.Obevo;
import com.gs.obevo.apps.reveng.AquaRevengArgs;
import com.gs.obevo.db.api.appdata.DbEnvironment;
import com.gs.obevo.db.impl.core.jdbc.JdbcHelper;
import com.gs.obevo.db.unittest.UnitTestDbBuilder;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HsqlDeployerTest {

    private JdbcHelper jdbc;

    private void setup() {
        this.jdbc = new JdbcHelper();
    }

    @Test
    public void testDeploy() throws Exception {
        DbEnvironment env = Obevo.readEnvironment("./src/test/resources/platforms/hsql/step1");
        var context = env.buildAppContext("sa", "");

        context.setupEnvInfra();
        context.cleanEnvironment();
        context.deploy();
        // do a clean and deploy again to ensure that the clean functionality works
        context.cleanEnvironment();
        context.deploy();

        this.setup();
        // simple test to assert that the table has been created
        try (var conn = context.getDataSource().getConnection()) {
            assertEquals(3, this.jdbc.queryForInt(conn, "select count(*) from DBDEPLOY01.TABLE_A"));
            assertEquals(3, this.jdbc.queryForInt(conn, "select count(*) from DBDEPLOY01.VIEW1"));
            // String columnListSql =
            // "select name from syscolumns where id in (select id from sysobjects where name = 'TEST_TABLE')";
            // List<String> columnsInTestTable = db2JdbcTemplate.query(columnListSql, new SingleColumnRowMapper<String>());
            // Assert.assertEquals(Lists.mutable.with("ID", "STRING", "MYNEWCOL"), FastList.newList(columnsInTestTable));
        }

        // Test out reverse engineering
        var args = new AquaRevengArgs();
        args.setDbSchema("DBDEPLOY01");
        args.setGenerateBaseline(false);
        //args.setJdbcUrl("jdbc:hsqldb:hsql://localhost:9092/myserver");
        args.setJdbcUrl(env.getJdbcUrl());
        args.setUsername("sa");
        args.setPassword("");

        var outputDir = new File("./target/outputRevengNew");
        FileUtils.deleteDirectory(outputDir);
        args.setOutputPath(outputDir);

        env.getPlatform().getDdlReveng().reveng(args);
    }

    @Test
    public void testUnitTestDeploy() throws SQLException {
        var context = UnitTestDbBuilder.newBuilder()
                .setEnvName("test2")
                .setDbPlatform(new HsqlDbPlatform())
                .setSourcePath("platforms/hsql/step1")
                .setDbServer("HsqlCustomName")
                .buildContext();
        context.setupEnvInfra();

        // run it twice to ensure that we can drop the schema
        context.cleanAndDeploy();
        context.cleanAndDeploy();

        var env = context.getEnvironment();
        System.out.println("Created env at " + env.getJdbcUrl());

        this.setup();
        try (var conn = context.getDataSource().getConnection()) {
            assertEquals(3, this.jdbc.queryForInt(conn, "select count(*) from DBDEPLOY01.TABLE_A"));
            assertEquals(3, this.jdbc.queryForInt(conn, "select count(*) from DBDEPLOY01.VIEW1"));
        }
    }
}
