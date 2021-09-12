package com.vincentvibe3.calculator.ui.calculator;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.color.MaterialColors;
import com.vincentvibe3.calculator.Calculator;
import com.vincentvibe3.calculator.R;

import java.util.regex.Pattern;

public class CalculatorFragment extends Fragment {

    private CalculatorViewModel calculatorViewModel;

    boolean finalAns = false;
    boolean badExpression = false;
    EditText eqview;
    TextView ansview;
    int defaultEqColor;
    int defaultAnsColor;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        calculatorViewModel = new ViewModelProvider(this).get(CalculatorViewModel.class);
        View root = inflater.inflate(R.layout.fragment_calculator, container, false);
        setupButtons(root);
        eqview = root.findViewById(R.id.equationview);
        eqview.setShowSoftInputOnFocus(false);
        eqview.addTextChangedListener(new TextListener(this));
        ansview = root.findViewById(R.id.ansView);
        defaultAnsColor = ansview.getCurrentTextColor();
        defaultEqColor = eqview.getCurrentTextColor();
        return root;
    }

    public void setupButtons(View view){
        ClickHandler clickHandler = new ClickHandler(this);
        LongClickHandler longClickHandler = new LongClickHandler(this);
        ViewGroup rootView = (ViewGroup) view;
        for (int i=0; i<rootView.getChildCount(); i++){
            View child1 = rootView.getChildAt(i);
            if (child1 instanceof ConstraintLayout){
                for (int itemIndex = 0; itemIndex<((ConstraintLayout) child1).getChildCount(); itemIndex++){
                    View child2 = ((ConstraintLayout) child1).getChildAt(itemIndex);
                    if (child2 instanceof  Button){
                        child2.setOnClickListener(clickHandler);
                        if (child2.getId()==R.id.buttonClear){
                            child2.setOnLongClickListener(longClickHandler);
                        }
                    }
                }
            }

        }
    }

    public boolean addToEquation(String valueToAdd,int cursorStart, int cursorEnd){
        String previousChar;
        if (!"*.-÷+".contains(formatEq(valueToAdd))){
            eqview.getText().insert(cursorStart, valueToAdd);
            return true;
        } else {
            if (cursorEnd!=0){
                previousChar = formatEq(String.valueOf(eqview.getText().charAt(cursorEnd-1)));
                if (!"*.-÷+".contains(previousChar)){
                    eqview.getText().insert(cursorStart, valueToAdd);
                    return true;
                } else if (valueToAdd.equals("-")&&!previousChar.equals("-")){
                    eqview.getText().insert(cursorStart, valueToAdd);
                    return true;
                }
            } else if (valueToAdd.equals("-")){
                eqview.getText().insert(cursorStart, valueToAdd);
                return true;
            }
        }
        return false;

    }

    public void deleteFromEquation(int cursorStart, int cursorEnd){
        if (!eqview.getText().toString().isEmpty()) {
            if (cursorStart!=cursorEnd&&cursorStart!=0){
                eqview.getText().delete(cursorEnd, cursorStart);
            } else if (cursorStart!=0){
                eqview.getText().delete(cursorEnd-1, cursorStart);
            }
        }
    }

    public void clearEquation(){
        eqview.setText("");
    }

    public String formatEq(String equation){
        equation = equation.replace("÷", "/");
        equation = equation.replace("×", "*");
        return equation;
    }

    public void textViewChangeColor(TextView textView, int colorStart, int colorEnd){
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorStart, colorEnd);
        colorAnimation.setDuration(500);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator updatedAnimation) {
                int animatedValue = (int)updatedAnimation.getAnimatedValue();
                textView.setTextColor(animatedValue);
            }
        });
        colorAnimation.start();
    }

    public boolean checkValid(String equation){
        equation = formatEq(equation);
        Pattern operatorsEnd = Pattern.compile("[*/+-]$");
        Pattern operatorsStart = Pattern.compile("^[*./+]");
        Pattern op2op = Pattern.compile("[/*+.-][/*+.]");
        Pattern doubleMinus = Pattern.compile("--");
        boolean doubleMinusValid = !doubleMinus.matcher(equation).find();
        boolean midValid = !op2op.matcher(equation).find();
        boolean startValid = !operatorsStart.matcher(equation).find();
        boolean endValid = !operatorsEnd.matcher(equation).find();
        return startValid&&endValid&&midValid&&doubleMinusValid;
    }

    public void getFinalAns(){
        String equation = eqview.getText().toString();
        if (!checkValid(equation)){
            ansview.setText("Bad expression");
            textViewChangeColor(ansview, ansview.getCurrentTextColor(), getContext().getColor(R.color.error));
            textViewChangeColor(eqview, ansview.getCurrentTextColor(), getContext().getColor(R.color.error));
        }else if (equation.isEmpty()){
            eqview.setText("");
        }else {
            finalAns = true;
            eqview.setText(Calculator.main(formatEq(equation)));
        }

    }

    public void moveCursor(int cursorEnd, int selectionSize, boolean deletion, boolean added) {
        String equation = eqview.getText().toString();
        if (equation.length() <= 1 || finalAns) {
            eqview.setSelection(equation.length());
        } else if ((cursorEnd != 0 && deletion && selectionSize > 0) || badExpression) {
            eqview.setSelection(cursorEnd);
        } else if (cursorEnd != 0 && deletion) {
            eqview.setSelection(cursorEnd - 1);
        } else if (added) {
            eqview.setSelection(cursorEnd + 1);
        }
    }
}

class ClickHandler implements View.OnClickListener {

    //Store calculatorFragment it is attached to
    CalculatorFragment calc;

    public ClickHandler(CalculatorFragment fragment){
        calc = fragment;
    }

    public void onClick(View view) {
        int cursorEnd = calc.eqview.getSelectionStart();
        int cursorStart = calc.eqview.getSelectionEnd();
        int selectionSize = Math.abs(cursorEnd-cursorStart);
        boolean deletion = false;
        boolean added = false;
        int id = view.getId();
        if (id == R.id.button1) {
            added = calc.addToEquation("1", cursorStart, cursorEnd);
        } else if (id == R.id.button2) {
            added = calc.addToEquation("2", cursorStart, cursorEnd);
        } else if (id == R.id.button3) {
            added = calc.addToEquation("3", cursorStart, cursorEnd);
        } else if (id == R.id.button4) {
            added = calc.addToEquation("4", cursorStart, cursorEnd);
        } else if (id == R.id.button5) {
            added = calc.addToEquation("5", cursorStart, cursorEnd);
        } else if (id == R.id.button6) {
            added = calc.addToEquation("6", cursorStart, cursorEnd);
        } else if (id == R.id.button7) {
            added = calc.addToEquation("7", cursorStart, cursorEnd);
        } else if (id == R.id.button8) {
            added = calc.addToEquation("8", cursorStart, cursorEnd);
        } else if (id == R.id.button9) {
            added = calc.addToEquation("9", cursorStart, cursorEnd);
        } else if (id == R.id.button0) {
            added = calc.addToEquation("0", cursorStart, cursorEnd);
        } else if (id == R.id.buttonPlus) {
            added = calc.addToEquation("+", cursorStart, cursorEnd);
        } else if (id == R.id.buttonMinus) {
            added = calc.addToEquation("-", cursorStart, cursorEnd);
        } else if (id == R.id.buttonDivide) {
            added = calc.addToEquation("÷", cursorStart, cursorEnd);
        } else if (id == R.id.buttonMultiply) {
            added = calc.addToEquation("×", cursorStart, cursorEnd);
        } else if (id == R.id.buttonClear) {
            calc.deleteFromEquation(cursorStart, cursorEnd);
            deletion = true;
        } else if (id == R.id.buttonDec) {
            added = calc.addToEquation(".", cursorStart, cursorEnd);
        } else if (id == R.id.buttonEqual) {
            calc.getFinalAns();
        } else if (id == R.id.buttonParenthesisOpen){
            added = calc.addToEquation("(", cursorStart, cursorEnd);
        } else if (id == R.id.buttonParenthesisClose){
            added = calc.addToEquation(")", cursorStart, cursorEnd);
        }

        calc.moveCursor(cursorEnd, selectionSize, deletion, added);
        calc.finalAns = false;
        calc.badExpression = false;

    }
}

class LongClickHandler implements View.OnLongClickListener {

    //Store calculatorFragment it is attached to
    CalculatorFragment calc;

    public LongClickHandler(CalculatorFragment fragment) {
        calc = fragment;
    }

    public boolean onLongClick(View view) {
        if (view.getId()==R.id.buttonClear){
            calc.clearEquation();
        }
        return false;
    }
}

class TextListener implements TextWatcher{

    //Store calculatorFragment it is attached to
    CalculatorFragment calc;

    public TextListener(CalculatorFragment fragment) {
        calc = fragment;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        ;

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        ;

    }

    @Override
    public void afterTextChanged(Editable s) {
        String equation = calc.formatEq(calc.eqview.getText().toString());
        calc.textViewChangeColor(calc.ansview, calc.ansview.getCurrentTextColor(), calc.defaultAnsColor);
        calc.textViewChangeColor(calc.eqview, calc.eqview.getCurrentTextColor(), calc.defaultEqColor);
        if (equation.equals("")){
            calc.ansview.setText(equation);
        } else if (calc.checkValid(equation)&&!calc.finalAns){
            calc.ansview.setText(Calculator.main(equation));
        } else if (calc.finalAns&&calc.checkValid(equation)){
            calc.ansview.setText("");
        }
    }

}
