package com.example.expensemanager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CalculatorActivity extends AppCompatActivity {

    TextView txtExpression;
    TextView txtResult;

    String currentNumber = "";
    double firstNumber = 0;
    String operator = "";
    boolean newNumber = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_calculator);

        txtExpression = findViewById(R.id.txtExpression);
        txtResult = findViewById(R.id.txtResult);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        setNumberButton(R.id.btn0, "0");
        setNumberButton(R.id.btn1, "1");
        setNumberButton(R.id.btn2, "2");
        setNumberButton(R.id.btn3, "3");
        setNumberButton(R.id.btn4, "4");
        setNumberButton(R.id.btn5, "5");
        setNumberButton(R.id.btn6, "6");
        setNumberButton(R.id.btn7, "7");
        setNumberButton(R.id.btn8, "8");
        setNumberButton(R.id.btn9, "9");

        findViewById(R.id.btnDot).setOnClickListener(v -> {

            if (newNumber) {
                currentNumber = "0";
                newNumber = false;
            }

            if (!currentNumber.contains(".")) {
                currentNumber += ".";
                txtResult.setText(currentNumber);
            }
        });

        setOperator(R.id.btnPlus, "+");
        setOperator(R.id.btnMinus, "-");
        setOperator(R.id.btnMultiply, "×");
        setOperator(R.id.btnDivide, "÷");

        findViewById(R.id.btnPercent).setOnClickListener(v -> {

            if (!currentNumber.isEmpty()) {

                double value = Double.parseDouble(currentNumber);
                value = value / 100;

                currentNumber = format(value);
                txtResult.setText(currentNumber);
            }
        });

        findViewById(R.id.btnDelete).setOnClickListener(v -> {

            if (!currentNumber.isEmpty()) {

                currentNumber =
                        currentNumber.substring(
                                0,
                                currentNumber.length() - 1
                        );

                if (currentNumber.isEmpty()) {
                    txtResult.setText("0");
                } else {
                    txtResult.setText(currentNumber);
                }
            }
        });

        findViewById(R.id.btnClear).setOnClickListener(v -> clear());

        findViewById(R.id.btnEquals).setOnClickListener(v -> calculate());
    }

    private void setNumberButton(int id, String number) {

        Button button = findViewById(id);

        button.setOnClickListener(v -> {

            if (newNumber) {
                currentNumber = "";
                newNumber = false;
            }

            currentNumber += number;

            txtResult.setText(currentNumber);
        });
    }

    private void setOperator(int id, String selectedOperator) {

        findViewById(id).setOnClickListener(v -> {

            if (currentNumber.isEmpty()) {
                return;
            }

            firstNumber =
                    Double.parseDouble(currentNumber);

            operator = selectedOperator;

            txtExpression.setText(
                    format(firstNumber) + " " + operator
            );

            newNumber = true;
        });
    }

    private void calculate() {

        if (currentNumber.isEmpty() || operator.isEmpty()) {
            return;
        }

        double secondNumber =
                Double.parseDouble(currentNumber);

        double result = 0;

        switch (operator) {

            case "+":
                result = firstNumber + secondNumber;
                break;

            case "-":
                result = firstNumber - secondNumber;
                break;

            case "×":
                result = firstNumber * secondNumber;
                break;

            case "÷":

                if (secondNumber == 0) {
                    txtResult.setText("Error");
                    return;
                }

                result = firstNumber / secondNumber;
                break;
        }

        txtExpression.setText(
                format(firstNumber)
                        + " "
                        + operator
                        + " "
                        + format(secondNumber)
                        + " ="
        );

        currentNumber = format(result);

        txtResult.setText(currentNumber);

        operator = "";
        newNumber = true;
    }

    private void clear() {

        currentNumber = "";
        firstNumber = 0;
        operator = "";
        newNumber = true;

        txtExpression.setText("");
        txtResult.setText("0");
    }

    private String format(double value) {

        if (value == (long) value) {
            return String.valueOf((long) value);
        }

        return String.valueOf(value);
    }
}