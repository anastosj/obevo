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
package com.gs.obevo.dbmetadata.impl;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.collections.api.block.function.Function;

/**
 * Info object to hold data for Sybase view and routine definitions.
 */
public record ExtraRerunnableInfo(String name, String specificName, String definition, String type, int order2, int order1) {
    public static final Function<ExtraRerunnableInfo, String> TO_NAME = ExtraRerunnableInfo::getName;
    public static final Function<ExtraRerunnableInfo, String> TO_SPECIFIC_NAME = ExtraRerunnableInfo::getSpecificName;
    public static final Function<ExtraRerunnableInfo, Integer> TO_ORDER2 = ExtraRerunnableInfo::getOrder2;
    public static final Function<ExtraRerunnableInfo, Integer> TO_ORDER1 = ExtraRerunnableInfo::getOrder1;

    public ExtraRerunnableInfo(String name, String specificName, String definition) {
        this(name, specificName, definition, null, 0, 0);
    }

    public String getName() {
        return name;
    }

    public String getSpecificName() {
        return specificName;
    }

    public String getDefinition() {
        return definition;
    }

    private int getOrder2() {
        return order2;
    }

    private int getOrder1() {
        return order1;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("name", name)
                .append("specificName", specificName)
                .append("definition", definition)
                .append("order2", order2)
                .append("order1", order1)
                .append("type", type)
                .toString();
    }
}
