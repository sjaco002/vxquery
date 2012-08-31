package org.apache.vxquery.runtime.functions.datetime;

import java.io.DataOutput;
import java.io.IOException;

import org.apache.vxquery.datamodel.accessors.TaggedValuePointable;
import org.apache.vxquery.datamodel.accessors.atomic.XSDatePointable;
import org.apache.vxquery.datamodel.accessors.atomic.XSTimePointable;
import org.apache.vxquery.datamodel.util.DateTime;
import org.apache.vxquery.datamodel.values.ValueTag;
import org.apache.vxquery.exceptions.ErrorCode;
import org.apache.vxquery.exceptions.SystemException;
import org.apache.vxquery.runtime.functions.base.AbstractTaggedValueArgumentScalarEvaluator;
import org.apache.vxquery.runtime.functions.base.AbstractTaggedValueArgumentScalarEvaluatorFactory;

import edu.uci.ics.hyracks.algebricks.common.exceptions.AlgebricksException;
import edu.uci.ics.hyracks.algebricks.runtime.base.IScalarEvaluator;
import edu.uci.ics.hyracks.algebricks.runtime.base.IScalarEvaluatorFactory;
import edu.uci.ics.hyracks.api.context.IHyracksTaskContext;
import edu.uci.ics.hyracks.data.std.api.IPointable;
import edu.uci.ics.hyracks.data.std.util.ArrayBackedValueStorage;

public class FnDateTimeScalarEvaluatorFactory extends AbstractTaggedValueArgumentScalarEvaluatorFactory {
    private static final long serialVersionUID = 1L;

    public FnDateTimeScalarEvaluatorFactory(IScalarEvaluatorFactory[] args) {
        super(args);
    }

    @Override
    protected IScalarEvaluator createEvaluator(IHyracksTaskContext ctx, IScalarEvaluator[] args)
            throws AlgebricksException {
        final XSDatePointable datep = (XSDatePointable) XSDatePointable.FACTORY.createPointable();
        final XSTimePointable timep = (XSTimePointable) XSTimePointable.FACTORY.createPointable();
        final ArrayBackedValueStorage abvs = new ArrayBackedValueStorage();
        final DataOutput dOut = abvs.getDataOutput();

        return new AbstractTaggedValueArgumentScalarEvaluator(args) {
            @Override
            protected void evaluate(TaggedValuePointable[] args, IPointable result) throws SystemException {
                TaggedValuePointable tvp1 = args[0];
                if (tvp1.getTag() != ValueTag.XS_DATE_TAG) {
                    throw new SystemException(ErrorCode.FORG0006);
                }
                tvp1.getValue(datep);

                TaggedValuePointable tvp2 = args[1];
                if (tvp2.getTag() != ValueTag.XS_TIME_TAG) {
                    throw new SystemException(ErrorCode.FORG0006);
                }
                tvp2.getValue(timep);

                // Set the timezone.
                byte timezoneHour, timezoneMinute;
                if (datep.getTimezoneHour() == DateTime.TIMEZONE_HOUR_NULL
                        && datep.getTimezoneMinute() == DateTime.TIMEZONE_MINUTE_NULL
                        && timep.getTimezoneHour() == DateTime.TIMEZONE_HOUR_NULL
                        && timep.getTimezoneMinute() == DateTime.TIMEZONE_MINUTE_NULL) {
                    // both null.
                    timezoneHour = DateTime.TIMEZONE_HOUR_NULL;
                    timezoneMinute = DateTime.TIMEZONE_MINUTE_NULL;
                } else if (datep.getTimezoneHour() == DateTime.TIMEZONE_HOUR_NULL
                        && datep.getTimezoneMinute() == DateTime.TIMEZONE_MINUTE_NULL
                        && timep.getTimezoneHour() != DateTime.TIMEZONE_HOUR_NULL
                        && timep.getTimezoneMinute() != DateTime.TIMEZONE_MINUTE_NULL) {
                    // date is null.
                    timezoneHour = (byte) timep.getTimezoneHour();
                    timezoneMinute = (byte) timep.getTimezoneMinute();
                } else if (datep.getTimezoneHour() != DateTime.TIMEZONE_HOUR_NULL
                        && datep.getTimezoneMinute() != DateTime.TIMEZONE_MINUTE_NULL
                        && timep.getTimezoneHour() == DateTime.TIMEZONE_HOUR_NULL
                        && timep.getTimezoneMinute() == DateTime.TIMEZONE_MINUTE_NULL) {
                    // time is null.
                    timezoneHour = (byte) datep.getTimezoneHour();
                    timezoneMinute = (byte) datep.getTimezoneMinute();
                } else if (datep.getTimezoneHour() == timep.getTimezoneHour()
                        && datep.getTimezoneMinute() == timep.getTimezoneMinute()) {
                    // timezones are the same.
                    timezoneHour = (byte) datep.getTimezoneHour();
                    timezoneMinute = (byte) datep.getTimezoneMinute();
                } else {
                    // Neither match.
                    throw new SystemException(ErrorCode.FORG0008);
                }

                try {
                    abvs.reset();
                    dOut.write(ValueTag.XS_DATETIME_TAG);
                    dOut.write(datep.getByteArray(), datep.getStartOffset(),
                            XSDatePointable.TYPE_TRAITS.getFixedLength() - 2);
                    dOut.write(timep.getByteArray(), timep.getStartOffset(),
                            XSTimePointable.TYPE_TRAITS.getFixedLength() - 2);
                    dOut.write(timezoneHour);
                    dOut.write(timezoneMinute);

                    result.set(abvs);
                } catch (IOException e) {
                    throw new SystemException(ErrorCode.SYSE0001, e);
                }
            }
        };
    }
}