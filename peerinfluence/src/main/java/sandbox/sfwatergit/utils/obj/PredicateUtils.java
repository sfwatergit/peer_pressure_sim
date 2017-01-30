package sandbox.sfwatergit.utils.obj;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Created by sidneyfeygin on 1/25/16.
 */
public class PredicateUtils {
    public static final Predicate<Object> notNull = Objects::nonNull;

}
