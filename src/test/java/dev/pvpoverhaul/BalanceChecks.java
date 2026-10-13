package dev.pvpoverhaul;

import java.util.Random;

/** Dependency-free invariants for movement safety and the bow damage/draw tradeoff. */
public final class BalanceChecks {
    private static void require(boolean ok,String message) { if (!ok) throw new AssertionError(message); }
    public static void main(String[] args) {
        double goldRate=Balance.bowMultiplier(3)/Balance.bowDrawTicks(3);
        double netheriteRate=Balance.bowMultiplier(5)/Balance.bowDrawTicks(5);
        for(int level=1;level<=3;level++)
            require(Math.abs(Balance.boostStrength(level,0)-(0.36+0.18*level))<1e-9,"Boost must match the advertised stronger impulses");
        require(goldRate>netheriteRate && Balance.bowDrawTicks(3)==10,"Gold must trade durability for a much faster draw");
        require(Balance.bowDrawTicks(4)==26 && Balance.bowDrawTicks(5)==30,"High-tier bow draw times were not lengthened");
        require(Balance.crossbowDrawTicks(4)==32 && Balance.crossbowDrawTicks(5)==40,"High-tier crossbow load times were not lengthened");
        require(Balance.firstJumpMultiplier(1)==1 && Balance.firstJumpMultiplier(2)==1.25f,"Double Jump II must increase the initial jump");
        for(int rank=0;rank<=5;rank++) {
            require(Balance.bowMultiplier(rank)*Balance.DEAD_EYE_DAMAGE<=1.20,"Stacked bow/Dead Eye bonus exceeds 20%");
            int draw=Balance.bowDrawTicks(rank);
            require(Math.round((draw-1)*20f/draw)<20,"Bow fully charges before its advertised draw time");
        }
        for(int level=1;level<=20;level++) for(int aero=0;aero<=20;aero++) {
            require(Balance.boostCooldown(level,aero)>=120,"Overleveled enchantment bypasses boost cooldown floor");
            require(Balance.boostStrength(level,aero)<=0.90+1e-9,"Boost exceeds bounded impulse");
            require(Balance.boostStrength(level,aero)==Balance.boostStrength(level,0),"Aerodynamics must not alter Boost strength");
            require(Balance.boostCooldown(level,aero)==Balance.boostCooldown(level,0),"Aerodynamics must not alter Boost cooldown");
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
            require(Double.isFinite(amount) && amount>=0 && amount<=0.90+1e-9,"Invalid boost amount");
            double nx=vx+dx/length*amount,ny=vy+dy/length*amount,nz=vz+dz/length*amount;
            require(nx*nx+ny*ny+nz*nz<=Balance.BOOST_SPEED_CAP*Balance.BOOST_SPEED_CAP+1e-9,"Boost exceeds speed ceiling");
            double overload=Balance.allowedImpulse(vx,vy,vz,dx,dy,dz,Balance.OVERLOAD_STRENGTH,Balance.OVERLOAD_SPEED_CAP);
            require(Double.isFinite(overload) && overload>=0 && overload<=Balance.OVERLOAD_STRENGTH,"Invalid overload impulse");
            nx=vx+dx/length*overload;ny=vy+dy/length*overload;nz=vz+dz/length*overload;
            require(nx*nx+ny*ny+nz*nz<=Balance.OVERLOAD_SPEED_CAP*Balance.OVERLOAD_SPEED_CAP+1e-9,"Overload exceeds its speed ceiling");
        }
        require(Balance.overloadCost(432)==216 && Balance.overloadCost(433)==217,"Overload must cost half the maximum durability, rounded up");
        require(Balance.allowedImpulse(4,0,0,1,0,0,1.5,3)==0,"Overload must not add speed to above-cap momentum");
        require(glideTicks(2)>glideTicks(1) && glideTicks(1)>glideTicks(0),"Aerodynamics must increase level-glide airtime");
        for(int level=1;level<=20;level++) {
            require(Balance.glideGravityMultiplier(level)>=0.8,"Overleveled Aerodynamics must remain bounded");
            require(Balance.glideHorizontalDrag(level)<1,"Gliding must retain horizontal drag");
        }
        System.out.println("PASS: bow tradeoffs, charge timing, enchantment bounds, Overload durability, longer glides and 10,000 Boost/Overload vectors");
    }
    /** Native level-pitch glide equations from 100 blocks, with no thrust or rockets. */
    private static int glideTicks(int level) {
        double altitude=100,vy=0,vz=1;
        int ticks=0;
        while(altitude>0 && ticks<10_000) {
            vy-=0.02*Balance.glideGravityMultiplier(level);
            if(vy<0) { double convert=vy*-0.1;vz+=convert;vy+=convert; }
            vy*=0.98;vz*=Balance.glideHorizontalDrag(level);
            altitude+=vy;ticks++;
        }
        require(ticks<10_000,"Aerodynamics must not provide unlimited level-pitch flight");
        return ticks;
    }
}
