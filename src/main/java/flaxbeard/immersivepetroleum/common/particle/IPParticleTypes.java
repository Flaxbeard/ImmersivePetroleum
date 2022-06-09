package flaxbeard.immersivepetroleum.common.particle;

import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.particles.BasicParticleType;

public class IPParticleTypes{
	public static final BasicParticleType FLARE_FIRE = createBasicParticle("flare_fire", false);
	
	private static BasicParticleType createBasicParticle(String name, boolean alwaysShow){
		BasicParticleType particleType = new BasicParticleType(alwaysShow);
		particleType.setRegistryName(ResourceUtils.ip(name));
		return particleType;
	}
}
