package com.github.onsdigital.brian.readers.csdb;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class DateLabel {

    static final String YEAR = "Y";
    static final String ANNUAL = "A";
    static final String MONTH = "M";

    static final String MQA_MONTH_FORMAT = "%d %02d";
    static final String MQA_OTHER_FORMAT = "%d Q%d";

    private int startInd;
    private int year;
    private String mqa;
    private int iteration;

    public DateLabel(int year, int startInd, String mqa) {
        this.iteration = 1;
        this.startInd = startInd;
        this.year = year;
        this.mqa = mqa;
    }

    public int getStartInd() {
        return startInd;
    }

    public int getYear() {
        return year;
    }

    public String getMqa() {
        return mqa;
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
        if (YEAR.equals(mqa) || ANNUAL.equals(mqa)) {
            return String.valueOf(year + (iteration - 1));
        }

        if (MONTH.equals(mqa)) {
            int finalMonth = (startInd + iteration - 2) % 12;
            int yearsTaken = (startInd + iteration - 2) / 12;
            return String.format(MQA_MONTH_FORMAT, (year + yearsTaken), (finalMonth + 1));
        }

        int finalQuarter = (startInd + iteration - 2) % 4;
        int yearsTaken = (startInd + iteration - 2) / 4;
        return String.format(MQA_OTHER_FORMAT, (year + yearsTaken), (finalQuarter + 1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DateLabel dateLabel = (DateLabel) o;

        return new EqualsBuilder()
                .append(getStartInd(), dateLabel.getStartInd())
                .append(getYear(), dateLabel.getYear())
                .append(getIteration(), dateLabel.getIteration())
                .append(getMqa(), dateLabel.getMqa())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getStartInd())
                .append(getYear())
                .append(getMqa())
                .append(getIteration())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("startInd", startInd)
                .append("year", year)
                .append("mqa", mqa)
                .append("iteration", iteration)
                .toString();
    }
}
