package fr.amu.candycrush.model;

import fr.amu.candycrush.interfaces.ILevel;

public class LevelFactory {

	public static final int easy = 1;
	public static final int medium = 2;
	public static final int hard = 3;
	
	public LevelFactory(){
		
	}

	public ILevel getLevel(int levelType) {
		ILevel level = null;
		
		switch(levelType){
			case easy : level = new EasyLevel();
			case medium : level = new MediumLevel();
			case hard : level = new HardLevel();
			default : System.out.println("impossible to create a level");
		}
		
		return level;
	}
	
}
