package other;

import pt.unl.fct.di.centria.nohr.reasoner.translation.ontology.TranslationAlgorithm;

//TODO remove

/**
 * The Class Config.
 */
public class Config {

    /** The delimeter. */
    public static String delimeter = "#";

    /** The alt delimeter. */
    public static String altDelimeter = ":";

    /** The negation which should appear in the rules at the end. */
    public static String negation = "tnot";

    /** The search negation what should be replaced. */
    public static String searchNegation = "not";

    /** The equivalent symbols */
    public static String eq = ":-";

    /** The rule creation debug. */
    public static boolean ruleCreationDebug = false;

    public static TranslationAlgorithm translationAlgorithm = null;
}