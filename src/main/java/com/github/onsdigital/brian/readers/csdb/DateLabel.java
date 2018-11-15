package com.github.onsdigital.brian.readers.csdb;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class DateLabel {


    static final String MQA_MONTH_FORMAT = "%d %02d";
    static final String MQA_OTHER_FORMAT = "%d Q%d";

    private int startInd;
    private int year;
    private Period period;
    private int iteration;


    public DateLabel(Period period, int year, int startInd) {
        this.period = period;
        this.year = year;
        this.startInd = startInd;
        this.iteration = 1;
    }

    public int getStartInd() {
        return startInd;
    }

    public int getYear() {
        return year;
    }

    public int getIteration() {
        return iteration;
    }

    public String getNextIteration() {
        String result = getDateStr();
        iteration++;
        return result;
    }

    private String getDateStr() {
        switch (period) {
            case YEAR:
                // fall through to annual
            case ANNUAL:
                return getYearLabel();
            case MONTH:
                return getMonthLabel();
            default:
                // default to QUARTER
                return getQuaterLabel();
        }
    }

    private String getYearLabel() {
        return String.valueOf(year + (iteration - 1));
    }

    private String getMonthLabel() {
        int finalMonth = (startInd + iteration - 2) % 12;
        int yearsTaken = (startInd + iteration - 2) / 12;
        return String.format(MQA_MONTH_FORMAT, (year + yearsTaken), (finalMonth + 1));
    }

    private String getQuaterLabel() {
        int finalQuarter = (startInd + iteration - 2) % 4;
        int yearsTaken = (startInd + iteration - 2) / 4;
        return String.format(MQA_OTHER_FORMAT, (year + yearsTaken), (finalQuarter + 1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DateLabel dateLabel = (DateLabel) o;

        return new EqualsBuilder()
                .append(getStartInd(), dateLabel.getStartInd())
                .append(getYear(), dateLabel.getYear())
                .append(getIteration(), dateLabel.getIteration())
                .append(period, dateLabel.period)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getStartInd())
                .append(getYear())
                .append(period)
                .append(getIteration())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("startInd", startInd)
                .append("year", year)
                .append("period", period)
                .append("iteration", iteration)
                .toString();
    }
}
