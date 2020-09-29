package flaxbeard.immersivepetroleum.common.lubehandlers;

import blusunrize.immersiveengineering.client.ClientUtils;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import flaxbeard.immersivepetroleum.client.model.ModelLubricantPipes;
import flaxbeard.immersivepetroleum.common.IPContent.Fluids;
import flaxbeard.immersivepetroleum.common.blocks.metal.AutoLubricatorTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.metal.PumpjackTileEntity;
import flaxbeard.immersivepetroleum.dummy.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PumpjackLubricationHandler implements ILubricationHandler<PumpjackTileEntity>{
	private static Vector3i size=new Vector3i(4, 6, 3);
	
	@Override
	public Vector3i getStructureDimensions(){
		return size;
	}
	
	@Override
	public boolean isMachineEnabled(World world, PumpjackTileEntity mbte){
		return mbte.wasActive;
	}
	
	@Override
	public TileEntity isPlacedCorrectly(World world, AutoLubricatorTileEntity lubricator, Direction facing){
		
		BlockPos target = lubricator.getPos().offset(facing);
		TileEntity te = world.getTileEntity(target);
		
		if(te instanceof PumpjackTileEntity){
			PumpjackTileEntity master = ((PumpjackTileEntity) te).master();
			if(master!=null){
				Direction f = master.getIsMirrored() ? facing : facing.getOpposite();
				if(master.getFacing().rotateY() == f){
					return master;
				}
			}
		}
		
		return null;
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
	public void spawnLubricantParticles(World world, AutoLubricatorTileEntity lubricator, Direction facing, PumpjackTileEntity mbte){
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
			BlockState n = Fluids.lubricant.block.getDefaultState();
			world.addParticle(new BlockParticleData(ParticleTypes.FALLING_DUST, n), x, y, z, r1 * 0.04F, r3 * 0.0125F, r2 * 0.025F);
		}
	}
	
	private static ModelLubricantPipes.Pumpjack pumpjackM;
	private static ModelLubricantPipes.Pumpjack pumpjack;
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderPipes(World world, AutoLubricatorTileEntity lubricator, Direction facing, PumpjackTileEntity mbte){
		if(pumpjackM == null){
			pumpjackM = new ModelLubricantPipes.Pumpjack(true);
			pumpjack = new ModelLubricantPipes.Pumpjack(false);
		}
		
		GlStateManager.translatef(0, -1, 0);
		Vector3i offset = mbte.getPos().subtract(lubricator.getPos());
		GlStateManager.translatef(offset.getX(), offset.getY(), offset.getZ());
		
		Direction rotation = mbte.getFacing();
		switch(rotation){
			case NORTH:{
				GlStateManager.rotatef(90F, 0, 1, 0);
				GlStateManager.translatef(-6, 1, -1);
				break;
			}
			case SOUTH:{
				GlStateManager.rotatef(270F, 0, 1, 0);
				GlStateManager.translatef(-5, 1, -2);
				break;
			}
			case EAST:{
				GlStateManager.rotatef(0F, 0, 1, 0);
				GlStateManager.translatef(-5, 1, -1);
				break;
			}
			case WEST:{
				GlStateManager.rotatef(180F, 0, 1, 0);
				GlStateManager.translatef(-6, 1, -2);
				break;
			}
			default: break;
		}
		
		ClientUtils.bindTexture("immersivepetroleum:textures/block/lube_pipe12.png");
		if(mbte.getIsMirrored()){
			pumpjackM.render(0.0625F);
		}else{
			pumpjack.render(0.0625F);
		}
	}
	
	@Override
	public Tuple<BlockPos, Direction> getGhostBlockPosition(World world, PumpjackTileEntity mbte){
		if(!mbte.isDummy()){
			Direction mbFacing=mbte.getFacing().getOpposite();
			BlockPos pos = mbte.getPos()
					.offset(Direction.UP)
					.offset(mbFacing, 4)
					.offset(mbte.getIsMirrored() ? mbFacing.rotateY() : mbFacing.rotateYCCW(), 2);
			
			Direction f = (mbte.getIsMirrored() ? mbte.getFacing().getOpposite() : mbte.getFacing()).rotateYCCW();
			return new Tuple<BlockPos, Direction>(pos, f);
		}
		return null;
	}
}