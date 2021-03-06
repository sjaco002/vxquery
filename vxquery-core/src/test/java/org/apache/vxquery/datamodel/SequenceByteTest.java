/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.vxquery.datamodel;

import java.io.IOException;

import org.apache.hyracks.data.std.util.ArrayBackedValueStorage;
import org.apache.vxquery.datamodel.accessors.SequencePointable;
import org.apache.vxquery.datamodel.accessors.TaggedValuePointable;
import org.apache.vxquery.datamodel.builders.sequence.SequenceBuilder;
import org.apache.vxquery.datamodel.values.ValueTag;
import org.apache.vxquery.datamodel.values.XDMConstants;
import org.apache.vxquery.runtime.functions.util.FunctionHelper;
import org.junit.Assert;
import org.junit.Test;

/**
 * The sequence byte test covers empty sequences, single items and multi-item sequences.
 * 1. Empty sequence constant
 * 2. Empty sequence - {}
 * 3. Single item - 1l (XQuery single item sequences are just the item.)
 * 4. Many items - {1, 2.0, "three"}
 */
public class SequenceByteTest extends AbstractPointableTest {
    private final ArrayBackedValueStorage abvsResult = new ArrayBackedValueStorage();
    private final SequenceBuilder sb = new SequenceBuilder();
    private final TaggedValuePointable tvp = (TaggedValuePointable) TaggedValuePointable.FACTORY.createPointable();
    private final TaggedValuePointable tvp1 = (TaggedValuePointable) TaggedValuePointable.FACTORY.createPointable();
    private final TaggedValuePointable tvp2 = (TaggedValuePointable) TaggedValuePointable.FACTORY.createPointable();
    private final TaggedValuePointable tvp3 = (TaggedValuePointable) TaggedValuePointable.FACTORY.createPointable();
    private final SequencePointable sp = (SequencePointable) SequencePointable.FACTORY.createPointable();

    @Test
    public void testEmptySequenceConstant() {
        // Build test sequence
        XDMConstants.setEmptySequence(tvp);

        // Check results.
        if (tvp.getTag() != ValueTag.SEQUENCE_TAG) {
            Assert.fail("Type tag is incorrect. Expected: " + ValueTag.SEQUENCE_TAG + " Got: " + tvp.getTag());
        }
        tvp.getValue(sp);
        if (sp.getEntryCount() != 0) {
            Assert.fail("Sequence size is incorrect. Expected: 0 Got: " + sp.getEntryCount());
        }
    }

    @Test
    public void testEmptySequence() {
        // Build test sequence
        try {
            sb.reset(abvsResult);
            sb.finish();
        } catch (IOException e) {
            Assert.fail("Test failed to write the sequence pointable.");
        }
        tvp.set(abvsResult);

        // Check results.
        if (tvp.getTag() != ValueTag.SEQUENCE_TAG) {
            Assert.fail("Type tag is incorrect. Expected: " + ValueTag.SEQUENCE_TAG + " Got: " + tvp.getTag());
        }
        tvp.getValue(sp);
        if (sp.getEntryCount() != 0) {
            Assert.fail("Sequence size is incorrect. Expected: 0 Got: " + sp.getEntryCount());
        }
    }

    @Test
    public void testSingleItemSequence() {
        // Build test sequence
        try {
            sb.reset(abvsResult);
            getTaggedValuePointable(1l, tvp1);
            sb.addItem(tvp1);
            sb.finish();
        } catch (IOException e) {
            Assert.fail("Test failed to write the sequence pointable.");
        }
        tvp.set(abvsResult);

        // Check results.
        if (tvp.getTag() != ValueTag.XS_LONG_TAG) {
            Assert.fail("Type tag is incorrect. Expected: " + ValueTag.XS_LONG_TAG + " Got: " + tvp.getTag());
        }
        if (!FunctionHelper.arraysEqual(tvp, tvp1)) {
            Assert.fail("Item value is incorrect.");
        }
    }

    @Test
    public void testManyItemSequence() {
        // Build test sequence
        try {
            // Add three items
            sb.reset(abvsResult);
            getTaggedValuePointable(1, tvp1);
            sb.addItem(tvp1);
            getTaggedValuePointable(2.0, tvp2);
            sb.addItem(tvp2);
            getTaggedValuePointable("three", tvp3);
            sb.addItem(tvp3);
            sb.finish();
        } catch (IOException e) {
            Assert.fail("Test failed to write the sequence pointable.");
        }
        tvp.set(abvsResult);

        // Check results.
        if (tvp.getTag() != ValueTag.SEQUENCE_TAG) {
            Assert.fail("Sequence tag is incorrect. Expected: " + ValueTag.SEQUENCE_TAG + " Got: " + tvp.getTag());
        }
        tvp.getValue(sp);
        if (sp.getEntryCount() != 3) {
            Assert.fail("Sequence size is incorrect. Expected: 3 Got: " + sp.getEntryCount());
        }
        sp.getEntry(0, tvp);
        if (tvp.getTag() != ValueTag.XS_INT_TAG) {
            Assert.fail("Sequence item one type tag is incorrect. Expected: " + ValueTag.XS_INT_TAG + " Got: "
                    + tvp.getTag());
        }
        if (!FunctionHelper.arraysEqual(tvp, tvp1)) {
            Assert.fail("Sequence item one is incorrect. Expected: " + ValueTag.XS_INT_TAG + " Got: " + tvp.getTag());
        }
        sp.getEntry(1, tvp);
        if (tvp.getTag() != ValueTag.XS_DOUBLE_TAG) {
            Assert.fail("Sequence item two type tag is incorrect. Expected: " + ValueTag.XS_DOUBLE_TAG + " Got: "
                    + tvp.getTag());
        }
        if (!FunctionHelper.arraysEqual(tvp, tvp2)) {
            Assert.fail(
                    "Sequence item two is incorrect. Expected: " + ValueTag.XS_DOUBLE_TAG + " Got: " + tvp.getTag());
        }
        sp.getEntry(2, tvp);
        if (tvp.getTag() != ValueTag.XS_STRING_TAG) {
            Assert.fail("Sequence item three type tag is incorrect. Expected: " + ValueTag.XS_STRING_TAG + " Got: "
                    + tvp.getTag());
        }
        if (!FunctionHelper.arraysEqual(tvp, tvp3)) {
            Assert.fail(
                    "Sequence item three is incorrect. Expected: " + ValueTag.XS_STRING_TAG + " Got: " + tvp.getTag());
        }
    }

}
