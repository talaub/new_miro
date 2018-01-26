package parsing;

        import com.sirweb.miro.exceptions.MiroException;
        import com.sirweb.miro.lexer.Tokenizer;
        import com.sirweb.miro.parsing.Parser;
        import com.sirweb.miro.parsing.values.miro.Calculator;
        import com.sirweb.miro.parsing.values.miro.MiroValue;
        import com.sirweb.miro.parsing.values.miro.Numeric;
        import com.sirweb.miro.parsing.values.miro.StringValue;
        import org.junit.Test;

        import java.util.List;

        import static org.junit.Assert.assertEquals;

public class CalculatorTest {
    @Test
    public void testPostfix () throws MiroException {
        Tokenizer tokenizer = new Tokenizer("5 + 10");
        tokenizer.tokenize();
        Parser parser = new Parser(tokenizer);
        Calculator calculator = new Calculator(parser);
        List<Object> postfix = calculator.getPostfix();
        assertEquals(5, (int)((Numeric)postfix.get(0)).getNormalizedValue());
        assertEquals(10, (int)((Numeric)postfix.get(1)).getNormalizedValue());
        assertEquals(Calculator.Operator.PLUS, (Calculator.Operator) postfix.get(2));
    }

    @Test
    public void testAddNumerics () throws MiroException {
        Tokenizer tokenizer = new Tokenizer("5 + 10");
        tokenizer.tokenize();
        Parser parser = new Parser(tokenizer);
        Calculator calculator = new Calculator(parser);
        MiroValue result = calculator.eval();
        assertEquals(15, (int) ((Numeric) result).getNormalizedValue());
    }

    @Test
    public void testAddMultipleNumerics () throws MiroException {
        Tokenizer tokenizer = new Tokenizer("5 + 10 + 3 + 2 + 100");
        tokenizer.tokenize();
        Parser parser = new Parser(tokenizer);
        Calculator calculator = new Calculator(parser);
        MiroValue result = calculator.eval();
        assertEquals(120, (int) ((Numeric) result).getNormalizedValue());
    }

    @Test
    public void testAddNumericString () throws MiroException {
        Tokenizer tokenizer = new Tokenizer("5 + ' apples'");
        tokenizer.tokenize();
        Parser parser = new Parser(tokenizer);
        Calculator calculator = new Calculator(parser);
        MiroValue result = calculator.eval();
        assertEquals("5 apples", ((StringValue) result).getValue());
    }

    @Test
    public void testAddNumericWithUnitString () throws MiroException {
        Tokenizer tokenizer = new Tokenizer("5px + ''");
        tokenizer.tokenize();
        Parser parser = new Parser(tokenizer);
        Calculator calculator = new Calculator(parser);
        MiroValue result = calculator.eval();
        assertEquals("5px", ((StringValue) result).getValue());
    }

    @Test
    public void testAddString () throws MiroException {
        Tokenizer tokenizer = new Tokenizer("'Hello' + ' World'");
        tokenizer.tokenize();
        Parser parser = new Parser(tokenizer);
        Calculator calculator = new Calculator(parser);
        MiroValue result = calculator.eval();
        assertEquals("Hello World", ((StringValue) result).getValue());
    }

    @Test
    public void testAddStringNumeric () throws MiroException {
        Tokenizer tokenizer = new Tokenizer("'I am ' + 3px + ' large'");
        tokenizer.tokenize();
        Parser parser = new Parser(tokenizer);
        Calculator calculator = new Calculator(parser);
        MiroValue result = calculator.eval();
        assertEquals("I am 3px large", ((StringValue) result).getValue());
    }

    @Test
    public void testMultiplyNumerics () throws MiroException {
        Tokenizer tokenizer = new Tokenizer("3 * 7");
        tokenizer.tokenize();
        Parser parser = new Parser(tokenizer);
        Calculator calculator = new Calculator(parser);
        MiroValue result = calculator.eval();
        assertEquals(21, (int)((Numeric) result).getValue());
    }

    @Test
    public void testMultiplyMultipleNumerics () throws MiroException {
        Tokenizer tokenizer = new Tokenizer("3 * 7 * 2 * 3");
        tokenizer.tokenize();
        Parser parser = new Parser(tokenizer);
        Calculator calculator = new Calculator(parser);
        MiroValue result = calculator.eval();
        assertEquals(126, (int)((Numeric) result).getValue());
    }

    @Test
    public void testMultFirst () throws MiroException {
        Tokenizer tokenizer = new Tokenizer("5 + 2 * 3");
        tokenizer.tokenize();
        Parser parser = new Parser(tokenizer);
        Calculator calculator = new Calculator(parser);
        MiroValue result = calculator.eval();
        assertEquals(11, (int)((Numeric) result).getValue());
    }

    @Test
    public void testMultiplyNumericString () throws MiroException {
        Tokenizer tokenizer = new Tokenizer("3 * \"Test\"");
        tokenizer.tokenize();
        Parser parser = new Parser(tokenizer);
        Calculator calculator = new Calculator(parser);
        MiroValue result = calculator.eval();
        assertEquals("TestTestTest", ((StringValue) result).getValue());
    }

}