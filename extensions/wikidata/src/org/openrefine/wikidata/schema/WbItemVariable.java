/*******************************************************************************
 * MIT License
 *
 * Copyright (c) 2018 Antonin Delpeuch
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package org.openrefine.wikidata.schema;

import org.openrefine.wikidata.qa.QAWarning;
import org.openrefine.wikidata.schema.entityvalues.ReconItemIdValue;
import org.openrefine.wikidata.schema.exceptions.SkipSchemaExpressionException;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.refine.model.Cell;
import com.google.refine.model.Recon.Judgment;

/**
 * An item that depends on a reconciled value in a column.
 *
 * @author Antonin Delpeuch
 *
 */
public class WbItemVariable extends WbVariableExpr<ItemIdValue> {

    @JsonCreator
    public WbItemVariable() {

    }

    /**
     * Constructs a variable and sets the column it is bound to. Mostly used as a
     * convenience method for testing.
     *
     * @param columnName
     *            the name of the column the expression should draw its value from
     */
    public WbItemVariable(String columnName) {
        setColumnName(columnName);
    }

    @Override
    public ItemIdValue fromCell(Cell cell, ExpressionContext ctxt)
            throws SkipSchemaExpressionException {
        if (cell.recon != null
                && (Judgment.Matched.equals(cell.recon.judgment) || Judgment.New.equals(cell.recon.judgment))) {
            if (
                cell.recon.identifierSpace != null &&
                !cell.recon.identifierSpace.equals(Datamodel.SITE_WIKIDATA) &&
                !cell.recon.identifierSpace.equals("http://www.wikidata.org/entity/")
            ) {
                QAWarning warning = new QAWarning("invalid-identifier-space", null, QAWarning.Severity.INFO, 1);
                warning.setProperty("example_cell", cell.value.toString());
                ctxt.addWarning(warning);
                throw new SkipSchemaExpressionException();
            }
            return new ReconItemIdValue(cell.recon, cell.value.toString());
        }
        throw new SkipSchemaExpressionException();
    }

    @Override
    public boolean equals(Object other) {
        return equalAsVariables(other, WbItemVariable.class);
    }
}
