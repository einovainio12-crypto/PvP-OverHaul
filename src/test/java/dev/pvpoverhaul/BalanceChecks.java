package dev.pvpoverhaul;

import java.util.Random;

/** Dependency-free invariants for movement safety and the bow damage/draw tradeoff. */
public final class BalanceChecks {
    private static void require(boolean ok,String message) { if (!ok) throw new AssertionError(message); }
    public static void main(String[] args) {
        double goldRate=Balance.bowMultiplier(3)/Balance.bowDrawTicks(3);
        double netheriteRate=Balance.bowMultiplier(5)/Balance.bowDrawTicks(5);
        for(int level=1;level<=3;level++)
            require(Math.abs(Balance.boostStrength(level,0)-(0.24+0.12*level))<1e-9,"Boost's 20% strength increase must apply at every level");
        require(goldRate<=netheriteRate,"Gold's unenchanted draw advantage must not exceed netherite's sustained damage");
        for(int rank=0;rank<=5;rank++) {
            require(Balance.bowMultiplier(rank)*Balance.DEAD_EYE_DAMAGE<=1.20,"Stacked bow/Dead Eye bonus exceeds 20%");
            int draw=Balance.bowDrawTicks(rank);
            require(Math.round((draw-1)*20f/draw)<20,"Bow fully charges before its advertised draw time");
        }
        for(int level=1;level<=20;level++) for(int aero=0;aero<=20;aero++) {
            require(Balance.boostCooldown(level,aero)>=110,"Overleveled enchantment bypasses boost cooldown floor");
            require(Balance.boostStrength(level,aero)<=0.65+1e-9,"Boost exceeds bounded impulse");
        }
        require(Balance.allowedBoostImpulse(2.5,0,0,1,0,0,3,2)==0,"Must preserve above-cap rocket momentum without adding speed");
        require(Balance.allowedBoostImpulse(0,0,0,1,0,0,0,2)==0,"No Boost enchantment must mean no boost");
        require(Balance.allowedBoostImpulse(0,0,0,0,0,0,3,2)==0,"Zero direction must be rejected");
        require(Balance.allowedBoostImpulse(Double.NaN,0,0,1,0,0,3,2)==0,"Invalid motion must be rejected");
        Random random=new Random(26_3);
        for(int i=0;i<10_000;i++) {
            double vx=(random.nextDouble()-.5)*2,vy=(random.nextDouble()-.5)*2,vz=(random.nextDouble()-.5)*2;
            double dx=random.nextDouble()-.5,dy=random.nextDouble()-.5,dz=random.nextDouble()-.5;
            double length=Math.sqrt(dx*dx+dy*dy+dz*dz);
            double amount=Balance.allowedBoostImpulse(vx,vy,vz,dx,dy,dz,3,2);
            require(Double.isFinite(amount) && amount>=0 && amount<=0.65+1e-9,"Invalid boost amount");
            double nx=vx+dx/length*amount,ny=vy+dy/length*amount,nz=vz+dz/length*amount;
            require(nx*nx+ny*ny+nz*nz<=Balance.BOOST_SPEED_CAP*Balance.BOOST_SPEED_CAP+1e-9,"Boost exceeds speed ceiling");
        }
        System.out.println("PASS: bow tradeoffs, charge timing, enchantment bounds and 10,000 boost vectors");
    }
}
