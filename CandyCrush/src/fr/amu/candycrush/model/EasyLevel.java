package fr.amu.candycrush.model;

import java.util.Random;

import fr.amu.candycrush.interfaces.ILevel;

public class EasyLevel implements ILevel {

	private int move;
	private int goal;
	private boolean choosable;
	
	public EasyLevel(){
		choosable = false;
		setGoal();
		setMove();
	}

	public void setChoosable(boolean choosable) {
		this.choosable = choosable;
	}

	public int getMove() {
		return move;
	}
	public void setMove() {
		Random rand = new Random();
		int move = rand.nextInt(100-50+1)+50;
		this.move = move;
	}
	public int getGoal() {
		return goal;
	}
	public void setGoal() {
		Random rand = new Random();
		int goal = rand.nextInt(50-10+1)+10;
		this.goal = goal;
	}

	@Override
	public boolean isAchievedGoal(int points) {
		// TODO Auto-generated method stub
		if(move <= goal && points == goal){
			setChoosable(true);
			return true;
		}
		return false;
	}

}
