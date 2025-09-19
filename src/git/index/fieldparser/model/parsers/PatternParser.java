package git.index.fieldparser.model.parsers;

import git.index.dummylogger.LoggerImpl;
import git.index.fieldparser.interfaces.IFieldParser;
import git.index.fieldparser.model.FieldClassRef;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PatternParser implements IFieldParser<Pattern>
{
    private PatternParser()
    {
        // singleton class
    }

    @Override
    public Class<Pattern> getParsableClass()
    {
        return Pattern.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <G> G parseValue(String value, FieldClassRef<G> fieldClassRef, G defaultValue)
    {
        if (value == null)
        {
            return defaultValue;
        }
        if ((fieldClassRef == null) || (fieldClassRef.getRawClass() != getParsableClass()))
        {
            return defaultValue;
        }
        Pattern pattern = null;
        try
        {
            pattern = Pattern.compile(value);
        }
        catch (PatternSyntaxException e)
        {
            new LoggerImpl(getClass()).error("Error while compiling a pattern", e);
        }
        return (G) pattern;
    }

    private final static PatternParser INSTANCE = new PatternParser();

    public static PatternParser getInstance()
    {
        return INSTANCE;
    }
}
