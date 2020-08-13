package flaxbeard.immersivepetroleum.common.blocks.metal;

import flaxbeard.immersivepetroleum.common.blocks.IPMetalMultiblock;
import flaxbeard.immersivepetroleum.common.blocks.metal.DistillationTowerTileEntity.DistillationTowerParentTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class DistillationTowerBlock extends IPMetalMultiblock{
	public DistillationTowerBlock(){
		super("distillationtower",
				()->DistillationTowerTileEntity.TYPE,
				()->DistillationTowerParentTileEntity.TYPE);
	}
	
	@Override
	public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit){
		if(!player.getHeldItem(hand).isEmpty()){
			TileEntity te=world.getTileEntity(pos);
			if(te instanceof DistillationTowerTileEntity){
				BlockPos tPos=((DistillationTowerTileEntity)te).posInMultiblock;
				if((tPos.getY()==1 && tPos.getX()==3 && tPos.getZ()==3) && hit.getFace()==Direction.UP){
					return false;
				}
				if((tPos.getY()==1 && tPos.getX()==0 && tPos.getZ()==3)){
					return false;
				}
			}
		}
		return super.onBlockActivated(state, world, pos, player, hand, hit);
	}
	
	@Override
	public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity){
		TileEntity te=world.getTileEntity(pos);
		if(te instanceof DistillationTowerTileEntity){
			return ((DistillationTowerTileEntity)te).isLadder();
		}
		return false;
	}
}
