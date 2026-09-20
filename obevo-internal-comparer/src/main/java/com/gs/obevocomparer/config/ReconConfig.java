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
package com.gs.obevocomparer.config;

import java.util.ArrayList;
import java.util.List;

public class ReconConfig {

    private static final String FIELDS_DELIMITER = ",";

    private String name;

    private DataSourceConfig dataSource1;

    private DataSourceConfig dataSource2;

    private List<ReconFieldConfig> allFields;

    public ReconConfig() {
        this.init();
    }

    public ReconConfig(String nm, DataSourceConfig ds1, DataSourceConfig ds2) {
        this.init();
        this.setReconName(nm);
        this.setDataSource1(ds1);
        this.setDataSource2(ds2);
    }

    private void init() {
        this.allFields = new ArrayList<>();
    }

    public String getReconName() {
        return this.name;
    }

    public void setReconName(String reconName) {
        this.name = reconName;
    }

    public DataSourceConfig getDataSource1() {
        return this.dataSource1;
    }

    public void setDataSource1(DataSourceConfig dataSource1) {
        this.dataSource1 = dataSource1;
    }

    public DataSourceConfig getDataSource2() {
        return this.dataSource2;
    }

    public void setDataSource2(DataSourceConfig dataSource2) {
        this.dataSource2 = dataSource2;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name - ").append(this.name);
        sb.append(", Data Source 1 - ").append(this.dataSource1.getName());
        sb.append(", Data Source 2 - ").append(this.dataSource2.getName());
        if (!this.allFields.isEmpty()) {
            for (ReconFieldConfig rfc : this.allFields) {
                sb.append("\n").append(rfc);
            }
        }
        return sb.toString();
    }

    public List<ReconFieldConfig> getAllFields() {
        return this.allFields;
    }

    public void addField(ReconFieldConfig rfc) {
        this.allFields.add(rfc);
    }

    public void removeAllFields() {
        this.allFields.clear();
    }

    public void setAllField(String fields, String keyFields,
            String attrbuteFields, String excludedFields) {
        for (String field : fields.split(FIELDS_DELIMITER)) {
            this.allFields.add(new ReconFieldConfig(field.strip()));
        }
        for (String key : keyFields.split(FIELDS_DELIMITER)) {
            this.getField(key).setKey(true);
        }
        for (String attribute : attrbuteFields.split(FIELDS_DELIMITER)) {
            this.getField(attribute).setAttribute(true);
        }
        for (String exclude : excludedFields.split(FIELDS_DELIMITER)) {
            this.getField(exclude).setExcluded(true);
        }
    }

    private ReconFieldConfig getField(String name) {
        for (ReconFieldConfig rfc : this.allFields) {
            if (rfc.getName().equals(name)) {
                return rfc;
            }
        }
        return null;
    }

    public List<String> getKeyFields() {
        return this.allFields.stream()
                .filter(ReconFieldConfig::isKey)
                .map(ReconFieldConfig::getName)
                .toList();
    }

    public List<String> getFields() {
        return this.allFields.stream()
                .map(ReconFieldConfig::getName)
                .toList();
    }

    public List<String> getExcludedFields() {
        return this.allFields.stream()
                .filter(ReconFieldConfig::isExcluded)
                .map(ReconFieldConfig::getName)
                .toList();
    }
}
