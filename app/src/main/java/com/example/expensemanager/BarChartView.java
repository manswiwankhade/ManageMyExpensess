package com.example.expensemanager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BarChartView extends View {

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private List<String> labels = new ArrayList<>();
    private List<Double> values = new ArrayList<>();

    public BarChartView(Context context) {
        super(context);
        init();
    }

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BarChartView(
            Context context,
            AttributeSet attrs,
            int defStyleAttr) {

        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        // Bar
        barPaint.setColor(0xFF8B78C6);
        barPaint.setStyle(Paint.Style.FILL);

        // Text
        textPaint.setColor(0xFF514B70);
        textPaint.setTextSize(32f);
        textPaint.setTypeface(
                android.graphics.Typeface.DEFAULT_BOLD
        );

        // Grid
        gridPaint.setColor(0xFFE6E0EA);
        gridPaint.setStrokeWidth(2f);
        gridPaint.setStyle(Paint.Style.STROKE);

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    // =========================================================
    // SET DATA
    // =========================================================

    public void setData(
            List<String> labels,
            List<Double> values) {

        this.labels.clear();
        this.values.clear();

        if (labels != null) {
            this.labels.addAll(labels);
        }

        if (values != null) {
            this.values.addAll(values);
        }

        invalidate();
        requestLayout();
    }

    // =========================================================
    // MEASURE
    // =========================================================

    @Override
    protected void onMeasure(
            int widthMeasureSpec,
            int heightMeasureSpec) {

        int width =
                MeasureSpec.getSize(widthMeasureSpec);

        int desiredHeight =
                dpToPx(330);

        int height =
                resolveSize(
                        desiredHeight,
                        heightMeasureSpec
                );

        setMeasuredDimension(
                width,
                height
        );
    }

    // =========================================================
    // DRAW
    // =========================================================

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        if (labels.isEmpty() || values.isEmpty()) {

            drawEmptyMessage(canvas);
            return;
        }

        int width = getWidth();
        int height = getHeight();

        // -----------------------------------------------------
        // CHART AREA
        // -----------------------------------------------------

        float left =
                dpToPx(55);

        float right =
                width - dpToPx(15);

        float top =
                dpToPx(25);

        float bottom =
                height - dpToPx(55);

        float chartWidth =
                right - left;

        float chartHeight =
                bottom - top;

        // -----------------------------------------------------
        // FIND MAXIMUM
        // -----------------------------------------------------

        double maxValue = 0;

        for (Double value : values) {

            if (value != null &&
                    value > maxValue) {

                maxValue = value;
            }
        }

        // Prevent zero-height chart
        if (maxValue <= 0) {
            maxValue = 100;
        }

        // Add some space above highest bar
        double scaleMax =
                calculateScaleMax(maxValue);

        // -----------------------------------------------------
        // GRID LINES
        // -----------------------------------------------------

        int gridCount = 4;

        textPaint.setTextSize(
                dpToPx(10)
        );

        textPaint.setTypeface(
                android.graphics.Typeface.DEFAULT
        );

        for (int i = 0;
             i <= gridCount;
             i++) {

            float ratio =
                    (float) i / gridCount;

            float y =
                    bottom - (chartHeight * ratio);

            canvas.drawLine(
                    left,
                    y,
                    right,
                    y,
                    gridPaint
            );

            double gridValue =
                    scaleMax * ratio;

            String gridText =
                    formatShortAmount(gridValue);

            canvas.drawText(
                    gridText,
                    dpToPx(5),
                    y + dpToPx(4),
                    textPaint
            );
        }

        // -----------------------------------------------------
        // BARS
        // -----------------------------------------------------

        int count =
                Math.min(
                        labels.size(),
                        values.size()
                );

        if (count == 0) {
            return;
        }

        float slotWidth =
                chartWidth / count;

        float barWidth =
                Math.min(
                        dpToPx(42),
                        slotWidth * 0.55f
                );

        for (int i = 0;
             i < count;
             i++) {

            double value =
                    values.get(i) == null
                            ? 0
                            : values.get(i);

            float barHeight =
                    (float)
                            ((value / scaleMax)
                                    * chartHeight);

            float centerX =
                    left +
                            (slotWidth * i) +
                            (slotWidth / 2);

            float barLeft =
                    centerX -
                            (barWidth / 2);

            float barRight =
                    centerX +
                            (barWidth / 2);

            float barTop =
                    bottom - barHeight;

            // -------------------------------------------------
            // BAR
            // -------------------------------------------------

            if (value > 0) {

                RectF rect =
                        new RectF(
                                barLeft,
                                barTop,
                                barRight,
                                bottom
                        );

                canvas.drawRoundRect(
                        rect,
                        dpToPx(7),
                        dpToPx(7),
                        barPaint
                );
            }

            // -------------------------------------------------
            // AMOUNT ABOVE BAR
            // -------------------------------------------------

            textPaint.setTextSize(
                    dpToPx(10)
            );

            textPaint.setTypeface(
                    android.graphics.Typeface.DEFAULT_BOLD
            );

            String amountText =
                    formatBarAmount(value);

            float amountWidth =
                    textPaint.measureText(
                            amountText
                    );

            float amountY;

            if (value > 0) {

                amountY =
                        Math.max(
                                top + dpToPx(15),
                                barTop - dpToPx(8)
                        );

            } else {

                amountY =
                        bottom - dpToPx(8);
            }

            canvas.drawText(
                    amountText,
                    centerX - (amountWidth / 2),
                    amountY,
                    textPaint
            );

            // -------------------------------------------------
            // MONTH LABEL
            // -------------------------------------------------

            textPaint.setTextSize(
                    dpToPx(11)
            );

            textPaint.setTypeface(
                    android.graphics.Typeface.DEFAULT_BOLD
            );

            String label =
                    labels.get(i);

            float labelWidth =
                    textPaint.measureText(label);

            canvas.drawText(
                    label,
                    centerX - (labelWidth / 2),
                    height - dpToPx(20),
                    textPaint
            );
        }
    }

    // =========================================================
    // SCALE
    // =========================================================

    private double calculateScaleMax(
            double maxValue) {

        if (maxValue <= 100) {
            return 100;
        }

        if (maxValue <= 500) {
            return 500;
        }

        if (maxValue <= 1000) {
            return 1000;
        }

        if (maxValue <= 2000) {
            return 2000;
        }

        if (maxValue <= 5000) {
            return 5000;
        }

        if (maxValue <= 10000) {
            return 10000;
        }

        // Round up to a clean value
        double magnitude =
                Math.pow(
                        10,
                        Math.floor(
                                Math.log10(maxValue)
                        )
                );

        return Math.ceil(
                maxValue / magnitude
        ) * magnitude;
    }

    // =========================================================
    // EMPTY STATE
    // =========================================================

    private void drawEmptyMessage(
            Canvas canvas) {

        textPaint.setTextSize(
                dpToPx(14)
        );

        textPaint.setTypeface(
                android.graphics.Typeface.DEFAULT
        );

        String message =
                "No spending data available";

        float width =
                textPaint.measureText(message);

        canvas.drawText(
                message,
                (getWidth() - width) / 2,
                getHeight() / 2f,
                textPaint
        );
    }

    // =========================================================
    // FORMAT
    // =========================================================

    private String formatBarAmount(
            double amount) {

        if (amount >= 100000) {

            return String.format(
                    Locale.getDefault(),
                    "₹%.1fL",
                    amount / 100000
            );
        }

        if (amount >= 1000) {

            return String.format(
                    Locale.getDefault(),
                    "₹%.1fk",
                    amount / 1000
            );
        }

        return String.format(
                Locale.getDefault(),
                "₹%.0f",
                amount
        );
    }

    private String formatShortAmount(
            double amount) {

        if (amount >= 1000) {

            return String.format(
                    Locale.getDefault(),
                    "₹%.1fk",
                    amount / 1000
            );
        }

        return String.format(
                Locale.getDefault(),
                "₹%.0f",
                amount
        );
    }

    // =========================================================
    // DP
    // =========================================================

    private int dpToPx(float dp) {

        return (int) (
                dp *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }
}