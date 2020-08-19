package flaxbeard.immersivepetroleum.common.lubehandlers;

import com.mojang.blaze3d.platform.GlStateManager;

import blusunrize.immersiveengineering.client.ClientUtils;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import flaxbeard.immersivepetroleum.client.model.ModelLubricantPipes;
import flaxbeard.immersivepetroleum.common.IPContent.Fluids;
import flaxbeard.immersivepetroleum.common.blocks.metal.AutoLubricatorNewTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.metal.PumpjackTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PumpjackLubricationHandler implements ILubricationHandler<PumpjackTileEntity>{
	@Override
	public TileEntity isPlacedCorrectly(World world, AutoLubricatorNewTileEntity lubricator, Direction facing){
		BlockPos target = lubricator.getPos().offset(facing);
		TileEntity te = world.getTileEntity(target);
		
		if(te instanceof PumpjackTileEntity){
			PumpjackTileEntity master = ((PumpjackTileEntity)te).master();
			
			Direction f = master.getIsMirrored() ? facing : facing.getOpposite();
			if(master.getFacing().rotateY() == f){
				return master;
			}
		}
		
		return null;
	}
	
	@Override
	public boolean isMachineEnabled(World world, PumpjackTileEntity mbte){
		return mbte.wasActive;
	}
	
	@Override
	public void lubricate(World world, int ticks, PumpjackTileEntity mbte){
		if(!world.isRemote){
			if(ticks % 4 == 0){
				mbte.tick();
			}
		}else{
			mbte.activeTicks += 1F / 4F;
		}
	}
	
	@Override
	public void spawnLubricantParticles(World world, AutoLubricatorNewTileEntity lubricator, Direction facing, PumpjackTileEntity mbte){
		Direction f = mbte.getIsMirrored() ? facing : facing.getOpposite();
		float location = world.rand.nextFloat();
		
		boolean flip = f.getAxis() == Axis.Z ^ facing.getAxisDirection() == AxisDirection.POSITIVE ^ !mbte.getIsMirrored();
		float xO = 2.5F;
		float zO = -.15F;
		float yO = 2.25F;
		
		if(location > .5F){
			xO = 1.7F;
			yO = 2.9F;
			zO = -1.5F;
			
		}
		
		if(facing.getAxisDirection() == AxisDirection.NEGATIVE) xO = -xO + 1;
		if(!flip) zO = -zO + 1;
		
		float x = lubricator.getPos().getX() + (f.getAxis() == Axis.X ? xO : zO);
		float y = lubricator.getPos().getY() + yO;
		float z = lubricator.getPos().getZ() + (f.getAxis() == Axis.X ? zO : xO);
		
		for(int i = 0;i < 3;i++){
			float r1 = (world.rand.nextFloat() - .5F) * 2F;
			float r2 = (world.rand.nextFloat() - .5F) * 2F;
			float r3 = world.rand.nextFloat();
			BlockState n = Fluids.fluidLubricant.block.getDefaultState();
			world.addParticle(new BlockParticleData(ParticleTypes.FALLING_DUST, n), x, y, z, r1 * 0.04F, r3 * 0.0125F, r2 * 0.025F);
		}
	}
	
	private static Object pumpjackM;
	private static Object pumpjack;
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderPipes(World world, AutoLubricatorNewTileEntity lubricator, Direction facing, PumpjackTileEntity mbte){
		if(pumpjackM == null){
			pumpjackM = new ModelLubricantPipes.Pumpjack(true);
			pumpjack = new ModelLubricantPipes.Pumpjack(false);
		}
		
		GlStateManager.translatef(0, -1, 0);
		Vec3i offset = mbte.getPos().subtract(lubricator.getPos());
		GlStateManager.translatef(offset.getX(), offset.getY(), offset.getZ());
		
		Direction rotation = mbte.getFacing();
		if(rotation == Direction.NORTH){
			GlStateManager.rotatef(90F, 0, 1, 0);
			GlStateManager.translatef(-6, 1, -1);
		}else if(rotation == Direction.WEST){
			GlStateManager.rotatef(180F, 0, 1, 0);
			GlStateManager.translatef(-6, 1, -2);
		}else if(rotation == Direction.SOUTH){
			GlStateManager.rotatef(270F, 0, 1, 0);
			GlStateManager.translatef(-5, 1, -2);
		}else{
			GlStateManager.rotatef(0F, 0, 1, 0);
			GlStateManager.translatef(-5, 1, -1);
		}
		
		ClientUtils.bindTexture("immersivepetroleum:textures/block/lube_pipe12.png");
		if(mbte.getIsMirrored()){
			((ModelLubricantPipes.Pumpjack) pumpjackM).render(0.0625F);
		}else{
			((ModelLubricantPipes.Pumpjack) pumpjack).render(0.0625F);
		}
	}
	
	@Override
	public Tuple<BlockPos, Direction> getGhostBlockPosition(World world, PumpjackTileEntity mbte){
		if(!mbte.isDummy()){
			BlockPos pos = mbte.getPos().offset(mbte.getIsMirrored() ? mbte.getFacing().rotateYCCW() : mbte.getFacing().rotateY(), 2);
			Direction f = mbte.getIsMirrored() ? mbte.getFacing().rotateY() : mbte.getFacing().rotateYCCW();
			return new Tuple<BlockPos, Direction>(pos, f);
		}
		return null;
	}
	
	private static Vec3i size;
	@Override
	public Vec3i getStructureDimensions(){
		if(size==null)
			size=new Vec3i(4, 6, 3);
		return size;
	}
}