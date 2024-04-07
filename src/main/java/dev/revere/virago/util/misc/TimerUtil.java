package dev.revere.virago.util.misc;

public class TimerUtil {

    public long lastMS = System.currentTimeMillis();

    public void reset() {
        lastMS = System.currentTimeMillis();
    }

    /**
     * Check if the time has elapsed
     *
     * @param time  the time
     * @param reset if the time should be reset
     * @return if the time has elapsed
     */
    public boolean hasTimeElapsed(long time, boolean reset) {
        if (System.currentTimeMillis() - lastMS > time) {
            if (reset) reset();
            return true;
        }

        return false;
    }

    /**
     * Check if the time has elapsed
     *
     * @param time the time
     * @return if the time has elapsed
     */
    public boolean hasTimeElapsed(long time) {
        return System.currentTimeMillis() - lastMS > time;
    }

    /**
     * Check if the time has elapsed
     *
     * @param time  the time
     * @return if the time has elapsed
     */
    public boolean hasTimeElapsed(double time) {
        return hasTimeElapsed((long) time);
    }

    public long getTime() {
        return System.currentTimeMillis() - lastMS;
    }

    /**
     * Set the time
     *
     * @param time the time
     */
    public void setTime(long time) {
        lastMS = time;
    }

}
