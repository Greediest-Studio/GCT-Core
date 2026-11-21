package com.smd.gctcore.common.util;

/** Ballistic aiming for projectiles that move first and apply gravity after each tick. */
public final class CloudGiantBallistics {

    private static final double SEARCH_STEP = 0.25D;
    private static final int BISECTION_STEPS = 40;

    private CloudGiantBallistics() {
    }

    /**
     * Returns the earliest feasible launch velocity, or {@code null} when the target cannot be reached.
     * The returned XYZ velocity has the requested speed. Fractional flight times follow the straight
     * collision segment between consecutive tick positions, rather than a continuous parabola.
     */
    public static double[] getLaunchVelocity(double dx, double dy, double dz,
                                             double speed, double gravity, int maxFlightTicks) {
        if (!Double.isFinite(dx) || !Double.isFinite(dy) || !Double.isFinite(dz)
                || !Double.isFinite(speed) || !Double.isFinite(gravity)
                || speed <= 0.0D || gravity < 0.0D || maxFlightTicks <= 0
                || dx == 0.0D && dy == 0.0D && dz == 0.0D) {
            return null;
        }

        double horizontalDistance = Math.hypot(dx, dz);
        if (!Double.isFinite(horizontalDistance)) {
            return null;
        }
        double directTime = Math.hypot(horizontalDistance, dy) / speed;
        if (directTime > 0.0D && directTime <= 1.0D) {
            // Gravity has not changed the velocity anywhere on the first tick's segment.
            return velocity(dx, dy, dz, gravity, directTime);
        }
        if (gravity == 0.0D) {
            return directTime > 0.0D && directTime <= maxFlightTicks
                    ? velocity(dx, dy, dz, gravity, directTime) : null;
        }

        double previousTime = 0.0D;
        for (double time = SEARCH_STEP; time <= maxFlightTicks; time += SEARCH_STEP) {
            double crossingTime = time;
            double requiredSpeed = requiredSpeed(horizontalDistance, dy, gravity, time);
            if (requiredSpeed > speed) {
                // A narrow reachable interval near maximum range can fall between samples.
                // Within one tick the drop is linear, so its required-speed minimum is analytic.
                double minimumTime = intervalMinimumTime(horizontalDistance, dy, gravity,
                        previousTime, time);
                if (minimumTime > previousTime && minimumTime < time) {
                    double minimumSpeed = requiredSpeed(horizontalDistance, dy, gravity, minimumTime);
                    if (minimumSpeed <= speed) {
                        crossingTime = minimumTime;
                        requiredSpeed = minimumSpeed;
                    }
                }
            }
            if (requiredSpeed <= speed) {
                double low = previousTime;
                double high = crossingTime;
                for (int i = 0; i < BISECTION_STEPS; ++i) {
                    double middle = (low + high) * 0.5D;
                    if (requiredSpeed(horizontalDistance, dy, gravity, middle) > speed) {
                        low = middle;
                    } else {
                        high = middle;
                    }
                }
                return velocity(dx, dy, dz, gravity, (low + high) * 0.5D);
            }
            previousTime = time;
        }
        return null;
    }

    private static double requiredSpeed(double horizontalDistance, double dy,
                                        double gravity, double time) {
        return Math.hypot(horizontalDistance / time, (dy + drop(gravity, time)) / time);
    }

    private static double[] velocity(double dx, double dy, double dz, double gravity, double time) {
        return new double[] {dx / time, (dy + drop(gravity, time)) / time, dz / time};
    }

    private static double drop(double gravity, double time) {
        double ticks = Math.floor(time);
        double fraction = time - ticks;
        return gravity * (ticks * (ticks - 1.0D) * 0.5D + fraction * ticks);
    }

    private static double intervalMinimumTime(double horizontalDistance, double dy, double gravity,
                                              double start, double end) {
        double ticks = Math.floor((start + end) * 0.5D);
        double intercept = dy - gravity * ticks * (ticks + 1.0D) * 0.5D;
        double slope = gravity * ticks;
        double denominator = intercept * slope;
        if (denominator >= 0.0D) {
            return Double.NaN;
        }
        return -(horizontalDistance * horizontalDistance + intercept * intercept) / denominator;
    }
}
