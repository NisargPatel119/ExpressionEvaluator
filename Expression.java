package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {


    public static String delims = " \t*+-/()[]";
    public static String expr;
    public static ArrayList<Variable> vars;
    public static ArrayList<Array> arrays;
            
    /**
     * Populates the vars list with simple variables, and arrays lists with arrays
     * in the expression. For every variable (simple or array), a SINGLE instance is created 
     * and stored, even if it appears more than once in the expression.
     * At this time, values for all variables and all array items are set to
     * zero - they will be loaded from a file in the loadVariableValues method.
     * 
     * @param expr The expression
     * @param vars The variables array list - already created by the caller
     * @param arrays The arrays array list - already created by the caller
     */
    public static void 
    makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
        /** COMPLETE THIS METHOD **/
        /** DO NOT create new vars and arrays - they are already created before being sent in
         ** to this method - you just need to fill them in.
         **/
		String noChar = "";
        String noBracket = "]()*/+-";
        for(int i = 0; i < expr.length(); i++)
        {
            char a = expr.charAt(i);
            if(Character.isWhitespace(a))
            {continue;}
            else if (Character.isDigit(a))
            {continue;}
            else if (delims.contains(String.valueOf(a)) == false)
            {
                noChar += a;
            }
            else if (noBracket.contains(String.valueOf(a)))
            {

            if(noChar != "")
                {
                    Variable temp = new Variable(noChar);
                    if(vars.contains(temp) == false)
                    {
                        vars.add(temp);
                    }
                    noChar = "";
                }   
            }
            else if (a == '[')
            {
                Array temp = new Array(noChar);
                if(arrays.contains(temp) == false)
                {
                    arrays.add(temp);
                }
                noChar = "";
            }
        }
        Variable temp = new Variable(noChar);
        if((vars.contains(temp) != true) && (delims.contains(noChar)!=true)) {
            vars.add(temp);
        }
        
    }


	/**
     * Loads values for variables and arrays in the expression
     * 
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input 
     * @param vars The variables array list, previously populated by makeVariableLists
     * @param arrays The arrays array list - previously populated by makeVariableLists
     */
    public static void loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays) 
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String tok = st.nextToken();
            Variable var = new Variable(tok);
            Array arr = new Array(tok);
            int vari = vars.indexOf(var);
            int arri = arrays.indexOf(arr);
            if (vari == -1 && arri == -1) {
                continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                vars.get(vari).value = num;
            } else { // array symbol
                arr = arrays.get(arri);
                arr.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    arr.values[index] = val;              
                }
            }
        }
    }
    
    /**
     * Evaluates the expression.
     * 
     * @param vars The variables array list, with values for all variables in the expression
     * @param arrays The arrays array list, with values for all array items
     * @return Result of evaluation
     */
     public static float 
     evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) 
     {
 
    	String x = varstring (expr, vars, arrays) + " ";
        Array temp = null;
        
    
        
        Stack<Integer> index = new Stack<Integer>();
        for(int i = 0; i < x.length(); i++)
        {
            if(x.charAt(i) == '(' || x.charAt(i) == '[')
            {
                index.push(i);
            }
            else if(x.charAt(i) == ')')
            {
                int startIndex = index.pop();
                int endIndex = i;
                String sub = x.substring(startIndex+1, endIndex);
                String result = evalSimplify(sub);
                x = x.replace(x.substring(startIndex, endIndex+1), result);
                i = startIndex;
              
            }
            else if(x.charAt(i) == ']')
            {
                int startIndex = index.pop();
                int endIndex = i;
                char arr = x.charAt(startIndex-1);
                String sub = x.substring(startIndex+1, endIndex);
                String result = evalSimplify(sub);
                temp = arrays.get(arrays.indexOf(new Array(String.valueOf(arr))));
                String value = String.valueOf(temp.values[(int)Double.parseDouble(result)]);
                x = x.replace(x.substring(startIndex-1, endIndex+1), value);
                i = startIndex-1;
            }
        }
        float answer = Float.parseFloat(evalSimplify(x));
        return answer;
    }

    private static String evalSimplify(String exp)
    {
        if (exp.charAt(0) == '-' && "+*/".contains(exp) == false)
        {
            return exp;
        }
        
        StringTokenizer token= new StringTokenizer(exp, "*/+-", true);
        String[] arr = new String[token.countTokens()];
        float answer= 0;
        int l = arr.length;
        
        for(int i = 0; i < arr.length; i++)
        {
            arr[i] = token.nextToken();
        }
        for(int i = 0; i < l-2; i++)
        {
            if(delims.contains(arr[i]) == true && arr[i+1].equals("-"))
            {
                arr[i+2] = "-" + arr[i+2];
                for(int j = i; j < l-2; j++)
                {
                    arr[j+1] = arr[j+2];
                }
                l-=1;
            }
        }
        
        for(int i = 0; i < l; i++)
        {
            if(arr[i].equals("*"))
            {
                answer = Float.parseFloat(String.valueOf((arr[i-1])))*Float.parseFloat(String.valueOf((arr[i+1])));
                arr[i-1] =  String.valueOf(answer);
                for(int j = i; j < l-2; j++)
                {
                    arr[j] = arr[j+2];
                }
                l = l-2;
                i = i-1;
            }
            else if(arr[i].equals("/"))
            {
                answer = Float.parseFloat(String.valueOf((arr[i-1])))/Float.parseFloat(String.valueOf((arr[i+1])));
                arr[i-1] =  String.valueOf(answer);
                for(int j = i; j < l-2; j++)
                {
                    arr[j] = arr[j+2];
                }
                l = l-2;
                i = i-1;
            }
        }
        for(int i = 0; i < l; i++)
        {
            if(arr[i].equals("+"))
            {
                answer = Float.parseFloat(String.valueOf((arr[i-1])))+Float.parseFloat(String.valueOf((arr[i+1])));
                arr[i-1] =  String.valueOf(answer);
                for(int j = i; j < l-2; j++)
                {
                    arr[j] = arr[j+2];
                }
                l = l-2;
                i = i-1;
            }
            else if(arr[i].equals("-"))
            {
                answer = Float.parseFloat(String.valueOf((arr[i-1])))-Float.parseFloat(String.valueOf((arr[i+1])));
                arr[i-1] =  String.valueOf(answer);
                for(int j = i; j < l-2; j++)
                {
                    arr[j] = arr[j+2];
                }
                l = l-2;
                i = i-1;
            }
        }
        return arr[0];
    }

    private static String varstring(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
        String noChar = "+-*/()]";
        String temp = "";
        String answer = "";
        Variable temp1 = null;
        
        expr.replaceAll("\t", "");
        expr.replaceAll(" ", "");
        for(int i = 0; i<expr.length();i++) { 
            if(Character.isWhitespace(expr.charAt(i))) {
                continue;
            }
            if(Character.isDigit(expr.charAt(i))) {
                answer += expr.charAt(i);
                continue;
            }
            else if(expr.charAt(i)=='[') {
                answer += temp;
                answer += expr.charAt(i);
                temp = "";
            }
            else if(noChar.contains(Character.toString(expr.charAt(i))) == false) { 
                temp += expr.charAt(i);
            }
            else if (noChar.contains(Character.toString(expr.charAt(i)))){  
                if(temp != "") {
                    temp1 = vars.get(vars.indexOf(new Variable(temp)));
                    answer += temp1.value;
                } 
                answer += expr.charAt(i);
                temp1 = null;   
                temp = "";
            }
        }
        if (temp != "") {
            temp1 = vars.get(vars.indexOf(new Variable(temp)));
            answer += temp1.value;
        }
        
        return answer;
     }
}

    
