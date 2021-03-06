package com.sirweb.miro.parsing.values.miro;

import com.sirweb.miro.Miro;
import com.sirweb.miro.exceptions.*;
import com.sirweb.miro.lexer.Token;
import com.sirweb.miro.lexer.TokenType;
import com.sirweb.miro.lexer.Tokenizer;
import com.sirweb.miro.parsing.Parser;
import com.sirweb.miro.parsing.values.Unit;
import com.sirweb.miro.parsing.values.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Calculator implements MiroValue {

    @Override
    public Value callFunc(String functionName, List<MiroValue> parameters) throws MiroUnimplementedFuncException, MiroFuncParameterException {
        return null;
    }

    public enum Operator {
        AND(1),
        OR(1),
        EQUALSEQUALS(2),
        GREATER(2),
        SMALLER(2),
        GREATER_EQUALS(2),
        SMALLER_EQUALS(2),
        PLUS(3),
        MINUS(3),
        MULTIPLY(4),
        DIVIDE(4);

        private final int precedence;

        Operator(final int newPrecedence) {
            precedence = newPrecedence;
        }

        public int getPrecedence() { return precedence; }

        public static Operator toOperator (String s) throws MiroCalculationException {
            switch (s) {
                case "&&":
                    return Operator.AND;
                case "||":
                    return Operator.OR;
                case "==":
                    return Operator.EQUALSEQUALS;
                case "+":
                    return Operator.PLUS;
                case "-":
                    return Operator.MINUS;
                case "*":
                    return Operator.MULTIPLY;
                case "/":
                    return Operator.DIVIDE;
                case ">":
                    return Operator.GREATER;
                case ">=":
                    return Operator.GREATER_EQUALS;
                case "<":
                    return Operator.SMALLER;
                case "<=":
                    return Operator.SMALLER_EQUALS;
                default:
                    throw new MiroCalculationException("Unknown operator '"+s+"'");

            }
        }
    }

    private Tokenizer tokenizer;
    private Parser parser;
    private List<Object> postfix;

    public Calculator (Parser parser) throws MiroException {
        this(parser, false);
    }

    public Calculator (Parser parser, boolean openedByBracket) throws MiroException {
        this.parser = parser;
        this.tokenizer = parser.tokenizer();
        this.postfix = new ArrayList<>();
        parseCalculation(openedByBracket);
    }

    public List<Object> getPostfix () { return postfix; }

    private void parseCalculation(boolean openedByBracket) throws MiroException {
        Stack<Operator> operators = new Stack<>();

        if (openedByBracket)
            parser.consume(TokenType.O_R_TOKEN);

        parser.consumeWhitespaces();

        //parser.optional(TokenType.O_R_TOKEN);
        do {
            if (tokenizer.nextTokenType() == TokenType.ARITHMETIC_TOKEN) {
                Operator operator = Operator.toOperator(tokenizer.getNext().getToken());
                while (!operators.empty() &&
                        operators.peek().getPrecedence() > operator.getPrecedence()) {
                    postfix.add(operators.pop());
                }
                operators.push(operator);
            }
            else
                postfix.add(parser.parseValue());
            parser.consumeWhitespaces();
        } while (tokenizer.nextTokenType() != TokenType.C_R_TOKEN
                && tokenizer.nextTokenType() != TokenType.NEWLINE_TOKEN
                && tokenizer.nextTokenType() != TokenType.SEMICOLON_TOKEN
                && tokenizer.nextTokenType() != TokenType.COMMA_TOKEN
                && tokenizer.nextTokenType() != TokenType.C_C_TOKEN
                && tokenizer.nextTokenType() != TokenType.EOF
                && tokenizer.nextTokenType() != TokenType.MIRO_EXCLAMATION_TOKEN
                && tokenizer.nextTokenType() != TokenType.MIRO_DEBUG_TOKEN
                && tokenizer.nextTokenType() != TokenType.C_Q_TOKEN
                && tokenizer.nextTokenType() != TokenType.COLON_TOKEN);
        while (!operators.isEmpty())
            postfix.add(operators.pop());

        parser.consumeNewlines();
        parser.consumeWhitespaces();
        parser.consumeNewlines();

        if (openedByBracket)
            parser.consume(TokenType.C_R_TOKEN);

    }

    public MiroValue eval () throws MiroParserException {
        Stack<MiroValue> operands = new Stack<>();
        int position = 0;

        if (operands.size() == 1)
            return operands.pop();

        do {
            if (getPostfix().get(position) instanceof MiroValue)
                operands.push((MiroValue) getPostfix().get(position));
            else {
                MiroValue val2 = operands.pop();
                MiroValue val1 = operands.pop();
                MiroValue result = null;
                Operator operator = (Operator) getPostfix().get(position);
                switch (operator) {
                    case PLUS:
                        result = add(val1, val2);
                        break;
                    case MINUS:
                        result = subtract(val1, val2);
                    case MULTIPLY:
                        result = multiply(val1, val2);
                        break;
                    case DIVIDE:
                        result = divide(val1, val2);
                        break;
                    case OR:
                        result = Or(val1, val2);
                        break;
                    case AND:
                        result = And(val1, val2);
                        break;
                    case GREATER:
                        result = greater(val1, val2);
                        break;
                    case GREATER_EQUALS:
                        result = greaterEqual(val1, val2);
                        break;
                    case SMALLER:
                        result = smaller(val1, val2);
                        break;
                    case SMALLER_EQUALS:
                        result = smallerEqual(val1, val2);
                        break;
                    case EQUALSEQUALS:
                        result = equalEqual(val1, val2);
                        break;
                }
                operands.push(result);
            }
        } while (++position < getPostfix().size());

        return operands.pop();
    }

    private MiroValue add (MiroValue val1, MiroValue val2) throws MiroParserException {
        if (val1 instanceof Numeric)
            return addNumeric((Numeric) val1, val2);
        if (val1 instanceof StringValue)
            return addString((StringValue) val1, val2);

        throw new MiroParserException("Operator + is not defined for " + val1.getClass().getSimpleName() + " and " + val2.getClass().getSimpleName());
    }

    private MiroValue subtract (MiroValue val1, MiroValue val2) throws MiroParserException {
        if (val1 instanceof Numeric)
            return subtractNumeric((Numeric) val1, val2);

        throw new MiroParserException("Operator - is not defined for " + val1.getClass().getSimpleName() + " and " + val2.getClass().getSimpleName());
    }

    private MiroValue multiply (MiroValue val1, MiroValue val2) throws MiroParserException {
        if (val1 instanceof Numeric)
            return multiplyNumeric((Numeric) val1, val2);
        if (val1 instanceof StringValue)
            return addString((StringValue) val1, val2);

        throw new MiroParserException("Operator * is not defined for " + val1.getClass().getSimpleName() + " and " + val2.getClass().getSimpleName());
    }

    private MiroValue divide (MiroValue val1, MiroValue val2) throws MiroParserException {
        if (val1 instanceof Numeric)
            return divideNumeric((Numeric) val1, val2);

        throw new MiroParserException("Operator / is not defined for " + val1.getClass().getSimpleName() + " and " + val2.getClass().getSimpleName());
    }

    private MiroValue greater (MiroValue val1, MiroValue val2) throws MiroParserException {
        if (val1 instanceof Numeric)
            return greaterNumeric((Numeric) val1, val2);

        throw new MiroParserException("Operator > is not defined for " + val1.getClass().getSimpleName() + " and " + val2.getClass().getSimpleName());
    }

    private MiroValue greaterEqual (MiroValue val1, MiroValue val2) throws MiroParserException {
        if (val1 instanceof Numeric)
            return greaterEqualNumeric((Numeric) val1, val2);

        throw new MiroParserException("Operator >= is not defined for " + val1.getClass().getSimpleName() + " and " + val2.getClass().getSimpleName());
    }

    private MiroValue smaller (MiroValue val1, MiroValue val2) throws MiroParserException {
        if (val1 instanceof Numeric)
            return smallerNumeric((Numeric) val1, val2);

        throw new MiroParserException("Operator < is not defined for " + val1.getClass().getSimpleName() + " and " + val2.getClass().getSimpleName());
    }

    private MiroValue smallerEqual (MiroValue val1, MiroValue val2) throws MiroParserException {
        if (val1 instanceof Numeric)
            return smallerEqualNumeric((Numeric) val1, val2);

        throw new MiroParserException("Operator <= is not defined for " + val1.getClass().getSimpleName() + " and " + val2.getClass().getSimpleName());
    }

    private MiroValue equalEqual (MiroValue val1, MiroValue val2) throws MiroParserException {
        if (val1 instanceof Numeric)
            return equalEqualNumeric((Numeric) val1, val2);
        else if (val1 instanceof Bool)
            return equalEqualBool((Bool) val1, val2);

        throw new MiroParserException("Operator == is not defined for " + val1.getClass().getSimpleName() + " and " + val2.getClass().getSimpleName());
    }

    private MiroValue greaterNumeric (Numeric val1, MiroValue val2) throws MiroParserException {
        if (val2 instanceof Numeric)
            return new Bool(val1.getNormalizedValue() > ((Numeric) val2).getNormalizedValue());

        throw new MiroParserException("Operator > is not defined for " + val1.getClass().getSimpleName() + " and " + val2.getClass().getSimpleName());
    }

    private MiroValue smallerNumeric (Numeric val1, MiroValue val2) throws MiroParserException {
        if (val2 instanceof Numeric)
            return new Bool(val1.getNormalizedValue() < ((Numeric) val2).getNormalizedValue());

        throw new MiroParserException("Operator < is not defined for " + val1.getClass().getSimpleName() + " and " + val2.getClass().getSimpleName());
    }

    private MiroValue greaterEqualNumeric (Numeric val1, MiroValue val2) throws MiroParserException {
        if (val2 instanceof Numeric)
            return new Bool(val1.getNormalizedValue() >= ((Numeric) val2).getNormalizedValue());

        throw new MiroParserException("Operator >= is not defined for " + val1.getClass().getSimpleName() + " and " + val2.getClass().getSimpleName());
    }

    private MiroValue smallerEqualNumeric (Numeric val1, MiroValue val2) throws MiroParserException {
        if (val2 instanceof Numeric)
            return new Bool(val1.getNormalizedValue() <= ((Numeric) val2).getNormalizedValue());

        throw new MiroParserException("Operator <= is not defined for " + val1.getClass().getSimpleName() + " and " + val2.getClass().getSimpleName());
    }

    private MiroValue equalEqualNumeric (Numeric val1, MiroValue val2) throws MiroParserException {
        if (val2 instanceof Numeric)
            return new Bool(val1.getNormalizedValue() == ((Numeric) val2).getNormalizedValue());

        throw new MiroParserException("Operator == is not defined for " + val1.getClass().getSimpleName() + " and " + val2.getClass().getSimpleName());
    }

    private MiroValue equalEqualBool (Bool val1, MiroValue val2) throws MiroParserException {
        return new Bool(val1.getBoolean() == val2.getBoolean());
    }

    private MiroValue addNumeric (Numeric val1, MiroValue val2) throws MiroParserException {
        if (val2 instanceof Numeric)
            if (val1.getUnit() == Unit.PERCENT
                    || val1.getUnit() == Unit.VW
                    || val1.getUnit() == Unit.VH
                    || ((Numeric) val2).getUnit() == Unit.PERCENT
                    || ((Numeric) val2).getUnit() == Unit.VW
                    || ((Numeric) val2).getUnit() == Unit.VH) {
                MultiValue parameters = new MultiValue();
                com.sirweb.miro.parsing.values.miro.List calculation = new com.sirweb.miro.parsing.values.miro.List();
                calculation.addValue(val1);
                calculation.addValue(new Ident("+"));
                calculation.addValue(val2);
                parameters.addValue(calculation);
                return new Function("calc", parameters);
            }
            else
                return new Numeric(val1.getNormalizedValue() + ((Numeric)val2).getNormalizedValue(), val1.getUnit());
        if (val2 instanceof StringValue)
            return new StringValue(val1.toString() + ((StringValue) val2).getValue());

        throw new MiroParserException("Operator + is not defined for " + val1.getClass().getSimpleName() + " and " + val2.getClass().getSimpleName());
    }

    private MiroValue subtractNumeric (Numeric val1, MiroValue val2) throws MiroParserException {
        if (val2 instanceof Numeric)
            if (val1.getUnit() == Unit.PERCENT
                    || val1.getUnit() == Unit.VW
                    || val1.getUnit() == Unit.VH
                    || ((Numeric) val2).getUnit() == Unit.PERCENT
                    || ((Numeric) val2).getUnit() == Unit.VW
                    || ((Numeric) val2).getUnit() == Unit.VH) {
                MultiValue parameters = new MultiValue();
                com.sirweb.miro.parsing.values.miro.List calculation = new com.sirweb.miro.parsing.values.miro.List();
                calculation.addValue(val1);
                calculation.addValue(new Ident("-"));
                calculation.addValue(val2);
                parameters.addValue(calculation);
                return new Function("calc", parameters);
            }
            else
                return new Numeric(val1.getNormalizedValue() - ((Numeric)val2).getNormalizedValue(), val1.getUnit());
        if (val2 instanceof StringValue)
            return new StringValue(((StringValue) val2).getValue().replace(val1.toString(), ""));

        throw new MiroParserException("Operator - is not defined for " + val1.getClass().getSimpleName() + " and " + val2.getClass().getSimpleName());
    }

    private MiroValue divideNumeric (Numeric val1, MiroValue val2) throws MiroParserException {
        if (val2 instanceof Numeric)
            if (val1.getUnit() == Unit.PERCENT
                    || val1.getUnit() == Unit.VW
                    || val1.getUnit() == Unit.VH
                    || ((Numeric) val2).getUnit() == Unit.PERCENT
                    || ((Numeric) val2).getUnit() == Unit.VW
                    || ((Numeric) val2).getUnit() == Unit.VH) {
                MultiValue parameters = new MultiValue();
                com.sirweb.miro.parsing.values.miro.List calculation = new com.sirweb.miro.parsing.values.miro.List();
                calculation.addValue(val1);
                calculation.addValue(new Ident("/"));
                calculation.addValue(val2);
                parameters.addValue(calculation);
                return new Function("calc", parameters);
            }
            else
                return new Numeric(val1.getNormalizedValue() / ((Numeric)val2).getNormalizedValue(), val1.getUnit());


        throw new MiroParserException("Operator / is not defined for " + val1.getClass().getSimpleName() + " and " + val2.getClass().getSimpleName());
    }

    private MiroValue addString (StringValue val1, MiroValue val2) throws MiroParserException {
        if (val2 instanceof Numeric)
            return new StringValue(val1.getValue() + ((Numeric) val2).toString());
        if (val2 instanceof StringValue)
            return new StringValue(val1.getValue() + ((StringValue) val2).getValue());


        throw new MiroParserException("Operator + is not defined for " + val1.getClass().getSimpleName() + " and " + val2.getClass().getSimpleName());
    }

    private MiroValue multiplyNumeric (Numeric val1, MiroValue val2) throws MiroParserException {
        if (val2 instanceof Numeric)
            if (val1.getUnit() == Unit.PERCENT
                    || val1.getUnit() == Unit.VW
                    || val1.getUnit() == Unit.VH
                    || ((Numeric) val2).getUnit() == Unit.PERCENT
                    || ((Numeric) val2).getUnit() == Unit.VW
                    || ((Numeric) val2).getUnit() == Unit.VH) {
                MultiValue parameters = new MultiValue();
                com.sirweb.miro.parsing.values.miro.List calculation = new com.sirweb.miro.parsing.values.miro.List();
                calculation.addValue(val1);
                calculation.addValue(new Ident("*"));
                calculation.addValue(val2);
                parameters.addValue(calculation);
                return new Function("calc", parameters);
            }
            else
                return new Numeric(val1.getNormalizedValue() * ((Numeric)val2).getNormalizedValue(), val1.getUnit());
        if (val2 instanceof StringValue) {
            String resultString = "";
            for (int i = 0; i < val1.getValue(); i++)
                resultString += ((StringValue) val2).getValue();
            return new StringValue(resultString);
        }

        throw new MiroParserException("Operator * is not defined for " + val1.getClass().getSimpleName() + " and " + val2.getClass().getSimpleName());
    }

    private Bool Or (MiroValue val1, MiroValue val2) throws MiroParserException {
        return new Bool(val1.getBoolean() || val2.getBoolean());
    }

    private Bool And (MiroValue val1, MiroValue val2) throws MiroParserException {
        return new Bool(val1.getBoolean() && val2.getBoolean());
    }

    @Override
    public boolean getBoolean() throws MiroParserException {
        return eval().getBoolean();
    }
}
