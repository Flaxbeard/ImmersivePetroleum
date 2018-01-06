package flaxbeard.immersivepetroleum.common.blocks;

import flaxbeard.immersivepetroleum.common.fluid.FluidDiesel;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.IPContent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import javax.annotation.Nonnull;

public class BlockIPFluid extends BlockFluidClassic
{
	private int flammability = 0;
	private int fireSpread = 0;
	private PotionEffect[] potionEffects;

	public BlockIPFluid(String name, Fluid fluid, Material material)
	{
		super(fluid, material);
		this.setUnlocalizedName(ImmersivePetroleum.MODID + "." + name);
		this.setCreativeTab(ImmersivePetroleum.creativeTab);
		//ImmersivePetroleum.registerBlock(this, ItemBlock.class, name);
		IPContent.registeredIPBlocks.add(this);
	}

	public String getRenderName()
	{
		return getFluid().getName();
	}

	public BlockIPFluid setFlammability(int flammability, int fireSpread)
	{
		this.flammability = flammability;
		this.fireSpread = fireSpread;
		return this;
	}
	public BlockIPFluid setPotionEffects(PotionEffect... potionEffects)
	{
		this.potionEffects = potionEffects;
		return this;
	}

	@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face)
	{
		return this.flammability;
	}
	@Override
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face)
	{
		return fireSpread;
	}
	@Override
	public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face)
	{
		return this.flammability>0;
	}


	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity)
	{
		if(potionEffects!=null && entity instanceof EntityLivingBase)
		{
			for(PotionEffect effect : potionEffects)
				if(effect!=null)
					((EntityLivingBase)entity).addPotionEffect(new PotionEffect(effect));
		}
	}

	@Override
	public int place(World world, BlockPos pos, @Nonnull FluidStack fluidStack, boolean doPlace) {
		if (fluidStack.getFluid() == IPContent.fluidDiesel && FluidDiesel.hasSulfur(fluidStack)) {
			if (fluidStack.amount < Fluid.BUCKET_VOLUME)
			{
				return 0;
			}
			if (doPlace)
			{
				FluidUtil.destroyBlockOnFluidPlacement(world, pos);
				world.setBlockState(pos, IPContent.blockFluidDieselSulfur.getDefaultState(), 11);
			}
			return Fluid.BUCKET_VOLUME;
		}
		return super.place(world, pos, fluidStack, doPlace);
	}
}
