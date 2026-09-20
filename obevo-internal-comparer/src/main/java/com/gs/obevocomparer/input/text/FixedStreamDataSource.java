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
package com.gs.obevocomparer.input.text;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FixedStreamDataSource extends AbstractStreamDataSource {

    private final List<FixedField> fixedFields = new ArrayList<>();

    public FixedStreamDataSource(String name, Reader reader, Object... fieldInput) {
        super(name, reader);
        this.hasHeader = false;
        this.parseFields(fieldInput);
    }

    public void setHeader(boolean hasHeader) {
        throw new UnsupportedOperationException("Cannot set header for fixed width file");
    }

    private void parseFields(Object[] fieldInput) {
        if (fieldInput.length == 0) {
            throw new IllegalArgumentException("Input field list must not be empty");
        }

        if (fieldInput.length % 3 != 0) {
            throw new IllegalArgumentException("Input field list must be of form '[<field>, <start index>, <end index>]*'");
        }

        for (int i = 0; i < fieldInput.length; i += 3) {
            if (!(fieldInput[i] instanceof String) ||
                    !(fieldInput[i + 1] instanceof Integer) ||
                    !(fieldInput[i + 2] instanceof Integer)) {
                throw new IllegalArgumentException("Input field list must be of form '[<field>, <start index>, <end index>]*'");
            }

            this.fixedFields.add(new FixedField((String) fieldInput[i],
                    (Integer) fieldInput[i + 1], (Integer) fieldInput[i + 2]));
        }

        Collections.sort(this.fixedFields);
        this.fields.clear();
        for (FixedField field : this.fixedFields) {
            this.fields.add(field.field());
        }
    }

    protected String[] parseData(String line) {
        String[] data = new String[this.fixedFields.size()];

        for (int i = 0; i < this.fixedFields.size(); i++) {
            if (line.length() <= this.fixedFields.get(i).end()) {
                data[i] = line.substring(this.fixedFields.get(i).start());
            } else {
                data[i] = line.substring(this.fixedFields.get(i).start(), this.fixedFields.get(i).end());
            }
        }

        return data;
    }

    private record FixedField(String field, int start, int end) implements Comparable<FixedField> {
        @Override
        public int compareTo(FixedField o) {
            int val = Integer.compare(this.start, o.start);
            return val == 0 ? Integer.compare(this.end, o.end) : val;
        }
    }
}
