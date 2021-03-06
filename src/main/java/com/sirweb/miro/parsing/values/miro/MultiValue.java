package com.sirweb.miro.parsing.values.miro;

import com.sirweb.miro.exceptions.MiroFuncParameterException;
import com.sirweb.miro.exceptions.MiroUnimplementedFuncException;
import com.sirweb.miro.parsing.values.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultiValue implements MiroValue {
    private List<MiroValue> values;

    public MultiValue () {
        values = new ArrayList<>();
    }

    public void addValue (MiroValue value) { values.add(value); }

    public int size () {
        return values.size();
    }

    public MiroValue get (int index) { return values.get(index); }

    public Iterable<MiroValue> getValues () { return values; }

    public String toString () {
        String val = "";

        for (MiroValue value : values)
            val += ", " + value.toString();

        return val.substring(2);
    }

    @Override
    public Value callFunc(String functionName, List<MiroValue> parameters) throws MiroUnimplementedFuncException, MiroFuncParameterException {
        return null;
    }

    @Override
    public boolean getBoolean() {
        return values.size() > 0;
    }
}
