package com.vincentvibe3.calculator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.regex.Pattern;

public class Calculator {

    static HashMap<String, BigDecimal> subs = new HashMap<>();
    static Integer subCount = 0;
    static MathContext defaultMathContext = new MathContext(100, RoundingMode.HALF_UP);

    static Pattern additionOp = Pattern.compile("+", Pattern.LITERAL);
    static Pattern subtractionOp = Pattern.compile("(?<=[0-9])-", Pattern.LITERAL);
    static Pattern divisionOp = Pattern.compile("/", Pattern.LITERAL);
    static Pattern multiplyOp = Pattern.compile("*", Pattern.LITERAL);
    static Pattern exponentOp = Pattern.compile("^", Pattern.LITERAL);
    static Pattern numeric = Pattern.compile("[0-9]");

    public static void main(String[] args){
        String equation = "6/";
        String ans = calculate(equation);
        System.out.println(ans);
    }

    public static String calculate(String equation){

        try {
            equation = exponents(equation);
            equation = multiplyDivide(equation);
            equation = addSubstract(equation);
        } catch (NumberFormatException | NullPointerException | badExpression e){
            return "Bad expression";
        }

        return finalFormat(equation);
    }

    public static String eqFormatter(String equation){
        equation = equation.replace("/", "÷");
        equation = equation.replace("*", "×");
        return equation;
    }

    public static String substitute(String equation, String toReplace, BigDecimal value){
        subCount++;
        String subVar = "x"+subCount;
        subs.put(subVar, value);
        return equation.replace(toReplace, subVar);

    }

    public static BigDecimal getSub(String subVar){
        if (subVar.contains("-")){
            return subs.get(subVar.replace("-", "")).negate();
        } else {
            return subs.get(subVar);
        }
    }

    public static String finalFormat(String subVar){
        if (subVar==null){
            return "Undefined";
        } else {
            BigDecimal ans = subs.get(subVar);
            try{
                ans.toBigIntegerExact();
            } catch (ArithmeticException e){
                return subs.get(subVar).toString();
            }
            return subs.get(subVar).toBigIntegerExact().toString();
        }
    }

    public static String addSubstract(String equation) throws badExpression{
        BigDecimal ans = BigDecimal.ZERO;
        if (equation==null){
            return null;
        }
        for (String value : additionOp.split(equation)) {
            if (value.contains("x")){
                ans = ans.add(getSub(value));

            } else if (numeric.matcher(value).find()){
                ans = ans.add(new BigDecimal(value));
            } else {
                throw new badExpression();
            }
        }
        equation = substitute(equation, equation, ans);

        return equation;
    }

    public static BigDecimal divide(String equation) throws badExpression{
        BigDecimal ans = null;
        String[] nums = divisionOp.split(equation);
        if (nums.length<=1){
            throw new badExpression();
        }
        for (int i=0; i< nums.length; i++){
            if (i==0){
                if (nums[i].contains("x")){
                    ans = getSub(nums[i]);
                } else if (numeric.matcher(nums[i]).find()){
                    ans = new BigDecimal(nums[i]);
                }else {
                    throw new badExpression();
                }
            } else {
                try{
                    if (nums[i].contains("x")){
                        ans = ans.divide(getSub(nums[i]), defaultMathContext);
                    } else if (numeric.matcher(nums[i]).find()){
                        ans = ans.divide(new BigDecimal(nums[i]), defaultMathContext);
                    } else {
                        throw new badExpression();
                    }
                } catch (ArithmeticException e) {
                    return null;
                }

            }

        }
        return ans;
    }

    public static BigDecimal multiply(String equation) throws badExpression{
        BigDecimal ans = BigDecimal.ONE;
        String[] multiplyToDo = multiplyOp.split(equation);
        if (multiplyToDo.length==0){
            throw new badExpression();
        }
        for (String num : multiplyToDo){
            boolean isdivision = divisionOp.matcher(num).find();
            //check if the chain contains a division
            if (isdivision){
                BigDecimal divisionResult = divide(num);
                if (divisionResult==null){
                    return null;
                }
                ans = ans.multiply(divisionResult);

                //multiply
            } else {
                if (num.contains("x")){
                    ans = ans.multiply(getSub(num));
                } else if (numeric.matcher(num).find()){
                    ans = ans.multiply(new BigDecimal(num));
                } else {
                    throw new badExpression();
                }

            }
        }
        return ans;
    }

    public static String multiplyDivide(String equation) throws badExpression{
        BigDecimal ans = BigDecimal.ONE;
        //isolate multiplications
        equation = equation.replaceAll(subtractionOp.pattern(), "+-");
        String[] eqArray = additionOp.split(equation);

        //calculate multiplications
        for (String multiplication : eqArray) {
            boolean multiplyFound = multiplyOp.matcher(multiplication).find();
            boolean divisionFound = divisionOp.matcher(multiplication).find();
            //check if there is a multiplication(with or without division)
            if (multiplyFound){
                ans = multiply(multiplication);
                //check if there is only a division
            } else if (divisionFound) {
                ans = divide(multiplication);
            }

            if (ans == null){
                return null;

            } else if (multiplyFound||divisionFound){
                equation = substitute(equation, multiplication, ans);
            }
        }
        return equation;
    }

    public static String exponents(String equation){
        //(-2)^2
        //-2^2
        BigDecimal ans = null;
        for (String exponent : equation.split("[*/+-]")){
            boolean isExponent = exponentOp.matcher(exponent).find();
            if (isExponent){
                String[] exponentArray = exponentOp.split(exponent);
                for (int i=0; i<exponentArray.length; i++){
                    if (i==0){
                        ans = new BigDecimal(exponentArray[i]);
                    } else {
                        ans = ans.pow(Integer.parseInt(exponentArray[i]));
                    }
                }
                equation = substitute(equation, exponent, ans);
            }

        }
        return equation;
    }
}

class badExpression extends Exception {

    public badExpression() {

    }
}