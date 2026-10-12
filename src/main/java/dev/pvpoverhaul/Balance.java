package dev.pvpoverhaul;

/** Shared, bounded movement/skill defaults. Tick values assume Minecraft's normal 20 TPS. */
public final class Balance {
    public static final int LOCK_DURATION=60, LOCK_COOLDOWN=320, DEAD_EYE_DURATION=60, DEAD_EYE_COOLDOWN=400;
    public static final double LOCK_RANGE=24, LOCK_CONE=0.985;
    public static final double DEAD_EYE_DAMAGE=1.05;
    public static final float DEAD_EYE_SPREAD=0.60f;
    public static final double BOOST_SPEED_CAP=1.8;
    public static final int BOOST_DURABILITY_COST=3;
    private static int bounded(int level,int max) { return Math.max(0,Math.min(level,max)); }
    public static double bowMultiplier(int rank) {
        return switch(bounded(rank,5)) { case 1->1.03;case 2->1.06;case 4->1.09;case 5->1.12;default->1.0; };
    }
    public static int bowDrawTicks(int rank) { return rank==3 ? 18 : 20; }
    public static int boostCooldown(int level,int aerodynamics) {
        return 180-20*bounded(level,3)-5*bounded(aerodynamics,2);
    }
    public static double boostStrength(int level,int aerodynamics) {
        return 0.20+0.10*bounded(level,3)+0.025*bounded(aerodynamics,2);
    }
    /** Limit only the new impulse; never truncate momentum from rockets, Riptide or other techniques. */
    public static double allowedBoostImpulse(double vx,double vy,double vz,double dx,double dy,double dz,int level,int aerodynamics) {
        double speedSquared=vx*vx+vy*vy+vz*vz;
        double lengthSquared=dx*dx+dy*dy+dz*dz;
        if (!Double.isFinite(speedSquared) || !Double.isFinite(lengthSquared) ||
                speedSquared>=BOOST_SPEED_CAP*BOOST_SPEED_CAP || lengthSquared<1e-12 || level<=0) return 0;
        double length=Math.sqrt(lengthSquared);
        double projection=(vx*dx+vy*dy+vz*dz)/length;
        double available=-projection+Math.sqrt(projection*projection+BOOST_SPEED_CAP*BOOST_SPEED_CAP-speedSquared);
        return Math.max(0,Math.min(boostStrength(level,aerodynamics),available));
    }
    private Balance() {}
}
