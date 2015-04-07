package com.github.onsdigital.data.objects;

import com.github.onsdigital.data.DataCube;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by thomasridd on 31/03/15.
 */
public class DataCubeSet {
    //public List<Future<DataCube>> cubes = new ArrayList<Future<DataCube>>();
    public HashMap<String,Future<DataCube>> cubes = new HashMap<>();
}
