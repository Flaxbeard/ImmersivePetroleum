package flaxbeard.immersivepetroleum.client.page;

import java.lang.reflect.Field;
import java.util.List;

import blusunrize.immersiveengineering.api.multiblocks.ManualElementMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.gui.ManualScreen;
import net.minecraft.client.gui.widget.button.Button;

public class ManualPageBigMultiblock extends ManualElementMultiblock{
	public ManualPageBigMultiblock(ManualInstance manual, IMultiblock multiblock){
		super(manual, multiblock);
	}
	
	@Override
	public void onOpened(ManualScreen gui, int x, int y, List<Button> pageButtons){
		super.onOpened(gui, x, y, pageButtons);
		try{
			Field f =ManualElementMultiblock.class.getDeclaredField("transY");
			float transY=(float)(f.get(this));
			f.set(this, transY);
		}catch(Exception e){
			throw new RuntimeException("Something big and bad happend!", e);
		}
		
		
		//float yOff = ReflectionHelper.getPrivateValue(ManualPageMultiblock.class, this, 6);
		//ReflectionHelper.setPrivateValue(ManualPageMultiblock.class, this, yOff + 30, 6);
	}
	
	@Override
	public void mouseDragged(int x, int y, double clickX, double clickY, double mouseX, double mouseY, double lastX, double lastY, int mouseButton){
		if((clickX >= 40 && clickX < 144 && mouseX >= 20 && mouseX < 164) && (clickY >= 30 && clickY < 175 && mouseY >= 30 && mouseY < 225)){
			if(clickY >= 130 || mouseY >= 180){
				clickY -= 50;
				lastY -= 50;
				mouseY -= 50;
			}
			
			super.mouseDragged(x, y, clickX, clickY, mouseX, mouseY, lastX, lastY, mouseButton);
		}
	}
}
