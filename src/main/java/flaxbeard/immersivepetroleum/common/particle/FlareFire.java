package flaxbeard.immersivepetroleum.common.particle;

import java.util.Random;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.util.MCUtil;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

public class FlareFire extends SimpleAnimatedParticle{
	final double ogMotionY;
	final float red, green, blue;
	final float rotation;
	protected FlareFire(ClientWorld world, double x, double y, double z, double motionX, double motionY, double motionZ, IAnimatedSprite spriteWithAge){
		super(world, x, y, z, spriteWithAge, 0.0F);
		setSize(0.5F, 0.5F);
		selectSpriteWithAge(spriteWithAge);
		setBaseAirFriction(1.0F);
		setColor(1.0F, 1.0F, 1.0F);
		setMaxAge(60);
		// this.canCollide = false;
		this.particleScale = 8 / 16F;
		
		this.red = this.green = this.blue = 1.0F;
		
		this.ogMotionY = motionY;
		
		this.rotation = 0.250F * (world.rand.nextFloat() - 0.5F);
		
		this.prevParticleAngle = 360.0F * world.rand.nextFloat();
		this.particleAngle = this.prevParticleAngle + (this.rotation * world.rand.nextFloat());
		
		// These arent actualy used, setting them to 0 anyway though just incase
		this.motionX = this.motionY = this.motionZ = 0.0;
	}
	
	@Override
	public void tick(){
		float f = (this.age / (float) this.maxAge);
		Vector3f vec = Wind.getDirection();
		
		if(this.age++ >= this.maxAge){
			setExpired();
		}
		if(this.age == this.maxAge - 36){
			this.particleRed = this.particleGreen = this.particleBlue = (float)(0.4F * Math.random());
		}
		selectSpriteWithAge(this.spriteWithAge);
		
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.prevParticleAngle = this.particleAngle;
		
		this.move(vec.getX() * f, this.ogMotionY * (1F - f), vec.getZ() * f);
		this.particleAngle += this.rotation;
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class Factory implements IParticleFactory<BasicParticleType>{
		private final IAnimatedSprite spriteSet;
		
		public Factory(IAnimatedSprite spriteSet){
			this.spriteSet = spriteSet;
		}
		
		@Override
		public Particle makeParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed){
			return new FlareFire(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
		}
	}
	
	/**
	 * Global presudo-wind for the flarestack flame
	 * 
	 * @author TwistedGate
	 */
	@Mod.EventBusSubscriber(modid = ImmersivePetroleum.MODID, value = Dist.CLIENT)
	public static class Wind{
		private static Vector3f vec = new Vector3f(0.0F, 0.0F, 0.0F);
		private static long lastGT;
		private static float lastDirection;
		private static float thisDirection;
		
		public static Vector3f getDirection(){
			return vec;
		}
		
		@SubscribeEvent
		public static void clientTick(TickEvent.ClientTickEvent event){
			if(event.side == LogicalSide.CLIENT && event.phase == TickEvent.Phase.START){
				ClientWorld world = MCUtil.getWorld();
				if(world == null)
					return;
				
				long gameTime = world.getGameTime();
				if((gameTime / 20) != lastGT){
					lastGT = gameTime / 20;
					
					double fGameTime = (gameTime / 20D);
					Random lastRand = new Random(MathHelper.floor(fGameTime));
					Random thisRand = new Random(MathHelper.ceil(fGameTime));
					
					lastDirection = lastRand.nextFloat() * 360;
					thisDirection = thisRand.nextFloat() * 360;
				}
				
				double interpDirection = MathHelper.lerp(((gameTime % 20) / 20F), lastDirection, thisDirection);
				
				float xSpeed = (float) Math.sin(interpDirection) * .1F;
				float zSpeed = (float) Math.cos(interpDirection) * .1F;
				
				vec = new Vector3f(xSpeed, 0.0F, zSpeed);
			}
		}
	}
}
