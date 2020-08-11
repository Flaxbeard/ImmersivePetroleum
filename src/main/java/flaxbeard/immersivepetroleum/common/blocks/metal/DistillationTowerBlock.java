package flaxbeard.immersivepetroleum.common.blocks.metal;

import flaxbeard.immersivepetroleum.common.blocks.IPMetalMultiblock;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.DistillationTowerMultiblock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public class DistillationTowerBlock extends IPMetalMultiblock{
	public DistillationTowerBlock(){
		super("distillationtower",
				()->DistillationTowerTileEntity.TYPE,
				()->DistillationTowerTileEntity.DistillationTowerParentTileEntity.TYPE);
	}
	
	@Override
	public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity){
		TileEntity te=world.getTileEntity(pos);
		if(te instanceof DistillationTowerTileEntity){
			DistillationTowerTileEntity towerMaster=((DistillationTowerTileEntity)te).master();
			if(towerMaster!=null){
				BlockPos tPos=towerMaster.posInMultiblock;
				if(tPos.getX()==3 && tPos.getZ()==4 && (tPos.getY()>0 && tPos.getY()<DistillationTowerMultiblock.INSTANCE.getSize().getY()-2)){
					return true;
				}
			}
		}
		return false;
	}
}
