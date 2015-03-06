package com.github.onsdigital.data.objects;

/**
 * Created by Tom.Ridd on 05/03/15.
 */
public class ManifestPoint {
    public static final int UPDATE = 0;
    public static final int CREATE = 1;
    public static final int DELETE = 2;
    public static final int ERROR = 3;

    public TimeSeriesPoint point = null;
    public TimeSeriesPoint master = null;


    public ManifestPoint(TimeSeriesPoint point, TimeSeriesPoint master) {
        this.point = point;
        this.master = master;
    }

    public int getUpdateType() {
        if((point == null) & (master == null)) {
            return ERROR;
        } else if(point == null) {
            return CREATE;
        } else if(master == null) {
            return DELETE;
        } else {
            return UPDATE;
        }
    }
}
