package dev.pvpoverhaul;

/** Shared, bounded movement/skill defaults. Tick values assume Minecraft's normal 20 TPS. */
public final class Balance {
    public static final int LOCK_DURATION=60, LOCK_COOLDOWN=320, DEAD_EYE_DURATION=60, DEAD_EYE_COOLDOWN=400;
    public static final double LOCK_RANGE=24, LOCK_CONE=0.985;
    public static final double DEAD_EYE_DAMAGE=1.05;
    public static final float DEAD_EYE_SPREAD=0.60f;
    public static final double BOOST_SPEED_CAP=2.0;
    public static final int BOOST_DURABILITY_COST=3;
    public static final double OVERLOAD_STRENGTH=1.5, OVERLOAD_SPEED_CAP=3.0;
    public static final int OVERLOAD_COOLDOWN=400;
    private static int bounded(int level,int max) { return Math.max(0,Math.min(level,max)); }
    public static double bowMultiplier(int rank) {
        return switch(bounded(rank,5)) { case 1->1.03;case 2->1.06;case 4->1.09;case 5->1.12;default->1.0; };
    }
    public static int bowDrawTicks(int rank) { return rank==3 ? 18 : 20; }
    public static int boostCooldown(int level,int aerodynamics) {
        return 180-20*bounded(level,3);
    }
    public static double boostStrength(int level,int aerodynamics) {
        return 0.24+0.12*bounded(level,3);
    }
    public static int overloadCost(int maximumDurability) { return Math.max(1,(maximumDurability+1)/2); }
    public static double glideGravityMultiplier(int level) { return 1.0-0.10*bounded(level,2); }
    public static double glideHorizontalDrag(int level) { return 0.99+0.0025*bounded(level,2); }
    /** Limit only the new impulse; never truncate momentum from rockets, Riptide or other techniques. */
    public static double allowedBoostImpulse(double vx,double vy,double vz,double dx,double dy,double dz,int level,int aerodynamics) {
        return level<=0 ? 0 : allowedImpulse(vx,vy,vz,dx,dy,dz,boostStrength(level,aerodynamics),BOOST_SPEED_CAP);
    }
    public static double allowedImpulse(double vx,double vy,double vz,double dx,double dy,double dz,double strength,double cap) {
        double speedSquared=vx*vx+vy*vy+vz*vz;
        double lengthSquared=dx*dx+dy*dy+dz*dz;
        if (!Double.isFinite(speedSquared) || !Double.isFinite(lengthSquared) ||
                !Double.isFinite(strength) || !Double.isFinite(cap) || strength<=0 || cap<=0 ||
                speedSquared>=cap*cap || lengthSquared<1e-12) return 0;
        double length=Math.sqrt(lengthSquared);
        double projection=(vx*dx+vy*dy+vz*dz)/length;
        double available=-projection+Math.sqrt(projection*projection+cap*cap-speedSquared);
        return Math.max(0,Math.min(strength,available));
    }
    private Balance() {}
}
