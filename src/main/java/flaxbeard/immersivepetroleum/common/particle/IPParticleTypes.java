package flaxbeard.immersivepetroleum.common.particle;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.ResourceLocation;

public class IPParticleTypes{
	public static final BasicParticleType FLARE_FIRE = createBasicParticle("flare_fire", false);
	
	private static BasicParticleType createBasicParticle(String name, boolean alwaysShow){
		BasicParticleType particleType = new BasicParticleType(alwaysShow);
		particleType.setRegistryName(new ResourceLocation(ImmersivePetroleum.MODID, name));
		return particleType;
	}
}
