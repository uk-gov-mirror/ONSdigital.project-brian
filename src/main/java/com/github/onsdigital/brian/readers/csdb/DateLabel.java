package com.github.onsdigital.brian.readers.csdb;

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
}
