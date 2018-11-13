package com.github.onsdigital.brian.readers;

public class DateLabel {

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
        StringBuilder sb = new StringBuilder();

        if (mqa.equals("Y") || mqa.equals("A")) {
            return sb.append(year).append(iteration - 1).toString();
        } else if (mqa.equals("M")) {
            int finalMonth = (startInd + iteration - 2) % 12;
            int yearsTaken = (startInd + iteration - 2) / 12;

            return sb.append(year + yearsTaken)
                    .append(" ")
                    .append(String.format("%02d", finalMonth + 1))
                    .toString();
        } else {
            int finalQuarter = (startInd + iteration - 2) % 4;
            int yearsTaken = (startInd + iteration - 2) / 4;
            return String.format("%d Q%d", year + yearsTaken, finalQuarter + 1);
        }
    }
}
