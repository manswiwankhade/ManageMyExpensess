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

public class PieChartView extends View {

    private final Paint piePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private List<String> categories = new ArrayList<>();
    private List<Double> amounts = new ArrayList<>();

    // Fixed colors for categories.
    // The SAME category always gets the SAME color.
    private final int[] chartColors = {
            0xFF4FA3A5, // Teal
            0xFFE3A04B, // Orange
            0xFF7B61A8, // Purple
            0xFFE07A8A, // Rose
            0xFF6C9A6B, // Green
            0xFF5B8DB8, // Blue
            0xFFD19A66, // Brown
            0xFF9B7EBD, // Lavender
            0xFFD4B24C, // Gold
            0xFF7A8B99  // Slate
    };

    public PieChartView(Context context) {
        super(context);
        init();
    }

    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PieChartView(
            Context context,
            AttributeSet attrs,
            int defStyleAttr) {

        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        piePaint.setStyle(Paint.Style.FILL);

        textPaint.setTypeface(
                android.graphics.Typeface.DEFAULT_BOLD
        );

        centerPaint.setColor(0xFFFFF8EF);
        centerPaint.setStyle(Paint.Style.FILL);

        setLayerType(
                View.LAYER_TYPE_SOFTWARE,
                null
        );
    }

    // ---------------------------------------------------------
    // SET DATA
    // ---------------------------------------------------------

    public void setData(
            List<String> categories,
            List<Double> amounts) {

        this.categories.clear();
        this.amounts.clear();

        if (categories != null) {
            this.categories.addAll(categories);
        }

        if (amounts != null) {
            this.amounts.addAll(amounts);
        }

        invalidate();
        requestLayout();
    }

    // ---------------------------------------------------------
    // GET CATEGORY COLOR
    // ---------------------------------------------------------

    public int getCategoryColor(String category) {

        if (category == null) {
            return chartColors[0];
        }

        String name = category.trim().toLowerCase(
                Locale.getDefault()
        );

        /*
         * Fixed colors for common categories.
         * This prevents colors from changing when
         * another category is added.
         */

        switch (name) {

            case "food":
            case "food & dining":
            case "food or dining":
            case "dining":
                return 0xFFE3A04B; // Orange

            case "education":
                return 0xFF4FA3A5; // Teal

            case "shopping":
                return 0xFF7B61A8; // Purple

            case "transport":
            case "transportation":
                return 0xFF5B8DB8; // Blue

            case "entertainment":
                return 0xFFE07A8A; // Rose

            case "bills":
            case "utilities":
                return 0xFFD19A66; // Brown

            case "health":
            case "medical":
                return 0xFF6C9A6B; // Green

            case "rent":
            case "housing":
                return 0xFFD4B24C; // Gold

            case "travel":
                return 0xFF9B7EBD; // Lavender

            case "other":
                return 0xFF7A8B99; // Slate

            default:

                // Stable color for unknown categories
                int hash = Math.abs(name.hashCode());

                return chartColors[
                        hash % chartColors.length
                        ];
        }
    }

    // ---------------------------------------------------------
    // MEASURE
    // ---------------------------------------------------------

    @Override
    protected void onMeasure(
            int widthMeasureSpec,
            int heightMeasureSpec) {

        int width =
                MeasureSpec.getSize(widthMeasureSpec);

        int desiredHeight = dpToPx(370);

        int height =
                resolveSize(
                        desiredHeight,
                        heightMeasureSpec
                );

        setMeasuredDimension(width, height);
    }

    // ---------------------------------------------------------
    // DRAW
    // ---------------------------------------------------------

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        if (categories.isEmpty()
                || amounts.isEmpty()) {

            drawEmptyState(canvas);
            return;
        }

        double total = 0;

        for (Double amount : amounts) {

            if (amount != null && amount > 0) {
                total += amount;
            }
        }

        if (total <= 0) {

            drawEmptyState(canvas);
            return;
        }

        float width = getWidth();
        float height = getHeight();

        float centerX = width / 2f;

        float centerY =
                height / 2f - dpToPx(5);

        float radius =
                Math.min(
                        width * 0.37f,
                        height * 0.37f
                );

        piePaint.setShadowLayer(
                dpToPx(7),
                0,
                dpToPx(3),
                0x30000000
        );

        RectF pieRect =
                new RectF(
                        centerX - radius,
                        centerY - radius,
                        centerX + radius,
                        centerY + radius
                );

        float startAngle = -90f;

        // -----------------------------------------------------
        // PIE
        // -----------------------------------------------------

        for (int i = 0;
             i < amounts.size() && i < categories.size();
             i++) {

            Double amount = amounts.get(i);

            if (amount == null || amount <= 0) {
                continue;
            }

            float sweepAngle =
                    (float) (
                            (amount / total) * 360f
                    );

            String category =
                    categories.get(i);

            // FIXED COLOR FOR THIS CATEGORY
            piePaint.setColor(
                    getCategoryColor(category)
            );

            canvas.drawArc(
                    pieRect,
                    startAngle,
                    sweepAngle,
                    true,
                    piePaint
            );

            double percentage =
                    (amount / total) * 100;

            if (percentage >= 8) {

                float middleAngle =
                        startAngle +
                                sweepAngle / 2f;

                double radians =
                        Math.toRadians(middleAngle);

                float labelRadius =
                        radius * 0.67f;

                float labelX =
                        centerX +
                                (float)
                                        Math.cos(radians)
                                        * labelRadius;

                float labelY =
                        centerY +
                                (float)
                                        Math.sin(radians)
                                        * labelRadius;

                drawPercentage(
                        canvas,
                        labelX,
                        labelY,
                        percentage
                );
            }

            startAngle += sweepAngle;
        }

        piePaint.clearShadowLayer();

        // -----------------------------------------------------
        // CENTER HOLE
        // -----------------------------------------------------

        float holeRadius =
                radius * 0.43f;

        canvas.drawCircle(
                centerX,
                centerY,
                holeRadius,
                centerPaint
        );

        // -----------------------------------------------------
        // CENTER TEXT
        // -----------------------------------------------------

        textPaint.setTextAlign(
                Paint.Align.CENTER
        );

        textPaint.setTextSize(
                dpToPx(11)
        );

        textPaint.setColor(
                0xFF8B8494
        );

        canvas.drawText(
                "TOTAL",
                centerX,
                centerY - dpToPx(3),
                textPaint
        );

        textPaint.setTextSize(
                dpToPx(18)
        );

        textPaint.setColor(
                0xFF514B70
        );

        canvas.drawText(
                formatAmount(total),
                centerX,
                centerY + dpToPx(19),
                textPaint
        );

        textPaint.setTextAlign(
                Paint.Align.LEFT
        );
    }

    // ---------------------------------------------------------
    // PERCENTAGE
    // ---------------------------------------------------------

    private void drawPercentage(
            Canvas canvas,
            float x,
            float y,
            double percentage) {

        textPaint.setColor(0xFFFFFFFF);

        textPaint.setTextAlign(
                Paint.Align.CENTER
        );

        textPaint.setTextSize(
                dpToPx(12)
        );

        textPaint.setTypeface(
                android.graphics.Typeface.DEFAULT_BOLD
        );

        String text =
                String.format(
                        Locale.getDefault(),
                        "%.0f%%",
                        percentage
                );

        canvas.drawText(
                text,
                x,
                y,
                textPaint
        );

        textPaint.setTextAlign(
                Paint.Align.LEFT
        );
    }

    // ---------------------------------------------------------
    // EMPTY
    // ---------------------------------------------------------

    private void drawEmptyState(Canvas canvas) {

        textPaint.setColor(
                0xFF8B8494
        );

        textPaint.setTextSize(
                dpToPx(15)
        );

        textPaint.setTextAlign(
                Paint.Align.CENTER
        );

        canvas.drawText(
                "No expenses this month",
                getWidth() / 2f,
                getHeight() / 2f,
                textPaint
        );

        textPaint.setTextAlign(
                Paint.Align.LEFT
        );
    }

    // ---------------------------------------------------------
    // MONEY
    // ---------------------------------------------------------

    private String formatAmount(double amount) {

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

    private int dpToPx(float dp) {

        return (int) (
                dp *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }
}