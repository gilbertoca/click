/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.click.control;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import junit.framework.TestCase;

import org.apache.click.MockContext;
import org.apache.click.util.HtmlStringBuffer;

/**
 * Test Column behavior.
 */
public class ColumnTest extends TestCase {

    public void setUp() {
        MockContext.initContext();
    }

    /**
     * Sanity checks for Column.
     */
    public void testRenderTableData() {
        TestObject row = new TestObject("name", null);

        // Test rendering valid property
        Column column1 = new Column("name");

        HtmlStringBuffer buffer1 = new HtmlStringBuffer();
        column1.renderTableData(row, buffer1, null, 0);
        assertTrue(buffer1.length() > 0);

        // Test rendering a null property
        Column column2 = new Column("value");

        HtmlStringBuffer buffer2 = new HtmlStringBuffer();
        column2.renderTableData(row, buffer2, null, 0);
        assertTrue(buffer2.length() > 0);

        // Test rendering an invalid property
        try {
            Column column3 = new Column("missing");
            HtmlStringBuffer buffer3 = new HtmlStringBuffer();
            column3.renderTableData(row, buffer3, null, 0);
            assertTrue(false);

        } catch (RuntimeException expected) {
            assertTrue(true);
        }
    }

    /**
     * Check that Column renders when binding object is set.
     */
    public void testOuterJoin() {
        TestObject row = new TestObject("name", "label");

        // Test with child object
        row.setChild(new Child("mina"));

        Column column = new Column("child.name");

        HtmlStringBuffer buffer = new HtmlStringBuffer();
        column.renderTableData(row, buffer, null, 0);
        assertTrue(buffer.length() > 0);
    }

    /**
     * Check that Column renders when binding object is null.
     */
    public void testNullOuterJoin() {
        // Test with null child object
        TestObject row = new TestObject("name", "label");

        Column column = new Column("child.name");

        HtmlStringBuffer buffer = new HtmlStringBuffer();
        column.renderTableData(row, buffer, null, 0);
        assertTrue(buffer.length() > 0);
    }

    /**
     * Check that textfield value is escaped. This protects against cross-site
     * scripting attacks (XSS).
     */
    public void testEscapeValue() {
        String value = "<script>";
        TestObject row = new TestObject(value, null);

        // Test rendering valid property
        Column column = new Column("name");

        HtmlStringBuffer buffer = new HtmlStringBuffer();
        column.renderTableData(row, buffer, null, 0);

        String expected = "&lt;script&gt;";
        assertTrue(buffer.toString().indexOf(expected) > 1);

        // Check that the value <script> is not rendered
        assertTrue(buffer.toString().indexOf(value) < 0);
    }

    /**
     * CLK-59
     * Test that Column filter attributes can be configured properly.
     */
    public void testFilterAttributesConfiguration() {
        Column column = new Column("name", "Customer");

        // Assert defaults are safe
        assertNull(column.getFilterBy());
        assertEquals("", column.getFilterValue());

        // Set properties
        column.setFilterBy("customer.name");
        column.setFilterValue("MARCIO");

        // Validate state storage
        assertEquals("customer.name", column.getFilterBy());
        assertEquals("MARCIO", column.getFilterValue());
    }
    // ---------------------------------------------------------- Inner Classes
    public static class TestObject {

        private String name;

        private Object value;

        private Child child;

        public TestObject(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public Object getValue() {
            return value;
        }

        public Child getChild() {
            return child;
        }

        public void setChild(Child child) {
            this.child = child;
        }

        @Override
        public String toString() {
            return "TextObject [ " + getName() + ", " + getValue() + "]";
        }
    }

    /**
     * CLK-64: Column.setFormat must render JSR-310 (java.time) values.
     * MessageFormat only formats java.util.Date, so TemporalAccessor values
     * are rendered through DateTimeFormatter using the pattern inside the
     * "{0,date,PATTERN}" style.
     */
    public void testSetFormatWithTemporalAccessor() {
        SampleRow row = new SampleRow();

        // LocalDateTime — the case that used to throw
        // "Cannot format given Object as a Date"
        row.setDataHoraInicio(LocalDateTime.of(2026, 11, 13, 14, 0));
        Column inicio = new Column("dataHoraInicio", "Data Hora Início");
        inicio.setFormat("{0,date,dd/MM/yyyy HH:mm}");
        HtmlStringBuffer buffer = new HtmlStringBuffer();
        inicio.renderTableData(row, buffer, MockContext.initContext(), 0);
        assertTrue(buffer.toString().contains("13/11/2026 14:00"));

        // LocalDate
        row.setDataNascimento(LocalDate.of(1990, 5, 10));
        Column nascimento = new Column("dataNascimento", "Nascimento");
        nascimento.setFormat("{0,date,dd/MM/yyyy}");
        buffer = new HtmlStringBuffer();
        nascimento.renderTableData(row, buffer, MockContext.initContext(), 0);
        assertTrue(buffer.toString().contains("10/05/1990"));

        // LocalTime
        row.setHoraInicio(LocalTime.of(9, 30));
        Column hora = new Column("horaInicio", "Hora");
        hora.setFormat("{0,date,HH:mm}");
        buffer = new HtmlStringBuffer();
        hora.renderTableData(row, buffer, MockContext.initContext(), 0);
        assertTrue(buffer.toString().contains("09:30"));
    }

    /**
     * CLK-64 regression: legacy java.util.Date values keep rendering through
     * MessageFormat untouched.
     */
    public void testSetFormatWithLegacyDate() {
        SampleRow row = new SampleRow();
        row.setDataLegada(java.sql.Timestamp.valueOf("2026-11-13 14:00:00"));

        Column column = new Column("dataLegada", "Data");
        column.setFormat("{0,date,dd/MM/yyyy HH:mm}");

        HtmlStringBuffer buffer = new HtmlStringBuffer();
        column.renderTableData(row, buffer, MockContext.initContext(), 0);
        assertTrue(buffer.toString().contains("13/11/2026 14:00"));
    }

    /** CLK-64: row bean exposing java.time and legacy Date properties. */
    public static class SampleRow {

        private LocalDateTime dataHoraInicio;
        private LocalDate dataNascimento;
        private LocalTime horaInicio;
        private java.util.Date dataLegada;

        public LocalDateTime getDataHoraInicio() {
            return dataHoraInicio;
        }

        public void setDataHoraInicio(LocalDateTime dataHoraInicio) {
            this.dataHoraInicio = dataHoraInicio;
        }

        public LocalDate getDataNascimento() {
            return dataNascimento;
        }

        public void setDataNascimento(LocalDate dataNascimento) {
            this.dataNascimento = dataNascimento;
        }

        public LocalTime getHoraInicio() {
            return horaInicio;
        }

        public void setHoraInicio(LocalTime horaInicio) {
            this.horaInicio = horaInicio;
        }

        public java.util.Date getDataLegada() {
            return dataLegada;
        }

        public void setDataLegada(java.util.Date dataLegada) {
            this.dataLegada = dataLegada;
        }
    }

    public static class Child {

        private String name;

        public Child() {
        }

        public Child(String name) {
            setName(name);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Child [ " + getName() + "]";
        }
    }


}
