package flaxbeard.immersivepetroleum.common.sound;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.items.ItemEarmuffs;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

import javax.annotation.Nullable;
import java.util.Iterator;

public class IPEntitySound implements ITickableSound
{
	protected Sound sound;
	private SoundEventAccessor soundEvent;
	private SoundCategory category;
	public AttenuationType attenuation;
	public final ResourceLocation resource;
	public float volume;
	public float pitch;

	public Entity entity;
	public boolean canRepeat;
	public int repeatDelay;
	public float volumeAjustment = 1;


	public IPEntitySound(SoundEvent event, float volume, float pitch, boolean repeat, int repeatDelay, Entity e, AttenuationType attenuation, SoundCategory category)
	{
		this(event.getSoundName(), volume, pitch, repeat, repeatDelay, e, attenuation, category);
	}

	public IPEntitySound(ResourceLocation sound, float volume, float pitch, boolean repeat, int repeatDelay, Entity e, AttenuationType attenuation, SoundCategory category)
	{
		this.attenuation = attenuation;
		this.resource = sound;
		this.volume = volume;
		this.pitch = pitch;
		this.entity = e;
		this.canRepeat = repeat;
		this.repeatDelay = repeatDelay;
		this.category = category;
	}


	@Override
	public AttenuationType getAttenuationType()
	{
		return attenuation;
	}

	@Override
	public ResourceLocation getSoundLocation()
	{
		return resource;
	}

	@Nullable
	@Override
	public SoundEventAccessor createAccessor(SoundHandler handler)
	{
		this.soundEvent = handler.getAccessor(this.resource);
		if (this.soundEvent == null)
			this.sound = SoundHandler.MISSING_SOUND;
		else
			this.sound = this.soundEvent.cloneEntry();
		return this.soundEvent;
	}

	@Override
	public Sound getSound()
	{
		return sound;
	}

	@Override
	public SoundCategory getCategory()
	{
		return category;
	}

	@Override
	public float getVolume()
	{
		return volume * volumeAjustment;
	}

	@Override
	public float getPitch()
	{
		return pitch;
	}

	@Override
	public float getXPosF()
	{
		return (float) entity.posX;
	}

	@Override
	public float getYPosF()
	{
		return (float) entity.posY;
	}

	@Override
	public float getZPosF()
	{
		return (float) entity.posZ;
	}

	@Override
	public boolean canRepeat()
	{
		return canRepeat;
	}

	@Override
	public int getRepeatDelay()
	{
		return repeatDelay;
	}

	//	public void setPos(float x, float y, float z)
	//	{
	//		this.tileX=x;
	//		this.tileY=y;
	//		this.tileZ=z;
	//	}

	public void evaluateVolume()
	{
		volumeAjustment = 1f;
		if (ClientUtils.mc().player != null && ClientUtils.mc().player.getItemStackFromSlot(EntityEquipmentSlot.HEAD) != null)
		{
			ItemStack stack = ClientUtils.mc().player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
			if (ItemNBTHelper.hasKey(stack, "IE:Earmuffs"))
				stack = ItemNBTHelper.getItemStack(stack, "IE:Earmuffs");
			if (stack != null && IEContent.itemEarmuffs.equals(stack.getItem()))
				volumeAjustment = ItemEarmuffs.getVolumeMod(stack);
		}
		if (volumeAjustment > .1f)
			for (int dx = (int) Math.floor(entity.posX - 8) >> 4; dx <= (int) Math.floor(entity.posX + 8) >> 4; dx++)
			{
				for (int dz = (int) Math.floor(entity.posZ - 8) >> 4; dz <= (int) Math.floor(entity.posZ + 8) >> 4; dz++)
				{
					Iterator it = ClientUtils.mc().player.world.getChunk(dx, dz).getTileEntityMap().values().iterator();
					while (it.hasNext())
					{
						TileEntity tile = (TileEntity) it.next();
						if (tile != null && tile.getClass().getName().endsWith("TileEntitySoundMuffler"))
							if (tile.getBlockMetadata() != 1)
							{
								double d = tile.getDistanceSq(entity.posX, entity.posY, entity.posZ);
								if (d <= 64 && d > 0)
									volumeAjustment = .1f;
							}
					}
				}
			}

		if (entity.isDead) donePlaying = true;
	}


	@Override
	public void update()
	{
		if (ClientUtils.mc().player != null && ClientUtils.mc().player.world.getTotalWorldTime() % 40 == 0)
			evaluateVolume();
	}

	public boolean donePlaying = false;

	@Override
	public boolean isDonePlaying()
	{
		return donePlaying;
	}
}