package com.inledco.fluvalsmart.util;

import android.graphics.Color;
import android.support.annotation.NonNull;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DecimalFormat;

public class LineChartHelper {
    public static void init(@NonNull LineChart chart) {
        XAxis xAxis = chart.getXAxis();
        YAxis axisLeft = chart.getAxisLeft();
        YAxis axisRight = chart.getAxisRight();
        xAxis.setAxisMaximum(24 * 60);
        xAxis.setAxisMinimum(0);
        xAxis.setLabelCount(5, true);
        xAxis.setGranularity(1);
        xAxis.setGranularityEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setEnabled(true);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                DecimalFormat df = new DecimalFormat("00");
                return df.format((((int) value)%1440)/60) + ":" + df.format(((int) value)%60);
            }
        });

        IAxisValueFormatter formatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                DecimalFormat df = new DecimalFormat("##0");
                return df.format(value);
            }
        };
        axisLeft.setAxisMaximum(100);
        axisLeft.setAxisMinimum(0);
        axisLeft.setLabelCount(5, true);
        axisLeft.setValueFormatter(formatter);
        axisLeft.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        axisLeft.setTextColor(Color.WHITE);
        axisLeft.setTextSize(10);
        axisLeft.setDrawGridLines(true);
        axisLeft.setGridColor(0xFF9E9E9E);
        axisLeft.setGridLineWidth(0.75f);
        axisLeft.setDrawAxisLine(false);
        axisLeft.setAxisLineColor(Color.WHITE);
        axisLeft.setGranularity(1);
        axisLeft.setGranularityEnabled(true);
        axisLeft.setSpaceTop(0);
        axisLeft.setSpaceBottom(0);
        axisLeft.setEnabled(true);

        axisRight.setAxisMaximum(100);
        axisRight.setAxisMinimum(0);
        axisRight.setLabelCount(5, true);
        axisRight.setValueFormatter(formatter);
        axisRight.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        axisRight.setTextColor(Color.WHITE);
        axisRight.setTextSize(10);
        axisRight.setDrawGridLines(true);
        axisRight.setGridColor(0xFF9E9E9E);
        axisRight.setGridLineWidth(0.75f);
        axisRight.setDrawAxisLine(false);
        axisRight.setAxisLineColor(Color.BLACK);
        axisRight.setGranularity(1);
        axisRight.setGranularityEnabled(true);
        axisRight.setSpaceTop(0);
        axisRight.setSpaceBottom(0);
        axisRight.setEnabled(true);

        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setBorderColor(Color.CYAN);
        chart.setBorderWidth(1);
        chart.setDrawBorders(false);
        chart.setDrawGridBackground(true);
        chart.setGridBackgroundColor(Color.TRANSPARENT);
        chart.setDescription(null);
        chart.setMaxVisibleValueCount(0);
        chart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        chart.getLegend().setTextSize(12);
        chart.getLegend().setFormSize(12);
        chart.getLegend().setTextColor(Color.WHITE);
    }
}
