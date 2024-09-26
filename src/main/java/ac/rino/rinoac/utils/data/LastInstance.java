package ac.rino.rinoac.utils.data;

import ac.rino.rinoac.player.RinoPlayer;

public class LastInstance {
    int lastInstance = 100;

    public LastInstance(RinoPlayer player) {
        player.lastInstanceManager.addInstance(this);
    }

    public boolean hasOccurredSince(int time) {
        return lastInstance <= time;
    }

    public void reset() {
        lastInstance = 0;
    }

    public void tick() {
        // Don't overflow (a VERY long timer attack or a player playing for days could cause this to overflow)
        // The CPU can predict this branch, so it's only a few cycles.
        if (lastInstance == Integer.MAX_VALUE) lastInstance = 100;
        lastInstance++;
    }
}
