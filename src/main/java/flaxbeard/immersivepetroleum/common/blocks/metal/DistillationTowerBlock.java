package flaxbeard.immersivepetroleum.common.blocks.metal;

import flaxbeard.immersivepetroleum.common.IPTileTypes;
import flaxbeard.immersivepetroleum.common.blocks.IPMetalMultiblock;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.DistillationTowerTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class DistillationTowerBlock extends IPMetalMultiblock<DistillationTowerTileEntity>{
	public DistillationTowerBlock(){
		super("distillationtower", () -> IPTileTypes.TOWER.get());
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit){
		if(!player.getHeldItem(hand).isEmpty()){
			TileEntity te = world.getTileEntity(pos);
			if(te instanceof DistillationTowerTileEntity){
				DistillationTowerTileEntity tower = (DistillationTowerTileEntity) te;
				BlockPos tPos = tower.posInMultiblock;
				Direction facing = tower.getFacing();
				
				// Locations that don't require sneaking to avoid the GUI
				
				// Power input
				if(DistillationTowerTileEntity.Energy_IN.contains(tPos) && hit.getFace() == Direction.UP){
					return ActionResultType.FAIL;
				}
				
				// Redstone controller input
				if(DistillationTowerTileEntity.Redstone_IN.contains(tPos) && (tower.getIsMirrored() ? hit.getFace() == facing.rotateY() : hit.getFace() == facing.rotateYCCW())){
					return ActionResultType.FAIL;
				}
				
				// Fluid I/O Ports
				if((tPos.equals(DistillationTowerTileEntity.Fluid_IN) && (tower.getIsMirrored() ? hit.getFace() == facing.rotateYCCW() : hit.getFace() == facing.rotateY()))
				|| (tPos.equals(DistillationTowerTileEntity.Fluid_OUT) && hit.getFace() == facing.getOpposite())){
					return ActionResultType.FAIL;
				}
				
				// Item output port
				if(tPos.equals(DistillationTowerTileEntity.Item_OUT) && (tower.getIsMirrored() ? hit.getFace() == facing.rotateY() : hit.getFace() == facing.rotateYCCW())){
					return ActionResultType.FAIL;
				}
			}
		}
		return super.onBlockActivated(state, world, pos, player, hand, hit);
	}
	
	@Override
	public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity){
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof DistillationTowerTileEntity){
			return ((DistillationTowerTileEntity) te).isLadder();
		}
		return false;
	}
}
