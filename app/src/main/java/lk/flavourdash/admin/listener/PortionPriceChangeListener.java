package lk.flavourdash.admin.listener;

import android.util.Pair;

public interface PortionPriceChangeListener {
    void onPortionPriceChanged(int position, Pair<String, Double> updatedPair);
}
