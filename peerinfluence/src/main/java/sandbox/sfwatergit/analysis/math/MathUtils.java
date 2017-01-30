package sandbox.sfwatergit.analysis.math;

import gnu.trove.list.array.TDoubleArrayList;
import org.matsim.core.utils.collections.Tuple;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * These can obviously be found elsewhere.
 * <p>
 * Created by sidneyfeygin on 5/18/15.
 */
public class MathUtils {
    /**
     * k!
     * ------
     * (k-n)!
     *
     * @param k
     * @return
     */
    public static List<Tuple<Integer, Integer>> getPermutations(int k) {
        ArrayList<Tuple<Integer, Integer>> combos = new ArrayList<>();
        for (int i = k; i > 0; i--) {
            Tuple<Integer, Integer> combo = new Tuple<>(1, 2);
        }
        combos.add(new Tuple<Integer, Integer>(1, 2));
        return combos;
    }

    public static int factorial(int k) {
        if (k == 1) {
            return k;
        } else {
            return k * factorial(k - 1);
        }
    }

    public static double mean(TDoubleArrayList data) {
        return sum(data) / data.size();
    }

    public static double sum(TDoubleArrayList data) {
        double res = 0.0D;
        double[] els = data.toArray();
        for (int i = 0; i < data.size(); i++) {
            res += els[i];
        }
        return res;
    }


    public static double roundDouble(double dd, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Double.toString(dd));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }
}
