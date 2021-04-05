package flaxbeard.immersivepetroleum.common.util;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.entity.MotorboatEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class IPEffects{
	/**
	 * This is only as a burn prevention for when someone dismounts the
	 * {@link MotorboatEntity} while that is in lava<br>
	 */
	public static Effect ANTI_DISMOUNT_FIRE;
	
	public static void init(){
		ANTI_DISMOUNT_FIRE = new AntiFireEffect();
	}
	
	private static class AntiFireEffect extends IPEffect{
		public AntiFireEffect(){
			super("anti_fire", EffectType.BENEFICIAL, 0x7F7F7F);
		}
		
		@Override
		public boolean shouldRenderInvText(EffectInstance effect){
			return false;
		}
		
		@Override
		public boolean shouldRenderHUD(EffectInstance effect){
			return false;
		}
		
		@Override
		public void performEffect(LivingEntity living, int amplifier){
			living.extinguish();
		}
	}
	
	public static class IPEffect extends Effect{
		protected IPEffect(String name, EffectType type, int color){
			super(type, color);
			ForgeRegistries.POTIONS.register(this.setRegistryName(new ResourceLocation(ImmersivePetroleum.MODID, name)));
		}
	}
}
