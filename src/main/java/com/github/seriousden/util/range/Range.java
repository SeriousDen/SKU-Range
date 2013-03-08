package com.github.seriousden.util.range;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Description here
 * <br/>
 *
 * @author Denis.B.Demidov
 * @version 1.0
 */
public class Range implements Serializable {

    private static final long serialVersionUID = 3141592653589793238L;

    /**
     * Default regular expression for bounds
     */
    public static final String DEFAULT_BOUND_REGEX = "\\w+";

    /**
     * Default regular expression for bounds delimiter
     */
    public static final String DEFAULT_BOUNDS_DELIMITER_REGEX = "\\s*[-:]\\s*";

    /**
     * Default regular expression for tokens delimiter
     */
    public static final String DEFAULT_TOKENS_DELIMITER_REGEX = "\\s*[,;]\\s*";

    /**
     * Format for regular expressions for token
     */
    public static final String TOKEN_REGEX_FORMAT = "(%1$s)(?:(?:%2$s)(%1$s))?";

    /**
     * Format for regular expressions for string of ranges
     */
    public static final String RANGES_STRING_REGEX_FORMAT = "^\\s*?(?:(?:%s)(?:%s)?)+\\s*?$";

    // The regular expressions
    private String boundRegEx;
    private String boundDelimiterRegEx;
    private String tokenDelimiterRegEx;
    private String tokenRegEx;
    private String rangesStringRegEx;
    private boolean patternsAlreadyBuilt;

    // Main data
    private String rangesString;
    private List<String> tokensList;
    private boolean tokensAlreadyParsed;

    // parameters
    private boolean boundsWatching;
    private boolean caseSense;

    /**
     * Default constructor creates the instance of <code>Range</code> with defaults regular expressions for bounds,
     * delimiters and so on. Case sense is <code>false</code> by default. Order of bounds is never mind (yet).
     */
    public Range() {
        init(null, false);
        // TODO: change by (see next line)
        // init(null, true);
    }

    /**
     * This constructor creates the instance of <code>Range</code> with defaults regular expressions for bounds,
     * delimiters and so on. Case sense is <code>false</code> by default. Order of bounds is never mind (yet).
     * The instance will initialized with ranges are listed in argument
     * <br/>
     * @param rangesString The <code>String</code> instance with listed ranges
     */
    public Range(String rangesString) {
        init(rangesString, false);
        // TODO: change by (see next line)
        // init(rangesString, true);
    }

    private void init(String rangesString, boolean boundsWatching) {
        caseSense = false;
        patternsAlreadyBuilt = false;
        tokensAlreadyParsed = false;
        tokensList = new ArrayList<>();
        this.boundsWatching = boundsWatching;
        resetRegEx();
        if (rangesString != null) {
            setRangesString(rangesString);
        }
    }

    /**
     * This method checks whether the given value are contained in some listed ranges or not
     * <br/>
     * @param value The value is a candidate for checking
     * @return <code>true</code> if <code>value</code> are contained in some range, otherwise <code>false</code>
     * @throws IllegalArgumentException if the value is a null or empty string
     * @throws UnsupportedOperationException if some error occurred during extracting of boundaries
     */
    public boolean contains(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Tested value cannot be a null value or empty string.");
        }
        if (!patternsAlreadyBuilt) {
            buildRegEx();
        }
        if (!tokensAlreadyParsed) {
            parse();
        }
        for (String token : tokensList) {
            String[] bounds = token.split(boundDelimiterRegEx);
            if (bounds.length == 1) {
                if (caseSense ? bounds[0].equals(value) : bounds[0].equalsIgnoreCase(value)) {
                    return true;
                }
            } else if (bounds.length == 2) {
                String leftBound;
                String rightBound;
                if (!boundsWatching && !checkBounds(bounds[0], bounds[1])) {
                    leftBound = bounds[1];
                    rightBound = bounds[0];
                } else {
                    leftBound = bounds[0];
                    rightBound = bounds[1];
                }
                if (checkBounds(leftBound, value) && checkBounds(value, rightBound)) {
                    return true;
                }
            } else {
                throw new UnsupportedOperationException("The token must be either a single value or a range of values. May be your bound delimiter regex is wrong.");
            }
        }
        return false;
    }

    private void parse() {
        if (rangesString == null) {
            return;
        }
        String[] tokens = rangesString.trim().split(tokenDelimiterRegEx);
        for (String token : tokens) {
            tokensList.add(token);
        }
        tokensAlreadyParsed = true;
    }

    private void buildRegEx() {
        tokenRegEx = String.format(TOKEN_REGEX_FORMAT, boundRegEx, boundDelimiterRegEx);
        rangesStringRegEx = String.format(RANGES_STRING_REGEX_FORMAT, tokenRegEx, tokenDelimiterRegEx);
        patternsAlreadyBuilt = true;
        cleanRangesString();
    }

    private void cleanRangesString() {
        rangesString = null;
        tokensAlreadyParsed = false;
        tokensList.clear();
    }

    private boolean checkBounds(String leftBound, String rightBound) {
        return !(((leftBound.length() > rightBound.length()) ||
                (leftBound.length() == rightBound.length() &&
                        (caseSense ? leftBound.compareTo(rightBound) > 0 : leftBound.compareToIgnoreCase(rightBound) > 0)))
        );
    }

    private void checkRangesString(String rangesString) {
        if (!patternsAlreadyBuilt) {
            buildRegEx();
        }
        if (!rangesString.matches(rangesStringRegEx)) {
            throw new IllegalArgumentException("The string of ranges not matches with pattern.");
        }
        if (boundsWatching) {
            // TODO: implementation of this use-case!
            throw new UnsupportedOperationException("At present this ability are not available.");
        }
    }


    private void checkPattern(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("The pattern cannot be null or empty string.");
        }
        Pattern checkRegEx = Pattern.compile(pattern);
        checkRegEx.pattern();
    }

    /**
     * Setups the string with ranges
     * <br/>
     * @param rangesString The string with ranges
     * @throws IllegalArgumentException if the string of ranges not matches with pattern
     */
    public void setRangesString(String rangesString) {
        cleanRangesString();
        checkRangesString(rangesString);
        this.rangesString = rangesString;
    }

    /**
     * Setups the RexEx for bounds
     * <br/>
     * @param boundRegEx regular expression (e.g.: <code>"\\w+"</code>)
     * @throws IllegalArgumentException if parameter is a null or empty string
     * @throws PatternSyntaxException
     */
    public void setBoundRegEx(String boundRegEx) {
        checkPattern(boundRegEx);
        this.boundRegEx = boundRegEx;
        patternsAlreadyBuilt = false;
    }

    /**
     * Setups the RexEx for bounds delimiter
     * <br/>
     * @param boundDelimiterRegEx regular expression (e.g.: <code>"\\s*[-:]\\s*"</code>)
     * @throws IllegalArgumentException if parameter is a null or empty string
     * @throws PatternSyntaxException
     */
    public void setBoundDelimiterRegEx(String boundDelimiterRegEx) {
        checkPattern(boundDelimiterRegEx);
        this.boundDelimiterRegEx = boundDelimiterRegEx;
        patternsAlreadyBuilt = false;
    }

    /**
     * Setups the RexEx for tokens delimiter
     * <br/>
     * @param tokenDelimiterRegEx regular expression (e.g.: <code>"\\s*[,;]\\s*"</code>)
     * @throws IllegalArgumentException if parameter is a null or empty string
     * @throws PatternSyntaxException
     */
    public void setTokenDelimiterRegEx(String tokenDelimiterRegEx) {
        checkPattern(tokenDelimiterRegEx);
        this.tokenDelimiterRegEx = tokenDelimiterRegEx;
        patternsAlreadyBuilt = false;
    }

    /**
     * This method resets all regular expressions to default values
     */
    public void resetRegEx() {
        boundRegEx = DEFAULT_BOUND_REGEX;
        boundDelimiterRegEx = DEFAULT_BOUNDS_DELIMITER_REGEX;
        tokenDelimiterRegEx = DEFAULT_TOKENS_DELIMITER_REGEX;
        buildRegEx();
    }

    /**
     * Getter for ranges string
     * <br/>
     * @return Returns the ranges string
     */
    public String getRangesString() {
        return rangesString;
    }

    /**
     * Getter for case sense value
     * <br/>
     * @return Returns <code>false</code> if case sense is never mind, otherwise <code>true</code>
     */
    public boolean isCaseSense() {
        return caseSense;
    }

    /**
     * Setups case sense parameter. If <code>caseSense</code> is <code>true</code> than range <code>"2-F"</code> is smaller than range <code>"2-f"</code>.
     * <br/>
     * @param caseSense boolean value
     */
    public void setCaseSense(boolean caseSense) {
        this.caseSense = caseSense;
    }
}
