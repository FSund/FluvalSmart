package com.inledco.fluvalsmart.bean;

import java.util.Comparator;

public class PointComparator implements Comparator<TimerBrightPoint>
{

    @Override
    public int compare( TimerBrightPoint o1, TimerBrightPoint o2 )
    {
        int t1 = o1.getHour()*60+o1.getMinute();
        int t2 = o2.getHour()*60+o2.getMinute();
        if ( t1 < t2 )
        {
            return -1;
        }
        else if ( t1 > t2 )
        {
            return 1;
        }
        return 0;
    }
}
